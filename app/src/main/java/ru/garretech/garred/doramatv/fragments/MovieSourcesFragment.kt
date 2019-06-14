package ru.garretech.garred.doramatv.fragments

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.nodes.Element

import java.util.ArrayList
import java.util.concurrent.ExecutionException

import ru.garretech.garred.doramatv.R
import ru.garretech.garred.doramatv.Settings
import ru.garretech.garred.doramatv.activities.WebViewActivity
import ru.garretech.garred.doramatv.tools.*
import java.io.IOException
import java.util.regex.Pattern


class MovieSourcesFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private var arrayAdapter: ArrayAdapter<String>? = null
    internal lateinit var seriesList: JSONArray
    internal lateinit var listViewList: ArrayList<String>
    internal lateinit var listView: ListView
    internal lateinit var sourcesArray: JSONArray
    internal lateinit var sourcesInfo: JSONObject
    internal lateinit var URL: String
    internal lateinit var accessToken: String
    private var seriesSelected: Boolean = false
    internal lateinit var progressBottomSheet: ProgressBottomSheet
    internal lateinit var initialSeries: String
    internal var empty: Boolean = false
    val backSymbolText = "<-- "
    private var bag : CompositeDisposable = CompositeDisposable()

    val adapterClickListener = AdapterView.OnItemClickListener { _, _, i, _ ->
        if (hasConnection()) {
            if (!seriesSelected) {
                if (!progressBottomSheet.isAdded) {
                    progressBottomSheet.show(fragmentManager!!, "progressBar")
                }

                bag.add(getSourcesSingle(seriesList,URL,i).observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe( { jsonArray ->

                            var funSubList = ArrayList<String>()
                            funSubList.add(backSymbolText + (seriesList.get(i) as JSONObject).getString("name"))
                            for (index in 0 until jsonArray.length()) {
                                val jsonObject = jsonArray.get(index) as JSONObject
                                funSubList.add(jsonObject.getString("sub_unit"))
                            }
                            arrayAdapter!!.clear()
                            arrayAdapter!!.addAll(funSubList)
                            arrayAdapter!!.notifyDataSetChanged()
                            seriesSelected = true

                            if (progressBottomSheet.isAdded && progressBottomSheet.isVisible)
                                progressBottomSheet.dismissAllowingStateLoss()
                        }, { error ->
                            Log.d("SOURCE LIST","ERROR LOADING SOURCES LIST")
                        }))
            } else {
                when (i) {
                    0 -> {
                        try {
                            arrayAdapter!!.clear()
                            for (index in 0 until seriesList.length()) {
                                val seriesName = (seriesList.get(index) as JSONObject).getString("name")
                                arrayAdapter!!.add(seriesName)
                            }
                            arrayAdapter!!.notifyDataSetChanged()
                            seriesSelected = false
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    else -> {
                        if (!progressBottomSheet.isAdded) {
                            progressBottomSheet.show(fragmentManager!!, "progressBar")
                        }

                        Handler().postDelayed({
                            try {
                                val jsonObject = sourcesArray.get(i - 1) as JSONObject
                                val vkPattern = Pattern.compile("oid=(.?[\\d]+).+id=([\\d]+).+hash=(.+)\" a")
                                val sourceName = jsonObject.getString("sources_name")
                                val seriesId = jsonObject.getString("series_id")
                                var vkMovieId: String?

                                if (sourceName.contains("vk.com")) {

                                    val pageDownloader = PageDownloader()
                                    val pageContent = pageDownloader.execute(SiteWorker.TRAGUS_URL + seriesId).get()

                                    val tempElement: Element = pageContent?.getElementsByTag("iframe")?.first()
                                            ?: return@postDelayed

                                    val rawURLString = tempElement.toString()

                                    val matcher = vkPattern.matcher(rawURLString)

                                    if (matcher.find()) {
                                        val oid = matcher.group(1)
                                        val id = matcher.group(2)
                                        vkMovieId = "${oid}_${id}"

                                        var fileLink: JSONObject? = null

                                        VKRequest().loadSourcesAsSingle(vkMovieId).observeOn(AndroidSchedulers.mainThread())
                                                .subscribeOn(Schedulers.io())
                                                .subscribe({ posting ->
                                                    var jsonObject = posting.get("response") as JSONObject
                                                    var jsonArray = jsonObject.get("items") as JSONArray

                                                    if (jsonArray?.length() !=0) {
                                                        jsonObject = jsonArray.get(0) as JSONObject
                                                        fileLink = jsonObject.get("files") as JSONObject
                                                        val selectQualityFragment = SelectQualityFragment.newInstance(fileLink!!)
                                                        selectQualityFragment.show(fragmentManager!!, "Выберите качество")
                                                    } else
                                                        Toast.makeText(context,"Ошибка при загрузке списка качеств, попробуйте еще раз",Toast.LENGTH_LONG).show()

                                                    if (progressBottomSheet.isAdded && progressBottomSheet.isVisible)
                                                        progressBottomSheet.dismissAllowingStateLoss()
                                                }, { error ->
                                                    Log.d("QUALITY ERROR", "ERROR IN LOADING QUALITY LIST")
                                                })
                                    }
                                } else {
                                    val intent = Intent(activity, WebViewActivity::class.java)

                                    val pageDownloader = PageDownloader()
                                    val pageContent = pageDownloader.execute(SiteWorker.TRAGUS_URL + seriesId).get()
                                    val tempElement: Element? = pageContent?.getElementsByTag("iframe")?.first()
                                    var rawURLString = tempElement?.attr("src")
                                    if (tempElement != null && !rawURLString!!.contains("http") && !rawURLString!!.contains("https")) rawURLString = "https:" + rawURLString
                                    intent.putExtra("link", rawURLString)

                                    if (progressBottomSheet.isAdded && progressBottomSheet.isVisible)
                                        progressBottomSheet.dismissAllowingStateLoss()

                                    startActivity(intent)
                                }

                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            } catch (e: ExecutionException) {
                                e.printStackTrace()
                            } catch (e: JSONException) {
                                showConnectionError()
                            } catch (e: NullPointerException) {
                                showConnectionError()
                            } catch (e: IOException) {
                                showConnectionError()
                            } catch (e : ArrayIndexOutOfBoundsException) {
                                e.printStackTrace()
                            }
                        }, 100)
                    }
                }
            }
        } else {
            showConnectionError()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            try {
                progressBottomSheet = ProgressBottomSheet()
                sourcesInfo = JSONObject(arguments!!.getString(ARG_PARAM1))
                URL = sourcesInfo.getString("url")
                accessToken = Settings.access_token
                initialSeries = sourcesInfo.getString("initial_series")

                arrayAdapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, ArrayList())
                arrayAdapter!!.setNotifyOnChange(true)

                bag.add(getSeriesSingle(URL,initialSeries).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe( { jsonArray ->

                            listViewList = ArrayList<String>()

                            if (jsonArray.length() == 0) {
                                listViewList.add("Пусто")
                                empty = true
                            } else {
                                for (i in 0 until jsonArray.length()) {
                                    listViewList.add((jsonArray.get(i) as JSONObject).getString("name"))
                                }
                            }
                            arrayAdapter?.addAll(listViewList)
                            arrayAdapter?.notifyDataSetChanged()

                        }, { error ->
                            Log.d("SERIES LIST","ERROR LOADING SERIES LIST")
                        }))

            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_movie_sources, container, false)
        listView = view.findViewById(R.id.sourcesListView)
        listView.adapter = arrayAdapter

        if (!empty) listView.onItemClickListener = adapterClickListener

        return view
    }


    private fun getSeriesSingle(url : String, initial : String) : Single<JSONArray> {
        return Single.create<JSONArray> { observer ->
            try {
                seriesList = SiteWorker.formSeriesList(url, initial)
                observer.onSuccess(seriesList)
            } catch (e: InterruptedException) {
                if (!observer.isDisposed)
                    observer.onError(e)
            } catch (e: ExecutionException) {
                if (!observer.isDisposed)
                    observer.onError(e)
            } catch (e: JSONException) {
                if (!observer.isDisposed)
                    observer.onError(e)
            } catch (e: NullPointerException) {
                if (!observer.isDisposed)
                    observer.onError(e)
            } catch (e: ArrayIndexOutOfBoundsException) {
                if (!observer.isDisposed)
                    observer.onError(e)
            }
        }
    }


    private fun getSourcesSingle(list : JSONArray, url : String, i : Int) : Single<JSONArray> {
        return Single.create<JSONArray> { observer ->
            try {
                sourcesArray = SiteWorker.getSources(list, url, i)
                observer.onSuccess(sourcesArray)
            } catch (e: InterruptedException) {
                if (!observer.isDisposed)
                    observer.onError(e)
            } catch (e: ExecutionException) {
                if (!observer.isDisposed)
                    observer.onError(e)
            } catch (e: JSONException) {
                if (!observer.isDisposed)
                    observer.onError(e)
            } catch (e: NullPointerException) {
                if (!observer.isDisposed)
                    observer.onError(e)
            }
        }
    }

    internal fun showConnectionError() {
        if (progressBottomSheet.isAdded && progressBottomSheet.isVisible)
            progressBottomSheet.dismissAllowingStateLoss()
        Toast.makeText(context, getText(R.string.cant_connect_error), Toast.LENGTH_SHORT).show()
    }

    internal fun hasConnection(): Boolean {
        val cm = activity!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val ni = cm.getActiveNetworkInfo()
        return ni != null && ni.isConnected
    }

    companion object {
        private val ARG_PARAM1 = "info"


        fun newInstance(sourcesInfo: JSONObject): MovieSourcesFragment {
            val fragment = MovieSourcesFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, sourcesInfo.toString())
            fragment.arguments = args
            return fragment
        }
    }
}
