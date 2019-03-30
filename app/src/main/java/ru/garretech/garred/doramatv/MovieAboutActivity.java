package ru.garretech.garred.doramatv;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import ru.garretech.garred.doramatv.adapters.MovieAboutPagerAdapter;
import ru.garretech.garred.doramatv.fragments.MovieAboutFragment;
import ru.garretech.garred.doramatv.fragments.MovieSourcesFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieAboutActivity extends AppCompatActivity implements MovieAboutFragment.OnFragmentInteractionListener, MovieSourcesFragment.OnFragmentInteractionListener {


    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.container) ViewPager mViewPager;
    @BindView(R.id.tabs) TabLayout tabLayout;
    MovieAboutPagerAdapter mFragmentAdapter;
    JSONObject movieInfo;
    JSONArray sources;
    String title;
    String age;
    String genres;
    String production;
    String seriesNumber;
    String duration;
    String description;
    String imageURL;
    String movieURL;
    String accessToken;
    String initialSeries;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_about);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        try {
            movieInfo = new JSONObject(intent.getStringExtra("movie_info"));
            this.title = movieInfo.getString("title");
            this.genres = movieInfo.getString("genres").substring(1,movieInfo.getString("genres").length()-1);
            this.imageURL = movieInfo.getString("image_url");
            this.movieURL = movieInfo.getString("url");
            this.accessToken = movieInfo.getString("access_token");
            this.age = movieInfo.getString("age");
            this.description = movieInfo.getString("description");
            this.initialSeries = movieInfo.getString("initial_series");
            this.production = movieInfo.getString("production");
            this.seriesNumber = movieInfo.getString("series_number");
            this.duration = movieInfo.getString("duration");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        setupViewPager(mViewPager);
        tabLayout.setupWithViewPager(mViewPager);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));


    }


    private void setupViewPager(ViewPager viewPager) {
        mFragmentAdapter = new MovieAboutPagerAdapter(getSupportFragmentManager());

        try {
            JSONObject sourcesInfo = new JSONObject();
            sourcesInfo.put("url",movieURL);
            sourcesInfo.put("access_token",accessToken);
            sourcesInfo.put("initial_series",initialSeries);
            mFragmentAdapter.addFragment(MovieAboutFragment.newInstance(movieInfo), "О фильме");
            mFragmentAdapter.addFragment(MovieSourcesFragment.newInstance(sourcesInfo), "Источники");
            viewPager.setAdapter(mFragmentAdapter);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(title);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_movie_about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings: {
                Intent intent = new Intent(MovieAboutActivity.this,SettingsActivity.class);
                startActivity(intent);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

}

