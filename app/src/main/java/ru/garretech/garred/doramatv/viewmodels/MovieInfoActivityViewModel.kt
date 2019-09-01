package ru.garretech.garred.doramatv.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import ru.garretech.garred.doramatv.database.AppDataSource
import ru.garretech.garred.doramatv.model.History
import ru.garretech.garred.doramatv.model.Movie
import ru.garretech.garred.doramatv.tools.SiteWorker

class MovieInfoActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val dataSource = AppDataSource(application)
    var isFavorite : Boolean = false
    var currentMovie : Movie? = null
    var observable: Subject<Boolean> = PublishSubject.create()
    var disposable: Disposable? = null
    var isRandom : Boolean? = null


    fun addMovie(movie: Movie) =
        Completable.fromCallable {
            dataSource.addMovie(movie)
            dataSource.addHistory(movie)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())


    fun getMovieInfo(url: String) = SiteWorker.getMovieInfo(url)
            .map {
                currentMovie = it
                it
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    fun getMovieFromDatabase(url : String) = dataSource.getMovie(url)
            .map {
                currentMovie = it
                it
            }
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())


    val isInFavorite =
            Single.create<Boolean> {
                it.onSuccess(dataSource.isFavorite(currentMovie?.url!!))
            }.subscribeOn(Schedulers.io())
                    .map {
                        isFavorite = it
                        isFavorite
                    }

    val addFavorites =
            Completable.fromCallable { dataSource.addFavorites(currentMovie!!) }
                    .subscribeOn(Schedulers.io())

    val deleteFavorites =
            Completable.fromCallable { dataSource.deleteFavorites(currentMovie!!) }
                    .subscribeOn(Schedulers.io())


}