package ru.garretech.garred.doramatv.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import android.graphics.Bitmap
import com.chad.library.adapter.base.entity.MultiItemEntity


import java.io.Serializable
import java.util.Arrays

import ru.garretech.garred.doramatv.tools.ListConverter

@Entity
class Movie

(var title: String?, @field:ColumnInfo(name = "movie_image_url")
 var movieImageURL: String, @field:PrimaryKey
 var url: String) : Serializable, MultiItemEntity {

   @field:TypeConverters(ListConverter::class) var genres: List<String>? = null

    @ColumnInfo(name = "production_year")
    var productionYear: String? = null
    //private List<String> mainActors;
    //private List<String> actors;
    //private List<String> producers;
    @ColumnInfo(name = "production_country")
    var productionCountry: String? = null

    @ColumnInfo(name = "series_number")
    var seriesNumber: String? = null

    var duration: String? = null

    @Ignore
    @Transient
    var image: Bitmap? = null

    @ColumnInfo(name = "image_cached_path")
    var imageCachedPath: String? = null

    var description: String? = null

    @ColumnInfo(name = "initial_series")
    var initialSeries: String? = null

    override fun getItemType(): Int {
        return TYPE
    }

    companion object {
        const val TYPE = 0
    }

}

