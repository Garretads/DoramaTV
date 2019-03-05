package ru.garretech.garred.doramatv;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
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

public class MainActivity extends AppCompatActivity implements MovieTitleAdapter.OnMovieListener, MenuItem.OnActionExpandListener, NavigationView.OnNavigationItemSelectedListener {
    @BindView(R.id.movie_list) RecyclerView mRecyclerView;
    @BindView(R.id.toolbar_actionbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.ad_view) PublisherAdView adView;

    private RecyclerView.LayoutManager mLayoutManager;
    private SearchView searchView;
    private MovieTitleAdapter mMovieAdapter;
    private List<Movie> editorChoiceList;
    private ArrayList<Movie> mMovieList;
    private int GENRES_CODE = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        // Ads block
        //MobileAds.initialize(this, "ca-app-pub-1453289229022558~3850061937");
        PublisherAdRequest adRequest = new PublisherAdRequest.Builder().build();

        // Start loading the ad in the background.
        adView.loadAd(adRequest);


        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);

        editorChoiceList = new ArrayList<>();
        try {
            editorChoiceList = SiteWorker.getEditorChoiceMoviesList();
        } catch (InterruptedException e) {
            Toast.makeText(getApplicationContext(),getText(R.string.cant_connect_error),Toast.LENGTH_SHORT).show();
        } catch (ExecutionException e) {
            Toast.makeText(getApplicationContext(),getText(R.string.cant_connect_error),Toast.LENGTH_SHORT).show();
        }

        mMovieList = new ArrayList<>(editorChoiceList);
        mMovieAdapter = new MovieTitleAdapter(this, mMovieList,this);
        mRecyclerView.setAdapter(mMovieAdapter);


    }

    // Слушатель нажатий на элементы RecyclerView из класса MovieTitleAdapter
    @Override
    public void onItemClick(int position) {
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
                    updateDataList(SiteWorker.getEditorChoiceMoviesList());
                    setTitle("Выбор редакции");
                    break;
                }
                case R.id.nav_new: {
                    updateDataList(SiteWorker.getMovieList(SiteWorker.NEW_MOVIES));
                    setTitle("Новинки");
                    break;
                }
                case R.id.nav_best: {
                    updateDataList(SiteWorker.getMovieList(SiteWorker.BEST_MOVIES));
                    setTitle("Лучшие");
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
        mMovieAdapter.mMovieDataset.clear();
        mMovieAdapter.mMovieDataset.addAll(list);
        mMovieAdapter.notifyDataSetChanged();
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
                // A contact was picked.  Here we will just display it
                // to the user.
                try {
                    String resultPrefix = data.getStringExtra("genrePrefix");
                    String genreName = data.getStringExtra("genreName");
                    updateDataList(SiteWorker.getMovieList(resultPrefix));
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
}
