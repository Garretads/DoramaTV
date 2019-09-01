package ru.garretech.garred.doramatv.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log

import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.*
import ru.garretech.garred.doramatv.Settings
import ru.garretech.garred.doramatv.model.Movie

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import ru.garretech.garred.doramatv.model.Series
import ru.garretech.garred.doramatv.model.Source

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.Arrays
import java.util.HashMap
import java.util.concurrent.ExecutionException
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList

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
        var list: ArrayList<Movie>? = null
            private set
        private var parameters: HashMap<String, String>? = null
        private var context: Context? = null


        val nextQuery: Observable<List<Movie>>
            @Throws(ExecutionException::class, InterruptedException::class, NullPointerException::class,ArrayIndexOutOfBoundsException::class, IOException::class)
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

                        if (pageContent == null)
                            throw NullPointerException()

                        if (queryAmount == -1)
                            queryAmount = getMaxQueryElementCount(pageContent)

                        val result = movieListContentParse(context, pageContent, limit)

                        val resultArray = result["list"] as List<Movie>
                        list?.addAll(resultArray)

                        currentOffset += (result["offset"] as Int?)!!
                        Observable.fromArray(resultArray)
                    }

                    SEARCH_QUERY -> {
                        val client = OkHttpClient()

                        uriQuery = standartUri
                        uriQuery!!.appendPath(path)

                        parameters!![OFFSET_PARAM] = currentOffset.toString()

                        val request = searchRequest(uriQuery!!.toString(), parameters!!["q"], parameters!![OFFSET_PARAM])

                        val response = client.newCall(request).execute()
                        val responseString = response.body()?.string()

                        val pageContent = Jsoup.parse(responseString)

                        if (queryAmount == -1)
                            queryAmount = getMaxSearchElementCount(pageContent)

                        val result = movieListContentParse(context, pageContent, limit)

                        val resultArray = result["list"] as List<Movie>

                        list?.addAll(resultArray)

                        currentOffset += (result["offset"] as Int?)!!
                        Observable.fromArray(resultArray)
                    }

                    EDITOR_CHOICE_QUERY -> {
                        queryAmount = 5
                        currentOffset = 5
                        getEditorChoiceMoviesList(context).let { list = ArrayList(); list?.addAll(it); Observable.fromArray(it) }
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

        fun requestUri() : Uri.Builder? {
            return uriQuery
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
        //val SITE_URL = "http://doramatv.live"
        //private val SITE_URL1 = "doramatv.live"
        private val editorChoice = "row tiles-row short"
        val NEW_MOVIES_PARAMS = arrayOf("sortType", "created")
        val LIST_PREFIX = "list"
        val SEARCH_PREFIX = "search"
        val ONGOING_PREFIX = "list/tags/ongoing"
        val ONGOING_PARAMS = arrayOf("sortType", "rate")
        val RANDOM_MOVIE_PREFIX = "/internal/random"
        private val OFFSET_PARAM = "offset"
        val TRAGUS_URL = "http://grass.tragus.ru/internal/videoCode/"
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

            if (element != null) {
                matcher = pattern.matcher(element.text())
                if (matcher.find())
                    resultAmount = Integer.valueOf(matcher.group(1))
            }

            return resultAmount
        }


        @Throws(InterruptedException::class, ExecutionException::class, NullPointerException::class,ArrayIndexOutOfBoundsException::class,IOException::class)
        fun getEditorChoiceMoviesList(context: Context?): List<Movie> {
            val pageDownloader = PageDownloader()
            val pageContent: Document?
            val movieList = ArrayList<Movie>()
            pageContent = pageDownloader.execute(Settings.SITE_URL).get()

            if (pageContent == null)
                throw NullPointerException()

            //var imageDownloader: ImageDownloader
            var movie: Movie

            val tempElements = pageContent.getElementsByClass(editorChoice)
            if (tempElements == null)
                throw NullPointerException()
            else {
                val editorChoiceElements = tempElements.first().getElementsByClass("simple-tile ")

                for (i in editorChoiceElements.indices) {
                    val element1 = editorChoiceElements[i]
                    var url = Settings.SITE_URL + element1.getElementsByTag("a")[0].attr("href")
                    url = url.substring(0, url.lastIndexOf('/'))
                    val title = element1.getElementsByTag("img")[0].attr("alt")
                    val imageURL: String
                    imageURL = element1.getElementsByTag("img")[0].attr("data-original")
                    movie = Movie(title, imageURL, url)

                    /*var image: Bitmap? = null
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
                    }*/

                    //movie.image = image
                    movieList.add(movie)
                }
            }
            return movieList
        }


        val genresList =
           Single.create<JSONArray> {


                val genresList = JSONArray()
                val URL_PREFIX = "/list/genres/sort_name"
                val pageDownloader = PageDownloader()
                val pageContent: Document?

                pageContent = pageDownloader.execute(Settings.SITE_URL + URL_PREFIX).get()

                if (pageContent == null)
                    throw NullPointerException()

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

                it.onSuccess(genresList)
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

        @Throws(InterruptedException::class, ExecutionException::class, JSONException::class, NullPointerException::class,ArrayIndexOutOfBoundsException::class)
        fun getMovieInfo(URL: String) = Single.create<Movie> {
            val info = JSONObject()
            val pageDownloader = PageDownloader()
            val pageContent: Document?
            var name = ""
            var eng_name = ""
            var original_name = ""
            var image_url = ""
            var url = ""
            var genres : StringBuilder = StringBuilder()
            var description = ""
            var age = ""
            var production = ""

            pageContent = pageDownloader.execute(URL).get()

            if (pageContent == null) {
                throw NullPointerException()
            }

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
                genres.append(element1.tagName("a").text())
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


            val movie = Movie("$name | $eng_name | $original_name", image_url,url).also {
                it.genres = genres.split(", ")
                it.initialSeries = initialSeries
                it.productionCountry = production
                it.seriesNumber = seriesNumber
                it.duration = duration
                it.description = description
                it.productionYear = age
            }

            it.onSuccess(movie)
        }

        @Throws(ArrayIndexOutOfBoundsException::class)
        private fun movieListContentParse(context: Context?, pageContent: Document, limit: Int): HashMap<String, Any> {
            val movieList = ArrayList<Movie>()
            val result = HashMap<String, Any>()
            val elements = pageContent.getElementsByClass("tile col-sm-6 ")
            //var imageDownloader: ImageDownloader
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
                    val url = Settings.SITE_URL + tempElement.getElementsByTag("a")[0].attr("href")
                    tempElement = tempElement.getElementsByTag("img").first()
                    val title = tempElement.attr("title")
                    val imageURL = tempElement.attr("data-original")
                    tempElement = element.getElementsByClass("tags").first()

                    movie = Movie(title, imageURL, url)

                    /*var image: Bitmap? = null
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
                    }*/

                    //movie.image = image
                    movieList.add(movie)
                }
                iteration++
            }
            result["list"] = movieList
            result["offset"] = Integer.valueOf(iteration)
            return result
        }

        @Throws(NullPointerException::class,ArrayIndexOutOfBoundsException::class)
        fun getSortingParams(uri: Uri) : JSONArray {
            /*
            * sortType = name,rate,votes,created,updated
            (По алфавиту,по популярности,по рейтингу,новинки,по дате добавления)
            filter = high_rate,single,mature,completed,translated,many_chapters,wait_upload
            (Все,Высокий рейтинг,Полнометражка,Для взрослых,Завершенная,Переведено,Длинная,Ожидает загрузки)

            Выбор жанра /genre/%жанр

            Выбор страны /country/%страна : vetnam, hong_kong, indoneziia, china, malaiziia, north_korea, singapore, thailand, taiwan, philippines, south_korea, japan

            Прочее /tags/%тэг : web, stopped, mini_drama, ongoing, omnibus, coming_soon

            Рубрики : страна, жанр, прочее (/country/%страна, /genre/%жанр, /tags/%тэг)

            Модификаторы : сортировка, фильтр (sortType, filter)

            * Сам объект массив параметров с возможными значениями
            * Один элемент содержит:
            * sortingName =
            * type = prefix, param
            * key = /genre/, /tags/, /country/, sortType, filter
            * values = { , , }
            * translatedValues = { , , }
            * */

            var pageDownloader: PageDownloader
            var pageContent: Document
            var sortingContent : Element
            var tempElements : Elements
            val firstParamPattern = Pattern.compile("\\?(\\w+)=(\\w+)")
            val secondParamPattern = Pattern.compile("\\&(\\w+)=(\\w+)")
            val prefixPattern = Pattern.compile("\\/(\\w+)\\/(\\w+)\\?")



            pageDownloader = PageDownloader()
            pageContent = pageDownloader.execute(uri.toString()).get()

            if (pageContent == null) {
                throw NullPointerException()
            }

            sortingContent = pageContent.getElementsByClass("rightContent").first()
            // Формируем список возможных параметров (список хранится в теге ul)
            tempElements = sortingContent.getElementsByTag("ul")

            var index = 0
            val sortingVarJsonArray = JSONArray()
            val selectedOptionsJsonArray = JSONArray()
            for (element in tempElements) {
                val jsonObject = JSONObject()
                val loopElements = element.getElementsByTag("li")
                val selectedElements = element.getElementsByClass("listSelected")
                var name : String

                // Собираем основную информацию для json объекта
                /* sortingName =
                * type = prefix, param
                * isSelected
                * key = /genre/, /tags/, /country/, sortType, filter
                * */

                val element1 = loopElements.last().getElementsByTag("a")
                val link = element1.attr("href")
                var matcher : Matcher

                name = when (index)  {
                    0 -> "Сортировка"
                    1 -> "Фильтр"
                    2 -> "Жанры"
                    3 -> "Страны"
                    4 -> "Прочее"
                    else -> ""
                }

                matcher = when(index) {
                    0 -> firstParamPattern.matcher(link)
                    1 -> secondParamPattern.matcher(link)
                    2,3,4 -> prefixPattern.matcher(link)
                    else -> firstParamPattern.matcher(link)
                }


                if (matcher.find()) {
                    jsonObject.put("sortingName", name)

                    if (index < 2)
                        jsonObject.put("type", "param")
                    else
                        jsonObject.put("type", "prefix")

                    jsonObject.put("key", matcher.group(1))

                    val valuesArray = JSONArray()
                    val translatedValuesArray = JSONArray()
                    var position = 0
                    var selectedPosition : Int = -1

                    for (liElement in loopElements) {
                        val element1 = liElement.getElementsByTag("a")
                        val link = element1.attr("href")
                        val translatedValue = element1.text()
                        var matcherInternalLoop: Matcher

                        if (liElement.toString().contains("listSelected")) {
                            selectedPosition = position
                        }

                        if (liElement.toString().contains("Все")) {
                            valuesArray.put("")
                            translatedValuesArray.put("Все")
                        }

                        matcherInternalLoop = when(index) {
                            0 -> firstParamPattern.matcher(link)
                            1 -> secondParamPattern.matcher(link)
                            2,3,4 -> prefixPattern.matcher(link)
                            else -> firstParamPattern.matcher(link)
                        }

                        if (matcherInternalLoop.find()) {
                            valuesArray.put(matcherInternalLoop.group(2))
                            translatedValuesArray.put(translatedValue)
                        }
                        position++
                    }
                    jsonObject.put("selectedPosition", selectedPosition)
                    jsonObject.put("values", valuesArray)
                    jsonObject.put("translatedValues", translatedValuesArray)

                    sortingVarJsonArray.put(jsonObject)
                }
                index++
            }
            return sortingVarJsonArray
        }

       /* fun getSources(seriesList: ArrayList<Series>, URL: String, seriesIndex: Int) =

            Single.create<JSONArray> {
                val VOICE = "Озвучка"
                val SUBS = "Сабы"
                val SUBS_NORMAL = "Субтитры"
                val ADULT_PREFIX = "?mtr=1"
                var pageDownloader: PageDownloader
                var pageContent: Document
                var translation: String? = null

                val oneSeriesSources = JSONArray()
                pageDownloader = PageDownloader()

                pageContent = pageDownloader.execute(URL + (seriesList.get(seriesIndex) as JSONObject).getString("link") + ADULT_PREFIX).get()

                if (pageContent == null) {
                    it.onError(NullPointerException())
                }

                val elements = pageContent.getElementsByClass("chapter")
                for (element1 in elements) {
                    val jsonObject = JSONObject()
                    val subUnit: String
                    val seriesID: String = element1.getElementsByAttribute("data-sid").first().attr("data-sid")
                    val sourceName : String = element1.getElementsByClass("text-additional").first().text()

                    if (element1.getElementsByClass("person-link").first() != null)
                        subUnit = "Фансаб " + element1.getElementsByClass("person-link").first().text()
                    else
                        subUnit = "Оригинал"

                    if (element1.text().contains(VOICE))
                        translation = VOICE
                    else if (element1.text().contains(SUBS))
                        translation = SUBS_NORMAL

                    jsonObject.put("sources_name", sourceName)

                    jsonObject.put("series_id", seriesID)

                    if (translation != null)
                        jsonObject.put("sub_unit", "$subUnit ($translation) $sourceName")
                    else
                        jsonObject.put("sub_unit", "$subUnit $sourceName")

                    oneSeriesSources.put(jsonObject)
                }
                it.onSuccess(oneSeriesSources)
            }*/


        fun getSources(series: Series, URL: String) =

                Single.create<ArrayList<Source>> {
                    val VOICE = "Озвучка"
                    val SUBS = "Сабы"
                    val SUBS_NORMAL = "Субтитры"
                    val ADULT_PREFIX = "?mtr=1"
                    var pageDownloader: PageDownloader
                    var pageContent: Document
                    var translation: String? = null

                    val oneSeriesSources = ArrayList<Source>()
                    pageDownloader = PageDownloader()

                    pageContent = pageDownloader.execute(URL + series.url + ADULT_PREFIX).get()

                    if (pageContent == null) {
                        it.onError(NullPointerException())
                    }

                    val elements = pageContent.getElementsByClass("chapter")
                    for (element1 in elements) {
                        val subUnit: String
                        val seriesID: String = element1.getElementsByAttribute("data-sid").first().attr("data-sid")
                        val sourceName : String = element1.getElementsByClass("text-additional").first().text()

                        if (element1.getElementsByClass("person-link").first() != null)
                            subUnit = "Фансаб " + element1.getElementsByClass("person-link").first().text()
                        else
                            subUnit = "Оригинал"

                        if (element1.text().contains(VOICE))
                            translation = VOICE
                        else if (element1.text().contains(SUBS))
                            translation = SUBS_NORMAL

                        val source = if (translation != null)
                            Source(seriesID.toInt(),sourceName, "$subUnit ($translation) $sourceName")
                        else
                            Source(seriesID.toInt(),sourceName, "$subUnit $sourceName")

                        oneSeriesSources.add(source)
                    }
                    it.onSuccess(oneSeriesSources)
                }


        /*fun formSeriesList(URL: String, initialSeries: String) =

        Single.create<JSONArray> {
            val seriesList = JSONArray()
            val ADULT_PREFIX = "?mtr=1"
            val pageDownloader = PageDownloader()
            val pageContent: Document

            if (initialSeries == "/") {
                it.onSuccess(seriesList)
            }

            pageContent = pageDownloader.execute(URL + initialSeries + ADULT_PREFIX).get()

            if (pageContent == null) it.onError(NullPointerException())

            val element = pageContent.getElementById("chapterSelectorSelect")

            if (element == null) it.onError(NullPointerException())

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
            it.onSuccess(seriesList)
        }*/


        fun formSeriesList(URL: String, initialSeries: String) =

                Single.create<ArrayList<Series>> {
                    //val seriesList = JSONArray()

                    val seriesList = ArrayList<Series>()
                    val ADULT_PREFIX = "?mtr=1"
                    val pageDownloader = PageDownloader()
                    val pageContent: Document

                    if (initialSeries == "/") {
                        it.onSuccess(seriesList)
                    }

                    pageContent = pageDownloader.execute(URL + initialSeries + ADULT_PREFIX).get()

                    if (pageContent == null) it.onError(NullPointerException())

                    val element = pageContent.getElementById("chapterSelectorSelect")

                    if (element == null) it.onError(NullPointerException())

                    val elements = element.getElementsByTag("option")
                    var index = 0

                    for (element1 in elements) {
                        val name = element1.text()

                        var link = element1.attr("value")
                        link = link.substring(link.lastIndexOf("/"))

                        seriesList.add(Series(index,name).also { it.url = link })
                        index++
                    }
                    it.onSuccess(seriesList)
                }


        val standartUri: Uri.Builder
            get() {
                val builder = Uri.Builder()
                builder.scheme("http").authority(Settings.SITE_URL1)
                return builder
            }


        @Throws(Exception::class)
        fun searchRequest(vararg params : String?) : Request {
            val body = FormBody.Builder()
                    .addEncoded("q",params[1]!!)
                    .addEncoded("offset",params[2]!!)
                    .build()

            return Request.Builder().url(params[0]!!).post(body).build()
        }

    }

}
