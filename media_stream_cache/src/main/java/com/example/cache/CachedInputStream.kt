package com.example.cache

import com.example.cache.extension.log
import org.jetbrains.anko.doAsync
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile

/**
 * author : SUMMERLY
 * e-mail : yangbinyhbn@gmail.com
 * time   : 2017/9/1
 * desc   :
 */
class CachedInputStream(needCachedInputStream: InputStream, filePath: String) {

    companion object {
        private val lockForSource = java.lang.Object()
        private val lockForFile = java.lang.Object()
    }

    private val suffix = ".download"

    private val bufferSize = 4096

    private var cachedFile: File

    private var cachedData: RandomAccessFile

    //to record the length have read from caller
    private var readLength = 0L

    init {
        cachedFile = File(filePath + suffix).also {
            it.parentFile.mkdirs()
            it.delete()
            it.createNewFile()
        }
        cachedData = RandomAccessFile(cachedFile, "rw")
        doAsync {
            val buffer = ByteArray(bufferSize)
            var bytes = needCachedInputStream.read(buffer)
            while (bytes >= 0) {
                write(buffer, 0, bytes)
                log("通知... $bytes")
                notifyNewNetDataAvailable()
                bytes = needCachedInputStream.read(buffer)
            }
            downloadComplete()
        }
    }


    private fun downloadComplete() = synchronized(lockForFile) {
        if (!cachedFile.path.endsWith(suffix)) {
            return
        }
        close()
        val file = File(cachedFile.path.substringBeforeLast(suffix))
        val renamed = cachedFile.renameTo(file)
        if (!renamed) {
            error("failed to rename file")
        }
        cachedFile = file
        cachedData = RandomAccessFile(cachedFile, "r")
    }


    fun read(b: ByteArray, off: Int, len: Int): Int {
        while (cachedFile.path.endsWith(suffix) && readLength + len > available()) {
            log("waiting for source..")
            waitForNetData()
        }
        synchronized(lockForFile) {
            cachedData.seek(readLength)
            val read = cachedData.read(b, off, len)
            readLength += read
            return read
        }
    }

    private fun waitForNetData() {
        synchronized(lockForSource) {
            lockForSource.wait(1000)
        }
    }

    private fun notifyNewNetDataAvailable() {
        //if cached data is not enough to play..

        synchronized(lockForSource) {
            lockForSource.notifyAll()
        }
    }

    fun write(b: ByteArray, off: Int = 0, len: Int = b.size) = synchronized(lockForFile) {
        cachedData.seek(available())
        cachedData.write(b, off, len)
    }

    fun available(): Long = synchronized(lockForFile) {
        cachedData.length()
    }

    fun close() {
        synchronized(lockForFile) {
            cachedData.close()
        }
    }
}

