package ru.garretech.garred.doramatv.database

import android.content.Context
import android.util.Log
import io.reactivex.Completable

import io.reactivex.Observable
import io.reactivex.Single
import ru.garretech.garred.doramatv.model.Favorites
import ru.garretech.garred.doramatv.model.History
import ru.garretech.garred.doramatv.model.Movie
import kotlin.collections.ArrayList

class AppDataSource(context: Context) {
    private val appDatabase : AppDatabase by lazy { AppDatabase.getInstance(context)!! }
    private val movieDAO: MovieDAO by lazy {
        appDatabase.movieDAO()
    }
    private val favoritesDAO: FavoritesDAO by lazy {
        appDatabase.favoritesDAO()
    }
    private val historyDAO: HistoryDAO by lazy {
        appDatabase.historyDAO()
    }

    val allMovies: List<Movie>
        get() = movieDAO.allCachedMovies

    val listOfFavorites: Observable<List<Movie>>
        get() {
            val list = ArrayList<Movie>()

            val favorites = favoritesDAO.allFavorites

            for (favorite in favorites) {
                val movie = movieDAO.getMovie(favorite.movieURL)
                list.add(movie!!)
            }
            return Observable.fromArray(list)
        }

    val listOfHistoryObservable : Observable<List<Movie>>
        get() {
            val historyList = historyDAO.allHistory
            val movieList = ArrayList<Movie>()

            for (history in historyList) {
                val movie = movieDAO.getMovie(history.movieURL)
                if (movie != null)
                    movieList.add(movie)
            }
            return Observable.fromArray(movieList)
        }

    val listOfHistorySingle =
            Single.create<List<History>> {
                val list = historyDAO.allHistory

                it.onSuccess(list)
            }


    fun addMovie(movie: Movie) {
        movieDAO.addMovie(movie)
    }

    fun getMovie(URL: String) =
            Single.create<Movie> {
                val movie = movieDAO.getMovie(URL)

                if (movie != null)
                    it.onSuccess(movie)
                else
                    it.onError(NullPointerException())
            }

    fun isFavorite(URL: String): Boolean {
        val favorites = favoritesDAO.getFavoriteByURL(URL)
        return favorites != null
    }

    fun addFavorites(movie: Movie) {
        movieDAO.addMovie(movie)
        Log.d("Database", "Movie " + movie.url + " added")
        val favorite = Favorites(movie.url)
        favoritesDAO.addFavorites(favorite)
    }


    fun deleteFavorites(movie: Movie) {
        val favorites = favoritesDAO.getFavoriteByURL(movie.url)
        if (favorites != null)
            favoritesDAO.deleteFavorites(favorites)
    }

    fun getHistory(movie: Movie) =
        Single.create<History> {
            val history = historyDAO.getHistoryByURL(movie.url)

            if (history == null)
                it.onSuccess(History((movie.url)))
            else
                it.onSuccess(history)

        }

    fun addHistory(history: History) =
            Completable.fromCallable {
                historyDAO.addHistory(history)
            }

    fun addHistory(movie: Movie)  {
                var history = historyDAO.getHistoryByURL(movie.url)

                if (history == null) {
                    history = History(movie.url).also { it.series = HashMap() }
                    historyDAO.addHistory(history)
                }

            }


    fun updateHistory(history: History) =
            Completable.fromCallable {
                historyDAO.updateHistory(history)
            }


    fun clearHistory() =
            Completable.fromCallable {
                historyDAO.clearHistory()
            }

    fun clearFavorites() =
            Completable.fromCallable {
                favoritesDAO.clearFavorites()
            }
}
