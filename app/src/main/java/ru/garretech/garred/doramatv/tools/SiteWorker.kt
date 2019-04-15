package ru.garretech.garred.doramatv.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast

import io.reactivex.Observable
import ru.garretech.garred.doramatv.R
import ru.garretech.garred.doramatv.Settings
import ru.garretech.garred.doramatv.model.Movie

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap
import java.util.concurrent.ExecutionException
import java.util.regex.Matcher
import java.util.regex.Pattern

/*
* Класс для работы с сайтом
* Парсит списки с дорамками. Парсит списки с источниками
* При вызове конструктора сохраняет в себе контекст
* В отдельном методе формируется список фильмов editorChoice
*
* */
class SiteWorker {

    inner class RequestQuery {
        private var requestType: Int = 0
        private var queryAmount = -1
        private var limit: Int = 0
        private var path: String? = null
        private var currentOffset = 0
        private var uriQuery: Uri.Builder? = null
        var list: List<Movie>? = null
            private set
        private var parameters: HashMap<String, String>? = null
        private var context: Context? = null


        val nextQuery: Observable<List<Movie>>
            @Throws(ExecutionException::class, InterruptedException::class, NullPointerException::class)
            get() = if (queryAmount == -1 || currentOffset < queryAmount) {
                when (requestType) {
                    SIMPLE_QUERY -> {
                        val pageDownloader = PageDownloader()
                        uriQuery = standartUri

                        if (path!!.contains("/")) {
                            val pathArray = path!!.split("/".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()

                            for (s in pathArray) {
                                uriQuery!!.appendPath(s)
                            }
                        } else {
                            uriQuery!!.appendPath(path)
                        }


                        parameters!![OFFSET_PARAM] = currentOffset.toString()

                        for ((key, value) in parameters!!) {
                            uriQuery!!.appendQueryParameter(key, value)
                        }

                        val pageContent = pageDownloader.execute(uriQuery!!.toString()).get()

                        if (queryAmount == -1)
                            queryAmount = getMaxQueryElementCount(pageContent)

                        val result = movieListContentParse(context, pageContent, limit)
                        list = result["list"] as List<Movie>?


                        currentOffset += (result["offset"] as Int?)!!
                        Observable.fromArray(list!!)
                    }

                    SEARCH_QUERY -> {

                        val searchRequest = SearchRequest()

                        uriQuery = standartUri
                        uriQuery!!.appendPath(path)

                        parameters!![OFFSET_PARAM] = currentOffset.toString()

                        val pageContent = searchRequest.execute(uriQuery!!.toString(), parameters!!["q"], parameters!![OFFSET_PARAM]).get()

                        if (queryAmount == -1)
                            queryAmount = getMaxSearchElementCount(pageContent)

                        val result = movieListContentParse(context, pageContent, limit)

                        list = result["list"] as List<Movie>?

                        currentOffset += (result["offset"] as Int?)!!
                        Observable.fromArray(list!!)
                    }

                    EDITOR_CHOICE_QUERY -> {
                        queryAmount = 5
                        currentOffset = 5
                        getEditorChoiceMoviesList(context)
                    }
                    else -> Observable.empty()
                }
            } else
                Observable.empty()

        constructor(context: Context, requestType: Int, path: String, params: HashMap<String, String>, limit: Int) {
            this.context = context
            this.requestType = requestType
            this.limit = limit
            this.path = path
            this.parameters = params
        }

        constructor(context: Context, requestType: Int, path: String, params: HashMap<String, String>) {
            this.context = context
            this.requestType = requestType
            this.limit = Settings.max_loaded_in_screen()
            this.path = path
            this.parameters = params
        }

        constructor(context: Context, requestType: Int, path: String) {
            this.context = context
            this.requestType = requestType
            this.limit = Settings.max_loaded_in_screen()
            this.path = path
            parameters = HashMap()
        }

        constructor(context: Context, requestType: Int) {
            this.context = context
            this.requestType = requestType
            this.limit = Settings.max_loaded_in_screen()
            this.path = ""
            parameters = HashMap()
        }


        fun queryAmount(): Int {
            return queryAmount
        }

        fun limit(): Int {
            return limit
        }

        fun offset(): Int {
            return currentOffset
        }

        fun resetOffset() {
            currentOffset = 0
        }

    }

    companion object {
        val SITE_URL = "http://doramatv.ru"
        private val SITE_URL1 = "doramatv.ru"
        private val editorChoice = "row tiles-row short"
        val NEW_MOVIES_PARAMS = arrayOf("sortType", "created")
        val LIST_PREFIX = "list"
        val SEARCH_PREFIX = "search"
        val ONGOING_PREFIX = "list/tags/ongoing"
        val ONGOING_PARAMS = arrayOf("sortType", "rate")
        val RANDOM_MOVIE_PREFIX = "/internal/random"
        private val OFFSET_PARAM = "offset"
        private val TRAGUS_URL = "http://grass.tragus.ru/internal/videoCode/"
        val SIMPLE_QUERY = 0
        val SEARCH_QUERY = 1
        val EDITOR_CHOICE_QUERY = 2

        /*
    *  Сформировать ссылку запроса (или из поискового запроса или из выбранного жанра)
    *  Загрузить контент по ссылке
    *
    *
    * */

        private fun getMaxSearchElementCount(pageContent: Document): Int {
            val pattern = Pattern.compile("\\((\\d+)\\)")
            val matcher: Matcher
            var resultAmount = 0

            val element = pageContent.getElementById("mangaResults").getElementsByTag("h3").first()

            matcher = pattern.matcher(element.text())

            if (matcher.find())
                resultAmount = Integer.valueOf(matcher.group(1))

            return resultAmount
        }


        @Throws(InterruptedException::class, ExecutionException::class, NullPointerException::class)
        fun getEditorChoiceMoviesList(context: Context?): Observable<List<Movie>> {
            val pageDownloader = PageDownloader()
            val pageContent: Document
            val movieList = ArrayList<Movie>()
            pageContent = pageDownloader.execute(SITE_URL).get()
            var imageDownloader: ImageDownloader
            var movie: Movie

            val tempElements = pageContent.getElementsByClass(editorChoice)
            if (tempElements == null)
                throw NullPointerException()
            else {
                val editorChoiceElements = tempElements.first().getElementsByClass("simple-tile ")

                for (i in editorChoiceElements.indices) {
                    val element1 = editorChoiceElements[i]
                    var genres = element1.attr("title")
                    genres = genres.substring(genres.indexOf(". ") + 2)
                    var url = SITE_URL + element1.getElementsByTag("a")[0].attr("href")
                    url = url.substring(0, url.lastIndexOf('/'))
                    val title = element1.getElementsByTag("img")[0].attr("alt")
                    val imageURL: String
                    imageURL = element1.getElementsByTag("img")[0].attr("data-original")
                    movie = Movie(title, ArrayList(Arrays.asList(*genres.split(", ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray())), imageURL, url)

                    var image: Bitmap? = null
                    try {
                        image = getCachedImage(context!!, imageURL)
                        Log.d("STATUS: ", "$imageURL found")
                    } catch (e: FileNotFoundException) {
                        try {
                            imageDownloader = ImageDownloader()
                            image = imageDownloader.execute(imageURL).get()
                            saveImage(context!!, image!!, imageURL)
                        } catch (e1: ExecutionException) {
                            e.printStackTrace()
                        } catch (e1: InterruptedException) {
                            e.printStackTrace()
                        } catch (e1: FileNotFoundException) {
                            e1.printStackTrace()
                        } catch (e1: IOException) {
                            e1.printStackTrace()
                        }

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    movie.image = image
                    movieList.add(movie)
                }
            }
            return Observable.fromArray(movieList)
        }


        val genresList: JSONArray
            @Throws(InterruptedException::class, ExecutionException::class, JSONException::class, NullPointerException::class)
            get() {


                val genresList = JSONArray()
                val URL_PREFIX = "/list/genres/sort_name"
                val pageDownloader = PageDownloader()
                val pageContent: Document

                pageContent = pageDownloader.execute(SITE_URL + URL_PREFIX).get()

                var element = pageContent.getElementsByClass("table table-hover").first()
                element = element.getElementsByTag("tbody").first()
                val elements = element.getElementsByTag("tr")
                var index = 0
                for (element1 in elements) {
                    val tempElement = element1.getElementsByTag("td").first().getElementsByTag("a").first()
                    val jsonObject = JSONObject()
                    val genreName = tempElement.text()
                    var genreLink = tempElement.attr("href")
                    genreLink = genreLink.substring(1)
                    jsonObject.put("name", genreName)
                    jsonObject.put("link", genreLink)
                    genresList.put(index, jsonObject)
                    index++
                }
                return genresList
            }


        private fun getMaxQueryElementCount(pageContent: Document): Int {
            val pattern = Pattern.compile("(\\d+)")
            val matcher: Matcher
            var resultAmount = 0

            var elements = pageContent.getElementsByTag("h4")
            var patternText: String? = null

            for (element in elements) {

                if (element.getElementsContainingText("Список").let {
                            letElements -> elements = letElements; letElements.size != 0 }) {

                    patternText = elements.first().text()
                    break
                }
            }
            if (patternText != null) {
                matcher = pattern.matcher(patternText)

                if (matcher.find())
                    resultAmount = Integer.valueOf(matcher.group(1))
            } else
                resultAmount = 16870 // Текущее количество дорам на сайте
            return resultAmount
        }

        private fun getCurrentListElementCount(pageContent: Document): Int {
            val elements = pageContent.getElementsByClass("tile col-sm-6 ")
            return elements.size
        }

        @Throws(InterruptedException::class, ExecutionException::class, JSONException::class, NullPointerException::class)
        fun getMovieInfo(URL: String): JSONObject {
            val info = JSONObject()
            val pageDownloader = PageDownloader()
            val pageContent: Document
            var name = ""
            var eng_name = ""
            var original_name = ""
            var image_url = ""
            var url = ""
            val genres = ArrayList<String>()
            var description = ""
            var age = ""
            var production = ""

            pageContent = pageDownloader.execute(URL).get()
            var tempElement: Element?
            var tempElements: Elements
            var initialSeries = pageContent.getElementsByClass("subject-actions col-sm-7").first().getElementsByTag("a").last().attr("href")
            initialSeries = initialSeries.substring(initialSeries.lastIndexOf("/"))

            tempElement = pageContent.getElementsByAttributeValue("itemprop", "url").first()
            if (tempElement != null)
                url = tempElement.attr("content")

            tempElement = pageContent.getElementsByClass("name").first()
            if (tempElement != null)
                name = tempElement.text()

            tempElement = pageContent.getElementsByClass("eng-name").first()
            if (tempElement != null)
                eng_name = tempElement.text()

            tempElement = pageContent.getElementsByClass("original-name").first()
            if (tempElement != null)
                original_name = tempElement.text()

            tempElements = pageContent.getElementsByClass("elem_genre ")
            for (element1 in tempElements) {
                genres.add(element1.tagName("a").text())
            }

            tempElement = pageContent.getElementsByClass("manga-description").first()
            if (tempElement != null)
                description = tempElement.text()

            tempElement = pageContent.getElementsByClass("picture-fotorama").first()
            tempElement = tempElement!!.getElementsByTag("img").first()
            if (tempElement != null)
                image_url = tempElement.attr("data-thumb")

            tempElement = pageContent.getElementsByClass("elem_year ").first()
            if (tempElement != null)
                age = tempElement.text()

            tempElement = pageContent.getElementsByClass("elem_country ").first()
            if (tempElement != null)
                production = tempElement.text()

            tempElement = pageContent.getElementsByClass("subject-meta col-sm-7").first()
            tempElements = tempElement!!.getElementsByTag("p")

            tempElement = tempElements[0]
            val seriesNumber = tempElement!!.text()

            tempElement = tempElements[1]
            val duration = tempElement!!.text()


            info.put("title", "$name | $eng_name | $original_name")
            info.put("url", url)
            info.put("genres", genres.toString())
            info.put("image_url", image_url)
            info.put("initial_series", initialSeries)
            info.put("production", production)
            info.put("series_number", seriesNumber)
            info.put("duration", duration)
            info.put("description", description)
            info.put("age", age)

            return info
        }

        private fun movieListContentParse(context: Context?, pageContent: Document, limit: Int): HashMap<String, Any> {
            val movieList = ArrayList<Movie>()
            val result = HashMap<String, Any>()
            val elements = pageContent.getElementsByClass("tile col-sm-6 ")
            var imageDownloader: ImageDownloader
            var movie: Movie
            var iteration = 0
            for (element in elements) {

                if (limit != 0 && iteration > limit - 1)
                    break

                // Отсев книг и манги из результатов поиска
                val tempElements = element.getElementsByClass("tile-info").first().getElementsByTag("a")
                if (tempElements.size != 0) {
                    val genres: String
                    if (tempElements.size > 1) {
                        val stringBuilder = StringBuilder()
                        for (element1 in tempElements) {
                            stringBuilder.append(element1.text())
                            stringBuilder.append(", ")
                        }
                        genres = stringBuilder.toString().substring(0, stringBuilder.toString().lastIndexOf(", "))
                    } else {
                        genres = tempElements.first().text()
                    }

                    var tempElement = element.getElementsByClass("img").first()
                    val url = SITE_URL + tempElement.getElementsByTag("a")[0].attr("href")
                    tempElement = tempElement.getElementsByTag("img").first()
                    val title = tempElement.attr("title")
                    val imageURL = tempElement.attr("data-original")
                    tempElement = element.getElementsByClass("tags").first()

                    movie = Movie(title, ArrayList(Arrays.asList(*genres.split(", ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray())), imageURL, url)

                    var image: Bitmap? = null
                    try {
                        image = getCachedImage(context!!, imageURL)
                        Log.d("STATUS: ", "$imageURL found")
                    } catch (e: FileNotFoundException) {
                        try {
                            imageDownloader = ImageDownloader()
                            image = imageDownloader.execute(imageURL).get()
                            saveImage(context!!, image!!, imageURL)
                        } catch (e1: ExecutionException) {
                            e.printStackTrace()
                        } catch (e1: InterruptedException) {
                            e.printStackTrace()
                        } catch (e1: FileNotFoundException) {
                            e1.printStackTrace()
                        } catch (e1: IOException) {
                            e1.printStackTrace()
                        }

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    movie.image = image
                    movieList.add(movie)
                }
                iteration++
            }
            result["list"] = movieList
            result["offset"] = Integer.valueOf(iteration)
            return result
        }

        @Throws(InterruptedException::class, ExecutionException::class, JSONException::class, NullPointerException::class)
        fun getSources(seriesList: JSONArray, URL: String, seriesIndex: Int): JSONArray {

            /*
        * class=right controls_447318 hide hidden-xs hidden-sm
        *
            Перевод: Озвучка

        * */
            val vkPattern = Pattern.compile("oid=(.?[\\d]+).+id=([\\d]+).+hash=(.+)\" a")
            val VOICE = "Озвучка"
            val SUBS = "Сабы"
            val SUBS_NORMAL = "Субтитры"
            val TRANSLATION_PATTERN = "right controls_447318 hide hidden-xs hidden-sm"
            var matcher: Matcher
            val ADULT_PREFIX = "?mtr=1"
            var pageDownloader: PageDownloader
            var pageContent: Document
            var tempElement: Element?
            val tempElements: Elements
            var translation: String? = null

            val oneSeriesSources = JSONArray()
            pageDownloader = PageDownloader()

            pageContent = pageDownloader.execute(URL + (seriesList.get(seriesIndex) as JSONObject).getString("link") + ADULT_PREFIX).get()
            //Elements elements = pageContent.getElementsByClass("chapter-link");
            val elements = pageContent.getElementsByClass("chapter")
            for (element1 in elements) {
                val jsonObject = JSONObject()
                val subUnit: String
                val seriesID: String
                val oid: String
                val id: String
                val hash: String

                if (element1.getElementsByClass("person-link").first() != null)
                    subUnit = "Фансаб " + element1.getElementsByClass("person-link").first().text()
                else
                    subUnit = "Оригинал"


                if (element1.text().contains(VOICE))
                    translation = VOICE
                else if (element1.text().contains(SUBS))
                    translation = SUBS_NORMAL

                seriesID = element1.getElementsByAttribute("data-sid").first().attr("data-sid")

                pageDownloader = PageDownloader()
                pageContent = pageDownloader.execute(SiteWorker.TRAGUS_URL + seriesID).get()
                tempElement = pageContent.getElementsByTag("iframe").first()

                if (tempElement == null)
                    continue

                val tempURL = tempElement.toString()

                if (tempURL.contains("vk.com")) {
                    matcher = vkPattern.matcher(tempURL)
                    if (matcher.find()) {
                        oid = matcher.group(1)
                        id = matcher.group(2)
                        hash = matcher.group(3)

                        if (translation != null)
                            jsonObject.put("sub_unit", "$subUnit ($translation)")
                        else
                            jsonObject.put("sub_unit", subUnit)

                        jsonObject.put("movie_id", oid + "_" + id)
                        jsonObject.put("hash", hash)
                        oneSeriesSources.put(jsonObject)
                    }
                }
            }
            return oneSeriesSources
        }

        fun formSeriesList(URL: String, initialSeries: String): JSONArray {
            val seriesList = JSONArray()
            val ADULT_PREFIX = "?mtr=1"
            val pageDownloader = PageDownloader()
            val pageContent: Document
            try {
                if (initialSeries == "/") {
                    return seriesList
                }
                pageContent = pageDownloader.execute(URL + initialSeries + ADULT_PREFIX).get()
                val element = pageContent.getElementById("chapterSelectorSelect")
                val elements = element.getElementsByTag("option")
                var index = 0
                for (element1 in elements) {
                    val `object` = JSONObject()
                    `object`.put("name", element1.text())
                    var link = element1.attr("value")
                    link = link.substring(link.lastIndexOf("/"))
                    `object`.put("link", link)
                    seriesList.put(index, `object`)
                    index++
                }

            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }

            return seriesList
        }

        val standartUri: Uri.Builder
            get() {
                val builder = Uri.Builder()
                builder.scheme("http")
                        .authority(SITE_URL1)
                return builder
            }

        private fun transformFileName(url: String): String {
            val pathParts = url.split("/".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            val stringBuilder = StringBuilder()
            stringBuilder.append(pathParts[pathParts.size - 3])
            stringBuilder.append(pathParts[pathParts.size - 2])
            stringBuilder.append(pathParts[pathParts.size - 1])
            return stringBuilder.toString()
        }

        @Throws(FileNotFoundException::class, IOException::class)
        fun saveImage(context: Context, image: Bitmap, url: String) {

            val f = File(context.cacheDir, transformFileName(url))
            f.createNewFile()

            val bos = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos)
            val bitmapdata = bos.toByteArray()

            //write the bytes in file
            val fos = FileOutputStream(f)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
        }

        @Throws(FileNotFoundException::class, IOException::class)
        fun getCachedImage(context: Context, url: String): Bitmap? {
            var image: Bitmap?

            val f = File(context.cacheDir, transformFileName(url))
            val fis = FileInputStream(f)

            image = BitmapFactory.decodeStream(fis)
            fis.close()

            return image
        }
    }

}
