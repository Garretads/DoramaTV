package com.example.garred.doramatv;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MovieTitleAdapter extends RecyclerView.Adapter<MovieTitleAdapter.MovieTitleViewHolder> {
    private LayoutInflater inflater;
    List<Movie> mMovieDataset;

    public MovieTitleAdapter(Context context, List<Movie> mMovieDataset) {
        this.mMovieDataset = mMovieDataset;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public MovieTitleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.fragment_movies,parent,false);
        return new MovieTitleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieTitleViewHolder holder, int position) {
        Movie movie = mMovieDataset.get(position);
        holder.mMovieTitle.setText(movie.title);
        holder.mMovieAge.setText(movie.creationYear);
        holder.mMoviePhoto.setImageBitmap(movie.movieImage);
    }

    @Override
    public int getItemCount() { return mMovieDataset.size(); }


    public static class MovieTitleViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView mMovieTitle;
        TextView mMovieAge;
        ImageView mMoviePhoto;
        public MovieTitleViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            mMovieTitle = (TextView)itemView.findViewById(R.id.movie_title);
            mMovieAge = (TextView)itemView.findViewById(R.id.movie_age);
            mMoviePhoto = (ImageView)itemView.findViewById(R.id.movie_photo);
        }
    }
}
