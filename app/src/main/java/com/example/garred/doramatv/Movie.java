package com.example.garred.doramatv;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.garred.doramatv.Tools.PageDownloader;

import org.json.JSONArray;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class Movie {
    String title;
    String creationYear;
    List<String> genres;
    List<String> mainActors;
    List<String> actors;
    List<String> producers;
    String description;
    String movieImageURL;
    String URL;
    String initialSeries;
    JSONArray sources;
    Boolean isSerial;

    public Movie(String title,String creationYear, List genres,String description,String movieImageURL,String movieURL,Boolean isSerial) {
        this.title = title;
        this.creationYear = creationYear;
        this.URL = movieURL;
        this.genres = genres;
        this.description = description;
        this.movieImageURL = movieImageURL;
        this.isSerial = isSerial;
    }


}
