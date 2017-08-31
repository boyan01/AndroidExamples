package com.example.cache.extension

import android.util.Log
import com.example.cache.BuildConfig

/**
 * author : SUMMERLY
 * e-mail : yangbinyhbn@gmail.com
 * time   : 2017/9/1
 * desc   :
 */
fun log(any: Any? = null, priority: Int = Log.INFO) {
    debug {
        val traceElement = Exception().stackTrace[2]
        val traceInfo = with(traceElement) {
            val source = if (isNativeMethod) "(Native Method)"
            else if (fileName != null && lineNumber >= 0)
                "($fileName:$lineNumber)"
            else
                if (fileName != null) "($fileName)" else "(Unknown Source)"
            source + className.substringAfterLast('.') + "." + methodName
        }
        val message = "$traceInfo: ${any.toString()}"
        val _tag = BuildConfig.APPLICATION_ID.substringAfterLast('.')
        when (priority) {
            Log.VERBOSE -> Log.v(_tag, message)
            Log.DEBUG -> Log.d(_tag, message)
            Log.INFO -> Log.i(_tag, message)
            Log.WARN -> Log.w(_tag, message)
            Log.ERROR -> Log.e(_tag, message)
            else -> Unit
        }
    }
}

inline fun debug(block: () -> Unit) {
    if (BuildConfig.DEBUG) {
        block()
    }
}