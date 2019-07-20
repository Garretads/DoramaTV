package ru.garretech.garred.doramatv.tools

import androidx.room.TypeConverter

import java.util.Arrays

class ListConverter {
    @TypeConverter
    fun fromList(list: List<String>): String {
        return list.toString()
    }

    @TypeConverter
    fun toList(listAsString: String): List<String> {
        return Arrays.asList(*listAsString.split("\\s*,\\s*".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray())
    }

}
