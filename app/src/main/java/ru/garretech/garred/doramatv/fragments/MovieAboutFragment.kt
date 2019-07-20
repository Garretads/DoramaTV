package ru.garretech.garred.doramatv.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.yandex.mobile.ads.*


import org.json.JSONException

import ru.garretech.garred.doramatv.R
import ru.garretech.garred.doramatv.Settings
import ru.garretech.garred.doramatv.model.Movie
import ru.garretech.garred.doramatv.viewmodels.MovieInfoViewModel


class MovieAboutFragment : androidx.fragment.app.Fragment() {

    private lateinit var viewModel : MovieInfoViewModel

    private lateinit var pageLayout : LinearLayout

    private var selectedMovie : Movie? = null

    private var image: Bitmap? = null

    val mAdMobView: AdView by lazy { AdView(context!!) }
    private var mAdRequest: AdRequest? = null


    private val mBannerAdListener = object : AdEventListener {
        override fun onAdFailedToLoad(p0: AdRequestError) {}

        override fun onAdClosed() {}

        override fun onAdLeftApplication() {}

        override fun onAdLoaded() { mAdMobView.visibility = View.VISIBLE }

        override fun onAdOpened() {}
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(MovieInfoViewModel::class.java)

        if (viewModel.selectedMovie == null)
            viewModel.selectedMovie = selectedMovie
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

        val view = inflater.inflate(R.layout.fragment_movie_info, container, false)
        val movieTitleTextView = view.findViewById<TextView>(R.id.movieTitleText)
        val movieAgeView = view.findViewById<TextView>(R.id.movie_age_text)
        val movieGenresView = view.findViewById<TextView>(R.id.movie_genres_text)
        val movieProductionCountryView = view.findViewById<TextView>(R.id.movie_production_country_text)
        val movieSeriesNumberView = view.findViewById<TextView>(R.id.movie_series_number_text)
        val movieDurationView = view.findViewById<TextView>(R.id.movie_duration_text)
        val imageView = view.findViewById<ImageView>(R.id.movie_image_info)
        val movieDescriptionView = view.findViewById<TextView>(R.id.movie_description_text)
        pageLayout = view.findViewById(R.id.movieInfoScrollContent)

        movieGenresView.text = getString(R.string.genres_description) + " " + viewModel.selectedMovie?.genres?.toString()?.substring(1,viewModel.selectedMovie?.genres?.toString()?.length!!-1)
        movieTitleTextView.text = viewModel.selectedMovie?.title
        movieProductionCountryView.text = getString(R.string.production_country_description)  + " " +  viewModel.selectedMovie?.productionCountry
        movieSeriesNumberView.text = viewModel.selectedMovie?.seriesNumber
        movieDurationView.text = viewModel.selectedMovie?.duration
        movieAgeView.text = getString(R.string.age_description)  + " " +  viewModel.selectedMovie?.productionYear
        movieDescriptionView.text = viewModel.selectedMovie?.description
        //imageView.setImageBitmap(image)


        if (viewModel.selectedMovie?.movieImageURL != null && context != null) {
            Glide
                    .with(context!!)
                    .load(viewModel.selectedMovie?.movieImageURL!!)
                    .fitCenter()
                    //.placeholder(R.drawable.loading_spinner)
                    .into(imageView)
        }

        //initAdMobView()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //refreshBannerAd()
    }


    override fun onDestroy() {
        //mAdMobView.destroy()
        super.onDestroy()
    }


    companion object {

        @Throws(JSONException::class)
        fun newInstance(movie : Movie): MovieAboutFragment {
            return MovieAboutFragment().also {
                it.selectedMovie = movie
            }
        }
    }
}


