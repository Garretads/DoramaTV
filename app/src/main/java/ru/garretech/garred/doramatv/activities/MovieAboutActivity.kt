package ru.garretech.garred.doramatv.activities

import android.content.Intent
import android.net.Uri
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.support.v4.view.ViewPager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem

import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import ru.garretech.garred.doramatv.R
import ru.garretech.garred.doramatv.adapters.MovieAboutPagerAdapter
import ru.garretech.garred.doramatv.database.AppDataSource
import ru.garretech.garred.doramatv.fragments.MovieAboutFragment
import ru.garretech.garred.doramatv.fragments.MovieSourcesFragment

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.util.Arrays

import kotlinx.android.synthetic.main.activity_movie_about.*
import ru.garretech.garred.doramatv.Settings
import ru.garretech.garred.doramatv.model.Movie

class MovieAboutActivity : AppCompatActivity(), MovieAboutFragment.OnFragmentInteractionListener, MovieSourcesFragment.OnFragmentInteractionListener {



    internal lateinit var mFragmentAdapter: MovieAboutPagerAdapter
    internal lateinit var movieInfo: JSONObject
    internal lateinit var title: String
    internal lateinit var age: String
    internal lateinit var genres: String
    internal lateinit var production: String
    internal lateinit var seriesNumber: String
    internal lateinit var duration: String
    internal lateinit var description: String
    internal lateinit var imageURL: String
    internal lateinit var movieURL: String
    internal lateinit var initialSeries: String
    internal lateinit var currentMovie: Movie
    internal lateinit var dataSource: AppDataSource
    internal var isFavorite: Boolean = false
    internal var observable: Subject<Boolean> = PublishSubject.create()
    internal var disposable: Disposable? = null
    internal lateinit var optionsMenu: Menu


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_about)
        val intent = intent
        dataSource = AppDataSource(applicationContext)
        try {
            var movieInfoString : String? = intent.getStringExtra("movie_info")

            if (movieInfoString == null)
                throw NullPointerException()

            movieInfo = JSONObject(movieInfoString!!)


            /*
            * Запросить наличие фильма в избранных (completable)
            * Полученный результат хранится в переменной isFavorite, которая является observable
            * При изменении значения данной переменной подписчик выполняет свои действия (меняется иконку избранного)
            *
            * Занесение фильма в избранное.
            * Опять completable. С помощью него фильм заносится в БД.
            * */


            this.title = movieInfo.getString("title")
            this.genres = movieInfo.getString("genres")
            this.imageURL = movieInfo.getString("image_url")
            this.movieURL = movieInfo.getString("url")
            this.age = movieInfo.getString("age")
            this.description = movieInfo.getString("description")
            this.initialSeries = movieInfo.getString("initial_series")
            this.production = movieInfo.getString("production")
            this.seriesNumber = movieInfo.getString("series_number")
            this.duration = movieInfo.getString("duration")

            val bundle = intent.getBundleExtra("bundle")
            try {
                currentMovie = bundle.getSerializable("movie") as Movie
            } catch (e: NullPointerException) {
                currentMovie = Movie(title, Arrays.asList(*genres.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()), imageURL, movieURL)
                currentMovie.productionYear = age
                currentMovie.description = description
                currentMovie.initialSeries = initialSeries
                currentMovie.productionCountry = production
                currentMovie.seriesNumber = seriesNumber
                currentMovie.duration = duration
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            finish()
        }

        setupViewPager(viewPager)
        tabLayout.setupWithViewPager(viewPager)

        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewPager))


    }


    private fun setupViewPager(viewPager: ViewPager) {
        mFragmentAdapter = MovieAboutPagerAdapter(supportFragmentManager)

        try {
            val sourcesInfo = JSONObject()
            sourcesInfo.put("url", movieURL)
            sourcesInfo.put("initial_series", initialSeries)
            mFragmentAdapter.addFragment(MovieAboutFragment.newInstance(movieInfo), "О фильме")
            mFragmentAdapter.addFragment(MovieSourcesFragment.newInstance(sourcesInfo), "Источники")
            viewPager.adapter = mFragmentAdapter
            setSupportActionBar(toolbar)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = title
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    private fun flagFavorite(flag: Boolean) {
        val item = optionsMenu.getItem(0)
        if (flag)
            item.setIcon(R.drawable.ic_favorite_white_24dp)
        else
            item.setIcon(R.drawable.ic_favorite_border_white_24dp)

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_movie_about, menu)
        optionsMenu = menu


        disposable = observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Consumer<Boolean> {
                    override fun accept(t: Boolean) {
                        flagFavorite(t)
                    }
                })

        Completable.fromCallable {
            isFavorite = dataSource.isFavorite(currentMovie.url)
            emmitFavorite(isFavorite)
            null
        }.subscribeOn(Schedulers.io())
                .subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onComplete() {
                        Log.d("Task", "Subscribe to favorite changes completed")
                    }

                    override fun onError(e: Throwable) {

                    }
                })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_favorite -> {
                if (isFavorite) {
                    Completable.fromCallable {
                        dataSource.deleteFavorites(currentMovie)
                        null
                    }.subscribeOn(Schedulers.io())
                            .subscribe(object : CompletableObserver {
                                override fun onSubscribe(d: Disposable) {

                                }

                                override fun onComplete() {
                                    emmitFavorite(false)
                                    Log.d("Task", "Delete completable completed")
                                }

                                override fun onError(e: Throwable) {
                                    Log.d("Task", "Delete completable error")
                                }
                            })
                } else {
                    Completable.fromCallable {
                        dataSource.addFavorites(currentMovie)
                        null
                    }.subscribeOn(Schedulers.io())
                            .subscribe(object : CompletableObserver {
                                override fun onSubscribe(d: Disposable) {

                                }

                                override fun onComplete() {
                                    emmitFavorite(true)
                                    Log.d("Task", "Add completable completed")
                                }

                                override fun onError(e: Throwable) {
                                    Log.d("Task", "Add completable error")
                                }
                            })
                }
            }
            R.id.action_settings -> {
                val intent = Intent(this@MovieAboutActivity, SettingsActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onFragmentInteraction(uri: Uri) {

    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    internal fun emmitFavorite(value: Boolean) {
        isFavorite = value
        observable.onNext(isFavorite)
    }

}

