package ru.garretech.garred.doramatv.tools

import android.os.AsyncTask

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.StandardCharsets

class SearchRequest : AsyncTask<String, Void, Document>() {

    override fun doInBackground(vararg strings: String): Document? {
        var content: Document? = null
        try {
            val myURL = strings[0]
            val parameters = "q=" + strings[1] + "&offset=" + strings[2]
            var data: ByteArray?
            val inputStream: InputStream
            try {
                val url = URL(myURL)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.doInput = true

                conn.setRequestProperty("Content-Length", "" + Integer.toString(parameters.toByteArray().size))
                val os = conn.outputStream
                data = parameters.toByteArray(StandardCharsets.UTF_8)
                os.write(data)
                data = null

                conn.connect()
                val responseCode = conn.responseCode

                val baos = ByteArrayOutputStream()

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    inputStream = conn.inputStream

                    val buffer = ByteArray(8192) // Такого вот размера буфер
                    // Далее, например, вот так читаем ответ
                    var bytesRead: Int

                    while (inputStream.read(buffer).let {
                                bytesRead = it;
                                it != -1; }){
                        baos.write(buffer, 0, bytesRead)
                    }
                    data = baos.toByteArray()
                    val requestedContent = String(data, StandardCharsets.UTF_8)
                    content = Jsoup.parse(requestedContent)
                }

            } catch (e: MalformedURLException) {

                //resultString = "MalformedURLException:" + e.getMessage();
            } catch (e: IOException) {

                //resultString = "IOException:" + e.getMessage();
            } catch (e: Exception) {

                //resultString = "Exception:" + e.getMessage();
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return content
    }
}
