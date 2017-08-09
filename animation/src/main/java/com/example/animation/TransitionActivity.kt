package com.example.animation

import android.animation.ArgbEvaluator
import android.animation.TypeEvaluator
import android.graphics.PointF
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_transition.*
import kotlinx.android.synthetic.main.scene_player_collapsed.*
import kotlinx.android.synthetic.main.scene_player_expanded.*

class TransitionActivity : AppCompatActivity() {

    private val TAG = "TransitionActivity"


    private val pointVarieties = mutableMapOf<Int, Variety<PointF>>()
    private val colorVarieties = mutableMapOf<Int, Variety<Int>>()
    private val scaleVarieties = mutableMapOf<Int, Variety<PointF>>()

    val pointEvaluator = PointFEvaluator(PointF())
    val colorEvaluator = ArgbEvaluator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transition)

        playerCollapsed.post {
            capturesValues(playerExpanded, playerCollapsed)
            Log.i(TAG, pointVarieties.toString())
            Log.i(TAG, colorVarieties.toString())
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                val fraction: Float = progress.toFloat() / 1000
                pointVarieties.forEach {
                    val view = playerExpanded.findViewById<View>(it.key)
                    val point = pointEvaluator.evaluate(fraction, it.value.start, it.value.end)
                    Log.i(TAG, "$view transition to $point")
                    view.x = point.x
                    view.y = point.y
                }
                colorVarieties.forEach {
                    val view = playerExpanded.findViewById<View>(it.key)
                    val color = colorEvaluator.evaluate(fraction, it.value.start, it.value.end) as Int
                    view.setBackgroundColor(color)
                }
                scaleVarieties.forEach {
                    val view = playerExpanded.findViewById<View>(it.key)
                    val scale = pointEvaluator.evaluate(fraction, it.value.start, it.value.end)
                    view.scaleX = scale.x
                    view.scaleY = scale.y
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }

        })


    }

    private fun capturesValues(start: ViewGroup, end: ViewGroup) {
        for (i in 0..start.childCount - 1) {
            val view = start.getChildAt(i)
            val viewEnd: View? = end.findViewById<View>(view.id)
            captureChange(view, viewEnd)
        }
    }

    private fun captureChange(viewStart: View, viewEnd: View?) {
        Log.i(TAG, "view:$viewStart,$viewEnd")
        viewEnd ?: return
        pointVarieties.put(viewStart.id, PointF(viewStart.x, viewStart.y) t PointF(viewEnd.x, viewEnd.y))
        val background1 = viewStart.background
        val background2 = viewEnd.background
        if (background1 is ColorDrawable && background2 is ColorDrawable) {
            colorVarieties.put(viewStart.id, background1.color t background2.color)
        }
        scaleVarieties.put(viewStart.id, PointF(1f, 1f) t PointF(viewEnd.width.toFloat() / viewStart.width, viewEnd.height.toFloat() / viewStart.height))
    }

    private infix fun <T> T.t(that: T): Variety<T> = Variety(this, that)

    data class Variety<out T>(val start: T, val end: T)


    class PointFEvaluator(reuse: PointF) : TypeEvaluator<PointF> {


        private val mPoint: PointF = reuse


        override fun evaluate(fraction: Float, startValue: PointF, endValue: PointF): PointF {
            val x = startValue.x + fraction * (endValue.x - startValue.x)
            val y = startValue.y + fraction * (endValue.y - startValue.y)
            mPoint.set(x, y)
            return mPoint
        }
    }
}
