@file:Suppress("ConvertSecondaryConstructorToPrimary", "unused")

package com.example.animation

import android.animation.ArgbEvaluator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.graphics.PointF
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.View
import android.view.ViewGroup

/**
 * author : SUMMERLY
 * e-mail : yangbinyhbn@gmail.com
 * time   : 2017/8/10
 * desc   :
 */
class Variety {

    private val TAG = "Variety"

    private val pointVarieties = mutableMapOf<Int, Change<PointF>>()
    private val colorVarieties = mutableMapOf<Int, Change<Int>>()
    private val scaleVarieties = mutableMapOf<Int, Change<PointF>>()

    private val pointEvaluator = PointFEvaluator(PointF())
    private val colorEvaluator = ArgbEvaluator()

    private val start: View
    private val end: View

    constructor(container: ViewGroup, start: View, end: View) {
        //TODO if we got a container,we can just use the start and end layout id to get done this job
        this.start = start
        this.end = end
        container.post {
            capturesValues(start, end)
        }
    }

    private fun capturesValues(start: View, end: View) {
        if (start is ViewGroup && end is ViewGroup) {
            for (i in 0..start.childCount - 1) {
                val view = start.getChildAt(i)
                val viewEnd: View? = end.findViewById<View>(view.id)
                captureChange(view, viewEnd)
            }
        }
        captureChange(start, end)
    }


    private fun captureChange(viewStart: View, viewEnd: View?) {
        Log.i(TAG, "view:$viewStart,$viewEnd")
        viewEnd ?: return
        pointVarieties.put(viewStart.id, PointF(viewStart.x, viewStart.y) v PointF(viewEnd.x, viewEnd.y))
        val background1 = viewStart.background
        val background2 = viewEnd.background
        if (background1 is ColorDrawable && background2 is ColorDrawable) {
            colorVarieties.put(viewStart.id, background1.color v background2.color)
        }
        scaleVarieties.put(viewStart.id, PointF(1f, 1f) v PointF(viewEnd.width.toFloat() / viewStart.width, viewEnd.height.toFloat() / viewStart.height))
    }

    infix fun <T> T.v(that: T): Change<T> = Change(this, that)

    data class Change<out T>(val start: T, val end: T)

    val animator: ValueAnimator = ValueAnimator()
    fun animation(from: Float, to: Float) {
        animator.setFloatValues(from, to)
        animator.start()
        animator.addUpdateListener {
            val fraction = it.animatedValue as Float
            set(fraction)
        }
    }

    fun set(fraction: Float) {
        pointVarieties.forEach {
            val view = start.findViewById<View>(it.key)
            val point = pointEvaluator.evaluate(fraction, it.value.start, it.value.end)
            Log.i(TAG, "$view transition to $point")
            view.x = point.x
            view.y = point.y
        }
        colorVarieties.forEach {
            val view = start.findViewById<View>(it.key)
            val color = colorEvaluator.evaluate(fraction, it.value.start, it.value.end) as Int
            view.setBackgroundColor(color)
        }
        scaleVarieties.forEach {
            val view = start.findViewById<View>(it.key)
            val scale = pointEvaluator.evaluate(fraction, it.value.start, it.value.end)
            view.scaleX = scale.x
            view.scaleY = scale.y
        }
    }
}

class PointFEvaluator(reuse: PointF) : TypeEvaluator<PointF> {


    private val mPoint: PointF = reuse


    override fun evaluate(fraction: Float, startValue: PointF, endValue: PointF): PointF {
        val x = startValue.x + fraction * (endValue.x - startValue.x)
        val y = startValue.y + fraction * (endValue.y - startValue.y)
        mPoint.set(x, y)
        return mPoint
    }
}