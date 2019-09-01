package ru.garretech.garred.doramatv.tools

import androidx.room.TypeConverter
import org.json.JSONObject


class JsonConverter {

    @TypeConverter
    fun fromJson(jsonObject: JSONObject): String {
        return jsonObject.toString()
    }

    @TypeConverter
    fun toJson(value : String): JSONObject {
        return JSONObject(value)
    }
}