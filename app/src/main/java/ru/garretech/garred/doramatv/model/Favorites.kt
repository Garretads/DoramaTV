package ru.garretech.garred.doramatv.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "favorites", indices = [Index("movie_url")])
class Favorites(@PrimaryKey @field:ColumnInfo(name = "movie_url") var movieURL: String)
