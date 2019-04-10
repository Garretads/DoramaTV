package ru.garretech.garred.doramatv.fragments;

import android.arch.persistence.room.PrimaryKey;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import ru.garretech.garred.doramatv.tools.ImageDownloader;
import ru.garretech.garred.doramatv.R;
import ru.garretech.garred.doramatv.tools.SiteWorker;


public class MovieAboutFragment extends Fragment{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "title";
    private static final String ARG_PARAM2 = "genres";
    private static final String ARG_PARAM3 = "production";
    private static final String ARG_PARAM4 = "series_number";
    private static final String ARG_PARAM5 = "duration";
    private static final String ARG_PARAM6 = "age";
    private static final String ARG_PARAM7 = "description";
    private static final String ARG_PARAM8 = "imageURL";
    private static final String ARG_PARAM9 = "url";


    private static final String AGE = "Год: ";
    private static final String GENRES = "Жанры: ";
    private static final String PRODUCTION_COUNTRY = "Производство: ";


    private String movieAge;
    private String movieTitle;
    private String movieGenres;
    private String movieProduction;
    private String movieSeriesNumber;
    private String movieDuration;
    private String movieDescription;
    private String movieImageURL;
    private String movieURL;
    Bitmap image = null;


    private OnFragmentInteractionListener mListener;

    public MovieAboutFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static MovieAboutFragment newInstance(JSONObject movieInfo) throws JSONException {
        MovieAboutFragment fragment = new MovieAboutFragment();
        Bundle args = new Bundle();

        args.putString(ARG_PARAM1, movieInfo.getString("title"));
        args.putString(ARG_PARAM2, movieInfo.getString("genres").substring(1,movieInfo.getString("genres").length()-1));
        args.putString(ARG_PARAM3, movieInfo.getString("production"));
        args.putString(ARG_PARAM4, movieInfo.getString("series_number"));
        args.putString(ARG_PARAM5, movieInfo.getString("duration"));
        args.putString(ARG_PARAM6, movieInfo.getString("age"));
        args.putString(ARG_PARAM7, movieInfo.getString("description"));
        args.putString(ARG_PARAM8, movieInfo.getString("image_url"));
        args.putString(ARG_PARAM9, movieInfo.getString("url"));

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            movieTitle = getArguments().getString(ARG_PARAM1);
            movieGenres = getArguments().getString(ARG_PARAM2);
            movieProduction = getArguments().getString(ARG_PARAM3);
            movieSeriesNumber = getArguments().getString(ARG_PARAM4);
            movieDuration = getArguments().getString(ARG_PARAM5);
            movieAge = getArguments().getString(ARG_PARAM6);
            movieDescription = getArguments().getString(ARG_PARAM7);
            movieImageURL = getArguments().getString(ARG_PARAM8);
            movieURL = getArguments().getString(ARG_PARAM9);
        }

        try {
            image = SiteWorker.getCachedImage(getContext(),movieImageURL);
        } catch (FileNotFoundException e) {
            ImageDownloader imageDownloader = new ImageDownloader();
            try {
                image = imageDownloader.execute(movieImageURL).get();
            } catch (ExecutionException e1) {
                e1.printStackTrace();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie_about, container, false);
        TextView movieAgeView = view.findViewById(R.id.movie_age_text);
        TextView movieGenresView = view.findViewById(R.id.movie_genres_text);
        TextView movieProductionCountryView = view.findViewById(R.id.movie_production_country_text);
        TextView movieSeriesNumberView = view.findViewById(R.id.movie_series_number_text);
        TextView movieDurationView = view.findViewById(R.id.movie_duration_text);
        ImageView imageView = view.findViewById(R.id.movie_image_about);
        TextView movieDescriptionView = view.findViewById(R.id.movie_description_text);

        movieGenresView.setText(getString(R.string.genres_description) + movieGenres);
        movieProductionCountryView.setText(getString(R.string.production_country_description)+movieProduction);
        movieSeriesNumberView.setText(movieSeriesNumber);
        movieDurationView.setText(movieDuration);
        movieAgeView.setText(getString(R.string.age_description) + movieAge);
        movieDescriptionView.setText(movieDescription);
        imageView.setImageBitmap(image);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
