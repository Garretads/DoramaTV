package ru.garretech.garred.doramatv.model;

import org.json.JSONArray;
import java.util.List;

public class Movie {
    private String title;
    String creationYear;
    private List<String> genres;
    List<String> mainActors;
    List<String> actors;
    List<String> producers;
    private String description;
    private String movieImageURL;
    private String URL;
    private JSONArray sources;
    private Boolean isSerial;

    public Movie(String title, List<String> genres,String description,String movieImageURL,String movieURL,Boolean isSerial) {
        this.title = title;
        this.URL = movieURL;
        this.genres = genres;
        this.description = description;
        this.movieImageURL = movieImageURL;
        this.isSerial = isSerial;
    }


    public String getTitle() {
        return title;
    }

    public List<String> getGenres() {
        return genres;
    }

    public String getDescription() {
        return description;
    }

    public String getMovieImageURL() {
        return movieImageURL;
    }

    public String getURL() {
        return URL;
    }


    public JSONArray getSources() {
        return sources;
    }

    public Boolean getSerial() {
        return isSerial;
    }
}
