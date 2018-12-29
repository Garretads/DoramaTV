package com.example.garred.doramatv;

public class Movie {
    String title;
    String creationYear;
    String[] genres;
    String[] mainActors;
    String[] actors;
    String[] producers;
    String description;

    public Movie(String title,String creationYear, String[] genres,String description) {
        this.title = title;
        this.creationYear = creationYear;
        this.genres = genres;
        this.description = description;
    }


}
