package com.example.exo

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.source.ads.AdsMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_playlist.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class ExoPlayListActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ExoPlayListActivity"
    }

    private lateinit var adapter: PlaylistAdapter

    private val player by lazy { ExoPlayerFactory.newSimpleInstance(this) }

    private val lists = ArrayList<PlayBean>()

    private val mediaSource = ConcatenatingMediaSource()

    private val handler = Handler()

    private val adsLoader = MyAdsLoader()


    private val dataSourceFactory by lazy {
        DefaultDataSourceFactory(
                this@ExoPlayListActivity, Util.getUserAgent(this@ExoPlayListActivity, ""), null)
    }

    private val contract = object : PlayingContract {


        override fun play(bean: PlayBean) {
            player.seekTo(lists.indexOf(bean), 0)
            player.playWhenReady = true
            adapter.notifyDataSetChanged()
        }

        override fun currentPlaying(): PlayBean {
            return lists[player.currentWindowIndex]
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist)


        mediaSource.addEventListener(handler, object : DefaultMediaSourceEventListener() {
            override fun onLoadStarted(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?,
                                       loadEventInfo: MediaSourceEventListener.LoadEventInfo,
                                       mediaLoadData: MediaSourceEventListener.MediaLoadData) {
                Log.i(TAG, "onLoadStarted : windowIndex = $windowIndex ")
            }

        })

        adapter = PlaylistAdapter(lists, contract)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))


        playerView.player = player
        player.addListener(object : Player.EventListener {

            override fun onTimelineChanged(timeline: Timeline, manifest: Any?, reason: Int) {
                Log.i(TAG, "onTimeLineChanged : ${timeline.windowCount}, $reason")
                adapter.notifyDataSetChanged()
            }

            override fun onPositionDiscontinuity(reason: Int) {
                Log.i(TAG, "onPositionDiscontinuity  $reason")
                adapter.notifyDataSetChanged()
            }

        })

        doAsync {
            val input = assets.open("playlist.json")
            val data = Gson().fromJson<List<PlayBean>>(input.reader(), object : TypeToken<List<PlayBean>>() {}.type)
            lists.addAll(data)
            mediaSource.addMediaSources(lists.map { it.toMediaSource(dataSourceFactory) })
            uiThread {
                player.prepare(mediaSource)
                contract.play(lists[0])
            }
        }

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {


        menu.add("insert new list")
                .setOnMenuItemClickListener {
                    val playBean = lists[0].copy(name = "test ${lists.size}")
                    mediaSource.addMediaSource(playBean.toMediaSource(dataSourceFactory), handler) {
                        lists.add(playBean)
                    }
                    true
                }

        menu.add("show info")
                .setOnMenuItemClickListener {
                    Log.i(TAG, """
                        player info :  currentWindowIndex : ${player.currentWindowIndex} , currentPeriodIndex : ${player.currentPeriodIndex}
                        windowCount: ${player.currentTimeline.windowCount} periodCount: ${player.currentTimeline.periodCount}
                    """.trimIndent())
                    true
                }
        return true
    }


    private fun PlayBean.toMediaSource(factory: DataSource.Factory): MediaSource {
        val contentSource = ProgressiveMediaSource.Factory(factory).setTag(this).createMediaSource(Uri.parse(url))
        if (hasAd) {
            return AdsMediaSource(contentSource, factory, adsLoader, playerView)
        }
        return contentSource
    }


}
