package ru.garretech.garred.doramatv.adapters;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ru.garretech.garred.doramatv.R;
import ru.garretech.garred.doramatv.model.Movie;
import ru.garretech.garred.doramatv.tools.ImageDownloader;

public class RecyclerAdapter extends BaseQuickAdapter<Movie,BaseViewHolder> {

    @Override
    public void addData(@NonNull Movie data) {
        super.addData(data);
    }

    public RecyclerAdapter(int layoutResId, @Nullable List<Movie> data) {
        super(layoutResId, data);
    }

    public void setItems(Collection<Movie> movies) {
        getData().clear();
        for (Movie movie:movies) {
            getData().add(movie);
            notifyItemInserted(getItemCount()-1);
        }
    }

    public void clearItems() {
        getData().clear();
        notifyDataSetChanged();
    }

    @Override
    protected void convert(BaseViewHolder helper, Movie item) {
        helper.setText(R.id.movie_title, item.getTitle());
        helper.setImageBitmap(R.id.movie_photo, item.getImage());
    }
}
