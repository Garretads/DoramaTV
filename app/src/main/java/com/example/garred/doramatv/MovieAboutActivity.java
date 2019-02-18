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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieAboutActivity extends AppCompatActivity implements MovieAboutFragment.OnFragmentInteractionListener, MovieSourcesFragment.OnFragmentInteractionListener {




    @BindView(R.id.toolbar ) Toolbar toolbar;
    @BindView(R.id.container) ViewPager mViewPager;
    @BindView(R.id.tabs) TabLayout tabLayout;
    static String TRAGUS_URL = "http://grass.tragus.ru/internal/videoCode/";
    JSONObject movieInfo;
    JSONArray sources;
    String title;
    String age;
    String genres;
    String description;
    String imageURL;
    String movieURL;


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


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(title);

        toolbar.setTitle(title);
        setupViewPager(mViewPager);
        tabLayout.setupWithViewPager(mViewPager);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        formSeriesList(movieURL);

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        try {
            adapter.addFragment(MovieAboutFragment.newInstance(movieInfo), "О фильме");
            adapter.addFragment(new MovieSourcesFragment(), "Источники");
            viewPager.setAdapter(adapter);
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

    void formSeriesList(String URL) {

            /* Серия
                    Источник (имя фансаба)
                            id фильма в vk
                                            ссылки с различным качеством

                <select id=chapterSelectorSelect
                Взять блок option, где имеется атрибут selected="selected". Его значение будет количеством выпущенных серий

             */
        PageDownloader pageDownloader = new PageDownloader();
        Document pageContent;

        try {
            pageContent = pageDownloader.execute(URL+"/series1").get();
            Element element = pageContent.getElementById("chapterSelectorSelect");
            Elements elements = element.getElementsByTag("option");
            int serialLength = elements.size();


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    JSONArray getSources(String URL, int seriesIndex) throws InterruptedException,ExecutionException {
        Pattern vkPattern = Pattern.compile("oid=(.?[\\d]+).+id=([\\d]+).+hash=(.+)\" a");
        Matcher matcher;

        PageDownloader pageDownloader;
        Document pageContent;

        JSONArray oneSeriesSources = new JSONArray();
        pageDownloader = new PageDownloader();

        pageContent = pageDownloader.execute(URL+"/series"+seriesIndex).get();
        Elements elements = pageContent.getElementsByClass("chapter-link");

        for (Element element1 : elements) {
            JSONObject jsonObject = new JSONObject();
            String subUnit;
            String seriesID;
            String oid;
            String id;
            String hash;

            subUnit = element1.getElementsByClass("person-link").first().text();
            seriesID = element1.getElementsByAttribute("data-sid").first().attr("data-sid");

            pageDownloader = new PageDownloader();
            pageContent = pageDownloader.execute(TRAGUS_URL+seriesID).get();

            String tempURL = pageContent.getElementsByTag("iframe").first().toString();

            if (tempURL.contains("vk.com")) {
                matcher = vkPattern.matcher(tempURL);
                if (matcher.find()) {
                    oid = matcher.group(1);
                    id = matcher.group(2);
                    hash = matcher.group(3);

                    try {
                        jsonObject.put("sub_unit",subUnit);
                        jsonObject.put("movie_id",oid+"_"+id);
                        jsonObject.put("hash",hash);
                        oneSeriesSources.put(jsonObject);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            else
                continue;

        }
        return oneSeriesSources;
    }


}
