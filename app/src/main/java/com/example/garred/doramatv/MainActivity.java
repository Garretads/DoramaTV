package com.example.garred.doramatv;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MovieTitleAdapter.OnMovieListener {

    @BindView(R.id.movie_list) RecyclerView mRecyclerView;
    @BindView(R.id.main_toolbar) Toolbar mainToolbar;

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SiteWorker siteWorker;
    //private RecyclerView mRecyclerView;

    private List<Movie> mMovieList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //mRecyclerView = findViewById(R.id.movie_list);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        siteWorker = new SiteWorker(getApplicationContext());
        //setInitialData();
        MovieTitleAdapter mMovieAdapter = new MovieTitleAdapter(this,siteWorker.movieList,this);
        mRecyclerView.setAdapter(mMovieAdapter);

    }

    @Override
    public void onMovieClick(int position) {
        Movie selectedMovie = siteWorker.movieList.get(position);
        Intent intent = new Intent(MainActivity.this, MovieAboutActivity.class);

        intent.putExtra("title",selectedMovie.title);
        intent.putExtra("genres",selectedMovie.genres.toString());
        intent.putExtra("age",selectedMovie.creationYear);
        intent.putExtra("description",selectedMovie.description);
        intent.putExtra("movieImageIMG",selectedMovie.movieImageURL);
        intent.putExtra("movieURL",selectedMovie.URL);
        startActivity(intent);
    }
}
