package com.example.imagesviewer

import android.content.Context
import android.graphics.*
import android.support.annotation.ColorInt
import android.support.annotation.WorkerThread
import android.util.Log
import android.view.WindowManager
import android.widget.ImageView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File


/**
 * <pre>
 *     author : summerly
 *     e-mail : yangbinyhbn@gmail.com
 *     time   : 2017/6/30
 *     desc   :
 * </pre>
 */
fun Any.log(message: String?, tag: String = this.javaClass.name.substringAfterLast('.').asTag()) {
    if (true) {// todo close log output
        Log.i(if (tag.isEmpty()) "empty" else tag, message)
    }
}

private fun String.asTag() =
        if (length <= 22) this else substring(0, 22)

fun File.isImageFile(): Boolean {
    return name.endsWith(".png", true) || name.endsWith(".jpg", true)
}

@Suppress("DEPRECATION")
fun ImageView.setImagePath(imagePath: String,
                           widthWant: Int = (context.getSystemService(Context.WINDOW_SERVICE)
                                   as WindowManager).defaultDisplay.width / 3) {
    Observable
            .create<Bitmap> {
                //load image  (load empty bitmap first , load real image second)
                it.onNext(getImageByPath(imagePath, BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                    BitmapFactory.decodeFile(imagePath, this)
                    val width = outWidth
                    val height = outHeight

                    //first fill a loading bitmap to image view
                    val heightWant = ((widthWant.toFloat() * height) / width).toInt()
                    it.onNext(createPlaceHolderImage(widthWant, heightWant))

                    inSampleSize = width / widthWant //
                    inJustDecodeBounds = false
                }))
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                this.setImageBitmap(it)
            }
}

/**
 * set a blank image to the image view , show the loading action
 */
private fun createPlaceHolderImage(width: Int, height: Int,
                                   text: String = "loading...",
                                   @ColorInt backColor: Int = Color.GRAY,
                                   @ColorInt foreColor: Int = Color.BLACK)
        = (Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
    Canvas(this).let {
        //draw a blank bitmap
        Paint().apply {

            //draw the background ....
            color = backColor
            it.drawRect(0f, 0f, width.toFloat(), height.toFloat(), this)

            // draw the text loading on center
            color = foreColor
            textAlign = Paint.Align.CENTER
            textSize = 24f
            it.drawText(text, width.toFloat() / 2, height.toFloat() / 2, this)
        }
    }

})


@WorkerThread
fun getImageByPath(imagePath: String, options: BitmapFactory.Options): Bitmap {
    options.log("original bitmap : width = ${options.outWidth} height = ${options.outHeight}")
    val bitmap = BitmapFactory.decodeFile(imagePath, options)
    options.log("scaled bitmap : width = ${bitmap.width} height = ${bitmap.height}")
    Thread.sleep((5000 * Math.random()).toLong())// let it sleep seconds to simulate "delay loading"
    return bitmap
}