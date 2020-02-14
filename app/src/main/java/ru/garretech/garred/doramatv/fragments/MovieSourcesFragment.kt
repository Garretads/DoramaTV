package ru.garretech.garred.doramatv.fragments

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.nodes.Element
import ru.garretech.garred.doramatv.DisposableManager

import java.util.ArrayList
import java.util.concurrent.ExecutionException

import ru.garretech.garred.doramatv.R
import ru.garretech.garred.doramatv.Settings
import ru.garretech.garred.doramatv.activities.WebViewActivity
import ru.garretech.garred.doramatv.adapters.MovieSourceAdapter
import ru.garretech.garred.doramatv.model.Movie
import ru.garretech.garred.doramatv.tools.*
import ru.garretech.garred.doramatv.viewmodels.MovieSourcesFragmentViewModel
import java.io.IOException
import java.util.regex.Pattern


class MovieSourcesFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private var arrayAdapter: ArrayAdapter<String>? = null
    lateinit var viewModel : MovieSourcesFragmentViewModel

    private var sourcesProgressCircle : ProgressBar? = null

    private lateinit var seriesRecyclerView : RecyclerView
    private lateinit var seriesAdapter : MovieSourceAdapter

    private var currentMovie : Movie? = null
    private var empty: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MovieSourcesFragmentViewModel::class.java)

        arrayAdapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, ArrayList())
        arrayAdapter!!.setNotifyOnChange(true)

        seriesAdapter = MovieSourceAdapter(this, ArrayList())
        seriesAdapter.setHasStableIds(true)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_movie_sources, container, false)
        seriesRecyclerView = view.findViewById(R.id.sourcesRecyclerView)
        sourcesProgressCircle = view.findViewById(R.id.sourcesProgressCircle)

        seriesRecyclerView.adapter = seriesAdapter
        seriesRecyclerView.layoutManager = LinearLayoutManager(context)

        /*
        * Если delayedStart установлен, то отменить
        *
        *
        * */

        if (savedInstanceState != null) {
            savedInstanceState.getString(URL_MOVIE)?.let {
                viewModel.getMovieFromDatabase(it).subscribe { movie ->
                    currentMovie = movie
                    startLoading()
                }
            }
        } else {
            if (viewModel.currentMovie == null && currentMovie != null)
                viewModel.currentMovie = currentMovie

            startLoading()
        }

        return view
    }

    fun startLoading() {

        if (seriesAdapter.data.isEmpty()) {
            showProgressBar()

            viewModel.getHistory().subscribe({
                loadSeriesList()
            }, {
                Log.e("MovieSourcesFragment", "Ошибка при загрузке истории", it)
            })
        }
    }

    private fun loadSeriesList() {
        if (viewModel.seriesList.size == 0) {

            DisposableManager.add(
                    viewModel.getSeriesList(viewModel.currentMovie?.url!!, viewModel.currentMovie?.initialSeries!!)
                    .subscribe({ seriesList ->

                        val listViewList = ArrayList<String>()

                        if (seriesList.size == 0) {
                            listViewList.add("Пусто")
                            empty = true
                        } else {
                            seriesAdapter.addData(seriesList)
                        }

                        dismissProgressBar()

                    }, {
                        Log.e("SERIES LIST", "ERROR LOADING SERIES LIST", it)
                    }))
        } else {
            val listViewList = ArrayList<String>()

            if (viewModel.seriesList.size == 0) {
                listViewList.add("Пусто")
                empty = true
            } else {
                seriesAdapter.addData(viewModel.seriesList)
            }

            dismissProgressBar()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(URL_MOVIE,viewModel.currentMovie?.url)
    }

    private fun showProgressBar() {
        sourcesProgressCircle?.visibility = View.VISIBLE
    }

    private fun dismissProgressBar() {
        sourcesProgressCircle?.visibility = View.GONE
    }

    internal fun showConnectionError() {
        dismissProgressBar()
        Toast.makeText(context, getText(R.string.cant_connect_error), Toast.LENGTH_SHORT).show()
    }

    internal fun hasConnection(): Boolean {
        val cm = activity!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val ni = cm.activeNetworkInfo
        return ni != null && ni.isConnected
    }

    companion object {

        const val URL_MOVIE = "movie_url"

        fun newInstance(movie : Movie) = MovieSourcesFragment().also {
            it.currentMovie = movie
        }

    }
}
