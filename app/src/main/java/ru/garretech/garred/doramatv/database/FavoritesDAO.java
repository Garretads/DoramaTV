package ru.garretech.garred.doramatv.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import ru.garretech.garred.doramatv.model.Favorites;

@Dao
public interface FavoritesDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long addFavorites(Favorites favorites);

    @Query("SELECT * FROM favorites")
    List<Favorites> getAllFavorites();

    @Query("SELECT * FROM favorites WHERE id = :ids")
    Favorites getFavoriteByIndex(long ids);

    @Query("SELECT * FROM favorites WHERE movie_url = :URL")
    Favorites getFavoriteByURL(String URL);

    @Delete
    void deleteFavorites(Favorites favorites);
}
