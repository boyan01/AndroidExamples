package com.example.animation

import android.animation.ValueAnimator
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_transition.*
import kotlinx.android.synthetic.main.scene_player_expanded.*

class TransitionActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transition)

//        val variety = Variety(container, playerExpanded, playerCollapsed)
//        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
//                if (!fromUser) return
//                val fraction: Float = progress.toFloat() / 1000
//                variety.set(fraction)
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar) {
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar) {
//            }
//
//        })

        button.setOnClickListener {
            val animator = ValueAnimator.ofFloat(0f, 1f)
            animator.duration = 500
            animator.addUpdateListener {
                val value = it.animatedValue as Float
                seekBar.progress = (value * 1000).toInt()
                seekBar.translationY = -1024 * value
                seekBar.layoutParams = seekBar.layoutParams.apply {
                    width = (width + value * width).toInt()
                }

                playerExpanded.translationX = 102 * value

                playerExpanded.layoutParams = playerExpanded.layoutParams.apply {
                    width = (width + value * width).toInt()
                    height = (height + value * height).toInt()
                }

                image.layoutParams = image.layoutParams.apply {
                    width = (width + (value/110) * width).toInt()
                    height = (height + (value/110) * height).toInt()
                }

            }
            animator.start()
        }

    }


}
