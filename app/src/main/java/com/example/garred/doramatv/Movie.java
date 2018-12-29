package com.example.garred.doramatv;

import android.graphics.Bitmap;

public class Movie {
    String title;
    String creationYear;
    String[] genres;
    String[] mainActors;
    String[] actors;
    String[] producers;
    String description;
    Bitmap movieImage;
    String URL;

    public Movie(String title,String creationYear, String[] genres,String description,Bitmap movieImage) {
        this.title = title;
        this.creationYear = creationYear;
        this.genres = genres;
        this.description = description;
        this.movieImage = movieImage;
    }


}
