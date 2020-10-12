package com.example.commitcontentapi;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ImageDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(ImageModel imageModel);

    @Query("SELECT * from images ORDER BY created_at")
    LiveData<List<ImageModel>> getAllImages();
}
