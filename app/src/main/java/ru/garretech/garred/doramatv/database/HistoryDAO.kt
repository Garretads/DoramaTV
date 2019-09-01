package ru.garretech.garred.doramatv.database

import androidx.room.*
import ru.garretech.garred.doramatv.model.History

@Dao
interface HistoryDAO {

    @get:Query("SELECT * FROM history")
    val allHistory: List<History>

    @Update
    fun updateHistory(history: History)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addHistory(history: History): Long

    @Query("SELECT * FROM history WHERE movie_url = :URL")
    fun getHistoryByURL(URL: String): History?

    @Query("DELETE FROM history")
    fun clearHistory()

}