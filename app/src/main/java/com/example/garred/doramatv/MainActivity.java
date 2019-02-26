package com.example.garred.doramatv;

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
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MovieTitleAdapter.OnMovieListener, MenuItem.OnActionExpandListener, NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.movie_list) RecyclerView mRecyclerView;
    @BindView(R.id.toolbar_actionbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;

    private RecyclerView.LayoutManager mLayoutManager;
    private SiteWorker siteWorker;
    private SearchView searchView;
    MovieTitleAdapter mMovieAdapter;

    private List<Movie> editorChoiceList = new ArrayList<>();
    ArrayList<Movie> mMovieList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        siteWorker = new SiteWorker(getApplicationContext());
        try {
            editorChoiceList = siteWorker.getEditorChoiceMovies();
        } catch (InterruptedException e) {
            Toast.makeText(getApplicationContext(),getText(R.string.cant_connect_error),Toast.LENGTH_SHORT).show();
        } catch (ExecutionException e) {
            Toast.makeText(getApplicationContext(),getText(R.string.cant_connect_error),Toast.LENGTH_SHORT).show();
        }
        mMovieList = new ArrayList<>(editorChoiceList);
        mMovieAdapter = new MovieTitleAdapter(this, mMovieList,this);
        mRecyclerView.setAdapter(mMovieAdapter);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);

    }

    @Override
    public void onMovieClick(int position) {
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
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                // Метод поиска дорам
                Toast.makeText(getApplicationContext(),query,Toast.LENGTH_SHORT).show();
                try {
                    List<Movie> movieList = SiteWorker.searchMovies(query);
                    mMovieAdapter.mMovieDataset.clear();
                    mMovieAdapter.mMovieDataset.addAll(movieList);
                    mMovieAdapter.notifyDataSetChanged();
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
        List<Movie> movieList = null;
        switch (item.getItemId()) {
            case R.id.nav_editorchoice: {
                try {
                    movieList = SiteWorker.getEditorChoiceMovies();
                    mMovieAdapter.mMovieDataset.clear();
                    mMovieAdapter.mMovieDataset.addAll(movieList);
                    mMovieAdapter.notifyDataSetChanged();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                break;
            }
            case R.id.nav_new: {
                try {
                    movieList = SiteWorker.getNewMovies();
                    mMovieAdapter.mMovieDataset.clear();
                    mMovieAdapter.mMovieDataset.addAll(movieList);
                    mMovieAdapter.notifyDataSetChanged();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                break;
            }
            case R.id.nav_best: {
                try {
                    movieList = SiteWorker.getBestMovies();
                    mMovieAdapter.mMovieDataset.clear();
                    mMovieAdapter.mMovieDataset.addAll(movieList);
                    mMovieAdapter.notifyDataSetChanged();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                break;
            }
            case R.id.nav_genres: {

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
}
