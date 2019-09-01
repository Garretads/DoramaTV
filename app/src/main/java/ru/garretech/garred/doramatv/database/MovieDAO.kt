package ru.garretech.garred.doramatv.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import ru.garretech.garred.doramatv.model.Movie

@Dao
interface MovieDAO {


    @get:Query("SELECT * FROM movie")
    val allCachedMovies: List<Movie>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addMovie(movie: Movie): Long

    @Query("SELECT * FROM movie WHERE URL = :URL")
    fun getMovie(URL: String): Movie?

}
