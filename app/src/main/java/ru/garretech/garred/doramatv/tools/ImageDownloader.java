package ru.garretech.garred.doramatv.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.URL;

public class ImageDownloader extends AsyncTask<String,Void,Bitmap> {

    @Override
    protected Bitmap doInBackground(String... strings) {
        Bitmap image = null;
        try {
            URL url = new URL(strings[0]);
            image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }
}
