package ru.garretech.garred.doramatv.tools

import android.os.AsyncTask

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.ProtocolException
import java.net.URL

import javax.net.ssl.HttpsURLConnection

class VKRequest : AsyncTask<String, Void, String>() {
    private var urlToRequest: URL? = null


    override fun doInBackground(vararg url: String): String {
        val response = StringBuffer()
        try {
            urlToRequest = URL(url[0])
            val urlConnection = urlToRequest!!.openConnection() as HttpsURLConnection
            urlConnection.doOutput = true
            urlConnection.requestMethod = "GET"
            urlConnection.setRequestProperty("User-Agent", "")
            urlConnection.connect()

            val responseCode = urlConnection.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val input = BufferedReader(InputStreamReader(urlConnection.inputStream))
                var inputLine: String
                while (input.readLine().let { inputLine = it; it != null }) {
                    response.append(inputLine)
                }
                input.close()
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: ProtocolException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return response.toString()
    }
}
