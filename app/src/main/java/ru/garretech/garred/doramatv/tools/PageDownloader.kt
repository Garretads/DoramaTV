package ru.garretech.garred.doramatv.tools

import android.os.AsyncTask
import org.jsoup.HttpStatusException

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import java.io.IOException
import java.net.SocketException
import java.net.UnknownHostException

class PageDownloader : AsyncTask<String, Void, Document>() {

    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg strings: String): Document? {
        var result: Document? = null
        try {
            result = Jsoup.connect(strings[0]).get()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e : HttpStatusException) {
            e.printStackTrace()
        } catch (e : ArrayIndexOutOfBoundsException) {
            e.printStackTrace()
        } catch (e: SocketException) {
            e.printStackTrace()
        } catch (e : UnknownHostException) {
            e.printStackTrace()
        }

        return result
    }
}

