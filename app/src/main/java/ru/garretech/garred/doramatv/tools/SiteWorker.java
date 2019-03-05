package ru.garretech.garred.doramatv.tools;

import ru.garretech.garred.doramatv.Movie;
import ru.garretech.garred.doramatv.tools.PageDownloader;
import ru.garretech.garred.doramatv.tools.SearchRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* Класс для работы с сайтом
* Парсит списки с дорамками. Парсит списки с источниками
* При вызове конструктора сохраняет в себе контекст
* В отдельном методе формируется список фильмов editorChoice
*
* */
public class SiteWorker {
    final public static String SITE_URL = "http://doramatv.ru";
    final public static String editorChoice = "row tiles-row short";
    final public static String NEW_MOVIES = "/list?sortType=created";
    final public static String BEST_MOVIES = "/list";
    final public static String TRAGUS_URL = "http://grass.tragus.ru/internal/videoCode/";


    public static List<Movie> getMoviesListFromSearch(String searchString) throws InterruptedException,ExecutionException {
        ArrayList<Movie> movieList = new ArrayList<>();
        String URL_PREFIX = "/search";
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
        Document pageContent = searchRequest.execute(SITE_URL+URL_PREFIX,searchString).get();

        return movieListContentParse(pageContent);
    }


    public static List<Movie> getEditorChoiceMoviesList() throws InterruptedException, ExecutionException {
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
            String imageURL;
            imageURL = element1.getElementsByTag("img").get(0).attr("data-original");
            //div class="subject-actions col-sm-7"
            movieList.add(new Movie(title, new ArrayList<>(Arrays.asList(genres.split(", "))), "", imageURL, url,true));
        }

        return movieList;
    }


    public static JSONArray getGenresList() throws InterruptedException, ExecutionException, JSONException {
        /*
        * table class="table table-hover"
        *
        * Из тега <tbody> взять всех потомков <tr>
        * Внутри потомка из тега td взять тэг <a> , взять значение его аттрибута href и его текст
        *
        *
        *
        *
        * */
        JSONArray genresList = new JSONArray();
        String URL_PREFIX = "/list/genres/sort_name";
        PageDownloader pageDownloader = new PageDownloader();
        Document pageContent;

        pageContent = pageDownloader.execute(SITE_URL+URL_PREFIX).get();

        Element element = pageContent.getElementsByClass("table table-hover").first();
        element = element.getElementsByTag("tbody").first();
        Elements elements = element.getElementsByTag("tr");
        int index = 0;
        for (Element element1 : elements) {
            Element tempElement = element1.getElementsByTag("td").first().getElementsByTag("a").first();
            JSONObject jsonObject = new JSONObject();
            String genreName = tempElement.text();
            String genreLink = tempElement.attr("href");
            jsonObject.put("name",genreName);
            jsonObject.put("link",genreLink);
            genresList.put(index,jsonObject);
            index++;
        }
        return genresList;
    }

    public static List<Movie> getMovieList(String urlPrefix) throws InterruptedException, ExecutionException {
        PageDownloader pageDownloader = new PageDownloader();
        Document pageContent;

        pageContent = pageDownloader.execute(SITE_URL+urlPrefix).get();
        return movieListContentParse(pageContent);
    }

    public static JSONObject getMovieInfo(String URL) throws InterruptedException,ExecutionException,JSONException {
        JSONObject info = new JSONObject();
        PageDownloader pageDownloader = new PageDownloader();
        Document pageContent;

        pageContent = pageDownloader.execute(URL).get();
        Element element;
        String initialSeries = pageContent.getElementsByClass("subject-actions col-sm-7").first().getElementsByTag("a").last().attr("href");
        initialSeries = initialSeries.substring(initialSeries.lastIndexOf("/"));
        element = pageContent.getElementsByClass("manga-description").first();
        String description;
        if (element != null)
            description = element.text();
        else
            description = "";
        String age = pageContent.getElementsByClass("elem_year ").first().text();
        // subject-meta col-sm-7
        element = pageContent.getElementsByClass("subject-meta col-sm-7").first();

        Elements elements = element.getElementsByTag("p");
        element = elements.get(0);
        String seriesNumber = element.text();

        element = elements.get(1);
        String duration = element.text();

        String production = pageContent.getElementsByClass("elem_country ").first().text();

        info.put("initial_series",initialSeries);
        info.put("production",production);
        info.put("series_number",seriesNumber);
        info.put("duration",duration);
        info.put("description",description);
        info.put("age",age);

        return info;
    }

    private static List<Movie> movieListContentParse(Document pageContent) {
        ArrayList<Movie> movieList = new ArrayList<>();
        Elements elements = pageContent.getElementsByClass("tile col-sm-6 ");
        for (Element element : elements) {
            Elements tempElements = element.getElementsByClass("tile-info").first().getElementsByTag("a");
            if (tempElements.size() != 0) {
                String genres;
                if (tempElements.size() > 1) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (Element element1 : tempElements) {
                        stringBuilder.append(element1.text());
                        stringBuilder.append(", ");
                    }
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
                movieList.add(new Movie(title, new ArrayList<>(Arrays.asList(genres.split(", "))), "", imageURL, url,isSerial));
            }
        }
        return movieList;
    }

    public static JSONArray getSources(JSONArray seriesList, String URL, int seriesIndex) throws InterruptedException,ExecutionException, JSONException {
        Pattern vkPattern = Pattern.compile("oid=(.?[\\d]+).+id=([\\d]+).+hash=(.+)\" a");
        Matcher matcher;
        String ADULT_PREFIX = "?mtr=1";
        PageDownloader pageDownloader;
        Document pageContent;

        JSONArray oneSeriesSources = new JSONArray();
        pageDownloader = new PageDownloader();

        pageContent = pageDownloader.execute(URL + ((JSONObject) seriesList.get(seriesIndex)).getString("link")+ADULT_PREFIX).get();
        Elements elements = pageContent.getElementsByClass("chapter-link");

        for (Element element1 : elements) {
            JSONObject jsonObject = new JSONObject();
            String subUnit;
            String seriesID;
            String oid;
            String id;
            String hash;

            if (element1.getElementsByClass("person-link").first() != null)
                subUnit = "Фансаб "+element1.getElementsByClass("person-link").first().text();
            else
                subUnit = "Оригинал";

            seriesID = element1.getElementsByAttribute("data-sid").first().attr("data-sid");

            pageDownloader = new PageDownloader();
            pageContent = pageDownloader.execute(SiteWorker.TRAGUS_URL + seriesID).get();

            String tempURL = pageContent.getElementsByTag("iframe").first().toString();

            if (tempURL.contains("vk.com")) {
                matcher = vkPattern.matcher(tempURL);
                if (matcher.find()) {
                    oid = matcher.group(1);
                    id = matcher.group(2);
                    hash = matcher.group(3);

                    jsonObject.put("sub_unit", subUnit);
                    jsonObject.put("movie_id", oid + "_" + id);
                    jsonObject.put("hash", hash);
                    oneSeriesSources.put(jsonObject);
                }
            }
        }
        return oneSeriesSources;
    }

    public static JSONArray formSeriesList(String URL, String initialSeries) {
        JSONArray seriesList = new JSONArray();
        String ADULT_PREFIX = "?mtr=1";
        PageDownloader pageDownloader = new PageDownloader();
        Document pageContent;
        try {
            pageContent = pageDownloader.execute(URL+initialSeries+ADULT_PREFIX).get();
            Element element = pageContent.getElementById("chapterSelectorSelect");
            Elements elements = element.getElementsByTag("option");
            int index = 0;
            for (Element element1 : elements) {
                JSONObject object = new JSONObject();
                object.put("name",element1.text());
                String link = element1.attr("value");
                link = link.substring(link.lastIndexOf("/"));
                object.put("link",link);
                seriesList.put(index,object);
                index++;
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return seriesList;
    }
}
