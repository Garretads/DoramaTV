package ru.garretech.garred.doramatv.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import ru.garretech.garred.doramatv.model.Movie;

@Dao
public interface MovieDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long addMovie(Movie movie);


    @Query("SELECT * FROM movie")
    List<Movie> getAllCachedMovies();

    @Query("SELECT * FROM movie WHERE URL = :URL")
    Movie getMovie(String URL);

}
