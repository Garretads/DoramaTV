package ru.garretech.garred.doramatv.fragments

import android.arch.persistence.room.PrimaryKey
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.yandex.mobile.ads.*


import org.json.JSONException
import org.json.JSONObject

import java.io.FileNotFoundException
import java.io.IOException
import java.util.concurrent.ExecutionException

import ru.garretech.garred.doramatv.tools.ImageDownloader
import ru.garretech.garred.doramatv.R
import ru.garretech.garred.doramatv.Settings
import ru.garretech.garred.doramatv.tools.SiteWorker


class MovieAboutFragment : Fragment() {

    private lateinit var pageLayout : LinearLayout

    private val movieAge: String by lazy { arguments!!.getString(ARG_PARAM6)}
    private val movieTitle: String by lazy { arguments!!.getString(ARG_PARAM1) }
    private val movieGenres: String by lazy { arguments!!.getString(ARG_PARAM2) }
    private val movieProduction: String by lazy { arguments!!.getString(ARG_PARAM3) }
    private val movieSeriesNumber: String by lazy { arguments!!.getString(ARG_PARAM4) }
    private val movieDuration: String by lazy { arguments!!.getString(ARG_PARAM5) }
    private val movieDescription: String by lazy { arguments!!.getString(ARG_PARAM7) }
    private val movieImageURL: String by lazy { arguments!!.getString(ARG_PARAM8) }
    private val movieURL: String by lazy { arguments!!.getString(ARG_PARAM9) }
    private var image: Bitmap? = null

    val mAdMobView: AdView by lazy { AdView(context!!) }
    private var mAdRequest: AdRequest? = null


    private val mBannerAdListener = object : AdEventListener {
        override fun onAdFailedToLoad(p0: AdRequestError) {

        }

        override fun onAdClosed() {

        }


        override fun onAdLeftApplication() {

        }

        override fun onAdLoaded() {
            mAdMobView.visibility = View.VISIBLE
        }

        override fun onAdOpened() {

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            /*movieTitle = arguments!!.getString(ARG_PARAM1)
            movieGenres = arguments!!.getString(ARG_PARAM2)
            movieProduction = arguments!!.getString(ARG_PARAM3)
            movieSeriesNumber = arguments!!.getString(ARG_PARAM4)
            movieDuration = arguments!!.getString(ARG_PARAM5)
            //movieAge = arguments!!.getString(ARG_PARAM6)
            movieDescription = arguments!!.getString(ARG_PARAM7)
            movieImageURL = arguments!!.getString(ARG_PARAM8)
            movieURL = arguments!!.getString(ARG_PARAM9)*/

            try {
                image = SiteWorker.getCachedImage(context!!, movieImageURL)
            } catch (e: FileNotFoundException) {
                val imageDownloader = ImageDownloader()
                try {
                    image = imageDownloader.execute(movieImageURL).get()
                } catch (e1: ExecutionException) {
                    e1.printStackTrace()
                } catch (e1: InterruptedException) {
                    e1.printStackTrace()
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
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

        val view = inflater.inflate(R.layout.fragment_movie_about, container, false)
        val movieAgeView = view.findViewById<TextView>(R.id.movie_age_text)
        val movieGenresView = view.findViewById<TextView>(R.id.movie_genres_text)
        val movieProductionCountryView = view.findViewById<TextView>(R.id.movie_production_country_text)
        val movieSeriesNumberView = view.findViewById<TextView>(R.id.movie_series_number_text)
        val movieDurationView = view.findViewById<TextView>(R.id.movie_duration_text)
        val imageView = view.findViewById<ImageView>(R.id.movie_image_about)
        val movieDescriptionView = view.findViewById<TextView>(R.id.movie_description_text)
        pageLayout = view.findViewById(R.id.movieAboutScrollContent)

        movieGenresView.text = getString(R.string.genres_description) + " " + movieGenres
        movieProductionCountryView.text = getString(R.string.production_country_description)  + " " +  movieProduction
        movieSeriesNumberView.text = movieSeriesNumber
        movieDurationView.text = movieDuration
        movieAgeView.text = getString(R.string.age_description)  + " " +  movieAge!!
        movieDescriptionView.text = movieDescription
        imageView.setImageBitmap(image)

        initAdMobView()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshBannerAd()
    }



    override fun onDetach() {
        super.onDetach()
    }

    override fun onDestroy() {
        mAdMobView.destroy()
        super.onDestroy()
    }



    companion object {


        private val ARG_PARAM1 = "title"
        private val ARG_PARAM2 = "genres"
        private val ARG_PARAM3 = "production"
        private val ARG_PARAM4 = "series_number"
        private val ARG_PARAM5 = "duration"
        private val ARG_PARAM6 = "age"
        private val ARG_PARAM7 = "description"
        private val ARG_PARAM8 = "imageURL"
        private val ARG_PARAM9 = "url"


        private val AGE = "Год: "
        private val GENRES = "Жанры: "
        private val PRODUCTION_COUNTRY = "Производство: "


        // TODO: Rename and change types and number of parameters
        @Throws(JSONException::class)
        fun newInstance(movieInfo: JSONObject): MovieAboutFragment {
            val fragment = MovieAboutFragment()
            val args = Bundle()

            args.putString(ARG_PARAM1, movieInfo.getString("title"))
            args.putString(ARG_PARAM2, movieInfo.getString("genres"))
            args.putString(ARG_PARAM3, movieInfo.getString("production"))
            args.putString(ARG_PARAM4, movieInfo.getString("series_number"))
            args.putString(ARG_PARAM5, movieInfo.getString("duration"))
            args.putString(ARG_PARAM6, movieInfo.getString("age"))
            args.putString(ARG_PARAM7, movieInfo.getString("description"))
            args.putString(ARG_PARAM8, movieInfo.getString("image_url"))
            args.putString(ARG_PARAM9, movieInfo.getString("url"))

            fragment.arguments = args

            return fragment
        }
    }
}


