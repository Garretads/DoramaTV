package com.example.garred.doramatv;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.example.garred.doramatv.Fragments.MovieAboutFragment;
import com.example.garred.doramatv.Fragments.MovieSourcesFragment;
import com.example.garred.doramatv.Tools.PageDownloader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import java.util.concurrent.ExecutionException;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieAboutActivity extends AppCompatActivity implements MovieAboutFragment.OnFragmentInteractionListener, MovieSourcesFragment.OnFragmentInteractionListener {


    @BindView(R.id.toolbar ) Toolbar toolbar;
    @BindView(R.id.container) ViewPager mViewPager;
    @BindView(R.id.tabs) TabLayout tabLayout;
    ViewPagerAdapter mFragmentAdapter;
    JSONObject movieInfo;
    JSONArray sources;
    String title;
    String age;
    String genres;
    String description;
    String imageURL;
    String movieURL;
    String accessToken;
    Boolean isSerial;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_about);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        try {
            movieInfo = new JSONObject(intent.getStringExtra("movieInfo"));
            this.title = movieInfo.getString("title");
            this.genres = movieInfo.getString("genres").substring(1,movieInfo.getString("genres").length()-1);
            this.imageURL = movieInfo.getString("movieImageIMG");
            this.movieURL = movieInfo.getString("movieURL");
            this.accessToken = movieInfo.getString("access_token");
            this.isSerial = movieInfo.getBoolean("isSerial");

            JSONObject newMovieInfo = getMovieInfo(movieURL);

            this.age = newMovieInfo.getString("age");
            this.description = newMovieInfo.getString("description");

            movieInfo.put("description",description);
            movieInfo.put("age",age);

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        /*setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(title);*/

        setupViewPager(mViewPager);
        tabLayout.setupWithViewPager(mViewPager);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));


    }

    private void setupViewPager(ViewPager viewPager) {
        mFragmentAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        try {
            JSONObject sourcesInfo = new JSONObject();
            sourcesInfo.put("URL",movieURL);
            sourcesInfo.put("access_token",accessToken);
            sourcesInfo.put("isSerial",isSerial);
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    JSONObject getMovieInfo(String URL) throws InterruptedException,ExecutionException,JSONException {
        JSONObject info = new JSONObject();
        PageDownloader pageDownloader = new PageDownloader();
        Document pageContent;

        pageContent = pageDownloader.execute(URL).get();
        String description = pageContent.getElementsByClass("manga-description").first().text();
        String age = pageContent.getElementsByClass("elem_year ").first().text();
        info.put("description",description);
        info.put("age",age);

        return info;
    }

}
