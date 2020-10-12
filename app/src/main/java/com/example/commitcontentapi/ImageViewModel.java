package com.example.commitcontentapi;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class ImageViewModel extends AndroidViewModel {

    private ImageRepository imageRepository;

    public ImageViewModel(@NonNull Application application) {
        super(application);
        imageRepository = ImageRepository.getInstance(application);
    }

    public void insertImage(ImageModel image) {
        imageRepository.insert(image);
    }


    public LiveData<List<ImageModel>> getAllImages() {
        return imageRepository.getAllImages();
    }


}
