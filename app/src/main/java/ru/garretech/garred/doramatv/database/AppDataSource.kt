package ru.garretech.garred.doramatv.database

import android.content.Context
import android.graphics.Bitmap
import android.util.Log

import java.util.ArrayList

import io.reactivex.Observable
import io.reactivex.Single
import ru.garretech.garred.doramatv.model.Favorites
import ru.garretech.garred.doramatv.model.Movie
import ru.garretech.garred.doramatv.tools.SiteWorker

class AppDataSource(context: Context) {
    private val appDatabase : AppDatabase by lazy { AppDatabase.getInstance(context)!! }
    private val movieDAO: MovieDAO by lazy {
        appDatabase?.movieDAO()
    }
    private val favoritesDAO: FavoritesDAO by lazy {
        appDatabase?.favoritesDAO()
    }

    val allMovies: List<Movie>
        get() = movieDAO.allCachedMovies

    val listOfFavorites: Observable<List<Movie>>
        get() {
            val list = ArrayList<Movie>()

            val favorites = favoritesDAO.allFavorites

            for (favorite in favorites) {
                val movie = movieDAO.getMovie(favorite.movieURL)
                list.add(movie)
            }
            return Observable.fromArray(list)
        }

    init {

    }

    fun addMovie(movie: Movie) {
        movieDAO.addMovie(movie)
    }

    fun getMovie(URL: String): Movie {
        return movieDAO.getMovie(URL)
    }

    fun isFavorite(URL: String): Boolean {
        val favorites = favoritesDAO.getFavoriteByURL(URL)
        return favorites != null
    }

    fun addFavorites(movie: Movie) {
        movieDAO.addMovie(movie)
        Log.d("Database", "Movie " + movie.url + " added")
        val favorite = Favorites()
        favorite.movieURL = movie.url
        favoritesDAO.addFavorites(favorite)
    }


    fun deleteFavorites(movie: Movie) {
        val favorites = favoritesDAO.getFavoriteByURL(movie.url)
        favoritesDAO.deleteFavorites(favorites)
    }

}
