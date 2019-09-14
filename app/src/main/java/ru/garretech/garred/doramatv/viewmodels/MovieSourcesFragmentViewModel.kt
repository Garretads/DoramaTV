package ru.garretech.garred.doramatv.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.nodes.Element
import ru.garretech.garred.doramatv.database.AppDataSource
import ru.garretech.garred.doramatv.model.Movie
import ru.garretech.garred.doramatv.model.Series
import ru.garretech.garred.doramatv.model.Source
import ru.garretech.garred.doramatv.tools.HistoryProvider
import ru.garretech.garred.doramatv.tools.PageDownloader
import ru.garretech.garred.doramatv.tools.SiteWorker
import ru.garretech.garred.doramatv.tools.VKRequest
import java.util.regex.Pattern

class MovieSourcesFragmentViewModel(application: Application) : AndroidViewModel(application) {

    var currentMovie: Movie? = null
    var seriesList : List<Series> = ArrayList<Series>()
    var sourcesArray : List<Source> = ArrayList<Source>()
    var dataSource = AppDataSource(application)
    lateinit var historyProvider : HistoryProvider

    fun getWatchedIdInSeries(seriesIndex : Int) =
            historyProvider.getWatchedIdInSeries(seriesIndex)

    fun getWatchedSeriesIndexes() =
            historyProvider.getWatchedSeriesIndexes()

    fun getOneSeriesSources(series: Series, url : String) =
            SiteWorker.getSources(series, url)
                    .map {
                        sourcesArray = it
                        sourcesArray
                    }
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())


    fun getSeriesList(url : String, initial : String) =
            SiteWorker.formSeriesList(url, initial)
                    .map {
                        seriesList = it
                        seriesList
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

    fun getVkLink(source: Source) =
            Single.create<String> {
                val sourceId = source.sourceId
                val vkPattern = Pattern.compile("oid=(.?[\\d]+).+id=([\\d]+).+hash=(.+)\" a")
                var vkMovieId: String?


                val pageDownloader = PageDownloader()
                val pageContent = pageDownloader.execute(SiteWorker.TRAGUS_URL + sourceId).get()

                val tempElement = pageContent?.getElementsByTag("iframe")?.first()

                if (tempElement == null)
                    it.onError(NullPointerException())


                val rawURLString = tempElement.toString()

                val matcher = vkPattern.matcher(rawURLString)

                if (matcher.find()) {
                    val oid = matcher.group(1)
                    val id = matcher.group(2)
                    vkMovieId = "${oid}_$id"
                    var fileLink: JSONObject?
                    it.onSuccess(vkMovieId)
                }
                else
                    it.onError(NullPointerException())

            }.flatMap {
                VKRequest().loadSourcesAsSingle(it)
            }.map {
                var jsonObject = it.get("response") as JSONObject
                var jsonArray = jsonObject.get("items") as JSONArray

                if (jsonArray.length() !=0) {
                    jsonObject = jsonArray.get(0) as JSONObject
                    var fileLink = jsonObject.get("files") as JSONObject
                    fileLink
                }
                else JSONObject()
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    fun getOthersLink(source: Source) =
            Single.create<String> {
                val pageDownloader = PageDownloader()

                val sourceName = source.name
                val seriesId = source.sourceId

                val pageContent = pageDownloader.execute(SiteWorker.TRAGUS_URL + seriesId).get()
                val tempElement: Element? = pageContent?.getElementsByTag("iframe")?.first()

                if (tempElement == null)
                    it.onError(NullPointerException())

                var rawURLString = tempElement?.attr("src")

                if (rawURLString == null)
                    it.onError(NullPointerException())
                else
                    it.onSuccess(rawURLString)
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    fun getHistory() =
            dataSource.getHistory(currentMovie!!)
                    .map {
                        historyProvider = HistoryProvider(it)
                    }
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    fun addToHistory() =
            dataSource.saveHistory(historyProvider.history)
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())


    fun getMovieFromDatabase(url : String) = dataSource.getMovie(url)
            .map {
                currentMovie = it
                it
            }
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())


}