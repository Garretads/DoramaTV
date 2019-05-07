package ru.garretech.garred.doramatv.activities

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast

import com.chad.library.adapter.base.BaseQuickAdapter
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.yandex.mobile.ads.*
import io.fabric.sdk.android.Fabric

import org.json.JSONException
import org.json.JSONObject


import java.io.FileNotFoundException
import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.ExecutionException
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import ru.garretech.garred.doramatv.BuildConfig
import ru.garretech.garred.doramatv.R
import ru.garretech.garred.doramatv.Settings
import ru.garretech.garred.doramatv.adapters.RecyclerAdapter
import ru.garretech.garred.doramatv.database.AppDataSource
import ru.garretech.garred.doramatv.fragments.CustomLoadMoreView
import ru.garretech.garred.doramatv.fragments.ProgressBottomSheet
import ru.garretech.garred.doramatv.model.Movie
import ru.garretech.garred.doramatv.tools.ImageDownloader
import ru.garretech.garred.doramatv.tools.SiteWorker
import java.lang.Exception

class MainActivity : AppCompatActivity(), BaseQuickAdapter.OnItemClickListener, MenuItem.OnActionExpandListener, NavigationView.OnNavigationItemSelectedListener, BaseQuickAdapter.RequestLoadMoreListener, SwipeRefreshLayout.OnRefreshListener {

    private var searchView: SearchView? = null
    private var newMovieAdapter: RecyclerAdapter? = null
    private lateinit var progressBottomSheet : ProgressBottomSheet
    private var conMgr: ConnectivityManager? = null
    private var mSiteWorker: SiteWorker? = null
    private var requestQuery: SiteWorker.RequestQuery? = null
    private var appDataSource: AppDataSource? = null
    private var observable: Observable<List<Movie>>? = null

    private val getListMoviesObserver by lazy {
        object : CompletableObserver {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onComplete() {
                observable!!
                        .subscribeOn(Schedulers.io())
                        .map { movies ->
                            for (movie in movies) {
                                var image: Bitmap?
                                try {
                                    image = SiteWorker.getCachedImage(applicationContext, movie.movieImageURL)
                                } catch (e: FileNotFoundException) {
                                    val imageDownloader = ImageDownloader()
                                    image = imageDownloader.execute(movie.movieImageURL).get()
                                    SiteWorker.saveImage(applicationContext, image!!, movie.movieImageURL)
                                }

                                movie.image = image
                            }
                            movies
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(updateListConsumer!!)
                //Log.d("Task", "get favorites completable completed");
            }

            override fun onError(e: Throwable) {

            }
        }
    }

    private val getOnLoadMoreObserver by lazy {
        object : CompletableObserver {
            override fun onSubscribe(d: Disposable) {

            }

            override fun onComplete() {
                observable!!
                        .subscribeOn(Schedulers.io())
                        .map { movies ->
                            for (movie in movies) {
                                var image: Bitmap?
                                try {
                                    image = SiteWorker.getCachedImage(applicationContext, movie.movieImageURL)
                                } catch (e: FileNotFoundException) {
                                    val imageDownloader = ImageDownloader()
                                    image = imageDownloader.execute(movie.movieImageURL).get()
                                    SiteWorker.saveImage(applicationContext, image!!, movie.movieImageURL)
                                }

                                movie.image = image
                            }
                            movies
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(onLoadMoreConsumer!!)
                //Log.d("Task", "get favorites completable completed");
            }

            override fun onError(e: Throwable) {

            }
        }
    }

    private val onLoadMoreConsumer by lazy {
        Consumer<List<Movie>> { movies ->
            newMovieAdapter!!.addData(movies)
            newMovieAdapter!!.loadMoreComplete()
            mAdMobView.visibility = View.VISIBLE
        }
    }

    private val updateListConsumer by lazy {
        Consumer<List<Movie>> { movies ->
            updateDataList(movies)

            if (swipeContainer.isRefreshing) swipeContainer.isRefreshing = false

            if (progressBottomSheet.isAdded && progressBottomSheet.isVisible)
                progressBottomSheet.dismissAllowingStateLoss()
        }
    }

    private val mAdMobView: AdView by lazy { AdView(this) }
    private var mAdRequest: AdRequest? = null
    private var mAdViewContainer: RelativeLayout? = null


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

    private val GENRES_CODE = 15

    private var activityState = ACTIVITY_STATE.LOST_CONNECTION

    internal enum class ACTIVITY_STATE {
        ONLINE,
        FAVORITES,
        LOST_CONNECTION
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        mSiteWorker = SiteWorker()
        appDataSource = AppDataSource(applicationContext)
        progressBottomSheet = ProgressBottomSheet()

        mAdViewContainer = relativeContainer
        //initAdMobView()


        //requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        conMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


        navigationView.setNavigationItemSelectedListener(this)


        drawerLayout.setDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(p0: Int) {
                //super
            }

            override fun onDrawerSlide(p0: View, p1: Float) {

            }

            override fun onDrawerClosed(p0: View) {
                mAdMobView.visibility = View.VISIBLE
            }

            override fun onDrawerOpened(p0: View) {
                mAdMobView.visibility = View.GONE
            }

        })

        swipeContainer.setOnRefreshListener(this)

        setSupportActionBar(toolbarActionBar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_menu)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val mDivider = applicationContext.getDrawable(R.drawable.line_divider)
            val mDividerItemDecoration = CustomDivider(mDivider, 10, 10)
            movieListRecyclerView!!.addItemDecoration(mDividerItemDecoration)
        }


        val metrics = resources.displayMetrics
        var spanCount = Math.ceil((metrics.widthPixels / 240).toDouble()).toInt()
        Settings.max_loaded_in_screen = spanCount * 4

        movieListRecyclerView!!.layoutManager = GridLayoutManager(this,spanCount)


        movieListRecyclerView!!.setHasFixedSize(true)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbarActionBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        newMovieAdapter = RecyclerAdapter(R.layout.fragment_movie_new, ArrayList())
        movieListRecyclerView!!.adapter = newMovieAdapter
        newMovieAdapter!!.onItemClickListener = this
        newMovieAdapter!!.setOnLoadMoreListener(this, movieListRecyclerView)
        newMovieAdapter!!.setEnableLoadMore(false)
        newMovieAdapter!!.setLoadMoreView(CustomLoadMoreView())




        if (hasConnection()) {
            activityState = ACTIVITY_STATE.ONLINE

            Completable.fromCallable {
                try {
                    requestQuery = mSiteWorker!!.RequestQuery(applicationContext, SiteWorker.EDITOR_CHOICE_QUERY)
                    observable = requestQuery!!.nextQuery
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: ExecutionException) {
                    showConnectionError()
                } catch (e: NullPointerException) {
                    showConnectionError()
                }

                null
            }.subscribeOn(Schedulers.io())
                    .subscribe(getListMoviesObserver!!)

        } else
            showConnectionError()

        //refreshBannerAd()
    }


    private fun initAdMobView() {
        mAdMobView.adSize = AdSize.flexibleSize()

        mAdMobView.blockId = Settings.block_id()
        mAdMobView.adEventListener = mBannerAdListener

        mAdRequest = AdRequest.Builder().build()

        val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        mAdViewContainer!!.addView(mAdMobView, layoutParams)
    }

    private fun refreshBannerAd() {
        mAdMobView.visibility = View.INVISIBLE
        mAdMobView.loadAd(mAdRequest)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        val myActionMenuItem = menu.findItem(R.id.action_search)
        searchView = myActionMenuItem.actionView as SearchView

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView!!.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(queryString: String): Boolean {

                // Поиск дорам
                if (hasConnection()) {
                    activityState = ACTIVITY_STATE.ONLINE

                    if (!progressBottomSheet.isAdded) {
                        progressBottomSheet.show(supportFragmentManager, "progressBar")
                    }

                    Completable.fromCallable {
                        try {
                            val params = HashMap<String, String>()
                            params["q"] = queryString

                            requestQuery = mSiteWorker!!.RequestQuery(applicationContext, SiteWorker.SEARCH_QUERY, SiteWorker.SEARCH_PREFIX, params)
                            observable = requestQuery!!.nextQuery
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        } catch (e: ExecutionException) {
                            showConnectionError()
                        } catch (e: NullPointerException) {
                            showConnectionError()
                        }

                        null
                    }.subscribeOn(Schedulers.io())
                            .subscribe(getListMoviesObserver!!)

                    title = getString(R.string.search_hint) + ": $queryString"


                    if (!searchView!!.isIconified) {
                        searchView!!.isIconified = true
                    }
                } else
                    showConnectionError()

                myActionMenuItem.collapseActionView()
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                startActivity(intent)
            }
        }
        return true
    }

    override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
        return false
    }

    override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
        return false
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }



    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.nav_editorchoice -> {
                if (hasConnection()) {
                    activityState = ACTIVITY_STATE.ONLINE

                    if (!progressBottomSheet.isAdded) {
                        progressBottomSheet.show(supportFragmentManager, "progressBar")
                    }

                    Completable.fromCallable {
                    try {
                        requestQuery = mSiteWorker!!.RequestQuery(applicationContext, SiteWorker.EDITOR_CHOICE_QUERY)
                        observable = requestQuery!!.nextQuery
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    } catch (e: ExecutionException) {
                        showConnectionError()
                    } catch (e: NullPointerException) {
                        showConnectionError()
                    }
                    null
                }.subscribeOn(Schedulers.io())
                        .subscribe(getListMoviesObserver!!)

                    title = getString(R.string.editor_choice_title)

                } else
                    showConnectionError();
            }
            R.id.nav_new -> {

                if (hasConnection()) {
                    activityState = ACTIVITY_STATE.ONLINE


                    if (!progressBottomSheet.isAdded) {
                        progressBottomSheet.show(supportFragmentManager, "progressBar")
                    }

                    Completable.fromCallable {
                        try {
                            val params = HashMap<String, String>()
                            params[SiteWorker.NEW_MOVIES_PARAMS[0]] = SiteWorker.NEW_MOVIES_PARAMS[1]
                            requestQuery = mSiteWorker!!.RequestQuery(applicationContext, SiteWorker.SIMPLE_QUERY, SiteWorker.LIST_PREFIX, params)
                            observable = requestQuery!!.nextQuery
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        } catch (e: ExecutionException) {
                            showConnectionError()
                        } catch (e: NullPointerException) {
                            showConnectionError()
                        }

                        null
                    }.subscribeOn(Schedulers.io())
                            .subscribe(getListMoviesObserver!!)

                    title = getString(R.string.new_movie_title)

                    //newMovieAdapter!!.clear()
                } else
                    showConnectionError();
            }
            R.id.nav_best -> {

                if (hasConnection()) {
                    activityState = ACTIVITY_STATE.ONLINE

                    if (!progressBottomSheet.isAdded) {
                        progressBottomSheet.show(supportFragmentManager, "progressBar")
                    }

                    Completable.fromCallable {
                        try {
                            requestQuery = mSiteWorker!!.RequestQuery(applicationContext, SiteWorker.SIMPLE_QUERY, SiteWorker.LIST_PREFIX)
                            observable = requestQuery!!.nextQuery
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        } catch (e: ExecutionException) {
                            showConnectionError()
                        } catch (e: NullPointerException) {
                            showConnectionError()
                        }
                        null
                    }.subscribeOn(Schedulers.io())
                            .subscribe(getListMoviesObserver!!)

                    title = getString(R.string.best_movie_title)

                    //newMovieAdapter!!.clear()
                } else
                    showConnectionError();
            }

            R.id.nav_ongoing -> {

                if (hasConnection()) {
                    activityState = ACTIVITY_STATE.ONLINE

                    if (!progressBottomSheet.isAdded) {
                        progressBottomSheet.show(supportFragmentManager, "progressBar")
                    }

                    Completable.fromCallable {
                        try {
                            val params = HashMap<String, String>()
                            params[SiteWorker.ONGOING_PARAMS[0]] = SiteWorker.ONGOING_PARAMS[1]

                            requestQuery = mSiteWorker!!.RequestQuery(applicationContext, SiteWorker.SIMPLE_QUERY, SiteWorker.ONGOING_PREFIX, params)
                            observable = requestQuery!!.nextQuery
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        } catch (e: ExecutionException) {
                            showConnectionError()
                        } catch (e: NullPointerException) {
                            showConnectionError()
                        }

                        null
                    }.subscribeOn(Schedulers.io())
                            .subscribe(getListMoviesObserver!!)
                    title = getString(R.string.ongoing_title)


                } else
                    showConnectionError();
            }
            R.id.nav_random -> {

                if (hasConnection()) {


                    if (!progressBottomSheet.isAdded) {
                        progressBottomSheet.show(supportFragmentManager, "progressBar")
                    }


                    getMovieRequestSingle(SiteWorker.SITE_URL + SiteWorker.RANDOM_MOVIE_PREFIX).observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe( { json ->
                                val intent = Intent(this@MainActivity, MovieAboutActivity::class.java)

                                intent.putExtra("movie_info", json.toString())

                                if (progressBottomSheet.isAdded)
                                    progressBottomSheet.dismissAllowingStateLoss()

                                startActivity(intent)
                            }, { error ->
                                Log.d("Error occured",error.localizedMessage)
                            })


                } else
                    showConnectionError();
            }

            R.id.nav_genres -> {

                if (hasConnection()) {

                    activityState = ACTIVITY_STATE.ONLINE
                    try {
                        val jsonArray = SiteWorker.genresList
                        val intent = Intent(this@MainActivity, GenresActivity::class.java)
                        intent.putExtra("genres", jsonArray.toString())
                        startActivityForResult(intent, GENRES_CODE)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    } catch (e: ExecutionException) {
                        showConnectionError()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: NullPointerException) {
                        showConnectionError()
                    }
                } else
                    showConnectionError();

            }
            R.id.nav_favourites -> {

                //List<Movie> favoritesMovies = appDataSource.getListOfFavorites();
                activityState = ACTIVITY_STATE.FAVORITES
                Completable.fromCallable {
                    observable = appDataSource!!.listOfFavorites
                    null
                }.subscribeOn(Schedulers.io())
                        .subscribe(getListMoviesObserver!!)

                title = getString(R.string.action_favorite)

            }

            R.id.nav_history -> {
                Toast.makeText(applicationContext, "Функция в разработке", Toast.LENGTH_LONG).show()
            }

            R.id.nav_about -> {
                val intent = Intent(this@MainActivity, AboutApplicationActivity::class.java)
                startActivity(intent)
            }

            else -> { }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true

    }


    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GENRES_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val resultPrefix = data!!.getStringExtra("link")
                val genreName = data.getStringExtra("name")

                progressBottomSheet = ProgressBottomSheet()


                Completable.fromCallable {
                    try {
                        requestQuery = mSiteWorker!!.RequestQuery(applicationContext, SiteWorker.SIMPLE_QUERY, resultPrefix)
                        observable = requestQuery!!.nextQuery
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    } catch (e: ExecutionException) {
                        showConnectionError()
                    } catch (e: NullPointerException) {
                        showConnectionError()
                    }

                    null
                }.subscribeOn(Schedulers.io())
                        .subscribe(getListMoviesObserver!!)

                setTitle(genreName.substring(0, 1).toUpperCase() + genreName.substring(1))
            }
        }
    }


    public override fun onPause() {
        mAdMobView.pause()
        super.onPause()

        if (progressBottomSheet.isResumed || progressBottomSheet.isVisible)
            progressBottomSheet.dismissAllowingStateLoss()
    }

    /** Called when returning to the activity  */
    public override fun onResume() {
        super.onResume()

        if (progressBottomSheet.isResumed || progressBottomSheet.isVisible)
            progressBottomSheet.dismissAllowingStateLoss()

        //mAdMobView?.resume()
        refreshBannerAd()
    }



    /** Called before the activity is destroyed  */
    public override fun onStop() {
        mAdMobView.pause()
        super.onStop()

        if (progressBottomSheet.isResumed || progressBottomSheet.isVisible)
            progressBottomSheet.dismissAllowingStateLoss()
    }


    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        val selectedMovie = newMovieAdapter!!.data[position]
        if (hasConnection()) {
            //Handler().postDelayed({

                val jsonObject: JSONObject
            if (!progressBottomSheet.isAdded) {
                progressBottomSheet.show(supportFragmentManager, "progressBar")
            }

            getMovieRequestSingle(selectedMovie.url).observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe( { json ->
                        val intent = Intent(this@MainActivity, MovieAboutActivity::class.java)
                        selectedMovie.title = json.getString("title")
                        selectedMovie.initialSeries = json.getString("initial_series")
                        selectedMovie.productionCountry = json.getString("production")
                        selectedMovie.seriesNumber = json.getString("series_number")
                        selectedMovie.duration = json.getString("duration")
                        selectedMovie.description = json.getString("description")
                        selectedMovie.productionYear = json.getString("age")

                        val bundle = Bundle()

                        bundle.putSerializable("movie", selectedMovie)

                        intent.putExtra("bundle", bundle)
                        intent.putExtra("movie_info", json.toString())

                        if (progressBottomSheet.isAdded)
                            progressBottomSheet.dismissAllowingStateLoss()

                        startActivity(intent)
                    }, { error ->
                        Log.d("Error occured",error.localizedMessage)
                    })

        } else
            showConnectionError()

    }


    override fun onLoadMoreRequested() {
        if (activityState == ACTIVITY_STATE.ONLINE) {
            if (requestQuery != null) {

                if (requestQuery!!.offset() >= requestQuery!!.queryAmount()) {
                    newMovieAdapter!!.loadMoreComplete()
                    newMovieAdapter!!.setEnableLoadMore(false)
                } else {
                    if (hasConnection()) {
                        mAdMobView.visibility = View.GONE

                        Completable.fromCallable {
                            try {
                                observable = requestQuery!!.nextQuery
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            } catch (e: ExecutionException) {
                                showConnectionError()
                            } catch (e: NullPointerException) {
                                showConnectionError()
                            }

                            null
                        }.subscribeOn(Schedulers.io())
                                .subscribe(getOnLoadMoreObserver!!)

                    } else {
                        //Get more data failed
                        Toast.makeText(this@MainActivity, R.string.cant_connect_error, Toast.LENGTH_LONG).show()
                        newMovieAdapter!!.loadMoreFail()

                    }
                }
            } else {
                if (newMovieAdapter!!.isLoading) newMovieAdapter!!.loadMoreComplete()
                newMovieAdapter!!.setEnableLoadMore(false)
            }
        } else {
            if (newMovieAdapter!!.isLoading) newMovieAdapter!!.loadMoreComplete()
            newMovieAdapter!!.setEnableLoadMore(false)
        }
    }


    override fun onRefresh() {

        when(activityState) {

            ACTIVITY_STATE.ONLINE,ACTIVITY_STATE.LOST_CONNECTION -> {
                if (hasConnection()) {
                    Completable.fromCallable {
                        try {
                            if (requestQuery != null)
                                requestQuery!!.resetOffset()
                            else
                                requestQuery = mSiteWorker!!.RequestQuery(applicationContext, SiteWorker.EDITOR_CHOICE_QUERY)

                            observable = requestQuery!!.nextQuery
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        } catch (e: ExecutionException) {
                            showConnectionError()
                        } catch (e: NullPointerException) {
                            showConnectionError()
                        }

                        null
                    }.subscribeOn(Schedulers.io())
                            .subscribe(getListMoviesObserver!!)
                    refreshBannerAd()
                } else {
                    showConnectionError()
                }
            }
            ACTIVITY_STATE.FAVORITES -> {
                activityState = ACTIVITY_STATE.FAVORITES
                Completable.fromCallable {
                    observable = appDataSource!!.listOfFavorites
                    null
                }.subscribeOn(Schedulers.io())
                        .subscribe(getListMoviesObserver!!)
            }
        }
    }

    private fun updateDataList(list: List<Movie>) {

        newMovieAdapter!!.addAll(list)
        movieListRecyclerView!!.recycledViewPool.clear()

        if (newMovieAdapter!!.data.size != 0)
            movieListRecyclerView!!.scrollToPosition(0)

        if (activityState == ACTIVITY_STATE.ONLINE) {
            if (requestQuery != null && requestQuery!!.offset() < requestQuery!!.queryAmount())
                newMovieAdapter!!.setEnableLoadMore(true)
        }
    }

    internal fun showConnectionError() {
        activityState = ACTIVITY_STATE.LOST_CONNECTION
        Toast.makeText(applicationContext, getText(R.string.cant_connect_error), Toast.LENGTH_SHORT).show()
    }


    internal fun hasConnection() : Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val ni = cm.getActiveNetworkInfo()
        return ni.isConnected
}


    internal inner class CustomDivider(val mDivider: Drawable, val topOffset: Int, val bottomOffset: Int) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)

            outRect.top = topOffset
            outRect.bottom = bottomOffset
        }

    }

    fun getMovieRequestSingle(url: String) : Single<JSONObject> {
        return Single.create<JSONObject> { observer ->
            val jsonObject = SiteWorker.getMovieInfo(url)

            observer.onSuccess(jsonObject)
        }
    }
}
