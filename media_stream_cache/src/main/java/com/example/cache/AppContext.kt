package com.example.cache

import android.app.Application

/**
 * author : SUMMERLY
 * e-mail : yangbinyhbn@gmail.com
 * time   : 2017/9/1
 * desc   :
 */
class AppContext : Application() {
    companion object {
        private lateinit var _instance: AppContext

        val instance
            get() = _instance
    }

    override fun onCreate() {
        super.onCreate()
        _instance = this
    }
}