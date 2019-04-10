package ru.garretech.garred.doramatv.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "favorites",
        indices = {@Index("movie_url")},
        foreignKeys = @ForeignKey(entity = Movie.class,
                                    parentColumns = "URL",
                                    childColumns = "movie_url"))
public class Favorites {

    // Хранить ссылки на фильмы. Только и всего
    /*
    * Получить все ссылки (Полный список избранного. Или по частям, если с пагинацией)
    * Занести в избранное
    *
    * */

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMovieURL() {
        return movieURL;
    }

    public void setMovieURL(String movieURL) {
        this.movieURL = movieURL;
    }

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "movie_url") @NonNull
    private String movieURL;
}
