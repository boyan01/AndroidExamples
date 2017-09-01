package com.example.cache

import com.example.cache.extension.log
import org.jetbrains.anko.doAsync
import java.io.InputStream

/**
 * author : SUMMERLY
 * e-mail : yangbinyhbn@gmail.com
 * time   : 2017/9/1
 * desc   :
 */
class CachedInputStream(needCachedInputStream: InputStream?,
                        private val fileCache: FileCache) {

    constructor(fileCache: FileCache) : this(null, fileCache)

    private val lock = java.lang.Object()

    private val bufferSize = 4096

    init {
        log("fileCache.isComplete ${fileCache.isComplete}")
        if (!fileCache.isComplete) {
            doAsync {
                if (needCachedInputStream == null) {
                    error("cache is not complete , but without input stream")
                }
                val buffer = ByteArray(bufferSize)
                var bytes = needCachedInputStream.read(buffer)
                while (bytes >= 0) {
                    log("new data available")
                    fileCache.write(buffer, 0, bytes)
                    notifyNewNetDataAvailable()
                    bytes = needCachedInputStream.read(buffer)
                }
                fileCache.complete()
            }
        }
    }


    fun read(position: Long, b: ByteArray, off: Int, len: Int): Int {
        while (!fileCache.isComplete && position + len > available()) {
            log("waiting for source..")
            waitForNetData()
        }
        return fileCache.read(position, b, off, len)
    }

    fun available() = fileCache.available()

    /**
     * when caller need data , but we have not enough data to provide
     * so just wait a second
     */
    private fun waitForNetData() {
        synchronized(lock) {
            lock.wait(1000)
        }
    }

    /**
     * when new data is available for caller
     */
    private fun notifyNewNetDataAvailable() {
        //if cached data is not enough to play..
        synchronized(lock) {
            lock.notifyAll()
        }
    }

    /**
     * when caller does not need this input stream
     */
    fun close() {
        //Do nothing ,lets cache complete...
        //maybe pausing the download is the best way
        //and record the download state , when stream is need again, we can
        //resume the download
    }
}

