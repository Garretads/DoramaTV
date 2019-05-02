package ru.garretech.garred.doramatv.tools

import android.net.Uri
import io.reactivex.Single
import org.json.JSONObject
import ru.garretech.garred.doramatv.Settings
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.ProtocolException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class VKRequest {

    companion object {
        const val METHOD_NAME = "video.get"
    }


    fun loadSourcesAsSingle(requestedId : String): Single<JSONObject> {
        return Single.create { observer ->
            val response = StringBuffer()

            try {
                val builder = Uri.Builder()
                builder.scheme("https")
                        .authority("api.vk.com")
                        .appendPath("method")
                        .appendPath(METHOD_NAME)
                        .appendQueryParameter("videos", requestedId)
                        .appendQueryParameter("access_token", Settings.access_token())
                        .appendQueryParameter("v", Settings.version())
                builder.build()

                val urlToRequest = URL(builder.toString())
                val urlConnection = urlToRequest!!.openConnection() as HttpsURLConnection
                urlConnection.doOutput = true
                urlConnection.requestMethod = "GET"
                urlConnection.setRequestProperty("User-Agent", "")
                urlConnection.connect()

                val responseCode = urlConnection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val input = BufferedReader(InputStreamReader(urlConnection.inputStream))
                    var inputLine: String?
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
            }

            if (response.isNotEmpty())
                observer.onSuccess(JSONObject(response.toString()))
            else
                observer.onError(Throwable("Response is empty"))
        }
    }
}