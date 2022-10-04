package ru.garretech.garred.doramatv.data.model

import androidx.room.*
import ru.garretech.garred.doramatv.tools.MapTypeConverter

@Entity(tableName = "history", indices = [Index("movie_url")])
class History(@PrimaryKey @field:ColumnInfo(name = "movie_url") var movieURL: String) {

    @field:TypeConverters(MapTypeConverter::class)
    var series : HashMap<Int,List<Int>>? = null

}
