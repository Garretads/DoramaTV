package ru.garretech.garred.doramatv.data.database

import androidx.room.*
import ru.garretech.garred.doramatv.data.model.History

@Dao
interface HistoryDAO {

    @get:Query("SELECT * FROM history")
    val allHistory: List<History>

    @Update
    fun updateHistory(history: History)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveHistory(history: History): Long

    @Query("SELECT * FROM history WHERE movie_url = :URL")
    fun getHistoryByURL(URL: String): History?

    @Query("DELETE FROM history")
    fun clearHistory()

}