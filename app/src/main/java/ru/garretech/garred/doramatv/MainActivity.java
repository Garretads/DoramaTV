package ru.garretech.garred.doramatv;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
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
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import butterknife.BindView;
import butterknife.ButterKnife;
import ru.garretech.garred.doramatv.tools.SiteWorker;

public class MainActivity extends AppCompatActivity implements QuickAdapter.OnItemClickListener,MenuItem.OnActionExpandListener, NavigationView.OnNavigationItemSelectedListener {
    @BindView(R.id.movie_list) RecyclerView mRecyclerView;
    @BindView(R.id.toolbar_actionbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.ad_view) PublisherAdView adView;
    @BindView(R.id.progressBar) ProgressBar progressBar;

    private RecyclerView.LayoutManager mLayoutManager;
    private SearchView searchView;
    private QuickAdapter newMovieAdapter;
    private List<Movie> editorChoiceList;
    private ArrayList<Movie> mMovieList;
    private int GENRES_CODE = 15;
    ActionBarDrawerToggle toggle;
    ProgressBottomSheet progressBottomSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        progressBottomSheet = new ProgressBottomSheet();
        navigationView.setNavigationItemSelectedListener(this);
        setSupportActionBar(toolbar);
        ConnectivityManager conMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        // Ads block
        //MobileAds.initialize(this, "ca-app-pub-1453289229022558~3850061937");
        PublisherAdRequest adRequest = new PublisherAdRequest.Builder().build();

        // Start loading the ad in the background.
        adView.loadAd(adRequest);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);

        editorChoiceList = new ArrayList<>();
        if (conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            try {
                editorChoiceList = SiteWorker.getEditorChoiceMoviesList();
                mMovieList = new ArrayList<>(editorChoiceList);
                newMovieAdapter = new QuickAdapter(R.layout.fragment_movie, mMovieList);
                mRecyclerView.setAdapter(newMovieAdapter);
                newMovieAdapter.setOnItemClickListener(this);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
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
            public boolean onQueryTextSubmit(String query) {

                // Поиск дорам
                try {
                    updateDataList(SiteWorker.getMoviesListFromSearch(query));
                    setTitle(query);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                if( ! searchView.isIconified()) {
                    searchView.setIconified(true);
                }
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
    public boolean onMenuItemActionExpand(MenuItem menuItem) {
        return false;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem menuItem) {
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        try {
            switch (item.getItemId()) {
                case R.id.nav_editorchoice: {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                List<Movie> list = SiteWorker.getEditorChoiceMoviesList();
                                updateDataList(list);
                                if (progressBottomSheet.isVisible())
                                    progressBottomSheet.dismiss();
                                setTitle(getString(R.string.editor_choice_title));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                    },1000);
                    newMovieAdapter.clearItems();
                    progressBottomSheet.show(getSupportFragmentManager(),"progressBar");
                    break;
                }
                case R.id.nav_new: {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                List<Movie> list = SiteWorker.getMovieList(SiteWorker.NEW_MOVIES,0,0);
                                updateDataList(list);
                                if (progressBottomSheet.isVisible())
                                    progressBottomSheet.dismiss();
                                setTitle(getString(R.string.new_movie_title));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                    },1000);
                    newMovieAdapter.clearItems();
                    progressBottomSheet.show(getSupportFragmentManager(),"progressBar");
                    break;
                }
                case R.id.nav_best: {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                List<Movie> list = SiteWorker.getMovieList(SiteWorker.BEST_MOVIES,0,0);
                                updateDataList(list);
                                if (progressBottomSheet.isVisible())
                                    progressBottomSheet.dismiss();
                                setTitle(getString(R.string.best_movie_title));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                    },1000);
                    newMovieAdapter.clearItems();
                    progressBottomSheet.show(getSupportFragmentManager(),"progressBar");
                    break;
                }
                case R.id.nav_genres: {
                    JSONArray jsonArray = SiteWorker.getGenresList();
                    Intent intent = new Intent(MainActivity.this,GenresActivity.class);
                    intent.putExtra("genres",jsonArray.toString());
                    startActivityForResult(intent,GENRES_CODE);
                    break;
                }
                case R.id.nav_favourites: {

                    break;
                }
                case R.id.nav_history: {

                    break;
                }
                case R.id.nav_about: {

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

        drawer.closeDrawer(GravityCompat.START);
        return true;

    }

    void updateDataList(List<Movie> list) {
        newMovieAdapter.setItems(list);
        mRecyclerView.scrollToPosition(0);
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
                try {
                    String resultPrefix = data.getStringExtra("link");
                    String genreName = data.getStringExtra("name");
                    updateDataList(SiteWorker.getMovieList(resultPrefix,0,0));
                    setTitle(genreName.substring(0,1).toUpperCase()+genreName.substring(1));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    /** Called when returning to the activity */
    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }

    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }


    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        Movie selectedMovie = mMovieList.get(position);
        Intent intent = new Intent(MainActivity.this, MovieAboutActivity.class);
        // Ща будем делать json объект
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("title",selectedMovie.title);
            jsonObject.put("genres",selectedMovie.genres.toString());
            jsonObject.put("movieImageIMG",selectedMovie.movieImageURL);
            jsonObject.put("movieURL",selectedMovie.URL);
            jsonObject.put("access_token",Settings.access_token());
            jsonObject.put("isSerial",selectedMovie.isSerial);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        intent.putExtra("movieInfo",jsonObject.toString());
        startActivity(intent);
    }

    void showConnectionError() {
        Toast.makeText(getApplicationContext(), getText(R.string.cant_connect_error), Toast.LENGTH_SHORT).show();
    }
}
