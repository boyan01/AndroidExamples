package com.example.cache

import android.media.MediaDataSource
import com.example.cache.extension.log
import java.io.File
import java.net.URL

/**
 * author : SUMMERLY
 * e-mail : yangbinyhbn@gmail.com
 * time   : 2017/8/31
 * desc   :
 */
class CachedMediaDataSource(url: String) : MediaDataSource() {

    private val inputStream by lazy {
        //use CachedInputStream to wrap url stream

        val cache = FileCache(CacheHelper.buildCachedFile(url), object : CacheStrategy {
            override fun onFileCached(file: File) {
                //do nothing...
            }
        })
        if (!cache.isComplete) {
            val urlConnection = URL(url).openConnection()
            val contentLength = urlConnection.contentLength.toLong()
            CachedInputStream(urlConnection.getInputStream(),
                    cache)
        } else {
            CachedInputStream(cache)
        }
    }

    override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        log(" position : $position , offset = $offset , size = $size ")
        if (size == 0) {
            return 0
        }
        return inputStream.read(position, buffer, offset, size)
    }

    override fun getSize(): Long {
        //need to specify raw data size
        return inputStream.available()
    }

    override fun close() {
        inputStream.close()
    }

    private object CacheHelper {

        /**
         * the folder to save cache file
         */
        val dirCache by lazy {
            File(AppContext.instance.externalCacheDir, "media_cache").apply { mkdirs() }
        }

        fun buildCachedFile(url: String) = File(dirCache, getCachedFileName(url))

        fun getCachedFileName(url: String): String {
            return url.substringAfterLast('/')
        }

    }
}