package ru.garretech.garred.doramatv;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import butterknife.BindView;
import butterknife.ButterKnife;
import ru.garretech.garred.doramatv.adapters.RecyclerAdapter;
import ru.garretech.garred.doramatv.fragments.CustomLoadMoreView;
import ru.garretech.garred.doramatv.fragments.ProgressBottomSheet;
import ru.garretech.garred.doramatv.model.Movie;
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

    private final int GENRES_CODE = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        //newMovieAdapter.setPreLoadNumber(Settings.max_loaded_in_screen() - 3);

        mRecyclerView.setAdapter(newMovieAdapter);
        newMovieAdapter.setOnItemClickListener(this);
        newMovieAdapter.setOnLoadMoreListener(this,mRecyclerView);
        newMovieAdapter.setEnableLoadMore(false);
        newMovieAdapter.setLoadMoreView(new CustomLoadMoreView());
        mSiteworker = new SiteWorker();


        if (conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        requestQuery = mSiteworker.new RequestQuery(SiteWorker.EDITOR_CHOICE_QUERY);
                        List<Movie> list = requestQuery.getNextQuery();
                        updateDataList(list);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
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

                if (conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED
                        || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {

                                HashMap<String,String> params = new HashMap<>();
                                params.put("q",queryString);

                                requestQuery = mSiteworker.new RequestQuery(SiteWorker.SEARCH_QUERY,SiteWorker.SEARCH_PREFIX,params);
                                List<Movie> list = requestQuery.getNextQuery();
                                updateDataList(list);
                                newMovieAdapter.setEnableLoadMore(true);
                                if (progressBottomSheet.isVisible())
                                    progressBottomSheet.dismiss();
                                setTitle(queryString);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                    }, 1000);
                    newMovieAdapter.clear();
                    progressBottomSheet.show(getSupportFragmentManager(), "progressBar");

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
                Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
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
        if (conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

            try {
                switch (item.getItemId()) {
                    case R.id.nav_editorchoice: {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    requestQuery = mSiteworker.new RequestQuery(SiteWorker.EDITOR_CHOICE_QUERY);
                                    List<Movie> list = requestQuery.getNextQuery();
                                    updateDataList(list);
                                    newMovieAdapter.setEnableLoadMore(false);
                                    if (progressBottomSheet.isVisible())
                                        progressBottomSheet.dismiss();
                                    setTitle(getString(R.string.editor_choice_title));
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, 500);
                        newMovieAdapter.clear();
                        break;
                    }
                    case R.id.nav_new: {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    HashMap<String,String> params = new HashMap<>();
                                    params.put(SiteWorker.NEW_MOVIES_PARAMS[0],SiteWorker.NEW_MOVIES_PARAMS[1]);
                                    requestQuery = mSiteworker.new RequestQuery(SiteWorker.SIMPLE_QUERY,SiteWorker.LIST_PREFIX,params);
                                    List<Movie> list = requestQuery.getNextQuery();
                                    updateDataList(list);
                                    newMovieAdapter.setEnableLoadMore(true);
                                    if (progressBottomSheet.isVisible())
                                        progressBottomSheet.dismiss();
                                    setTitle(getString(R.string.new_movie_title));
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, 1000);
                        newMovieAdapter.clear();
                        progressBottomSheet.show(getSupportFragmentManager(), "progressBar");
                        break;
                    }
                    case R.id.nav_best: {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    requestQuery = mSiteworker.new RequestQuery(SiteWorker.SIMPLE_QUERY,SiteWorker.LIST_PREFIX);
                                    List<Movie> list = requestQuery.getNextQuery();
                                    updateDataList(list);
                                    newMovieAdapter.setEnableLoadMore(true);
                                    if (progressBottomSheet.isVisible())
                                        progressBottomSheet.dismiss();
                                    setTitle(getString(R.string.best_movie_title));
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, 1000);
                        newMovieAdapter.clear();
                        progressBottomSheet.show(getSupportFragmentManager(), "progressBar");
                        break;
                    }

                    case R.id.nav_ongoing: {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    HashMap<String,String> params = new HashMap<>();
                                    params.put(SiteWorker.ONGOING_PARAMS[0],SiteWorker.ONGOING_PARAMS[1]);

                                    requestQuery = mSiteworker.new RequestQuery(SiteWorker.SIMPLE_QUERY,SiteWorker.ONGOING_PREFIX,params);
                                    List<Movie> list = requestQuery.getNextQuery();
                                    updateDataList(list);
                                    newMovieAdapter.setEnableLoadMore(true);
                                    if (progressBottomSheet.isVisible())
                                        progressBottomSheet.dismiss();
                                    setTitle(getString(R.string.ongoing_title));
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, 1000);

                        newMovieAdapter.clear();
                        progressBottomSheet.show(getSupportFragmentManager(), "progressBar");
                        break;
                    }

                    case R.id.nav_random: {

                        final Intent intent = new Intent(MainActivity.this, MovieAboutActivity.class);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                JSONObject jsonObject = null;

                                try {
                                    jsonObject = SiteWorker.getMovieInfo(SiteWorker.SITE_URL + SiteWorker.RANDOM_MOVIE_PREFIX);
                                    jsonObject.put("access_token",Settings.access_token());

                                    intent.putExtra("movie_info",jsonObject.toString());

                                    if (progressBottomSheet.isVisible())
                                        progressBottomSheet.dismiss();

                                    startActivity(intent);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, 500);
                        progressBottomSheet.show(getSupportFragmentManager(), "progressBar");
                        break;
                    }

                    case R.id.nav_genres: {
                        JSONArray jsonArray = SiteWorker.getGenresList();
                        Intent intent = new Intent(MainActivity.this, GenresActivity.class);
                        intent.putExtra("genres", jsonArray.toString());
                        startActivityForResult(intent, GENRES_CODE);
                        break;
                    }
                    case R.id.nav_favourites: {
                        Toast.makeText(getApplicationContext(), "Функция в разработке", Toast.LENGTH_LONG).show();
                        break;
                    }
                    case R.id.nav_history: {
                        Toast.makeText(getApplicationContext(), "Функция в разработке", Toast.LENGTH_LONG).show();
                        break;
                    }
                    case R.id.nav_about: {
                        Intent intent = new Intent(MainActivity.this,AboutApplicationActivity.class);
                        startActivity(intent);
                        break;
                    }
                    default:
                        break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else
            showConnectionError();
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
                            requestQuery = mSiteworker.new RequestQuery(SiteWorker.SIMPLE_QUERY,resultPrefix);
                            List<Movie> list = requestQuery.getNextQuery();
                            updateDataList(list);
                            newMovieAdapter.setEnableLoadMore(true);
                            if (progressBottomSheet.isVisible())
                                progressBottomSheet.dismiss();
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

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                    JSONObject jsonObject;

                    try {
                        jsonObject = SiteWorker.getMovieInfo(selectedMovie.getURL());
                        jsonObject.put("title",selectedMovie.getTitle());
                        jsonObject.put("genres",selectedMovie.getGenres().toString());
                        jsonObject.put("image_url",selectedMovie.getMovieImageURL());
                        jsonObject.put("url",selectedMovie.getURL());
                        jsonObject.put("access_token",Settings.access_token());

                        intent.putExtra("movie_info",jsonObject.toString());

                        if (progressBottomSheet.isVisible())
                            progressBottomSheet.dismiss();

                        startActivity(intent);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
            }
        }, 500);
        progressBottomSheet.show(getSupportFragmentManager(), "progressBar");

    }


    @Override
    public void onLoadMoreRequested() {

        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (requestQuery.offset() >= requestQuery.queryAmount()) {
                    //Data was fully loaded.
                    newMovieAdapter.loadMoreEnd();
                } else {
                    if (hasConnection) {
                        //Successfully got more data
                        try {
                            newMovieAdapter.addData(requestQuery.getNextQuery());
                            newMovieAdapter.loadMoreComplete();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //mCurrentCounter = newMovieAdapter.getData().size();
                    } else {
                        //Get more data failed
                        hasConnection = true;
                        Toast.makeText(MainActivity.this, R.string.cant_connect_error, Toast.LENGTH_LONG).show();
                        newMovieAdapter.loadMoreFail();

                    }
                }
            }

        }, 1000);
    }


    @Override
    public void onRefresh() {
        swipeContainer.setRefreshing(false);
        if (requestQuery != null) {
            requestQuery.resetOffset();

            try {
                List<Movie> list = requestQuery.getNextQuery();
                updateDataList(list);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } else {

            try {
                requestQuery = mSiteworker.new RequestQuery(SiteWorker.EDITOR_CHOICE_QUERY);
                List<Movie> list = requestQuery.getNextQuery();
                updateDataList(list);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }



    private void updateDataList(List<Movie> list) {

        newMovieAdapter.addAll(list);
        mRecyclerView.getRecycledViewPool().clear();
        mRecyclerView.scrollToPosition(0);
    }

    void showConnectionError() {
        hasConnection = false;
        Toast.makeText(getApplicationContext(), getText(R.string.cant_connect_error), Toast.LENGTH_SHORT).show();
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

      /*   @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mDivider != null) {
                int dividerLeft = parent.getPaddingLeft();
                int dividerRight = parent.getWidth() - parent.getPaddingRight();

                int childCount = parent.getChildCount();
                for (int i = 0; i < childCount - 1; i++) {
                    View child = parent.getChildAt(i);

                    RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                    int dividerTop = child.getBottom() + params.bottomMargin;
                    int dividerBottom = dividerTop + mDivider.getIntrinsicHeight();

                    mDivider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom);
                    mDivider.draw(c);
                }
            } else {
                super.onDraw(c, parent, state);
            }
        }*/
    }
}
