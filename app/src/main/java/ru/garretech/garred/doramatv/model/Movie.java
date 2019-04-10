package ru.garretech.garred.doramatv.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;


import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import ru.garretech.garred.doramatv.tools.ListConverter;

@Entity
public class Movie implements Serializable {

    private String title;

    @ColumnInfo(name = "production_year")
    private String productionYear;

    @TypeConverters(ListConverter.class)
    private List<String> genres;
    //private List<String> mainActors;
    //private List<String> actors;
    //private List<String> producers;
    @ColumnInfo(name = "production_country")
    private String productionCountry;

    @ColumnInfo(name = "series_number")
    private String seriesNumber;

    private String duration;

    @Ignore
    private transient Bitmap image;

    @ColumnInfo(name = "image_cached_path")
    private String imageCachedPath;

    private String description;

    @ColumnInfo(name = "movie_image_url")
    private String movieImageURL;

    @ColumnInfo(name = "initial_series")
    private String initialSeries;

    @PrimaryKey @NonNull
    private String URL;

    /*info.put("title",name + " | " + eng_name + " | " + original_name);
                        info.put("url",url);
                        info.put("genres",genres.toString());
                        info.put("image_url",image_url);
                        info.put("initial_series",initialSeries);
                        info.put("production",production);
                        info.put("series_number",seriesNumber);
                        info.put("duration",duration);
                        info.put("description",description);
                        info.put("age",age);
*/

    public Movie(String title, List<String> genres, String movieImageURL, String URL) {
        this.title = title;
        this.URL = URL;
        this.genres = genres;
        this.movieImageURL = movieImageURL;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProductionYear() {
        return productionYear;
    }

    public void setProductionYear(String productionYear) {
        this.productionYear = productionYear;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getImageCachedPath() {
        return imageCachedPath;
    }

    public void setImageCachedPath(String imageCachedPath) {
        this.imageCachedPath = imageCachedPath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMovieImageURL() {
        return movieImageURL;
    }

    public void setMovieImageURL(String movieImageURL) {
        this.movieImageURL = movieImageURL;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getProductionCountry() {
        return productionCountry;
    }

    public void setProductionCountry(String productionCountry) {
        this.productionCountry = productionCountry;
    }

    public String getSeriesNumber() {
        return seriesNumber;
    }

    public void setSeriesNumber(String seriesNumber) {
        this.seriesNumber = seriesNumber;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getInitialSeries() {
        return initialSeries;
    }

    public void setInitialSeries(String initialSeries) {
        this.initialSeries = initialSeries;
    }

}

