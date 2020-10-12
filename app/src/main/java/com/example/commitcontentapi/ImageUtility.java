package com.example.commitcontentapi;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class ImageUtility {

    public static File createImageFile(Context context, String imageFileName) {
        if (imageFileName == null) {
            imageFileName = "CC_IME_" + System.currentTimeMillis() + ".png";
        }
        File imagePath = new File(context.getFilesDir(), Constant.IMAGE_DIR);
        if (imagePath == null || !imagePath.mkdirs()) {
            Log.e("ImageUtility", "Directory not created");
        }
        File image = new File(imagePath, imageFileName);
        return image;
    }

    public static void copyImage(Context context, Uri srcUri, File outputFile) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(srcUri);
            if (inputStream == null) return;
            OutputStream outputStream = new FileOutputStream(outputFile);
            IOUtils.copy(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            Log.e("ImageUtility", "Unable to copy image from gallery.", e);
        }
    }
}
