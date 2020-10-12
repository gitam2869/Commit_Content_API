package com.example.commitcontentapi;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {ImageModel.class}, version = 1, exportSchema = true)
public abstract class IMEImagesDatabase extends RoomDatabase {

    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    private static volatile IMEImagesDatabase INSTANCE;

    public static IMEImagesDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (IMEImagesDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            IMEImagesDatabase.class, Constant.DATABASE_NAME)
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            //.addMigrations(DatabaseMigration.MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract ImageDao imageDao();

}