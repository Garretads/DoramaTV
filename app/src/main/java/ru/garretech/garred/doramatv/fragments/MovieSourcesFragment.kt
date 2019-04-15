package ru.garretech.garred.doramatv.fragments

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.util.ArrayList
import java.util.concurrent.ExecutionException

import ru.garretech.garred.doramatv.R
import ru.garretech.garred.doramatv.Settings
import ru.garretech.garred.doramatv.tools.SiteWorker
import ru.garretech.garred.doramatv.tools.VKRequest


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [MovieSourcesFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [MovieSourcesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MovieSourcesFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private val mInfo: String? = null
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
    private var conMgr: ConnectivityManager? = null

    private var mListener: OnFragmentInteractionListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            try {
                progressBottomSheet = ProgressBottomSheet()
                conMgr = activity!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                sourcesInfo = JSONObject(arguments!!.getString(ARG_PARAM1))
                URL = sourcesInfo.getString("url")
                accessToken = sourcesInfo.getString("access_token")
                initialSeries = sourcesInfo.getString("initial_series")
                seriesList = SiteWorker.formSeriesList(URL, initialSeries)
                listViewList = ArrayList<String>()

                if (seriesList.length() == 0) {
                    listViewList.add("Пусто")
                    empty = true
                } else {
                    for (i in 0 until seriesList.length()) {
                        listViewList.add((seriesList.get(i) as JSONObject).getString("name"))
                    }
                }

                arrayAdapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, listViewList)
                arrayAdapter!!.setNotifyOnChange(true)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_movie_sources, container, false)
        listView = view.findViewById(R.id.sourcesListView)
        listView.adapter = arrayAdapter
        val backSymbolText = "<-- "

        if (!empty) {
            listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, i, l ->
                if (hasConnection()!!) {
                    if (!seriesSelected) {
                        Handler().postDelayed({
                            try {
                                sourcesArray = SiteWorker.getSources(seriesList, URL, i)
                                var funSubList = ArrayList<String>()
                                funSubList.add(backSymbolText + (seriesList.get(i) as JSONObject).getString("name"))
                                for (index in 0 until sourcesArray.length()) {

                                    val jsonObject = sourcesArray.get(index) as JSONObject
                                    funSubList.add(jsonObject.getString("sub_unit"))
                                }
                                arrayAdapter!!.clear()
                                arrayAdapter!!.addAll(funSubList)
                                arrayAdapter!!.notifyDataSetChanged()
                                seriesSelected = true
                                if (progressBottomSheet.isVisible)
                                    progressBottomSheet.dismiss()
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            } catch (e: ExecutionException) {
                                e.printStackTrace()
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            } catch (e: NullPointerException) {
                                showConnectionError()
                            }
                        }, 1000)
                        if (!progressBottomSheet.isAdded) {
                            progressBottomSheet.show(fragmentManager!!, "progressBar")
                        }
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
                                //Выбор качества, воспроизведение
                                /*
                                     * Формируем запрос в vk api
                                     * https://api.vk.com/method/video.get?videos=-66384560_456239143&access_token=d053e5de82599c59b61a8a138cfe732d462a245623f8807ee3a4bf5a9dad3e22f1179377b0499001932f0&v=5.92
                                     *
                                     * Парсим ответ в JSONObject. Выцепляем оттуда
                                     *
                                     *
                                     *
                                     * */
                                Handler().postDelayed({
                                    try {
                                        val jsonObject = sourcesArray.get(i - 1) as JSONObject
                                        val METHOD_NAME = "video.get"
                                        val builder = Uri.Builder()
                                        builder.scheme("https")
                                                .authority("api.vk.com")
                                                .appendPath("method")
                                                .appendPath(METHOD_NAME)
                                                .appendQueryParameter("videos", jsonObject.getString("movie_id"))
                                                .appendQueryParameter("access_token", accessToken)
                                                .appendQueryParameter("v", Settings.version())
                                        builder.build()

                                        val vkRequest = VKRequest()
                                        var `object` = JSONObject(vkRequest.execute(builder.toString()).get())
                                        `object` = `object`.get("response") as JSONObject
                                        `object` = (`object`.get("items") as JSONArray).get(0) as JSONObject
                                        val fileLink = `object`.get("files") as JSONObject

                                        val selectQualityFragment = SelectQualityFragment.newInstance(fileLink)
                                        selectQualityFragment.show(fragmentManager!!, "Выберите качество")
                                        if (progressBottomSheet.isVisible)
                                            progressBottomSheet.dismiss()
                                    } catch (e: InterruptedException) {
                                        e.printStackTrace()
                                    } catch (e: ExecutionException) {
                                        e.printStackTrace()
                                    } catch (e: JSONException) {
                                        e.printStackTrace()
                                    } catch (e: NullPointerException) {
                                        showConnectionError()
                                    }
                                }, 1000)
                                if (!progressBottomSheet.isAdded) {
                                    progressBottomSheet.show(fragmentManager!!, "progressBar")
                                }

                            }
                        }
                    }
                } else {
                    showConnectionError()
                }
            }
        }
        return view
    }


    fun onButtonPressed(uri: Uri) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    internal fun showConnectionError() {
        if (progressBottomSheet.isVisible)
            progressBottomSheet.dismiss()
        Toast.makeText(context, getText(R.string.cant_connect_error), Toast.LENGTH_SHORT).show()
    }

    internal fun hasConnection(): Boolean? {
        return conMgr!!.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).state == NetworkInfo.State.CONNECTED || conMgr!!.getNetworkInfo(ConnectivityManager.TYPE_WIFI).state == NetworkInfo.State.CONNECTED
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_PARAM1 = "info"


        // TODO: Rename and change types and number of parameters
        fun newInstance(sourcesInfo: JSONObject): MovieSourcesFragment {
            val fragment = MovieSourcesFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, sourcesInfo.toString())
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
