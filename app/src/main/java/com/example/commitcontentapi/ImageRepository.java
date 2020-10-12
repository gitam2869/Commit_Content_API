package com.example.commitcontentapi;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class ImageRepository {

    private static ImageRepository imageRepository;
    private ImageDao imageDao;

    private ImageRepository(Application application) {
        IMEImagesDatabase db = IMEImagesDatabase.getInstance(application);
        imageDao = db.imageDao();
    }

    public static ImageRepository getInstance(Application application) {
        if (imageRepository == null) {
            synchronized (ImageRepository.class) {
                if (imageRepository == null) {
                    imageRepository = new ImageRepository(application);
                }
            }
        }
        return imageRepository;
    }

    public void insert(final ImageModel image) {
        IMEImagesDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                imageDao.insert(image);
            }
        });
    }

    public LiveData<List<ImageModel>> getAllImages() {
        return imageDao.getAllImages();
    }

}
