package ru.garretech.garred.doramatv.flow.movieInfo

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.garretech.garred.doramatv.tools.DisposableManager
import java.util.ArrayList
import ru.garretech.garred.doramatv.R
import ru.garretech.garred.doramatv.flow.movieInfo.adapter.MovieSourceAdapter
import ru.garretech.garred.doramatv.data.model.Movie
import ru.garretech.garred.doramatv.flow.movieInfo.viewModel.MovieSourcesFragmentViewModel


class MovieSourcesFragment : Fragment() {

    private lateinit var arrayAdapter: ArrayAdapter<String>
    lateinit var viewModel : MovieSourcesFragmentViewModel

    private var sourcesProgressCircle : ProgressBar? = null

    private lateinit var seriesRecyclerView : RecyclerView
    private lateinit var seriesAdapter : MovieSourceAdapter

    private var currentMovie : Movie? = null
    private var empty: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MovieSourcesFragmentViewModel::class.java)

        arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, ArrayList())
        arrayAdapter.setNotifyOnChange(true)

        seriesAdapter = MovieSourceAdapter(this, ArrayList())
        seriesAdapter.setHasStableIds(true)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_movie_sources, container, false)
        seriesRecyclerView = view.findViewById(R.id.sourcesRecyclerView)
        sourcesProgressCircle = view.findViewById(R.id.sourcesProgressCircle)

        seriesRecyclerView.adapter = seriesAdapter
        seriesRecyclerView.layoutManager = LinearLayoutManager(context)

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
        if (viewModel.seriesList.isEmpty()) {

            DisposableManager.add(
                    viewModel.getSeriesList(viewModel.currentMovie?.url!!, viewModel.currentMovie?.initialSeries!!)
                    .subscribe({ seriesList ->

                        val listViewList = ArrayList<String>()

                        if (seriesList.isEmpty()) {
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

            if (viewModel.seriesList.isEmpty()) {
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
        val cm = requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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
