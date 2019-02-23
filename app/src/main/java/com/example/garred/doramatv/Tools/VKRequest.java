package com.example.garred.doramatv.Tools;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class VKRequest extends AsyncTask<String,Void, String> {
    URL urlToRequest;

    @Override
    protected String doInBackground(String... url) {
        StringBuffer response = new StringBuffer();
        try {
            urlToRequest = new URL(url[0]);
            HttpsURLConnection urlConnection = (HttpsURLConnection) urlToRequest.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("User-Agent", "");
            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response.toString();
    }
}
