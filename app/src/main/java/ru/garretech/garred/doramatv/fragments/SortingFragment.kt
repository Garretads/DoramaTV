package ru.garretech.garred.doramatv.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.TableRow
import org.json.JSONArray
import org.json.JSONObject

import ru.garretech.garred.doramatv.R
import ru.garretech.garred.doramatv.adapters.CustomTableLayout


class SortingFragment : DialogFragment() {
    private var initialURL: Uri? = null
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var paramsJSONArray : JSONArray

    private lateinit var countriesRadioGroup : CustomTableLayout
    private lateinit var sortingRadioGroup : CustomTableLayout
    private lateinit var filterRadioGroup : CustomTableLayout
    private lateinit var othersRadioGroup : CustomTableLayout

    private var optionsMap = HashMap<String,JSONObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            paramsJSONArray = JSONArray(it.getString(ARG_PARAM1))
            initialURL = Uri.parse(it.getString(ARG_PARAM2))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sorting, container, false)
        var selectedPosition : Int
        countriesRadioGroup = view.findViewById<CustomTableLayout>(R.id.countriesRadioGroup)
        sortingRadioGroup = view.findViewById<CustomTableLayout>(R.id.sortingRadioGroup)
        filterRadioGroup = view.findViewById<CustomTableLayout>(R.id.filterRadioGroup)
        othersRadioGroup = view.findViewById<CustomTableLayout>(R.id.othersRadioGroup)



        var jsonObject = paramsJSONArray.getJSONObject(COUNTRIES_INDEX)
        optionsMap.put("countries",jsonObject)
        selectedPosition = jsonObject.getString("selectedPosition").toInt()
        var namesJson = jsonObject.getJSONArray("translatedValues")

        var tableRow = TableRow(context)
        for (i in 0 until namesJson.length()) {
            if (i % 3 == 0) {
                countriesRadioGroup.addView(tableRow)
                tableRow = TableRow(context)
            }
            val radioButton = inflater.inflate(R.layout.custom_radio_button,null) as RadioButton
            radioButton.text = namesJson.getString(i)
            radioButton.tag = i
            tableRow.addView(radioButton)
        }
        countriesRadioGroup.addView(tableRow)
        if (selectedPosition != -1) countriesRadioGroup.checkChildAt(selectedPosition)



        jsonObject = paramsJSONArray.getJSONObject(SORTING_INDEX)
        optionsMap.put("sorting",jsonObject)
        selectedPosition = jsonObject.getString("selectedPosition").toInt()
        namesJson = jsonObject.getJSONArray("translatedValues")

        tableRow = TableRow(context)
        for (i in 0 until namesJson.length()) {
            if (i % 2 == 0) {
                sortingRadioGroup.addView(tableRow)
                tableRow = TableRow(context)
            }
            val radioButton = inflater.inflate(R.layout.custom_radio_button,null) as RadioButton
            radioButton.text = namesJson.getString(i)
            radioButton.tag = i
            tableRow.addView(radioButton)
        }
        sortingRadioGroup.addView(tableRow)
        if (selectedPosition != -1) sortingRadioGroup.checkChildAt(selectedPosition)



        jsonObject = paramsJSONArray.getJSONObject(FILTER_INDEX)
        optionsMap.put("filter",jsonObject)
        selectedPosition = jsonObject.getString("selectedPosition").toInt()
        namesJson = jsonObject.getJSONArray("translatedValues")

        tableRow = TableRow(context)
        for (i in 0 until namesJson.length()) {
            if (i % 2 == 0) {
                filterRadioGroup.addView(tableRow)
                tableRow = TableRow(context)
            }
            val radioButton = inflater.inflate(R.layout.custom_radio_button,null) as RadioButton
            radioButton.text = namesJson.getString(i)
            radioButton.tag = i
            tableRow.addView(radioButton)
        }
        filterRadioGroup.addView(tableRow)
        if (selectedPosition != -1) filterRadioGroup.checkChildAt(selectedPosition)



        jsonObject = paramsJSONArray.getJSONObject(OTHERS_INDEX)
        optionsMap.put("others",jsonObject)
        selectedPosition = jsonObject.getString("selectedPosition").toInt()
        namesJson = jsonObject.getJSONArray("translatedValues")

        tableRow = TableRow(context)
        for (i in 0 until namesJson.length()) {
            if (i % 2 == 0) {
                othersRadioGroup.addView(tableRow)
                tableRow = TableRow(context)
            }
            val radioButton = inflater.inflate(R.layout.custom_radio_button,null) as RadioButton
            radioButton.text = namesJson.getString(i)
            radioButton.tag = i
            tableRow.addView(radioButton)
        }

        othersRadioGroup.addView(tableRow)
        if (selectedPosition != -1) othersRadioGroup.checkChildAt(selectedPosition)

        val sortingButton = view.findViewById<Button>(R.id.sortButton)
        sortingButton.setOnClickListener { onSortButtonClick() }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(result : Map<String,Any>)
    }

    private fun onSortButtonClick() {
        listener?.onFragmentInteraction(formQueryString())
        dismiss()
    }

    private fun formQueryString() : Map<String,Any> {
        /* 1. Собрать выбранные элементы из каждого поля
         * 2. Если зайдействованы одновременно элементы из полей страна и прочее, выбрать прочее (для начала)
         * 3. Выяснить, есть ли среди выбранных префиксы
         * 3.1. Если есть, то новая ссылка формируется заново
         * 3.2. Если нет, то используется старый префикс
         * 4. формируется новая ссылка как в uri.builder
         * requestQuery = mSiteWorker!!.RequestQuery(applicationContext, SiteWorker.SIMPLE_QUERY, SiteWorker.ONGOING_PREFIX, params)
         */

        var selectedCountry = countriesRadioGroup.activeRadioButtonIndex
        val selectedSorting = sortingRadioGroup.activeRadioButtonIndex
        val selectedFilter = filterRadioGroup.activeRadioButtonIndex
        var selectedOthers = othersRadioGroup.activeRadioButtonIndex


        var selectedPrefixIndex : Int

        var hasPrefix = false
        var prefix = StringBuilder()
        var paramsMap = HashMap<String,String>()
        var resultingMap = HashMap<String,Any>()

        var jsonObject : JSONObject

        if (selectedOthers != -1 && selectedCountry != -1)
            selectedOthers = -1

        if (selectedCountry != -1 || selectedOthers != -1)
            hasPrefix = true

        if (hasPrefix) {
            if (selectedCountry != -1) {
                jsonObject = optionsMap.get("countries")!!
                selectedPrefixIndex = selectedCountry
            }
            else {
                jsonObject = optionsMap.get("others")!!
                selectedPrefixIndex = selectedOthers
            }

            prefix.append("list")

            if (selectedPrefixIndex != 0) {
                val selectedKey = jsonObject.getString("key")
                val selectedValue = (jsonObject.get("values") as JSONArray).get(selectedPrefixIndex).toString()

                prefix.append("/$selectedKey")
                prefix.append("/$selectedValue")
            }
        }
        else
            prefix.append(initialURL?.path?.substring(1))

        if (selectedSorting != -1 && selectedSorting != 0) {
            jsonObject = optionsMap.get("sorting")!!

            val selectedKey = jsonObject.getString("key")
            val selectedValue = (jsonObject.get("values") as JSONArray).get(selectedSorting).toString()

            paramsMap[selectedKey] = selectedValue
        }

        if (selectedFilter != -1 && selectedFilter != 0) {
            jsonObject = optionsMap.get("filter")!!

            val selectedKey = jsonObject.getString("key")
            val selectedValue = (jsonObject.get("values") as JSONArray).get(selectedFilter).toString()

            paramsMap[selectedKey] = selectedValue
        }

        resultingMap.put("path",prefix.toString())

        resultingMap.put("params",paramsMap)

        return resultingMap
    }


    companion object {

        private const val ARG_PARAM1 = "jsoArray"
        private const val ARG_PARAM2 = "initialURL"
        private const val SORTING_INDEX = 0
        private const val FILTER_INDEX = 1
        private const val GENRES_INDEX = 2
        private const val COUNTRIES_INDEX = 3
        private const val OTHERS_INDEX = 4


        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(jsonArray: JSONArray, initialURL : String) =
            SortingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1,jsonArray.toString())
                    putString(ARG_PARAM2,initialURL)
                }
            }
    }
}
