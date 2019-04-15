package ru.garretech.garred.doramatv.activities

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast

import com.chad.library.adapter.base.BaseQuickAdapter

import org.json.JSONException
import org.json.JSONObject


import java.io.FileNotFoundException
import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.ExecutionException
import butterknife.BindView
import butterknife.ButterKnife
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import ru.garretech.garred.doramatv.R
import ru.garretech.garred.doramatv.Settings
import ru.garretech.garred.doramatv.adapters.RecyclerAdapter
import ru.garretech.garred.doramatv.database.AppDataSource
import ru.garretech.garred.doramatv.fragments.CustomLoadMoreView
import ru.garretech.garred.doramatv.fragments.ProgressBottomSheet
import ru.garretech.garred.doramatv.model.Movie
import ru.garretech.garred.doramatv.tools.ImageDownloader
import ru.garretech.garred.doramatv.tools.SiteWorker

class MainActivity : AppCompatActivity(), BaseQuickAdapter.OnItemClickListener, MenuItem.OnActionExpandListener, NavigationView.OnNavigationItemSelectedListener, BaseQuickAdapter.RequestLoadMoreListener, SwipeRefreshLayout.OnRefreshListener {
    @BindView(R.id.movie_list)
    internal var mRecyclerView: RecyclerView? = null
    @BindView(R.id.toolbar_actionbar)
    internal lateinit var toolbar: Toolbar
    @BindView(R.id.drawer_layout)
    internal lateinit var drawer: DrawerLayout
    @BindView(R.id.nav_view)
    internal lateinit var navigationView: NavigationView
    @BindView(R.id.progressBar)
    internal lateinit var progressBar: ProgressBar
    @BindView(R.id.swipe_container)
    internal lateinit var swipeContainer: SwipeRefreshLayout

    private var searchView: SearchView? = null
    private var newMovieAdapter: RecyclerAdapter? = null
    private var progressBottomSheet: ProgressBottomSheet? = null
    private var conMgr: ConnectivityManager? = null
    private var mSiteWorker: SiteWorker? = null
    private var requestQuery: SiteWorker.RequestQuery? = null
    private var hasConnection: Boolean? = true
    private var appDataSource: AppDataSource? = null
    private var observable: Observable<List<Movie>>? = null
    private var getListMoviesObserver: CompletableObserver? = null
    private var getOnLoadMoreObserver: CompletableObserver? = null
    private var onLoadMoreConsumer: Consumer<List<Movie>>? = null
    private var updateListConsumer: Consumer<List<Movie>>? = null

    private val GENRES_CODE = 15

    private var activityState = ACTIVITY_STATE.CACHED

    internal enum class ACTIVITY_STATE {
        ONLINE,
        CACHED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mSiteWorker = SiteWorker()
        appDataSource = AppDataSource(applicationContext)


        updateListConsumer = Consumer { movies ->
            //requestQuery = null;
            updateDataList(movies)
            if (progressBottomSheet!!.isVisible)
                progressBottomSheet!!.dismissAllowingStateLoss()
        }

        onLoadMoreConsumer = Consumer { movies ->
            newMovieAdapter!!.addData(movies)
            newMovieAdapter!!.loadMoreComplete()
        }

        getListMoviesObserver = object : CompletableObserver {
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

        getOnLoadMoreObserver = object : CompletableObserver {
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


        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        conMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        ButterKnife.bind(this)
        progressBottomSheet = ProgressBottomSheet()
        navigationView!!.setNavigationItemSelectedListener(this)
        swipeContainer!!.setOnRefreshListener(this)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_menu)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val mDivider = applicationContext.getDrawable(R.drawable.line_divider)
            val mDividerItemDecoration = CustomDivider(mDivider, 10, 10)
            mRecyclerView!!.addItemDecoration(mDividerItemDecoration)
        }


        mRecyclerView!!.layoutManager = object : LinearLayoutManager(this) {
            override fun supportsPredictiveItemAnimations(): Boolean {
                return false
            }
        }
        mRecyclerView!!.setHasFixedSize(true)

        val toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer!!.addDrawerListener(toggle)
        toggle.syncState()

        newMovieAdapter = RecyclerAdapter(R.layout.fragment_movie, ArrayList())
        mRecyclerView!!.adapter = newMovieAdapter
        newMovieAdapter!!.setOnItemClickListener(this)
        newMovieAdapter!!.setOnLoadMoreListener(this, mRecyclerView)
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
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        val myActionMenuItem = menu.findItem(R.id.action_search)
        searchView = myActionMenuItem.actionView as SearchView

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView!!.setSearchableInfo(
                searchManager.getSearchableInfo(componentName))

        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(queryString: String): Boolean {

                // Поиск дорам
                if (hasConnection()!!) {
                    activityState = ACTIVITY_STATE.ONLINE

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

                    title = "Поиск: $queryString"

                    newMovieAdapter!!.clear()
                    if (!progressBottomSheet!!.isAdded) {
                        progressBottomSheet!!.show(supportFragmentManager, "progressBar")
                    }

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

                    newMovieAdapter!!.clear()
                    if (!progressBottomSheet!!.isAdded) {
                        progressBottomSheet!!.show(supportFragmentManager, "progressBar")
                    }
                } else
                    showConnectionError();
            }
            R.id.nav_best -> {

                if (hasConnection()) {

                    activityState = ACTIVITY_STATE.ONLINE

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

                    newMovieAdapter!!.clear()
                    if (!progressBottomSheet!!.isAdded) {
                        progressBottomSheet!!.show(supportFragmentManager, "progressBar")
                    }
                } else
                    showConnectionError();
            }

            R.id.nav_ongoing -> {

                if (hasConnection()) {
                    activityState = ACTIVITY_STATE.ONLINE

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

                    newMovieAdapter!!.clear()
                    if (!progressBottomSheet!!.isAdded) {
                        progressBottomSheet!!.show(supportFragmentManager, "progressBar")
                    }
                } else
                    showConnectionError();
            }
            R.id.nav_random -> {

                if (hasConnection()) {

                    val intent = Intent(this@MainActivity, MovieAboutActivity::class.java)

                    Handler().postDelayed({

                        var jsonObject: JSONObject? = null

                        try {
                            jsonObject = SiteWorker.getMovieInfo(SiteWorker.SITE_URL + SiteWorker.RANDOM_MOVIE_PREFIX)
                            jsonObject!!.put("access_token", Settings.access_token())

                            intent.putExtra("movie_info", jsonObject.toString())

                            if (progressBottomSheet!!.isVisible)
                                progressBottomSheet!!.dismissAllowingStateLoss()

                            startActivity(intent)

                        } catch (e: JSONException) {
                            e.printStackTrace()
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        } catch (e: ExecutionException) {
                            showConnectionError()
                        } catch (e: NullPointerException) {
                            showConnectionError()
                        }
                    }, 0)
                    if (!progressBottomSheet!!.isAdded) {
                        progressBottomSheet!!.show(supportFragmentManager, "progressBar")
                    }
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
                activityState = ACTIVITY_STATE.CACHED
                Completable.fromCallable {
                    observable = appDataSource!!.listOfFavorites
                    null
                }.subscribeOn(Schedulers.io())
                        .subscribe(getListMoviesObserver!!)

                title = getString(R.string.action_favorite)
            }//Toast.makeText(getApplicationContext(), "Функция в разработке", Toast.LENGTH_LONG).show();
            R.id.nav_history -> {
                Toast.makeText(applicationContext, "Функция в разработке", Toast.LENGTH_LONG).show()
            }
            R.id.nav_about -> {
                val intent = Intent(this@MainActivity, AboutApplicationActivity::class.java)
                startActivity(intent)
            }
            else -> {
            }
        }
        drawer!!.closeDrawer(GravityCompat.START)
        return true

    }


    override fun onBackPressed() {
        if (drawer!!.isDrawerOpen(GravityCompat.START)) {
            drawer!!.closeDrawer(GravityCompat.START)
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
                newMovieAdapter!!.clear()
            }
        }
    }


    public override fun onPause() {
        /*if (adView != null) {
            adView.pause();
        }*/
        super.onPause()
    }

    /** Called when returning to the activity  */
    public override fun onResume() {
        super.onResume()
        /*if (adView != null) {
            adView.resume();
        }*/
    }

    /** Called before the activity is destroyed  */
    public override fun onDestroy() {
        /*if (adView != null) {
            adView.destroy();
        } */
        super.onDestroy()
    }


    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        val selectedMovie = newMovieAdapter!!.data[position]
        val intent = Intent(this@MainActivity, MovieAboutActivity::class.java)
        if (hasConnection()) {
            Handler().postDelayed({

                val jsonObject: JSONObject

                try {
                    jsonObject = SiteWorker.getMovieInfo(selectedMovie.url)

                    selectedMovie.title = jsonObject.getString("title")
                    selectedMovie.initialSeries = jsonObject.getString("initial_series")
                    selectedMovie.productionCountry = jsonObject.getString("production")
                    selectedMovie.seriesNumber = jsonObject.getString("series_number")
                    selectedMovie.duration = jsonObject.getString("duration")
                    selectedMovie.description = jsonObject.getString("description")
                    selectedMovie.productionYear = jsonObject.getString("age")
                    //jsonObject.put("title", selectedMovie.getTitle());
                    //jsonObject.put("genres", selectedMovie.getGenres().toString());
                    //jsonObject.put("image_url", selectedMovie.getMovieImageURL());
                    //jsonObject.put("url", selectedMovie.getURL());
                    jsonObject.put("access_token", Settings.access_token())

                    val bundle = Bundle()

                    bundle.putSerializable("movie", selectedMovie)

                    intent.putExtra("bundle", bundle)
                    intent.putExtra("movie_info", jsonObject.toString())

                    if (progressBottomSheet!!.isVisible)
                        progressBottomSheet!!.dismiss()

                    startActivity(intent)

                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: ExecutionException) {
                    showConnectionError()
                } catch (e: NullPointerException) {
                    showConnectionError()
                }
            }, 500)
            if (!progressBottomSheet!!.isAdded) {
                progressBottomSheet!!.show(supportFragmentManager, "progressBar")
            }
        } else
            showConnectionError()

    }


    override fun onLoadMoreRequested() {
        if (activityState == ACTIVITY_STATE.ONLINE) {
            if (requestQuery != null) {

                if (requestQuery!!.offset() >= requestQuery!!.queryAmount()) {
                    // Все данные загружены
                    newMovieAdapter!!.setEnableLoadMore(false)
                } else {
                    if (hasConnection!!) {

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
                        hasConnection = true
                        Toast.makeText(this@MainActivity, R.string.cant_connect_error, Toast.LENGTH_LONG).show()
                        newMovieAdapter!!.loadMoreFail()

                    }
                }
            } else
                newMovieAdapter!!.setEnableLoadMore(false)
        } else
            newMovieAdapter!!.setEnableLoadMore(false)
    }


    override fun onRefresh() {
        swipeContainer!!.isRefreshing = false
        if (activityState == ACTIVITY_STATE.ONLINE) {
            if (requestQuery != null) {
                requestQuery!!.resetOffset()

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
                        .subscribe(getListMoviesObserver!!)


            } else {

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
            }
        } else {
            activityState = ACTIVITY_STATE.CACHED
            Completable.fromCallable {
                observable = appDataSource!!.listOfFavorites
                null
            }.subscribeOn(Schedulers.io())
                    .subscribe(getListMoviesObserver!!)
        }
    }


    private fun updateDataList(list: List<Movie>) {

        newMovieAdapter!!.addAll(list)
        mRecyclerView!!.recycledViewPool.clear()
        mRecyclerView!!.scrollToPosition(0)

        if (requestQuery != null && requestQuery!!.offset() < requestQuery!!.queryAmount())
            newMovieAdapter!!.setEnableLoadMore(true)
    }

    internal fun showConnectionError() {
        hasConnection = false
        Toast.makeText(applicationContext, getText(R.string.cant_connect_error), Toast.LENGTH_SHORT).show()
    }

    internal fun hasConnection(): Boolean {
        return conMgr!!.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).state == NetworkInfo.State.CONNECTED || conMgr!!.getNetworkInfo(ConnectivityManager.TYPE_WIFI).state == NetworkInfo.State.CONNECTED
    }


    internal inner class CustomDivider(val mDivider: Drawable, val topOffset: Int, val bottomOffset: Int) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)

            outRect.top = topOffset
            outRect.bottom = bottomOffset
        }

    }
}
