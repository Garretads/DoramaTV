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

public class Movie implements Parcelable {
    String title;
    String creationYear;
    List<String> genres;
    List<String> mainActors;
    List<String> actors;
    List<String> producers;
    String description;
    String movieImageURL;
    String URL;
    JSONArray sources;

    public Movie(String title,String creationYear, List genres,String description,String movieImageURL,String movieURL) {
        this.title = title;
        this.creationYear = creationYear;
        this.URL = movieURL;
        this.genres = genres;
        this.description = description;
        this.movieImageURL = movieImageURL;
    }

    public Movie(Parcel parcel) {
        Object[] array;
        array = parcel.createStringArray();
        this.title = (String) array[0];
        this.creationYear = (String) array[1];;
        this.genres = (List<String>) array[2];
        this.description = (String) array[3];
        this.movieImageURL = (String) array[4];
        this.URL = (String) array[5];
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeArray(new Object[] { title, creationYear, genres, description,movieImageURL,URL});
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel parcel) {
            return new Movie(parcel);
        }

        @Override
        public Movie[] newArray(int i) {
            return new Movie[i];
        }
    };

        void getSources() {

            /* Серия
                    Источник (имя фансаба)
                            id фильма в vk
                                            ссылки с различным качеством

                <select id=chapterSelectorSelect
                Взять блок option, где имеется атрибут selected="selected". Его значение будет количеством выпущенных серий

             */
            PageDownloader pageDownloader = new PageDownloader();
            Document pageContent;

            try {
                pageContent = pageDownloader.execute(URL).get();
                Element element = pageContent.getElementById("chapterSelectorSelect");
                Elements elements = element.getElementsByAttribute("selected");
                element = elements.last();
                int serialLength = Integer.valueOf(element.text().substring(element.text().indexOf(" ")+1));



            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        }
}
