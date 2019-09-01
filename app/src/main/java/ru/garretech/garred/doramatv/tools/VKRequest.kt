package ru.garretech.garred.doramatv.tools

import android.net.Uri
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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


    fun loadSourcesAsSingle(requestedId : String) =
        Single.create<JSONObject> { observer ->
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
                val urlConnection = urlToRequest.openConnection() as HttpsURLConnection
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
                if (observer.isDisposed)
                    observer.onError(e)
            } catch (e: ProtocolException) {
                if (observer.isDisposed)
                    observer.onError(e)
            } catch (e: IOException) {
                if (observer.isDisposed)
                    observer.onError(e)
            }

            if (response.isNotEmpty())
                observer.onSuccess(JSONObject(response.toString()))
            else
                observer.onError(Throwable("Response is empty"))
        }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
}