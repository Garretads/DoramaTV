package ru.garretech.garred.doramatv.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import ru.garretech.garred.doramatv.R;
import ru.garretech.garred.doramatv.Settings;
import ru.garretech.garred.doramatv.adapters.RecyclerAdapter;
import ru.garretech.garred.doramatv.database.AppDataSource;
import ru.garretech.garred.doramatv.fragments.CustomLoadMoreView;
import ru.garretech.garred.doramatv.fragments.ProgressBottomSheet;
import ru.garretech.garred.doramatv.model.Movie;
import ru.garretech.garred.doramatv.tools.ImageDownloader;
import ru.garretech.garred.doramatv.tools.SiteWorker;

public class MainActivity extends AppCompatActivity implements RecyclerAdapter.OnItemClickListener,
                                                                MenuItem.OnActionExpandListener,
                                                                NavigationView.OnNavigationItemSelectedListener,
                                                                BaseQuickAdapter.RequestLoadMoreListener,
                                                                SwipeRefreshLayout.OnRefreshListener {
    @BindView(R.id.movie_list) RecyclerView mRecyclerView;
    @BindView(R.id.toolbar_actionbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.progressBar) ProgressBar progressBar;
    @BindView(R.id.swipe_container) SwipeRefreshLayout swipeContainer;

    private SearchView searchView;
    private RecyclerAdapter newMovieAdapter;
    private ProgressBottomSheet progressBottomSheet;
    private ConnectivityManager conMgr;
    private SiteWorker mSiteworker;
    private SiteWorker.RequestQuery requestQuery;
    private Boolean hasConnection = true;
    private AppDataSource appDataSource;
    private Observable<List<Movie>> observable;
    private CompletableObserver getCachedMoviesObserver;

    private final int GENRES_CODE = 15;

    enum ACTIVITY_STATE {
        ONLINE,
        CACHED
    }

    private ACTIVITY_STATE activityState = ACTIVITY_STATE.CACHED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSiteworker = new SiteWorker();
        appDataSource = new AppDataSource(getApplicationContext());
        getCachedMoviesObserver = new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }
            @Override
            public void onComplete() {
                observable
                        .subscribeOn(Schedulers.io())
                        .map( movies -> {
                            for (Movie movie : movies) {
                                Bitmap image;
                                try {
                                    image = SiteWorker.getCachedImage(getApplicationContext(),movie.getMovieImageURL());
                                } catch (FileNotFoundException e) {
                                    ImageDownloader imageDownloader = new ImageDownloader();
                                    image = imageDownloader.execute(movie.getMovieImageURL()).get();
                                    SiteWorker.saveImage(getApplicationContext(), image, movie.getMovieImageURL());
                                }
                                movie.setImage(image);
                            }
                            return movies;
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<List<Movie>>() {
                            @Override
                            public void accept(List<Movie> movies) throws Exception {
                                requestQuery = null;
                                updateDataList(movies);
                                setTitle(getString(R.string.action_favorite));
                            }
                        });
                Log.d("Task", "get favorites completable completed");
            }

            @Override
            public void onError(Throwable e) {

            }
        };


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        conMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        ButterKnife.bind(this);
        progressBottomSheet = new ProgressBottomSheet();
        navigationView.setNavigationItemSelectedListener(this);
        swipeContainer.setOnRefreshListener(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Drawable mDivider = getApplicationContext().getDrawable(R.drawable.line_divider);
            CustomDivider mDividerItemDecoration = new CustomDivider(mDivider, 10, 10);
            mRecyclerView.addItemDecoration(mDividerItemDecoration);
        }


        mRecyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        });
        mRecyclerView.setHasFixedSize(true);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        newMovieAdapter = new RecyclerAdapter(R.layout.fragment_movie, new ArrayList<Movie>());
        mRecyclerView.setAdapter(newMovieAdapter);
        newMovieAdapter.setOnItemClickListener(this);
        newMovieAdapter.setOnLoadMoreListener(this,mRecyclerView);
        newMovieAdapter.setEnableLoadMore(false);
        newMovieAdapter.setLoadMoreView(new CustomLoadMoreView());


        if (hasConnection()) {
            activityState = ACTIVITY_STATE.ONLINE;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        requestQuery = mSiteworker.new RequestQuery(getApplicationContext(),SiteWorker.EDITOR_CHOICE_QUERY);
                        List<Movie> list = requestQuery.getNextQuery();
                        updateDataList(list);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        showConnectionError();
                    } catch (NullPointerException e) {
                        showConnectionError();
                    }
                }
            },0);
        } else
            showConnectionError();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main,menu);
        final MenuItem myActionMenuItem = menu.findItem( R.id.action_search);
        searchView = (SearchView) myActionMenuItem.getActionView();

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String queryString) {

                // Поиск дорам
                if (hasConnection()) {
                    activityState = ACTIVITY_STATE.ONLINE;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {

                                HashMap<String,String> params = new HashMap<>();
                                params.put("q",queryString);

                                requestQuery = mSiteworker.new RequestQuery(getApplicationContext(),SiteWorker.SEARCH_QUERY,SiteWorker.SEARCH_PREFIX,params);
                                List<Movie> list = requestQuery.getNextQuery();
                                updateDataList(list);
                                if (progressBottomSheet.isVisible())
                                    progressBottomSheet.dismissAllowingStateLoss();
                                setTitle("Поиск: " + queryString);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                showConnectionError();
                            } catch (NullPointerException e) {
                                showConnectionError();
                            }
                        }
                    }, 1000);
                    newMovieAdapter.clear();
                    if (!progressBottomSheet.isAdded()) {
                        progressBottomSheet.show(getSupportFragmentManager(), "progressBar");
                    }

                    if (!searchView.isIconified()) {
                        searchView.setIconified(true);
                    }
                } else
                    showConnectionError();

                myActionMenuItem.collapseActionView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings: {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            }
        }
        return true;
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem menuItem) {
        return false;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem menuItem) {
        return false;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_editorchoice: {
                if (!hasConnection()) {
                    showConnectionError();
                    break;
                }
                activityState = ACTIVITY_STATE.ONLINE;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            requestQuery = mSiteworker.new RequestQuery(getApplicationContext(),SiteWorker.EDITOR_CHOICE_QUERY);
                            List<Movie> list = requestQuery.getNextQuery();
                            updateDataList(list);
                            if (progressBottomSheet.isVisible())
                                progressBottomSheet.dismissAllowingStateLoss();
                            setTitle(getString(R.string.editor_choice_title));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            showConnectionError();
                        } catch (NullPointerException e) {
                            showConnectionError();
                        }
                    }
                }, 500);
                newMovieAdapter.clear();
                break;
            }
            case R.id.nav_new: {
                if (!hasConnection()) {
                    showConnectionError();
                    break;
                }
                activityState = ACTIVITY_STATE.ONLINE;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            HashMap<String,String> params = new HashMap<>();
                            params.put(SiteWorker.NEW_MOVIES_PARAMS[0],SiteWorker.NEW_MOVIES_PARAMS[1]);
                            requestQuery = mSiteworker.new RequestQuery(getApplicationContext(),SiteWorker.SIMPLE_QUERY,SiteWorker.LIST_PREFIX,params);
                            List<Movie> list = requestQuery.getNextQuery();
                            updateDataList(list);
                            if (progressBottomSheet.isVisible())
                                progressBottomSheet.dismissAllowingStateLoss();
                            setTitle(getString(R.string.new_movie_title));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            showConnectionError();
                        } catch (NullPointerException e) {
                            showConnectionError();
                        }
                    }
                }, 1000);
                newMovieAdapter.clear();
                if (!progressBottomSheet.isAdded()) {
                    progressBottomSheet.show(getSupportFragmentManager(), "progressBar");
                }
                break;
            }
            case R.id.nav_best: {
                if (!hasConnection()) {
                    showConnectionError();
                    break;
                }
                activityState = ACTIVITY_STATE.ONLINE;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            requestQuery = mSiteworker.new RequestQuery(getApplicationContext(),SiteWorker.SIMPLE_QUERY,SiteWorker.LIST_PREFIX);
                            List<Movie> list = requestQuery.getNextQuery();
                            updateDataList(list);
                            if (progressBottomSheet.isVisible())
                                progressBottomSheet.dismissAllowingStateLoss();
                            setTitle(getString(R.string.best_movie_title));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                           showConnectionError();
                        } catch (NullPointerException e) {
                            showConnectionError();
                        }
                    }
                }, 1000);
                newMovieAdapter.clear();
                if (!progressBottomSheet.isAdded()) {
                    progressBottomSheet.show(getSupportFragmentManager(), "progressBar");
                }
                break;
            }

            case R.id.nav_ongoing: {
                if (!hasConnection()) {
                    showConnectionError();
                    break;
                }
                activityState = ACTIVITY_STATE.ONLINE;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            HashMap<String,String> params = new HashMap<>();
                            params.put(SiteWorker.ONGOING_PARAMS[0],SiteWorker.ONGOING_PARAMS[1]);

                            requestQuery = mSiteworker.new RequestQuery(getApplicationContext(),SiteWorker.SIMPLE_QUERY,SiteWorker.ONGOING_PREFIX,params);
                            List<Movie> list = requestQuery.getNextQuery();
                            updateDataList(list);
                            if (progressBottomSheet.isVisible())
                                progressBottomSheet.dismissAllowingStateLoss();
                            setTitle(getString(R.string.ongoing_title));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            showConnectionError();
                        } catch (NullPointerException e) {
                            showConnectionError();
                        }
                    }
                }, 1000);

                newMovieAdapter.clear();
                if (!progressBottomSheet.isAdded()) {
                    progressBottomSheet.show(getSupportFragmentManager(), "progressBar");
                }
                break;
            }
            case R.id.nav_random: {
                if (!hasConnection()) {
                    showConnectionError();
                    break;
                }

                final Intent intent = new Intent(MainActivity.this, MovieAboutActivity.class);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        JSONObject jsonObject = null;

                        try {
                            jsonObject = SiteWorker.getMovieInfo(SiteWorker.SITE_URL + SiteWorker.RANDOM_MOVIE_PREFIX);
                            jsonObject.put("access_token", Settings.access_token());

                            intent.putExtra("movie_info",jsonObject.toString());

                            if (progressBottomSheet.isVisible())
                                progressBottomSheet.dismissAllowingStateLoss();

                            startActivity(intent);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            showConnectionError();
                        } catch (NullPointerException e) {
                            showConnectionError();
                        }
                    }
                }, 500);
                if (!progressBottomSheet.isAdded()) {
                    progressBottomSheet.show(getSupportFragmentManager(), "progressBar");
                }
                break;
            }

            case R.id.nav_genres: {
                if (!hasConnection()) {
                    showConnectionError();
                    break;
                }
                activityState = ACTIVITY_STATE.ONLINE;
                try {
                    JSONArray jsonArray = SiteWorker.getGenresList();
                    Intent intent = new Intent(MainActivity.this, GenresActivity.class);
                    intent.putExtra("genres", jsonArray.toString());
                    startActivityForResult(intent, GENRES_CODE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    showConnectionError();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    showConnectionError();
                }
                break;
            }
            case R.id.nav_favourites: {

                //List<Movie> favoritesMovies = appDataSource.getListOfFavorites();
                activityState = ACTIVITY_STATE.CACHED;
                Completable.fromCallable(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        observable = appDataSource.getListOfFavorites();
                        return null;
                    }
                }).subscribeOn(Schedulers.io())
                        .subscribe(getCachedMoviesObserver);

                //Toast.makeText(getApplicationContext(), "Функция в разработке", Toast.LENGTH_LONG).show();
                break;
            }
            case R.id.nav_history: {
                Toast.makeText(getApplicationContext(), "Функция в разработке", Toast.LENGTH_LONG).show();
                break;
            }
            case R.id.nav_about: {
                Intent intent = new Intent(MainActivity.this, AboutApplicationActivity.class);
                startActivity(intent);
                break;
            }
            default:
                break;
            }
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GENRES_CODE) {
            if (resultCode == RESULT_OK) {
                final String resultPrefix = data.getStringExtra("link");
                final String genreName = data.getStringExtra("name");
                progressBottomSheet = new ProgressBottomSheet();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            requestQuery = mSiteworker.new RequestQuery(getApplicationContext(),SiteWorker.SIMPLE_QUERY,resultPrefix);
                            List<Movie> list = requestQuery.getNextQuery();
                            updateDataList(list);
                            if (progressBottomSheet.isVisible())
                                progressBottomSheet.dismissAllowingStateLoss();
                            setTitle(genreName.substring(0,1).toUpperCase()+genreName.substring(1));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                },1000);
                newMovieAdapter.clear();
            }
        }
    }


    @Override
    public void onPause() {
        /*if (adView != null) {
            adView.pause();
        }*/
        super.onPause();
    }

    /** Called when returning to the activity */
    @Override
    public void onResume() {
        super.onResume();
        /*if (adView != null) {
            adView.resume();
        }*/
    }

    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        /*if (adView != null) {
            adView.destroy();
        } */
        super.onDestroy();
    }


    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        final Movie selectedMovie = newMovieAdapter.getData().get(position);
        final Intent intent = new Intent(MainActivity.this, MovieAboutActivity.class);
        if (hasConnection()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    JSONObject jsonObject;

                    try {
                        jsonObject = SiteWorker.getMovieInfo(selectedMovie.getURL());
                        /*
                        * this.title = title;
                            this.URL = movieURL;
                            this.genres = genres;
                            this.movieImageURL = movieImageURL;
                        * */
                        selectedMovie.setTitle(jsonObject.getString("title"));
                        selectedMovie.setInitialSeries(jsonObject.getString("initial_series"));
                        selectedMovie.setProductionCountry(jsonObject.getString("production"));
                        selectedMovie.setSeriesNumber(jsonObject.getString("series_number"));
                        selectedMovie.setDuration(jsonObject.getString("duration"));
                        selectedMovie.setDescription(jsonObject.getString("description"));
                        selectedMovie.setProductionYear(jsonObject.getString("age"));
                        //jsonObject.put("title", selectedMovie.getTitle());
                        //jsonObject.put("genres", selectedMovie.getGenres().toString());
                        //jsonObject.put("image_url", selectedMovie.getMovieImageURL());
                        //jsonObject.put("url", selectedMovie.getURL());
                        jsonObject.put("access_token", Settings.access_token());

                        Bundle bundle = new Bundle();

                        bundle.putSerializable("movie",selectedMovie);

                        intent.putExtra("bundle",bundle);
                        intent.putExtra("movie_info", jsonObject.toString());

                        if (progressBottomSheet.isVisible())
                            progressBottomSheet.dismiss();

                        startActivity(intent);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        showConnectionError();
                    } catch (NullPointerException e) {
                        showConnectionError();
                    }
                }
            }, 500);
            if (!progressBottomSheet.isAdded()) {
                progressBottomSheet.show(getSupportFragmentManager(), "progressBar");
            }
        } else
            showConnectionError();

    }


    @Override
    public void onLoadMoreRequested() {
        if (activityState == ACTIVITY_STATE.ONLINE) {
            if (requestQuery != null) {
                mRecyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (requestQuery.offset() >= requestQuery.queryAmount()) {
                            // Все данные загружены
                            newMovieAdapter.setEnableLoadMore(false);
                        } else {
                            if (hasConnection) {
                                try {
                                    newMovieAdapter.addData(requestQuery.getNextQuery());
                                    newMovieAdapter.loadMoreComplete();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    showConnectionError();
                                } catch (NullPointerException e) {
                                    showConnectionError();
                                }
                            } else {
                                //Get more data failed
                                hasConnection = true;
                                Toast.makeText(MainActivity.this, R.string.cant_connect_error, Toast.LENGTH_LONG).show();
                                newMovieAdapter.loadMoreFail();

                            }
                        }
                    }

                }, 1000);
            } else
                newMovieAdapter.setEnableLoadMore(false);
        } else
            newMovieAdapter.setEnableLoadMore(false);
    }


    @Override
    public void onRefresh() {
        swipeContainer.setRefreshing(false);
        if (activityState == ACTIVITY_STATE.ONLINE) {
            if (requestQuery != null) {
                requestQuery.resetOffset();

                try {
                    List<Movie> list = requestQuery.getNextQuery();
                    updateDataList(list);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    showConnectionError();
                } catch (NullPointerException e) {
                    showConnectionError();
                }

            } else {

                try {
                    requestQuery = mSiteworker.new RequestQuery(getApplicationContext(), SiteWorker.EDITOR_CHOICE_QUERY);
                    List<Movie> list = requestQuery.getNextQuery();
                    updateDataList(list);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    showConnectionError();
                } catch (NullPointerException e) {
                    showConnectionError();
                }

            }
        } else {
            activityState = ACTIVITY_STATE.CACHED;
            Completable.fromCallable(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    observable = appDataSource.getListOfFavorites();
                    return null;
                }
            }).subscribeOn(Schedulers.io())
            .subscribe(getCachedMoviesObserver);
        }
    }


    private void updateDataList(List<Movie> list) {

        newMovieAdapter.addAll(list);
        mRecyclerView.getRecycledViewPool().clear();
        mRecyclerView.scrollToPosition(0);

        if (requestQuery != null && requestQuery.offset() < requestQuery.queryAmount())
            newMovieAdapter.setEnableLoadMore(true);
    }

    void showConnectionError() {
        hasConnection = false;
        Toast.makeText(getApplicationContext(), getText(R.string.cant_connect_error), Toast.LENGTH_SHORT).show();
    }

    Boolean hasConnection() {
        if (conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            return true;
        } else
            return false;
    }



    class CustomDivider extends RecyclerView.ItemDecoration {
        final Drawable mDivider;
        final int topOffset;
        final int bottomOffset;

       CustomDivider(Drawable divider,int topOffset, int bottomOffset) {
           mDivider = divider;
           this.topOffset = topOffset;
           this.bottomOffset = bottomOffset;
       }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);

            outRect.top = topOffset;
            outRect.bottom = bottomOffset;
        }

    }
}
