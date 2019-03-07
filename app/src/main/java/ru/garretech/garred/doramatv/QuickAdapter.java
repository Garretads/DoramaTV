package ru.garretech.garred.doramatv;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ru.garretech.garred.doramatv.tools.ImageDownloader;

public class QuickAdapter extends BaseQuickAdapter<Movie,BaseViewHolder> {


    public QuickAdapter(int layoutResId, @Nullable List<Movie> data) {
        super(layoutResId, data);
    }

    public void setItems(Collection<Movie> movies) {
        getData().clear();
        getData().addAll(movies);
        notifyDataSetChanged();
    }

    public void clearItems() {
        getData().clear();
        notifyDataSetChanged();
    }

    @Override
    protected void convert(BaseViewHolder helper, Movie item) {
        helper.setText(R.id.movie_title,item.title);

        Bitmap image;
        ImageDownloader imageDownloader = new ImageDownloader();
        try {
            image = imageDownloader.execute(item.movieImageURL).get();
            helper.setImageBitmap(R.id.movie_photo,image);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

}
