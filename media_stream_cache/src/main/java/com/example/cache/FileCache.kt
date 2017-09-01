package com.example.cache

import android.util.Log
import com.example.cache.extension.log
import java.io.File
import java.io.RandomAccessFile

/**
 * author : SUMMERLY
 * e-mail : yangbinyhbn@gmail.com
 * time   : 2017/9/1
 * desc   :
 */
class FileCache(file: File,
                private val cacheStrategy: CacheStrategy) {
    private val suffix = ".download"

    private var cacheFile: File
    private var cacheData: RandomAccessFile

    val isComplete: Boolean
        @Synchronized
        get() = cacheFile.exists() && !cacheFile.path.endsWith(suffix)

    init {
        file.parentFile.mkdirs()
        if (file.exists()) {//already have cache file
            cacheFile = file
            cacheData = RandomAccessFile(file, "r")
        } else {
            cacheFile = File(file.parent, file.name + suffix)
            cacheData = RandomAccessFile(cacheFile, "rw")
        }
    }


    @Synchronized
    fun read(position: Long, b: ByteArray, off: Int, len: Int): Int {
        cacheData.seek(position)
        return cacheData.read(b, off, len)
    }

    @Synchronized
    fun write(b: ByteArray, off: Int = 0, len: Int = b.size) {
        if (isComplete) {
            log("you can not write to file when cache is complete", Log.ERROR)
            return
        }
        cacheData.seek(available())
        cacheData.write(b, off, len)
    }

    @Synchronized
    fun available(): Long =
            cacheData.length()

    @Synchronized
    fun complete() {
        val file = File(cacheFile.path.substringBeforeLast(suffix))
        val renamed = cacheFile.renameTo(file)
        if (!renamed) {
            error("failed to rename file")
        }
        cacheFile = file
        cacheData = RandomAccessFile(cacheFile, "r")
        cacheStrategy.onFileCached(file)
    }

}