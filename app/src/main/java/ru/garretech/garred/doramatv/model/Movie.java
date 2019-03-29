package ru.garretech.garred.doramatv.model;

import android.graphics.Bitmap;

import org.json.JSONArray;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ru.garretech.garred.doramatv.tools.ImageDownloader;

public class Movie {
    private String title;
    private String creationYear;
    private List<String> genres;
    private List<String> mainActors;
    private List<String> actors;
    private List<String> producers;
    private Bitmap image;
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

    public void setImage(Bitmap image) {
        this.image = image;
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

    public Bitmap getImage() { return image; }


    public JSONArray getSources() {
        return sources;
    }

    public Boolean getSerial() {
        return isSerial;
    }
}
