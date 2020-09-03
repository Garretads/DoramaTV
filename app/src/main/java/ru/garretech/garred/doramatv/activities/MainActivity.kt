package ru.garretech.garred.doramatv.activities


import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProviders
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.android.material.navigation.NavigationView
import com.yandex.mobile.ads.*
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import org.json.JSONArray
import ru.garretech.garred.doramatv.BuildConfig
import ru.garretech.garred.doramatv.DisposableManager
import ru.garretech.garred.doramatv.R
import ru.garretech.garred.doramatv.Settings
import ru.garretech.garred.doramatv.adapters.MovieListAdapter
import ru.garretech.garred.doramatv.fragments.ConfirmationFragment
import ru.garretech.garred.doramatv.fragments.CustomLoadMoreView
import ru.garretech.garred.doramatv.fragments.DisclaimerFragment
import ru.garretech.garred.doramatv.fragments.SortingFragment
import ru.garretech.garred.doramatv.model.Movie
import ru.garretech.garred.doramatv.tools.SiteWorker
import ru.garretech.garred.doramatv.viewmodels.MainActivityViewModel
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), BaseQuickAdapter.OnItemClickListener, MenuItem.OnActionExpandListener, NavigationView.OnNavigationItemSelectedListener, BaseQuickAdapter.RequestLoadMoreListener, androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener, SortingFragment.OnFragmentInteractionListener {

    private lateinit var searchView: SearchView
    private lateinit var movieAdapter: MovieListAdapter
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var menu: Menu
    private val sortingMenuItem by lazy { menu.findItem(R.id.action_sort) }
    private val clearMenuItem by lazy { menu.findItem(R.id.action_clear) }
    private val searchMenuItem by lazy { menu.findItem(R.id.action_search) }

    private val getListMoviesObserver by lazy {
        object : CompletableObserver {
            override fun onSubscribe(d: Disposable) {}

            override fun onComplete() {

                DisposableManager.add(viewModel.observable!!
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(updateListConsumer))
            }

            override fun onError(e: Throwable) {
                Log.e("LIST LOAD", "Ошибка получения списка фильмов, попробуйте еще раз", e)
            }
        }
    }

    private val getOnLoadMoreObserver by lazy {
        object : CompletableObserver {
            override fun onSubscribe(d: Disposable) {}

            override fun onComplete() {


                DisposableManager.add(viewModel.observable!!
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(onLoadMoreConsumer))
            }

            override fun onError(e: Throwable) {
                Log.e("ON LOAD MORE", "Ошибка получения списка фильмов, попробуйте еще раз", e)
            }
        }
    }

    private val onLoadMoreConsumer by lazy {
        Consumer<List<Movie>> { movies ->
            movieAdapter.addData(movies)
            movieAdapter.loadMoreComplete()
            //mAdMobView.visibility = View.VISIBLE
        }
    }

    private val updateListConsumer by lazy {
        Consumer<List<Movie>> { movies ->
            updateDataList(movies, true)

            if (swipeContainer.isRefreshing)
                swipeContainer.isRefreshing = false

            dismissProgressBar()
        }
    }

    private val mAdMobView: AdView by lazy { AdView(this) }
    private var mAdRequest: AdRequest? = null


    private val mBannerAdListener = object : AdEventListener {
        override fun onAdFailedToLoad(p0: AdRequestError) {}

        override fun onAdClosed() {}


        override fun onAdLeftApplication() {}

        override fun onAdLoaded() {
            mAdMobView.visibility = View.VISIBLE
        }

        override fun onAdOpened() {}

    }

    private val GENRES_CODE = 15

    private var activityState = ACTIVITY_STATE.LOST_CONNECTION

    enum class ACTIVITY_STATE {
        EDITOR_CHOICE,
        MOVIE_LIST,
        FAVORITES,
        HISTORY,
        LOST_CONNECTION
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)

        //initAdMobView()

        navigationView.setNavigationItemSelectedListener(this)


        drawerLayout.setDrawerListener(object : androidx.drawerlayout.widget.DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(p0: Int) {
                //super
            }

            override fun onDrawerSlide(p0: View, p1: Float) {

            }

            override fun onDrawerClosed(p0: View) {
                //mAdMobView.visibility = View.VISIBLE
            }

            override fun onDrawerOpened(p0: View) {
                //mAdMobView.visibility = View.GONE
            }

        })

        swipeContainer.setOnRefreshListener(this)

        setSupportActionBar(toolbarActionBar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_menu)

        title = viewModel.title

        val metrics = resources.displayMetrics
        var spanCount = (metrics.widthPixels / (115 * metrics.scaledDensity)).toInt()
        Settings.max_loaded_in_screen = spanCount * 8

        movieListRecyclerView!!.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, spanCount)


        movieListRecyclerView!!.setHasFixedSize(true)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbarActionBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        movieAdapter = MovieListAdapter(R.layout.cardview_movie, ArrayList())
        movieListRecyclerView!!.adapter = movieAdapter
        movieAdapter.onItemClickListener = this
        movieAdapter.setOnLoadMoreListener(this, movieListRecyclerView)
        movieAdapter.setEnableLoadMore(false)
        movieAdapter.setLoadMoreView(CustomLoadMoreView())

        firstStartDisclaimer()

        //performInitialRequest()

        //refreshBannerAd()
    }

    fun showProgressBar() {
        if (!viewModel.progressBottomSheet.isAdded) {
            viewModel.progressBottomSheet.show(supportFragmentManager, "progressBar")
        }
    }

    fun dismissProgressBar() {
        if (viewModel.progressBottomSheet.isAdded)
            viewModel.progressBottomSheet.dismissAllowingStateLoss()
    }


    private fun initAdMobView() {
        mAdMobView.adSize = AdSize.flexibleSize()

        mAdMobView.blockId = Settings.block_id()
        mAdMobView.adEventListener = mBannerAdListener

        mAdRequest = AdRequest.Builder().build()

        val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        //relativeContainer.addView(mAdMobView, layoutParams)
    }

    private fun refreshBannerAd() {
        mAdMobView.visibility = View.INVISIBLE
        mAdMobView.loadAd(mAdRequest)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        this.menu = menu

        performInitialRequest()

        searchView = searchMenuItem.actionView as SearchView

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(queryString: String): Boolean {
                // Поиск дорам
                if (hasConnection()) {

                    changeState(ACTIVITY_STATE.MOVIE_LIST)
                    showProgressBar()

                    val params = HashMap<String, String>()
                    params["q"] = queryString

                    viewModel.getRequestQueryCompletable(SiteWorker.SEARCH_QUERY, SiteWorker.SEARCH_PREFIX, params)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(getListMoviesObserver)

                    title = getString(R.string.search_hint) + ": $queryString"
                    viewModel.title = title.toString()

                    //if (!searchView.isIconified) {
                    searchView.isIconified = true
                    // }
                } else
                    showConnectionError()

                searchMenuItem.collapseActionView()
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
            R.id.action_sort -> {
                if (viewModel.requestQuery!!.requestUri() != null) {

                    showProgressBar()

                    DisposableManager.add(Single.create<JSONArray> { observer ->
                        val jsonArray = SiteWorker.getSortingParams(viewModel.requestQuery?.requestUri()?.build()!!)
                        observer.onSuccess(jsonArray)
                    }.observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({ jsonArray ->
                                val sortingFragment = SortingFragment.newInstance(jsonArray, viewModel.requestQuery?.requestUri()?.toString()!!)
                                sortingFragment.show(supportFragmentManager, "sortingFragment")

                                dismissProgressBar()

                            }, { Log.d("SORTING FRAGMENT", "CAN'T GET SORTING WINDOW") }))
                }
            }
            R.id.action_clear -> {

                var confirmationDialog: ConfirmationFragment? = null

                when (activityState) {
                    ACTIVITY_STATE.FAVORITES -> {

                        confirmationDialog = ConfirmationFragment.newInstance(
                                "Предупреждение",
                                "Вы действительно хотите удалить содержимое избранного?"
                        )
                        //confirmationDialog.setStyle(DialogFragment.STYLE_NORMAL,R.style.CustomDialog)

                        confirmationDialog.setConfirmationListener(object :
                                ConfirmationFragment.OnFragmentInteractionListener {
                            override fun onAcceptPressed() {
                                showProgressBar()

                                DisposableManager.add(viewModel.clearFavorites().subscribe {
                                    viewModel.historyObservable.subscribe(getListMoviesObserver)
                                })
                            }

                            override fun onCancelPressed() {}
                        })

                    }
                    ACTIVITY_STATE.HISTORY -> {

                        confirmationDialog = ConfirmationFragment.newInstance(
                                "Предупреждение",
                                "Вы действительно хотите удалить содержимое истории?"
                        )
                        //confirmationDialog.setStyle(DialogFragment.STYLE_NORMAL,R.style.CustomDialog)

                        confirmationDialog.setConfirmationListener(object :
                                ConfirmationFragment.OnFragmentInteractionListener {
                            override fun onAcceptPressed() {
                                showProgressBar()

                                DisposableManager.add(viewModel.clearHistory().subscribe {
                                    viewModel.historyObservable.subscribe(getListMoviesObserver)
                                })
                            }

                            override fun onCancelPressed() {}
                        })
                    }
                }

                confirmationDialog?.show(supportFragmentManager, "confirmationDialog")
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
                    changeState(ACTIVITY_STATE.EDITOR_CHOICE)

                    showProgressBar()

                    viewModel.getRequestQueryCompletable(SiteWorker.EDITOR_CHOICE_QUERY)
                            .subscribe(getListMoviesObserver)

                    title = getString(R.string.editor_choice_title)
                    viewModel.title = title.toString()

                } else
                    showConnectionError()
            }
            R.id.nav_list -> {

                if (hasConnection()) {
                    changeState(ACTIVITY_STATE.MOVIE_LIST)

                    showProgressBar()

                    viewModel.getRequestQueryCompletable(SiteWorker.SIMPLE_QUERY, SiteWorker.LIST_PREFIX)
                            .subscribe(getListMoviesObserver)

                    title = getString(R.string.list_movie_title)
                    viewModel.title = title.toString()

                    //movieAdapter!!.clear()
                } else
                    showConnectionError()
            }

            R.id.nav_ongoing -> {

                if (hasConnection()) {
                    changeState(ACTIVITY_STATE.MOVIE_LIST)

                    val params = HashMap<String, String>()
                    params[SiteWorker.ONGOING_PARAMS[0]] = SiteWorker.ONGOING_PARAMS[1]

                    showProgressBar()

                    viewModel.getRequestQueryCompletable(SiteWorker.SIMPLE_QUERY, SiteWorker.ONGOING_PREFIX, params)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(getListMoviesObserver)
                    title = getString(R.string.ongoing_title)
                    viewModel.title = title.toString()


                } else
                    showConnectionError()
            }
            R.id.nav_random -> {

                if (hasConnection()) {

                    showProgressBar()

                    val intent = Intent(this@MainActivity, MovieInfoActivity::class.java)

                    intent.putExtra("is_random", true)

                    startActivity(intent)


                } else
                    showConnectionError()
            }

            R.id.nav_genres -> {

                if (hasConnection()) {

                    changeState(ACTIVITY_STATE.MOVIE_LIST)

                    showProgressBar()

                    viewModel.getGenresList().subscribe({
                        val intent = Intent(this@MainActivity, GenresActivity::class.java)
                        intent.putExtra("genres", it.toString())
                        startActivityForResult(intent, GENRES_CODE)
                        dismissProgressBar()
                    }, {
                        Log.e("MainActivity", "Ошибка при получении списка жанров", it)
                        Toast.makeText(this, "Ошибка при получении списка жанров, попробуйте еще раз", Toast.LENGTH_SHORT).show()
                    })
                } else
                    showConnectionError()

            }
            R.id.nav_favourites -> {
                changeState(ACTIVITY_STATE.FAVORITES)

                viewModel.favoritesObservable.subscribe(getListMoviesObserver)

                title = getString(R.string.action_favorite)
                viewModel.title = title.toString()
            }

            R.id.nav_history -> {
                changeState(ACTIVITY_STATE.HISTORY)

                viewModel.historyObservable.subscribe(getListMoviesObserver)

                title = getString(R.string.action_history)
                viewModel.title = title.toString()
            }

            R.id.nav_about -> {
                val intent = Intent(this@MainActivity, AboutApplicationActivity::class.java)
                startActivity(intent)
            }

            else -> {
            }
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
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GENRES_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                data?.let {
                    val resultPrefix = data.getStringExtra("link") ?: ""
                    val genreName = data.getStringExtra("name") ?: ""

                    showProgressBar()

                    changeState(ACTIVITY_STATE.MOVIE_LIST)

                    viewModel.getRequestQueryCompletable(SiteWorker.SIMPLE_QUERY, resultPrefix)
                            .subscribe(getListMoviesObserver)

                    title = genreName.substring(0, 1).toUpperCase() + genreName.substring(1)
                    viewModel.title = title.toString()
                }
            }
        }
    }


    public override fun onPause() {
        //mAdMobView.pause()
        //bag.dispose()
        super.onPause()

        dismissProgressBar()
    }

    /** Called when returning to the activity  */
    public override fun onResume() {
        super.onResume()

        dismissProgressBar()

        //mAdMobView.resume()
        //refreshBannerAd()
    }


    /** Called before the activity is destroyed  */
    public override fun onStop() {
        //mAdMobView.pause()
        //bag.dispose()
        super.onStop()

        dismissProgressBar()
    }

    override fun onFragmentInteraction(result: Map<String, Any>) {

        if (hasConnection()) {

            changeState(ACTIVITY_STATE.MOVIE_LIST)

            val params = result.get("params") as HashMap<String, String>
            var completable: Completable

            showProgressBar()

            completable = if (params.size == 0)
                viewModel.getRequestQueryCompletable(SiteWorker.SIMPLE_QUERY, result["path"] as String)
            else
                viewModel.getRequestQueryCompletable(SiteWorker.SIMPLE_QUERY, result["path"] as String, params)

            completable.subscribeOn(Schedulers.io())
                    .subscribe(getListMoviesObserver)

            //movieAdapter!!.clear()
        } else
            showConnectionError()
    }


    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        val selectedMovie = movieAdapter.data[position] as Movie
        if (hasConnection()) {

            val intent = Intent(this@MainActivity, MovieInfoActivity::class.java)
            intent.putExtra("is_random", false)
            intent.putExtra("movie_url", selectedMovie.url)

            startActivity(intent)

        } else
            showConnectionError()

    }


    override fun onLoadMoreRequested() {
        if (activityState == ACTIVITY_STATE.EDITOR_CHOICE || activityState == ACTIVITY_STATE.MOVIE_LIST) {
            if (viewModel.requestQuery != null) {

                if (viewModel.requestQuery!!.offset() >= viewModel.requestQuery!!.queryAmount()) {
                    movieAdapter.loadMoreComplete()
                    movieAdapter.setEnableLoadMore(false)
                } else {
                    if (hasConnection()) {
                        //mAdMobView.visibility = View.GONE

                        viewModel.nextQueryObservable.subscribe(getOnLoadMoreObserver)

                    } else {
                        //Get more data failed
                        Toast.makeText(this@MainActivity, R.string.cant_connect_error, Toast.LENGTH_LONG).show()
                        movieAdapter.loadMoreFail()

                    }
                }
            } else {
                if (movieAdapter.isLoading) movieAdapter.loadMoreComplete()
                movieAdapter.setEnableLoadMore(false)
            }
        } else {
            if (movieAdapter.isLoading) movieAdapter.loadMoreComplete()
            movieAdapter.setEnableLoadMore(false)
        }
    }


    override fun onRefresh() {

        when (activityState) {

            ACTIVITY_STATE.EDITOR_CHOICE, ACTIVITY_STATE.MOVIE_LIST, ACTIVITY_STATE.LOST_CONNECTION -> {
                if (hasConnection()) {
                    viewModel.onRefreshObservableNetwork.subscribe(getListMoviesObserver)
                    //refreshBannerAd()
                } else {
                    showConnectionError()
                }
            }
            ACTIVITY_STATE.FAVORITES -> {
                viewModel.favoritesObservable.subscribe(getListMoviesObserver)
            }
            ACTIVITY_STATE.HISTORY -> {
                viewModel.historyObservable.subscribe(getListMoviesObserver)
            }
        }
    }

    private fun updateDataList(list: List<Movie>, clear: Boolean) {

        if (clear)
            movieAdapter.clear()

        movieAdapter.addData(list)
        movieListRecyclerView!!.recycledViewPool.clear()

        if (movieAdapter.data.size != 0)
            movieListRecyclerView!!.scrollToPosition(0)

        if (activityState == ACTIVITY_STATE.EDITOR_CHOICE || activityState == ACTIVITY_STATE.MOVIE_LIST) {
            if (viewModel.requestQuery != null && viewModel.requestQuery!!.offset() < viewModel.requestQuery!!.queryAmount())
                movieAdapter.setEnableLoadMore(true)
        }
    }

    internal fun showConnectionError() {
        activityState = ACTIVITY_STATE.LOST_CONNECTION
        Toast.makeText(applicationContext, getText(R.string.cant_connect_error), Toast.LENGTH_SHORT).show()
    }

    internal fun hasConnection(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val ni = cm.activeNetworkInfo
        return ni != null && ni.isConnected
    }


    fun performInitialRequest() {
        if (hasConnection()) {
            changeState(ACTIVITY_STATE.EDITOR_CHOICE)


            if (viewModel.requestQuery != null) {

                viewModel.observable?.subscribe(updateListConsumer)
            } else {
                showProgressBar()

                viewModel.getRequestQueryCompletable(SiteWorker.EDITOR_CHOICE_QUERY)
                        .subscribe(getListMoviesObserver)
            }
        } else
            showConnectionError()
    }

    private fun changeState(newState: ACTIVITY_STATE) {
        when (newState) {
            ACTIVITY_STATE.EDITOR_CHOICE -> {
                activityState = ACTIVITY_STATE.EDITOR_CHOICE
                searchMenuItem.isVisible = true
                sortingMenuItem.isVisible = true
                clearMenuItem.isVisible = false
            }
            ACTIVITY_STATE.MOVIE_LIST -> {
                activityState = ACTIVITY_STATE.MOVIE_LIST
                searchMenuItem.isVisible = true
                sortingMenuItem.isVisible = true
                clearMenuItem.isVisible = false

            }
            ACTIVITY_STATE.LOST_CONNECTION -> {
                activityState = ACTIVITY_STATE.LOST_CONNECTION

            }
            ACTIVITY_STATE.HISTORY -> {
                activityState = ACTIVITY_STATE.HISTORY
                searchMenuItem.isVisible = false
                sortingMenuItem.isVisible = false
                clearMenuItem.isVisible = true

            }
            ACTIVITY_STATE.FAVORITES -> {
                activityState = ACTIVITY_STATE.FAVORITES
                searchMenuItem.isVisible = false
                sortingMenuItem.isVisible = false
                clearMenuItem.isVisible = true

            }
        }
    }


    private fun firstStartDisclaimer() {
        val mSettings = getSharedPreferences(Settings.APP_PREFERENCES, Context.MODE_PRIVATE)
        var isFirstRun = mSettings.getBoolean(Settings.APP_FIRST_RUN, true)
        val currentVersion = BuildConfig.VERSION_CODE
        val savedVersion = mSettings.getInt(Settings.VERSION_CODE, 0)

        if (isFirstRun) {
            val editor = mSettings.edit()
            editor.putBoolean(Settings.APP_FIRST_RUN, false)
            editor.apply()

            val disclaimerFragment = DisclaimerFragment()
            disclaimerFragment.show(supportFragmentManager, "disclaimer")
        }

        if (currentVersion != savedVersion) {
            val editor = mSettings.edit()
            editor.putInt(Settings.VERSION_CODE, currentVersion)
            editor.apply()

            val disclaimerFragment = DisclaimerFragment.newInstance(getString(R.string.changelog))
            disclaimerFragment.show(supportFragmentManager, "changelog")
        }
    }
}
