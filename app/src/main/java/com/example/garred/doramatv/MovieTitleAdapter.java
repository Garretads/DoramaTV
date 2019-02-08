package com.example.garred.doramatv;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieTitleAdapter extends RecyclerView.Adapter<MovieTitleAdapter.MovieTitleViewHolder> {
    private LayoutInflater inflater;
    List<Movie> mMovieDataset;
    OnMovieListener mOnMovieListener;

    public MovieTitleAdapter(Context context, List<Movie> mMovieDataset,OnMovieListener mOnMovieListener) {
        this.mMovieDataset = mMovieDataset;
        this.inflater = LayoutInflater.from(context);
        this.mOnMovieListener = mOnMovieListener;
    }

    @NonNull
    @Override
    public MovieTitleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.fragment_movies,parent,false);
        return new MovieTitleViewHolder(view,mOnMovieListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieTitleViewHolder holder, int position) {
        Movie movie = mMovieDataset.get(position);
        holder.mMovieTitle.setText(movie.title);
        holder.mMovieAge.setText(movie.creationYear);

        Bitmap image = null;
        ImageDownloader imageDownloader = new ImageDownloader();
        try {
            image = imageDownloader.execute(movie.movieImageURL).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        holder.mMoviePhoto.setImageBitmap(image);

    }

    @Override
    public int getItemCount() { return mMovieDataset.size(); }


    public static class MovieTitleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.cv) CardView cv;
        @BindView(R.id.movie_title) TextView mMovieTitle;
        @BindView(R.id.movie_age) TextView mMovieAge;
        @BindView(R.id.movie_photo) ImageView mMoviePhoto;
        OnMovieListener onMovieListener;


        public MovieTitleViewHolder(View itemView, OnMovieListener onMovieListener) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            this.onMovieListener = onMovieListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onMovieListener.onMovieClick(getAdapterPosition());
        }
    }

    public interface OnMovieListener {
        void onMovieClick(int position);
    }
}
