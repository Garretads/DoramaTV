package ru.garretech.garred.doramatv.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ru.garretech.garred.doramatv.model.Movie

class MovieInfoViewModel(application: Application) : AndroidViewModel(application) {

    var selectedMovie : Movie? = null



}