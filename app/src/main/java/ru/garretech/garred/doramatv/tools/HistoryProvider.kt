package ru.garretech.garred.doramatv.tools

import ru.garretech.garred.doramatv.data.model.History


class HistoryProvider(val history: History) {

    init {
        if (history.series == null)
            history.series = HashMap()
    }


    fun getWatchedIdInSeries(seriesIndex: Int) : List<Int> {
        if (history.series!!.containsKey(seriesIndex)) {
            val idArray = history.series!![seriesIndex]
            return idArray!!
        }
        else {
            return emptyList()
        }
    }

    fun getWatchedSeriesIndexes() : List<Int> {
        return if (history.series != null)
            history.series!!.keys.toList()
        else
            emptyList<Int>()
    }

    fun addSeries(seriesIndex : Int, seriesId : Int) {
        if (history.series!!.containsKey(seriesIndex)) {
            val idArray = history.series!!.get(seriesIndex)!!
            val newArray = ArrayList<Int>()
            newArray.addAll(idArray)

            if (!newArray.contains(seriesId))
                newArray.add(seriesId)

            history.series!![seriesIndex] = newArray.toList()
        }
        else {
            val idArray = ArrayList<Int>()
            idArray.add(seriesId)
            history.series!![seriesIndex] = idArray
        }
    }


    companion object {
        const val SERIES_NAME = "series"
        const val SOURCES = "watched_sources"
    }

}