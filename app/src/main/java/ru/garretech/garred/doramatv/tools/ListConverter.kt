package ru.garretech.garred.doramatv.tools

import androidx.room.TypeConverter

class ListConverter {
    @TypeConverter
    fun fromList(list: List<String>): String {
        val newString = list.toString().substring(1,list.toString().length-1)
        return newString
    }

    @TypeConverter
    fun toList(listAsString: String): List<String> {
        val newList = listOf(*listAsString.split("\\s*,\\s*".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray())
        return newList
    }

}
