package ru.garretech.garred.doramatv.flow.movieInfo.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.garretech.garred.doramatv.data.database.AppDataSource
import ru.garretech.garred.doramatv.data.model.Movie

class MovieAboutFragmentViewModel(application: Application) : AndroidViewModel(application) {

    var currentMovie : Movie? = null
    var dataSource = AppDataSource(application)


    fun getMovieFromDatabase(url : String) = dataSource.getMovie(url)
            .map {
                currentMovie = it
                it
            }
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())


}