package ru.garretech.garred.doramatv.tools;

import android.net.Uri;

import ru.garretech.garred.doramatv.Settings;
import ru.garretech.garred.doramatv.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    final private static String SITE_URL = "http://doramatv.ru";
    final private static String SITE_URL1 = "doramatv.ru";
    final private static String editorChoice = "row tiles-row short";
    final public static String[] NEW_MOVIES_PARAMS = {"sortType","created"};
    final public static String LIST_PREFIX = "list";
    final public static String SEARCH_PREFIX = "search";
    final public static String ONGOING_PREFIX = "list/tags/ongoing";
    final public static String[] ONGOING_PARAMS = {"sortType","rate"};
    final private static String OFFSET_PARAM = "offset";
    final private static String TRAGUS_URL = "http://grass.tragus.ru/internal/videoCode/";
    final public static int SIMPLE_QUERY = 0;
    final public static int SEARCH_QUERY = 1;

    /*
    *  Сформировать ссылку запроса (или из поискового запроса или из выбранного жанра)
    *  Загрузить контент по ссылке
    *
    *
    * */


    private static int getSearchElementCount(Document pageContent) {
        Pattern pattern = Pattern.compile("\\((\\d+)\\)");
        Matcher matcher;
        int resultAmount = 0;

        Element element = pageContent.getElementById("mangaResults").getElementsByTag("h3").first();

        matcher = pattern.matcher(element.text());

        if (matcher.find())
            resultAmount = Integer.valueOf(matcher.group(1));

        return resultAmount;
    }


    public static List<Movie> getEditorChoiceMoviesList() throws InterruptedException, ExecutionException {
        PageDownloader pageDownloader = new PageDownloader();
        Document pageContent;
        List<Movie> movieList;
        pageContent = pageDownloader.execute(SITE_URL).get();
        ImageDownloader imageDownloader;
        Movie movie;

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
            movie = new Movie(title, new ArrayList<>(Arrays.asList(genres.split(", "))), "", imageURL, url,true);
            imageDownloader = new ImageDownloader();
            movie.setImage(imageDownloader.execute(imageURL).get());

            movieList.add(movie);
        }

        return movieList;
    }


    public static JSONArray getGenresList() throws InterruptedException, ExecutionException, JSONException {


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
            genreLink = genreLink.substring(1);
            jsonObject.put("name",genreName);
            jsonObject.put("link",genreLink);
            genresList.put(index,jsonObject);
            index++;
        }
        return genresList;
    }



    private static int getQueryElementCount(Document pageContent) {
        Pattern pattern = Pattern.compile("(\\d+)");
        Matcher matcher;
        int resultAmount = 0;

        Elements elements = pageContent.getElementsByTag("h4");
        String patternText = null;

        for (Element element : elements) {
            if ( (elements = element.getElementsContainingText("Список")).size() != 0) {
                patternText = elements.first().text();
                break;
            }
        }
        if (patternText != null) {
            matcher = pattern.matcher(patternText);

            if (matcher.find())
                resultAmount = Integer.valueOf(matcher.group(1));
        } else
            resultAmount = 16870; // Текущее количество дорам на сайте
        return resultAmount;
    }

    public static JSONObject getMovieInfo(String URL) throws InterruptedException,ExecutionException,JSONException {
        JSONObject info = new JSONObject();
        PageDownloader pageDownloader = new PageDownloader();
        Document pageContent;
        String description = "";
        String age = "";
        String production = "";

        pageContent = pageDownloader.execute(URL).get();
        Element tempElement;
        Elements tempElements;
        String initialSeries = pageContent.getElementsByClass("subject-actions col-sm-7").first().getElementsByTag("a").last().attr("href");
        initialSeries = initialSeries.substring(initialSeries.lastIndexOf("/"));

        tempElement = pageContent.getElementsByClass("manga-description").first();
        if (tempElement != null)
            description = tempElement.text();

        tempElement = pageContent.getElementsByClass("elem_year ").first();
        if (tempElement != null)
            age = tempElement.text();

        tempElement = pageContent.getElementsByClass("elem_country ").first();
        if (tempElement != null)
            production = tempElement.text();

        tempElement = pageContent.getElementsByClass("subject-meta col-sm-7").first();
        tempElements = tempElement.getElementsByTag("p");

        tempElement = tempElements.get(0);
        String seriesNumber = tempElement.text();

        tempElement = tempElements.get(1);
        String duration = tempElement.text();


        info.put("initial_series",initialSeries);
        info.put("production",production);
        info.put("series_number",seriesNumber);
        info.put("duration",duration);
        info.put("description",description);
        info.put("age",age);

        return info;
    }

    private static List<Movie> movieListContentParse(Document pageContent,int limit) {
        ArrayList<Movie> movieList = new ArrayList<>();
        Elements elements = pageContent.getElementsByClass("tile col-sm-6 ");
        ImageDownloader imageDownloader = new ImageDownloader();
        Movie movie;
        int iteration = 0;
        for (Element element : elements) {

            if (limit != 0 && iteration > limit - 1)
                break;

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

                movie = new Movie(title, new ArrayList<>(Arrays.asList(genres.split(", "))), "", imageURL, url,isSerial);

                try {
                    imageDownloader = new ImageDownloader();
                    movie.setImage(imageDownloader.execute(imageURL).get());
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                movieList.add(movie);
                iteration++;
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

        pageContent = pageDownloader.execute(URL + ((JSONObject) seriesList.get(seriesIndex)).getString("link") + ADULT_PREFIX).get();
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
            Element tempElement = pageContent.getElementsByTag("iframe").first();

            if (tempElement == null)
                continue;

            String tempURL = tempElement.toString();

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
            if (initialSeries.equals("/")) {
                return seriesList;
            }
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

    public static Uri.Builder getStandartUri() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority(SITE_URL1);
        return builder;
    }

    public class RequestQuery {
        private int requestType;
        private int queryAmount = -1;
        private int limit;
        private String path;
        private int currentOffset = 0;
        private Uri.Builder uriQuery;
        private List<Movie> movieList;
        private HashMap<String,String> parameters;

        public RequestQuery(int requestType, String path, HashMap<String,String> params, int limit) {
            this.requestType = requestType;
            this.limit = limit;
            this.path = path;
            this.parameters = params;
        }

        public RequestQuery(int requestType, String path, HashMap<String,String> params) {
            this.requestType = requestType;
            this.limit = Settings.max_loaded_in_screen();
            this.path = path;
            this.parameters = params;
        }

        public RequestQuery(int requestType, String path) {
            this.requestType = requestType;
            this.limit = Settings.max_loaded_in_screen();
            this.path = path;
            parameters = new HashMap<>();
        }


        public List<Movie> getNextQuery() throws ExecutionException, InterruptedException {
            if (queryAmount == -1 || currentOffset+limit < queryAmount) {
                switch (requestType) {
                    case SIMPLE_QUERY: {
                        PageDownloader pageDownloader = new PageDownloader();
                        uriQuery = getStandartUri();

                        if (path.contains("/")) {
                            String[] pathArray = path.split("/");

                            for (String s : pathArray) {
                                uriQuery.appendPath(s);
                            }
                        } else {
                            uriQuery.appendPath(path);
                        }


                        parameters.put(OFFSET_PARAM, String.valueOf(currentOffset));

                        for (Map.Entry<String, String> cursor : parameters.entrySet()) {
                            uriQuery.appendQueryParameter(cursor.getKey(), cursor.getValue());
                        }

                        Document pageContent = pageDownloader.execute(uriQuery.toString()).get();

                        if (queryAmount == -1)
                            queryAmount = getQueryElementCount(pageContent);

                        movieList = movieListContentParse(pageContent, limit);


                        currentOffset += movieList.size();
                        return movieList;
                    }

                    case SEARCH_QUERY: {

                        SearchRequest searchRequest = new SearchRequest();

                        uriQuery = getStandartUri();
                        uriQuery.appendPath(path);

                        parameters.put(OFFSET_PARAM, String.valueOf(currentOffset));

                        Document pageContent = searchRequest.execute(uriQuery.toString(),parameters.get("q"),parameters.get(OFFSET_PARAM)).get();

                        if (queryAmount == -1)
                            queryAmount = getSearchElementCount(pageContent);

                        movieList = movieListContentParse(pageContent, limit);

                        currentOffset += movieList.size();
                        return movieList;
                    }
                    default:
                        return new ArrayList<>();
                }
            }
            else
                return new ArrayList<>();
        }


        public int queryAmount() {
            return queryAmount;
        }

        public int limit() {
            return limit;
        }

        public int offset() { return currentOffset;}

    }
}
