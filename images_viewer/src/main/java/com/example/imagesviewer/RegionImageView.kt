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

    /**
     * the max size of image draw on top right
     */
    private val SIZE_SUSPENSION = 200

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    var bitmap: Bitmap? = null

    var bitmapRegion: Bitmap? = null

    var bitmapRegionDecoder by Delegates.notNull<BitmapRegionDecoder>()

    val optionRegion = BitmapFactory.Options()


    val options = BitmapFactory.Options().apply {
        inPreferredConfig = Bitmap.Config.RGB_565
    }

    //fixme use URI?
    var imagePath = ""

    val paint = Paint()

    val focusPoint = PointF()

    val bitmapHeight
        get() = bitmap?.height ?: 0

    val bitmapWidth
        get() = bitmap?.width ?: 0


    private val scaleGestureDetector: ScaleGestureDetector

    private val gestureDetector: GestureDetector

    /**
     * to prevent the memory jitter
     * when we need Matrix instance , we can use this to get a Matrix instance
     */
    private val rectFPool: ObjectPool<RectF>


    /**
     * to prevent the memory jitter
     * when we need RectF instance , we can use this to get a rectF instance
     */
    private val matrixPool: ObjectPool<Matrix>

    /**
     * use to draw the bitmap to screen , it contains all translate and scale
     */
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
                focusPoint.set(detector.focusX, detector.focusY)
                scaleFactorOld = this@RegionImageView.matrixDraw.getScale()
                log("onDraw : onScaleBegin .....--------------------")
                return true
            }


            override fun onScale(detector: ScaleGestureDetector): Boolean {
                scaleImage(scaleFactorOld * detector.scaleFactor, focusPoint)
                return false
            }
        })
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                log("onScroll $distanceX,$distanceY")
                moveImageBy(-distanceX, -distanceY)
                return true
            }


        })

        viewTreeObserver.addOnGlobalLayoutListener(this)

    }

    /**
     * if need, display the high definition picture on screen
     */
    private fun displayHDImage() {
        //if raw image has never been scaled
        if (options.inSampleSize == 1) {
            return
        }

        optionRegion.apply {
            inSampleSize = getUsableInSampleSize(options.inSampleSize / matrixDraw.getScale())
        }
        //if hd inSampleSize is large than raw, obviously we do not need to redisplay the "HD' image
        if (optionRegion.inSampleSize >= options.inSampleSize) {
            return
        }
        val displayRegionRaw = getDisplayRegionRaw()

        //fixme: decode region in main thread maybe not good
        bitmapRegion = bitmapRegionDecoder.decodeRegion((displayRegionRaw * options.inSampleSize).toRect(), optionRegion)
        log("onScaleEnd : $displayRegionRaw scale : ${optionRegion.inSampleSize}")
        rectFPool.restore(displayRegionRaw)
        invalidate()
    }


    /**
     * We want the actual sample size that will be used, so round down to nearest power of 2.
     */
    private fun getUsableInSampleSize(scale: Float): Int {
        var power = 1
        while (power * 2 < scale) {
            power *= 2
        }
        return power
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
        bitmapRegion = null

        gestureDetector.onTouchEvent(event)
        scaleGestureDetector.onTouchEvent(event)

        if (event.actionMasked == MotionEvent.ACTION_UP) {
            displayHDImage()
        }
        return true
    }


    override fun onDraw(canvas: Canvas) = bitmap?.let {

        log("options scale : ${options.inSampleSize}")
        log("matrix scale : ${matrixDraw.getScale()}")

        canvas.drawBitmap(it, matrixDraw, paint)



        bitmapRegion?.let {
            val screen = getDisplayRegion()
            canvas.drawBitmap(it, null, screen, paint)
            rectFPool.restore(screen)
        }


        //debug used to show some important info
        //draw a suspension image on right top
        val suspensionRect = getSuspensionRect()
        canvas.drawBitmap(it, null, suspensionRect, paint)

        val rectRawBitmap = rectFPool.get().apply {
            set(0f, 0f, bitmapWidth.toFloat(), bitmapHeight.toFloat())
        }

//        val rawPosition = focusPoint.getRawPosition()
//        val mappingPoint = suspensionRect.getMappingPoint(rawPosition, rectRawBitmap)

        //draw the focus point on suspension image
//        canvas.drawCircle(mappingPoint.x, mappingPoint.y, 10f, paint.apply {
//            style = Paint.Style.FILL
//            color = Color.GREEN
//        })

        //draw the showing region on suspension
        //to find the showing rect
        val displayRegionRaw = getDisplayRegionRaw()

        //first get the matrix reflect raw to suspension
        val matrixRowToSus = matrixPool.get()
        matrixRowToSus.setRectToRect(rectRawBitmap, suspensionRect, Matrix.ScaleToFit.FILL)

        matrixRowToSus.mapRect(displayRegionRaw) {
            canvas.drawRect(it, paint.apply {
                style = Paint.Style.STROKE
                color = Color.GREEN
                strokeWidth = 5f
            })
        }
        rectFPool.restore(suspensionRect, displayRegionRaw, rectRawBitmap)
        //draw the focus point of screen we touched
//        canvas.drawCircle(focusPoint.x, focusPoint.y, 10f, paint.apply {
//            style = Paint.Style.FILL
//            color = Color.RED
//        })

    } ?: Unit

    private fun getDisplayRegionRaw(): RectF = rectFPool.get().apply {
        matrixDraw.invert { inverse ->
            val displayRegion = getDisplayRegion()
            inverse.mapRect(this, displayRegion)
            rectFPool.restore(displayRegion)
        }
    }

    private fun getDisplayRegion(): RectF {
        //get the screen rect
        val rectScreen = rectFPool.get().apply {
            set(0F, 0F, width.toFloat(), height.toFloat())
        }
        //get the image display rect
        val rectImageDisplay = rectFPool.get().apply {
            val rectBitmap = rectFPool.get()
            rectBitmap.set(0f, 0f, bitmapWidth.toFloat(), bitmapHeight.toFloat())
            matrixDraw.mapRect(this, rectBitmap)
            rectFPool.restore()
        }

        //get the intersect rect of image and screen
        val displayRegion = rectFPool.get().apply {
            setIntersect(rectScreen, rectImageDisplay)
        }
        return displayRegion
    }

    private fun getSuspensionRect(): RectF {
        if (bitmapWidth == 0 || bitmapHeight == 0) {
            return rectFPool.get()
        }
        if (bitmapWidth > bitmapHeight) {
            return rectFPool.get().apply {
                set(width - dip(SIZE_SUSPENSION).toFloat(), 0f
                        , width.toFloat(), (dip(SIZE_SUSPENSION).toFloat() / bitmapWidth) * bitmapHeight)
            }
        } else {
            return rectFPool.get().apply {
                //adapt the image
                set(width - (dip(SIZE_SUSPENSION).toFloat() / bitmapHeight) * bitmapWidth, 0f,
                        width.toFloat(), dip(SIZE_SUSPENSION).toFloat())
            }
        }
    }


    /**
     * get the point in this rect which mapping p point from src Rect
     */
    @Suppress("unused")
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
        matrixDraw.invert { invert ->
            val dest = FloatArray(2)
            invert.mapPoints(dest, floatArrayOf(this@getRawPosition.x, this@getRawPosition.y))
            x = dest[0]
            y = dest[1]
        }

    }

    /**
     * get matrix map rect and use it (when predicate is done , the rectFPool will restore it)
     */
    private inline fun Matrix.mapRect(src: RectF, predicate: (RectF) -> Unit) {
        val dest = rectFPool.get()
        this@mapRect.mapRect(dest, src)
        predicate(dest)
        rectFPool.restore()
    }

    private inline fun Matrix.invert(predicate: (Matrix) -> Unit) {
        val invert = matrixPool.get().apply {
            this@invert.invert(this)
        }
        predicate(invert)
        matrixPool.restore(invert)
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

            inSampleSize = getUsableInSampleSize(outWidth.toFloat() / measuredWidth)

            inJustDecodeBounds = false
            bitmap = BitmapFactory.decodeFile(imagePath, this)
            val scaleFactor = measuredWidth / bitmapWidth.toFloat()
            matrixDraw.reset()
            log("onGlobalLayout : scaleFactor = $scaleFactor")
            matrixDraw.postScale(scaleFactor, scaleFactor)
            //move picture to center
            matrixDraw.postTranslate(0f, measuredHeight / 2f - (bitmapHeight * scaleFactor) / 2)
        }
        invalidate()
        viewTreeObserver.removeOnGlobalLayoutListener(this)
    }

    private fun RectF.toRect() = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())


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

    private operator fun RectF.times(scale: Int) = apply {
        left *= scale
        right *= scale
        top *= scale
        bottom *= scale
    }

    /**
     * a pool for restore the object to prevents memory jitter
     */
    abstract class ObjectPool<T>(capacity: Int = 16) {
        val objects: Queue<T>

        var size = capacity

        init {
            objects = LinkedList()
        }

        /**
         *  get an object from pool , must restore it after using
         */
        fun get(): T {
            if (objects.size == 0) {
                return generateAnObject()
            }
            return reset(objects.poll())
        }


        fun restore(vararg t: T) {
            for (e in t) {
                if (objects.size < size) {
                    objects.offer(e)
                }
            }
        }

        protected abstract fun reset(t: T): T


        protected abstract fun generateAnObject(): T


    }

}








