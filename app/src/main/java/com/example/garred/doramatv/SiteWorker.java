package com.example.garred.doramatv;

import android.content.Context;

import com.example.garred.doramatv.Tools.PageDownloader;
import com.example.garred.doramatv.Tools.SearchRequest;

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
* При вызове конструктора сохраняет в себе контекст
* В отдельном методе формируется список фильмов editorChoice
*
* */
public class SiteWorker {
    String currentContent;
    final static String SITE_URL = "http://doramatv.ru";
    String editorChoice = "row tiles-row short";
    Context context;

    public SiteWorker(Context context) {
        this.context = context;

    }

    static List<Movie> searchMovies(String searchString) throws InterruptedException,ExecutionException {
        ArrayList<Movie> movieList = new ArrayList<>();
        String addPath = "/search";
        /*
        * Формируется post запрос на сервер. Полученные данные парсятся
        * <div class="tile col-sm-6"> (карточки с результатами поиска)
         * Если в div class="tile-info"> нет тега <strong>Книга</strong>
         *  Из <div class=img> берется тег <a> точнее значение атрибута href
         * Из img берутся значения аттриб data-original, title
         *
         * Из div class="tile-info" берем содержимое тега <a> (перечень жанров)
         * div class=tags
        * span class="mangaSingle" показатель полнометражки
        * */
        SearchRequest searchRequest = new SearchRequest();
        Document pageContent = searchRequest.execute(SITE_URL+addPath,searchString).get();
        Elements elements = pageContent.getElementsByClass("tile col-sm-6 ");
        for (Element element : elements) {
            Elements tempElements = element.getElementsByClass("tile-info").first().getElementsByTag("a");
            if (tempElements.size() != 0) {
                String genres;
                if (tempElements.size() > 1) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (Element element1 : tempElements)
                        stringBuilder.append(element1.text() + ", ");
                    genres = stringBuilder.toString().substring(0, stringBuilder.toString().lastIndexOf(", "));
                }
                else {
                    genres = tempElements.first().text();
                }

                Element tempElement = element.getElementsByClass("img").first();
                String url = SITE_URL + tempElement.getElementsByTag("a").get(0).attr("href");
                tempElement = tempElement.getElementsByTag("img").first();
                String title = tempElement.attr("title");
                String imageURL = tempElement.attr("data-original");
                tempElement = element.getElementsByClass("tags").first();
                Boolean isSerial = tempElement.getElementsByClass("mangaSingle").isEmpty();
                movieList.add(new Movie(title, "1994", new ArrayList<>(Arrays.asList(genres.split(", "))), "", imageURL, url,isSerial));
            }
        }
        int a = 5;

        return movieList;
    }


    List<Movie> getEditorChoiceMovies() throws InterruptedException, ExecutionException {
        PageDownloader pageDownloader = new PageDownloader();
        Document pageContent;
        List<Movie> movieList;

        pageContent = pageDownloader.execute(SITE_URL).get();

        Element element = pageContent.getElementsByClass(editorChoice).first();
        Elements editorChoiceElements = element.getElementsByClass("simple-tile ");
        movieList = new ArrayList<>();

        for (int i = 0; i < editorChoiceElements.size(); i++) {
            Element element1 = editorChoiceElements.get(i);
            String genres = element1.attr("title");
            genres = genres.substring(genres.indexOf(". ") + 2);
            String url = SITE_URL + element1.getElementsByTag("a").get(0).attr("href");
            url = url.substring(0, url.lastIndexOf('/'));
            String title = element1.getElementsByTag("img").get(0).attr("alt");
            String imageURL = "";
            imageURL = element1.getElementsByTag("img").get(0).attr("data-original");
            movieList.add(new Movie(title, "1994", new ArrayList<>(Arrays.asList(genres.split(", "))), "", imageURL, url,true));
        }

        return movieList;
    }
}
