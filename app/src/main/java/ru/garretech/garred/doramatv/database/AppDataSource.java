package ru.garretech.garred.doramatv.database;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import ru.garretech.garred.doramatv.model.Favorites;
import ru.garretech.garred.doramatv.model.Movie;
import ru.garretech.garred.doramatv.tools.SiteWorker;

public class AppDataSource {

    private MovieDAO movieDAO;
    private FavoritesDAO favoritesDAO;

    public AppDataSource(Context context) {
        AppDatabase appDatabase = AppDatabase.getInstance(context);
        movieDAO = appDatabase.movieDAO();
        favoritesDAO = appDatabase.favoritesDAO();
    }

    public void addMovie(Movie movie) {
        movieDAO.addMovie(movie);
    }

    public List<Movie> getAllMovies() {
        return movieDAO.getAllCachedMovies();
    }

    public  Movie getMovie(String URL) {
        return movieDAO.getMovie(URL);
    }

    public Observable<List<Movie>> getListOfFavorites() {
        List<Movie> list = new ArrayList<>();

        List<Favorites> favorites = favoritesDAO.getAllFavorites();

        for (Favorites favorite : favorites) {
            Movie movie = movieDAO.getMovie(favorite.getMovieURL());
            list.add(movie);
        }
        return Observable.fromArray(list);
    }

    public Boolean isFavorite(String URL) {
         Favorites favorites = favoritesDAO.getFavoriteByURL(URL);
         return favorites != null;
    }

    public void addFavorites(Movie movie) {
        movieDAO.addMovie(movie);
        Log.d("Database", "Movie "+movie.getURL()+" added");
        Favorites favorite = new Favorites();
        favorite.setMovieURL(movie.getURL());
        favoritesDAO.addFavorites(favorite);
    }


    public void deleteFavorites(Movie movie) {
        Favorites favorites = favoritesDAO.getFavoriteByURL(movie.getURL());
        favoritesDAO.deleteFavorites(favorites);
    }

}
