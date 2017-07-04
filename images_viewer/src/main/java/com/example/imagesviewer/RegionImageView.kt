package com.example.imagesviewer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.*
import org.jetbrains.anko.dip
import java.util.*
import kotlin.properties.Delegates


class RegionImageView : View, ViewTreeObserver.OnGlobalLayoutListener {

    companion object {
        private val STATE_DRAG = 1
        private val STATE_SCALE = 2
        private val STATE_NORMAL = 3
    }

    private val WIDTH_SUSPENSION = 200

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    var bitmap: Bitmap? = null

    var bitmapRegionDecoder by Delegates.notNull<BitmapRegionDecoder>()

    val options = BitmapFactory.Options().apply {
        inPreferredConfig = Bitmap.Config.RGB_565
    }

    //todo use URI?
    var imagePath = ""

    val paint = Paint()

    val focusPoint = PointF()

    val bitmapHeight
        get() = bitmap?.height ?: 0

    val bitmapWidth
        get() = bitmap?.width ?: 0


    private val scaleGestureDetector: ScaleGestureDetector

    private val gestureDetector: GestureDetector

    private val rectFPool: ObjectPool<RectF>

    private val matrixPool: ObjectPool<Matrix>

    private var currentState = STATE_NORMAL

    private val matrixDraw: Matrix


    init {

        rectFPool = object : ObjectPool<RectF>() {
            override fun reset(t: RectF) = t.apply {
                setEmpty()
            }

            override fun generateAnObject(): RectF = RectF()
        }

        matrixPool = object : ObjectPool<Matrix>() {
            override fun reset(t: Matrix) = t.apply {
                reset()
            }

            override fun generateAnObject() = Matrix()

        }

        matrixDraw = matrixPool.get()

        scaleGestureDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

            private var scaleFactorOld = 1f //use to remember the last scale factor

            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                focusPoint.set(detector.focusX, detector.focusY)
                val rawPosition = focusPoint.getRawPosition()
                if (!rawPosition.isInBitmap()) {
                    log(" $rawPosition is not in bitmap")
                    focusPoint.set(1f, 1f)
                    return false
                }
//                currentState = STATE_SCALE
                focusPoint.set(detector.focusX, detector.focusY)
                scaleFactorOld = this@RegionImageView.matrixDraw.getScale()
                log("onDraw : onScaleBegin .....--------------------")
                return true
            }


            override fun onScale(detector: ScaleGestureDetector): Boolean {
//                if (currentState == STATE_SCALE) {
                scaleImage(scaleFactorOld * detector.scaleFactor, focusPoint)
//                }
                return false
            }

            override fun onScaleEnd(detector: ScaleGestureDetector?) {
                currentState = STATE_NORMAL
            }


        })
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                log("onScroll $distanceX,$distanceY")
                if (currentState != STATE_SCALE)
                    moveImageBy(-distanceX, -distanceY)
                return true
            }
        })

        viewTreeObserver.addOnGlobalLayoutListener(this)

    }

    fun moveImageBy(dx: Float, dy: Float) {
        log("moveImageBy : $dx ,$dy")
        matrixDraw.postTranslate(dx, dy)
        invalidate()
    }

    /**
     * @param scale : the scale factor of the picture
     * @param focus : the point of scale
     */
    fun scaleImage(scale: Float, focus: PointF) {
        log("scaleImage -- focus: $focus , scale: $scale")
        focusPoint.set(focus)
        matrixDraw.apply {
            //apply scale
            val oldScale = getScale()
            postScale(scale / oldScale, scale / oldScale, focus.x, focus.y)
        }
        invalidate()
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        scaleGestureDetector.onTouchEvent(event)
        return true
    }


    override fun onDraw(canvas: Canvas) = bitmap?.let {

        canvas.drawBitmap(it, matrixDraw, paint)

        //debug used to show some important info

        //draw a suspension image on right top


        canvas.drawBitmap(it, null, getSuspensionRect(), paint)

        rectFPool.use { rectRawBitmap ->
            rectRawBitmap.set(0f, 0f, bitmapWidth.toFloat(), bitmapHeight.toFloat())
            val rawPosition = focusPoint.getRawPosition()
            val mappingPoint = getSuspensionRect().getMappingPoint(rawPosition, rectRawBitmap)

//            canvas.drawRect(rectRawBitmap, paint.apply {
//                style = Paint.Style.STROKE
//                color = Color.BLUE
//                strokeWidth = 5f
//            })

            //draw the focus point on raw image
//            canvas.drawCircle(rawPosition.x, rawPosition.y, 10f, paint.apply {
//                style = Paint.Style.FILL
//                color = Color.BLUE
//            })


            //draw the focus point on suspension image
            canvas.drawCircle(mappingPoint.x, mappingPoint.y, 10f, paint.apply {
                style = Paint.Style.FILL
                color = Color.GREEN
            })

        }


        matrixDraw.invert { inverse ->
            rectFPool.use { dst ->
                rectFPool.use { src ->
                    src.set(0F, 0F, width.toFloat(), height.toFloat())
                    inverse.mapRect(dst, src)
                }

//                canvas.drawRect(dst, paint.apply {
//                    style = Paint.Style.STROKE
//                    color = Color.BLUE
//                    strokeWidth = 5f
//                })


                //draw the current showing screen stroke on suspension
                matrixPool.use { matrixRowToSus ->
                    rectFPool.use { rectBitmap ->
                        rectBitmap.set(0f, 0f, bitmapWidth.toFloat(), bitmapHeight.toFloat())
                        matrixRowToSus.setRectToRect(rectBitmap, getSuspensionRect(), Matrix.ScaleToFit.FILL)

                        rectFPool.use {
                            matrixRowToSus.mapRect(it, dst)
                            canvas.drawRect(it, paint.apply {
                                style = Paint.Style.STROKE
                                color = Color.GREEN
                                strokeWidth = 5f
                            })
                        }

                    }
                }
            }
        }

        //draw the focus point of screen we touched
        canvas.drawCircle(focusPoint.x, focusPoint.y, 10f, paint.apply {
            style = Paint.Style.FILL
            color = Color.RED
        })

    } ?: Unit

    private fun getSuspensionRect() =
            rectFPool.get().apply {
                set(width - dip(WIDTH_SUSPENSION).toFloat(), 0f
                        , width.toFloat(), (dip(WIDTH_SUSPENSION).toFloat() / bitmapWidth) * bitmapHeight)
            }

    /**
     * get the point in this rect which mapping p point from src Rect
     */
    private fun RectF.getMappingPoint(p: PointF, src: RectF) = PointF(
            getMappingX(p.x, src),
            getMappingY(p.y, src)
    )

    private fun RectF.getMappingX(x: Float, src: RectF) = (x - src.left) / src.width() * width() + left

    private fun RectF.getMappingY(y: Float, src: RectF) = (y - src.top) / src.height() * height() + top


    private fun Matrix.getScale(): Float {
        val values = FloatArray(9)
        this.getValues(values)
        return values[0]
    }


    /**
     * use screen point to find this point of bitmap
     */
    private fun PointF.getRawPosition(): PointF = PointF(-1f, -1f).apply {
        matrixDraw.invert {
            val dest = FloatArray(2)
            it.mapPoints(dest, floatArrayOf(this@getRawPosition.x, this@getRawPosition.y))
            x = dest[0]
            y = dest[1]
        }
    }

    private inline fun Matrix.invert(predicate: (Matrix) -> Unit) = matrixPool.use {
        this.invert(it)
        predicate(it)
    }


    /**
     * use bitmap position to find the screen point
     */
    private fun PointF.getScreenPosition(): PointF = PointF(-1f, -1f).apply {
        val dest = FloatArray(2)
        matrixDraw.mapPoints(dest, floatArrayOf(this@getScreenPosition.x, this@getScreenPosition.y))
        x = dest[0]
        y = dest[1]
    }


    fun setImage(path: String) {
        imagePath = path
        //create a bitmapRegionDecoder for this file
        bitmapRegionDecoder = BitmapRegionDecoder.newInstance(path, false)
        invalidate()
    }

    // display the whole image when this is view first create
    override fun onGlobalLayout() {
        with(options) {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(imagePath, this)
            inSampleSize = outWidth / measuredWidth
            inJustDecodeBounds = false
            bitmap = BitmapFactory.decodeFile(imagePath, this)

            val scaleFactor = measuredWidth / bitmapWidth.toFloat()
            matrixDraw.reset()
            log("onGlobalLayout : scaleFactor = $scaleFactor")
            matrixDraw.postScale(scaleFactor, scaleFactor)
            matrixDraw.postTranslate(0f, height / 2f - bitmapHeight / 2)
        }
        invalidate()
        viewTreeObserver.removeOnGlobalLayoutListener(this)
    }


    private fun PointF.isInBitmap() = x in 0..bitmapWidth && y in 0..bitmapHeight

    private operator fun PointF.plus(point: PointF) = PointF().apply {
        x = this@plus.x + point.x
        y = this@plus.y + point.y
    }

    private operator fun PointF.minus(point: PointF) = PointF().apply {
        x = this@minus.x - point.x
        y = this@minus.y - point.y
    }

    private operator fun PointF.div(div: Float) = PointF().apply {
        x = this@div.x / div
        y = this@div.y / div
    }

    /**
     * a pool to restore the object to prevents memory jitter
     */
    abstract class ObjectPool<T>(capacity: Int = 16) {
        val objects: Queue<T>

        var size = capacity

        init {
            objects = LinkedList()
        }

        fun get(): T {
            if (objects.size == 0) {
                size *= 2
                return generateAnObject()
            }
            return reset(objects.poll())
        }

        inline fun use(predicate: (t: T) -> Unit) {
            val t = get()
            try {
                predicate(t)
            } finally {
                restore(t)
            }
        }

        fun restore(t: T) {
            if (objects.size < size) {
                objects.offer(t)
            }
        }

        abstract fun reset(t: T): T


        protected abstract fun generateAnObject(): T


    }

}



