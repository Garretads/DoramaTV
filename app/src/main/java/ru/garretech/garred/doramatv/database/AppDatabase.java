package ru.garretech.garred.doramatv.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;

import ru.garretech.garred.doramatv.model.Favorites;
import ru.garretech.garred.doramatv.model.Movie;

@Database(entities = {Movie.class,Favorites.class},version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "app_database";
    private static AppDatabase INSTANCE = null;


    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
         synchronized (AppDatabase.class) {

             INSTANCE = Room.databaseBuilder(context,AppDatabase.class,DATABASE_NAME)
                     .fallbackToDestructiveMigration()
                     .build();
         }
        }
        return INSTANCE;
    }

    public abstract MovieDAO movieDAO();
    public abstract FavoritesDAO favoritesDAO();


    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(final SupportSQLiteDatabase database) {
            //database.execSQL("ALTER TABLE favorites DROP TO birthday INTEGER DEFAULT 0 NOT NULL");
        }
    };
}
