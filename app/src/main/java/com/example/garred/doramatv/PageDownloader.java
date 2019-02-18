package com.example.garred.doramatv;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class PageDownloader extends AsyncTask<String,Void,Document> {

    @Override
    protected Document doInBackground(String... strings) {
        Document result = null;
        try {
            result = Jsoup.connect(strings[0]).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
