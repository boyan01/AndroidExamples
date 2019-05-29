package com.example.cache

import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.attempt
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.concurrent.Future

class MainActivity : AppCompatActivity() {

    private val url = "http://dl.last.fm/static/1504193371/131211148/9049f51407b76b721372095a411a03a235a3a30c35dd0e1af1c77d3d0e67ddc8/Death+Grips+-+Get+Got.mp3"

    private lateinit var mediaPlay: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mediaPlay = MediaPlayer()
    }

    override fun onStart() {
        super.onStart()
        button.setOnClickListener {
            mediaPlay.setDataSource(CachedMediaDataSource(url))
            mediaPlay.prepareAsync()
            mediaPlay.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mediaPlay.setOnPreparedListener {
                it.start()
            }
        }
    }

    private var future: Future<*>? = null

    override fun onResume() {
        super.onResume()
        //create background thread to watch player state
        future = doAsync {
            while (true) {
                if (!mediaPlay.isPlaying) {
                    continue
                }
                val currentPosition = mediaPlay.currentPosition
                val duration = mediaPlay.duration
                uiThread {
                    textView.text = "%s / %s".format(currentPosition.toMusicTimeStamp(),
                            duration.toMusicTimeStamp())
                }
                Thread.sleep(1000)
            }
        }
    }

    private fun Int.toMusicTimeStamp(): String = with(this / 1000) {
        val second = this % 60
        val minute = this / 60
        "%02d:%02d".format(minute, second)
    }


    override fun onDestroy() {
        super.onDestroy()
        attempt {
            future?.cancel(true)
        }
        mediaPlay.release()
    }

}
