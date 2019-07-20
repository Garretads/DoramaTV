package ru.garretech.garred.doramatv.database

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import android.content.Context

import ru.garretech.garred.doramatv.model.Favorites
import ru.garretech.garred.doramatv.model.Movie

@Database(entities = [Movie::class, Favorites::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun movieDAO(): MovieDAO
    abstract fun favoritesDAO(): FavoritesDAO

    companion object {

        private val DATABASE_NAME = "app_database"
        private var INSTANCE: AppDatabase? = null


        fun getInstance(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class.java) {

                    INSTANCE = Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .build()
                }
            }
            return INSTANCE
        }


        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                //database.execSQL("ALTER TABLE favorites DROP TO birthday INTEGER DEFAULT 0 NOT NULL");
            }
        }
    }
}
