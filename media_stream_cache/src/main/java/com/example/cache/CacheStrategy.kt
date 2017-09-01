package com.example.cache

import java.io.File

/**
 * author : SUMMERLY
 * e-mail : yangbinyhbn@gmail.com
 * time   : 2017/9/1
 * desc   : define strategy to save the file
 */
interface CacheStrategy {

    /**
     * on a new cached file arrived
     */
    fun onFileCached(file: File)

}