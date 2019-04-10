package ru.garretech.garred.doramatv.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import ru.garretech.garred.doramatv.R;
import ru.garretech.garred.doramatv.adapters.MovieAboutPagerAdapter;
import ru.garretech.garred.doramatv.database.AppDataSource;
import ru.garretech.garred.doramatv.fragments.MovieAboutFragment;
import ru.garretech.garred.doramatv.fragments.MovieSourcesFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.garretech.garred.doramatv.model.Favorites;
import ru.garretech.garred.doramatv.model.Movie;

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
    Movie currentMovie;
    AppDataSource dataSource;
    Boolean isFavorite = false;
    Subject<Boolean> observable = PublishSubject.create();
    Disposable disposable;
    Menu optionsMenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_about);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        dataSource = new AppDataSource(getApplicationContext());
        try {
            movieInfo = new JSONObject(intent.getStringExtra("movie_info"));


            /*
            * Запросить наличие фильма в избранных (completable)
            * Полученный результат хранится в переменной isFavorite, которая является observable
            * При изменении значения данной переменной подписчик выполняет свои действия (меняется иконку избранного)
            *
            * Занесение фильма в избранное.
            * Опять completable. С помощью него фильм заносится в БД.
            * */
            /*

            this.title = currentMovie.getTitle();
            this.genres = currentMovie.getGenres().toString().substring(1,currentMovie.getGenres().toString().length()-1);
            this.imageURL = currentMovie.getMovieImageURL();
            this.movieURL = currentMovie.getURL();
            this.age = currentMovie.getProductionYear();
            this.description = currentMovie.getDescription();
            this.initialSeries = currentMovie.getInitialSeries();
            this.production = currentMovie.getProductionCountry();
            this.seriesNumber = currentMovie.getSeriesNumber();
            this.duration = currentMovie.getDuration();

            this.accessToken = movieInfo.getString("access_token");*/
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

            Bundle bundle = intent.getBundleExtra("bundle");
            try {
                currentMovie = (Movie) bundle.getSerializable("movie");
            }
            catch (NullPointerException e) {
                currentMovie = new Movie(title, Arrays.asList(genres.split(",")),imageURL,movieURL);
                currentMovie.setProductionYear(age);
                currentMovie.setDescription(description);
                currentMovie.setInitialSeries(initialSeries);
                currentMovie.setProductionCountry(production);
                currentMovie.setSeriesNumber(seriesNumber);
                currentMovie.setDuration(duration);
            }

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

    private void flagFavorite(Boolean flag) {
        MenuItem item = optionsMenu.getItem(0);
        if (flag)
            item.setIcon(R.drawable.ic_favorite_white_24dp);
        else
            item.setIcon(R.drawable.ic_favorite_border_white_24dp);

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
        optionsMenu = menu;


        disposable = observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        flagFavorite(aBoolean);
                    }
                });

        Completable.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                isFavorite = dataSource.isFavorite(currentMovie.getURL());
                emmitFavorite(isFavorite);
                return null;
            }
        }).subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        Log.d("Task", "Delete completable completed");
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_favorite: {
                if (isFavorite) {
                    Completable.fromCallable(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            dataSource.deleteFavorites(currentMovie);
                            return null;
                        }
                    }).subscribeOn(Schedulers.io())
                    .subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onComplete() {
                            emmitFavorite(false);
                            Log.d("Task", "Delete completable completed");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d("Task", "Delete completable error");
                        }
                    });
                }
                else {
                    Completable.fromCallable(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            dataSource.addFavorites(currentMovie);
                            return null;
                        }
                    }).subscribeOn(Schedulers.io())
                    .subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onComplete() {
                            emmitFavorite(true);
                            Log.d("Task", "Add completable completed");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d("Task", "Add completable error");
                        }
                    });
                }
                break;
            }
            case R.id.action_settings: {
                Intent intent = new Intent(MovieAboutActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    void emmitFavorite(Boolean value) {
        isFavorite = value;
        observable.onNext(isFavorite);
    }

}

