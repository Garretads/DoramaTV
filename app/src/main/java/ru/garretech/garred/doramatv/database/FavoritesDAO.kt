package ru.garretech.garred.doramatv.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

import ru.garretech.garred.doramatv.model.Favorites

@Dao
interface FavoritesDAO {

    @get:Query("SELECT * FROM favorites")
    val allFavorites: List<Favorites>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addFavorites(favorites: Favorites): Long

    @Query("SELECT * FROM favorites WHERE id = :ids")
    fun getFavoriteByIndex(ids: Long): Favorites

    @Query("SELECT * FROM favorites WHERE movie_url = :URL")
    fun getFavoriteByURL(URL: String): Favorites

    @Delete
    fun deleteFavorites(favorites: Favorites)
}
