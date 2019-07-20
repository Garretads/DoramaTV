package ru.garretech.garred.doramatv.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "favorites", indices = [Index("movie_url")], foreignKeys = [ForeignKey(entity = Movie::class, parentColumns = ["url"], childColumns = ["movie_url"])])
class Favorites {

    // Хранить ссылки на фильмы. Только и всего
    /*
    * Получить все ссылки (Полный список избранного. Или по частям, если с пагинацией)
    * Занести в избранное
    *
    * */

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @ColumnInfo(name = "movie_url")
    lateinit var movieURL: String
}
