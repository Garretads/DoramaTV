package ru.garretech.garred.doramatv.tools

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask

import java.io.IOException
import java.net.URL

class ImageDownloader : AsyncTask<String, Void, Bitmap>() {

    override fun doInBackground(vararg strings: String): Bitmap? {
        var image: Bitmap? = null
        try {
            val url = URL(strings[0])
            image = BitmapFactory.decodeStream(url.openConnection().getInputStream())
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return image
    }
}
