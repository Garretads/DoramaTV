package ru.garretech.garred.doramatv.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverter
import android.arch.persistence.room.TypeConverters
import android.graphics.Bitmap


import java.io.Serializable
import java.util.Arrays

import ru.garretech.garred.doramatv.tools.ListConverter

@Entity
class Movie
/*info.put("title",name + " | " + eng_name + " | " + original_name);
                        info.put("url",url);
                        info.put("genres",genres.toString());
                        info.put("image_url",image_url);
                        info.put("initial_series",initialSeries);
                        info.put("production",production);
                        info.put("series_number",seriesNumber);
                        info.put("duration",duration);
                        info.put("description",description);
                        info.put("age",age);
*/
(var title: String?, @field:TypeConverters(ListConverter::class)
var genres: List<String>?, @field:ColumnInfo(name = "movie_image_url")
 var movieImageURL: String, @field:PrimaryKey
 var url: String) : Serializable {

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

}

