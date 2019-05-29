package com.example.exo

import android.net.Uri
import android.util.Log
import android.widget.TextView
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ads.AdPlaybackState
import com.google.android.exoplayer2.source.ads.AdsLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

class MyAdsLoader : AdsLoader {


    companion object {
        private const val TAG = "MyAdsLoader"
    }

    private val groupIndex: Int = 0

    override fun handlePrepareError(adGroupIndex: Int, adIndexInAdGroup: Int, exception: IOException?) {
        exception?.printStackTrace()
        Log.e(TAG, "exception : $adGroupIndex ,  $adIndexInAdGroup")
    }

    override fun start(eventListener: AdsLoader.EventListener, adViewProvider: AdsLoader.AdViewProvider) {
        Log.i(TAG, "start")
        val adViewGroup = adViewProvider.adViewGroup
        val textView = TextView(adViewGroup.context)
        textView.text = "广告"
        adViewGroup.addView(textView)

        GlobalScope.launch {
            delay(10000)
            launch(Dispatchers.Main) {
                eventListener.onAdPlaybackState(AdPlaybackState(1000 * 10)
                        .withAdUri(groupIndex, 0, Uri.parse("asset:///ads.mp4"))
                        .withAdCount(groupIndex, 1)
                )
            }

        }

    }

    override fun stop() {
        Log.i(TAG, "stop")

    }

    override fun setSupportedContentTypes(vararg contentTypes: Int) {
        Log.i(TAG, "setSupportedContentTypes : ${contentTypes.joinToString()}")

    }

    override fun release() {
        Log.i(TAG, "release")
    }

    override fun setPlayer(player: Player?) {
        Log.i(TAG, "setPlayer")
    }

}