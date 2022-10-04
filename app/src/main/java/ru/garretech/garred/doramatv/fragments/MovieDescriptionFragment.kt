package ru.garretech.garred.doramatv.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import ru.garretech.garred.doramatv.R
import ru.garretech.garred.doramatv.model.Movie


class MovieDescriptionFragment : Fragment() {
    lateinit var movie : Movie


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_movie_description, container, false)
    }


    companion object {

        @JvmStatic
        fun newInstance(movie: Movie) =
                MovieDescriptionFragment().apply {
                  this.movie = movie
                }
    }
}
