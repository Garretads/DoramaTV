package ru.garretech.garred.doramatv.flow.movieInfo

import com.google.android.material.tabs.TabLayout
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import io.reactivex.Single

import io.reactivex.android.schedulers.AndroidSchedulers
import ru.garretech.garred.doramatv.R
import ru.garretech.garred.doramatv.flow.movieInfo.adapter.MovieAboutPagerAdapter

import kotlinx.android.synthetic.main.activity_movie_info.*
import ru.garretech.garred.doramatv.tools.DisposableManager
import ru.garretech.garred.doramatv.Settings
import ru.garretech.garred.doramatv.data.model.Movie
import ru.garretech.garred.doramatv.tools.SiteWorker
import ru.garretech.garred.doramatv.flow.movieInfo.viewModel.MovieInfoActivityViewModel

class MovieInfoActivity : AppCompatActivity() {



    private lateinit var mFragmentAdapter: MovieAboutPagerAdapter
    lateinit var viewModel: MovieInfoActivityViewModel
    private lateinit var optionsMenu: Menu


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_info)
        viewModel = ViewModelProviders.of(this).get(MovieInfoActivityViewModel::class.java)
        showProgressCircle()

        if (viewModel.isRandom == null)
            viewModel.isRandom = intent.getBooleanExtra("is_random",true)

        val url = if (viewModel.currentMovie == null) {
                    if (viewModel.isRandom!!) {
                        Settings.SITE_URL + SiteWorker.RANDOM_MOVIE_PREFIX
                    }
                    else {
                        intent.getStringExtra("movie_url") ?: ""
                    }
                } else {
                    viewModel.currentMovie?.url!!
                }

        DisposableManager.add(viewModel
                .getMovieFromDatabase(url)
                .flatMap {
                    if(it.description == null)
                        viewModel.getMovieInfo(url)
                    else
                        Single.just(it)
                }
                .subscribe( { movie ->
                    prepareMovie(movie)
                },{
                    viewModel.getMovieInfo(url)
                    .subscribe( { movie ->
                        viewModel.addMovie(movie).subscribe( {
                        prepareMovie(movie)
                        },{
                            Log.e("MovieInfoActivity","Ошибка сохранения манги в БД", it)
                            dismissProgressCircle()
                        })
                    },{
                        Log.e("MOVIE INFO OBSERVER","Ошибка получения информации о фильме", it)
                    })
                })
        )

    }



    private fun setupViewPager(viewPager: androidx.viewpager.widget.ViewPager) {
        mFragmentAdapter = MovieAboutPagerAdapter(supportFragmentManager)

        if (viewModel.currentMovie != null) {

            mFragmentAdapter.addFragment(MovieAboutFragment.newInstance(viewModel.currentMovie!!), "О фильме")
            //mFragmentAdapter.addFragment(MovieDescriptionFragment.newInstance(currentMovie),"Подробнее")
            mFragmentAdapter.addFragment(MovieSourcesFragment.newInstance(viewModel.currentMovie!!), "Источники")
            viewPager.adapter = mFragmentAdapter
            setSupportActionBar(toolbar)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = ""
            var sourcesNotLoaded = true

            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (tab?.position == 1 && sourcesNotLoaded) {
                        sourcesNotLoaded = false
                        val fragment = mFragmentAdapter.getItem(1) as MovieSourcesFragment
                        fragment.startLoading()
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }
            })

        } else
            Toast.makeText(this,"Ошибка получения информации о фильме, повторите попытку еще раз", Toast.LENGTH_SHORT).show()

    }

    private fun flagFavorite(flag: Boolean) {
        val item = optionsMenu.getItem(0)
        if (flag)
            item.setIcon(R.drawable.ic_favorite)
        else
            item.setIcon(R.drawable.ic_favorite_border)
    }

    private fun showProgressCircle() {
        infoProgressCircle.visibility = View.VISIBLE
    }

    private fun dismissProgressCircle() {
        infoProgressCircle.visibility = View.GONE
    }

    private fun prepareMovie(movie: Movie) {

        setupViewPager(viewPager)
        tabLayout.setupWithViewPager(viewPager)

        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewPager))
        dismissProgressCircle()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_movie_about, menu)
        optionsMenu = menu
        optionsMenu.getItem(1).isVisible = false

        DisposableManager.add(viewModel.observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { t -> flagFavorite(t) })

        DisposableManager.add(viewModel.isInFavorite.subscribe(
                { isInFavorite ->
                    emmitFavorite(isInFavorite)
                },{
                    Log.e("MovieInfoActivity","Ошибка проверки манги в избранном",it)
                }))

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_favorite -> {
                if (viewModel.isFavorite) {
                    viewModel.deleteFavorites
                            .subscribe( {
                                emmitFavorite(false)
                                Log.d("MovieInfoActivity","Удаление из избранного успешно")
                            },{
                                Log.e("MovieInfoActivity","Удаление из избранного завершилось с ошибкой",it)
                            })
                } else {
                    viewModel.addFavorites
                            .subscribe( {
                                emmitFavorite(true)
                                Log.d("MovieInfoActivity","Добавление в избранное успешно")
                            },{
                                Log.e("MovieInfoActivity","Добавление в избранное завершилось ошибкой",it)
                            })
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onDestroy() {
        super.onDestroy()
        DisposableManager.dispose()
    }

    internal fun emmitFavorite(value: Boolean) {
        viewModel.isFavorite = value
        viewModel.observable.onNext(value)
    }

}

