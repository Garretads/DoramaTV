package com.example.garred.doramatv;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MovieTitleAdapter.OnMovieListener {

    @BindView(R.id.movie_list) RecyclerView mRecyclerView;
    @BindView(R.id.main_toolbar) Toolbar mainToolbar;

    private RecyclerView.LayoutManager mLayoutManager;
    private SiteWorker siteWorker;

    private List<Movie> mMovieList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        siteWorker = new SiteWorker(getApplicationContext());
        try {
            mMovieList = siteWorker.getEditorChoiceMovies();
        } catch (InterruptedException e) {
            Toast.makeText(getApplicationContext(),getText(R.string.cant_connect_error),Toast.LENGTH_SHORT).show();
        } catch (ExecutionException e) {
            Toast.makeText(getApplicationContext(),getText(R.string.cant_connect_error),Toast.LENGTH_SHORT).show();
        }
        //setInitialData();
        MovieTitleAdapter mMovieAdapter = new MovieTitleAdapter(this,mMovieList,this);
        mRecyclerView.setAdapter(mMovieAdapter);

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
        } catch (JSONException e) {
            e.printStackTrace();
        }

        intent.putExtra("movieInfo",jsonObject.toString());
        startActivity(intent);
    }
}
