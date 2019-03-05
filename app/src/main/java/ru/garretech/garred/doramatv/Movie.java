package ru.garretech.garred.doramatv;

import org.json.JSONArray;
import java.util.List;

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

    public Movie(String title, List<String> genres,String description,String movieImageURL,String movieURL,Boolean isSerial) {
        this.title = title;
        this.URL = movieURL;
        this.genres = genres;
        this.description = description;
        this.movieImageURL = movieImageURL;
        this.isSerial = isSerial;
    }


}
