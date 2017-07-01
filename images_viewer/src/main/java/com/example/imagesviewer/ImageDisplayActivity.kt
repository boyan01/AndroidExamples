package com.example.imagesviewer

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.davemorrissey.labs.subscaleview.ImageSource
import kotlinx.android.synthetic.main.activity_image_display.*
import java.io.File

class ImageDisplayActivity : AppCompatActivity() {

    companion object {
        val ARG_PATH = "image_path"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_display)
        val path: String? = intent.getStringExtra(ARG_PATH)
        image.setBackgroundResource(android.R.color.transparent)
        path?.let {
            image.setImage(ImageSource.uri(Uri.fromFile(File(it))))
        }
    }
}
