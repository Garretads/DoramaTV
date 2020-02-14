package ru.garretech.garred.doramatv.fragments

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.yandex.mobile.ads.*


import org.json.JSONException

import ru.garretech.garred.doramatv.R
import ru.garretech.garred.doramatv.Settings
import ru.garretech.garred.doramatv.model.Movie
import ru.garretech.garred.doramatv.viewmodels.MovieAboutFragmentViewModel
import java.lang.StringBuilder


class MovieAboutFragment : Fragment() {

    private lateinit var viewModel : MovieAboutFragmentViewModel

    private var currentMovie : Movie? = null

    val mAdMobView: AdView by lazy { AdView(context!!) }
    private var mAdRequest: AdRequest? = null

    lateinit var rootView : View
    private val movieTitleTextView : TextView by lazy { rootView.findViewById<TextView>(R.id.movieTitleText) }
    private val movieAgeView : TextView by lazy {  rootView.findViewById<TextView>(R.id.movie_age_text) }
    private val movieGenresView : TextView by lazy { rootView.findViewById<TextView>(R.id.movie_genres_text) }
    private val movieProductionCountryView : TextView by lazy { rootView.findViewById<TextView>(R.id.movie_production_country_text) }
    private val movieSeriesNumberView : TextView by lazy { rootView.findViewById<TextView>(R.id.movie_series_number_text) }
    private val movieDurationView : TextView by lazy { rootView.findViewById<TextView>(R.id.movie_duration_text) }
    private val imageView : ImageView by lazy { rootView.findViewById<ImageView>(R.id.movie_image_info) }
    private val movieDescriptionView : TextView by lazy { rootView.findViewById<TextView>(R.id.movie_description_text) }
    private val pageLayout : LinearLayout by lazy { rootView.findViewById<LinearLayout>(R.id.movieInfoScrollContent) }

    private val progressCircle : ProgressBar by lazy { rootView.findViewById<ProgressBar>(R.id.movieInfoProgressCircle) }



    private val mBannerAdListener = object : AdEventListener {
        override fun onAdFailedToLoad(p0: AdRequestError) {}

        override fun onAdClosed() {}

        override fun onAdLeftApplication() {}

        override fun onAdLoaded() { mAdMobView.visibility = View.VISIBLE }

        override fun onAdOpened() {}
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(MovieAboutFragmentViewModel::class.java)

    }

    private fun initAdMobView() {
        mAdMobView.adSize = AdSize.flexibleSize()

        mAdMobView.blockId = Settings.BLOCK_ID1
        mAdMobView.adEventListener = mBannerAdListener

        mAdRequest = AdRequest.Builder().build()

        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL
        pageLayout.addView(mAdMobView, layoutParams)
    }

    private fun refreshBannerAd() {
        mAdMobView.visibility = View.INVISIBLE
        mAdMobView.loadAd(mAdRequest)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        rootView = inflater.inflate(R.layout.fragment_movie_info, container, false)

        showProgressBar()

        if (savedInstanceState != null) {
            savedInstanceState.getString(URL_MOVIE)?.let {
                viewModel.getMovieFromDatabase(it).subscribe { movie ->
                    startLoading()
                }
            }
        } else {
            if (viewModel.currentMovie == null && currentMovie != null)
                viewModel.currentMovie = currentMovie

            startLoading()
        }

        return rootView
    }


    private fun startLoading() {
        var genresString = StringBuilder()

        for (genre in viewModel.currentMovie?.genres ?: ArrayList())
            genresString.append("$genre, ")

        if (genresString.isNotEmpty())
            movieGenresView.text = getString(R.string.genres_description) + " " + genresString.substring(0,genresString.length - 2)

        movieTitleTextView.text = viewModel.currentMovie?.title
        movieProductionCountryView.text = getString(R.string.production_country_description)  + " " +  viewModel.currentMovie?.productionCountry
        movieSeriesNumberView.text = viewModel.currentMovie?.seriesNumber
        movieDurationView.text = viewModel.currentMovie?.duration
        movieAgeView.text = getString(R.string.age_description)  + " " +  viewModel.currentMovie?.productionYear
        movieDescriptionView.text = viewModel.currentMovie?.description


        if (viewModel.currentMovie?.movieImageURL != null) {
            Glide
                    .with(context!!)
                    .load(viewModel.currentMovie?.movieImageURL!!)
                    .fitCenter()
                    //.placeholder(R.drawable.loading_spinner)
                    .into(imageView)
        }

        initAdMobView()

        dismissProgressBar()
    }

    private fun showProgressBar() {
        progressCircle?.visibility = View.VISIBLE
    }

    private fun dismissProgressBar() {
        progressCircle?.visibility = View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshBannerAd()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(URL_MOVIE,viewModel.currentMovie?.url)
    }


    override fun onDestroy() {
        mAdMobView.destroy()
        super.onDestroy()
    }


    companion object {

        const val URL_MOVIE = "movie_url"

        @Throws(JSONException::class)
        fun newInstance(movie : Movie): MovieAboutFragment {
            return MovieAboutFragment().also {
                it.currentMovie = movie
            }
        }
    }
}


