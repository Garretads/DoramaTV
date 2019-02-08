package com.example.garred.doramatv;

import android.content.Context;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/*
* Класс для работы с сайтом
* Парсит списки с дорамками. Парсит списки с источниками
*
*
* */
public class SiteWorker {
    String currentContent;
    final static String SITE_URL = "http://doramatv.ru";
    String editorChoice = "row tiles-row short";
    Document pageContent;
    Context context;
    List<Movie> movieList;

    public SiteWorker(Context context) {
        this.context = context;
        DocumentDownloader documentDownloader = new DocumentDownloader();
        try {
            pageContent = documentDownloader.execute(SITE_URL).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        int a = 5;
        Element element = pageContent.getElementsByClass(editorChoice).first();
        Elements editorChoiceElements = element.getElementsByClass("simple-tile ");
        movieList = new ArrayList<>();

        for (int i=0; i<editorChoiceElements.size();i++) {
            Element element1 = editorChoiceElements.get(i);
            Movie movie;
            String genres = element1.attr("title");
            genres = genres.substring(genres.indexOf(". ")+2);
            String url = SITE_URL+element1.getElementsByTag("a").get(0).attr("href");
            url = url.substring(0,url.lastIndexOf('/'));
            String title = element1.getElementsByTag("img").get(0).attr("alt");
            String imageURL = "";
            imageURL = element1.getElementsByTag("img").get(0).attr("data-original");
            movieList.add(new Movie(title,"1994",new ArrayList<>(Arrays.asList(genres.split(", "))),"",imageURL,url));
            int y = 5;
        }
    }
}
