package com.example.commitcontentapi;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "images")
public class ImageModel {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private long id;

    @NonNull
    @ColumnInfo(name = "image_name")
    private String imageName;

    @ColumnInfo(name = "created_at")
    private long creationAt;


    public ImageModel(@NonNull String imageName, long creationAt) {
        this.imageName = imageName;
        this.creationAt = creationAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getImageName() {
        return imageName;
    }

    public void setImageName(@NonNull String imageName) {
        this.imageName = imageName;
    }

    public long getCreationAt() {
        return creationAt;
    }

    public void setCreationAt(long creationAt) {
        this.creationAt = creationAt;
    }

    @Override
    public String toString() {
        return "ImageModel{" +
                "id=" + id +
                ", imageName='" + imageName + '\'' +
                ", creationAt=" + creationAt +
                '}';
    }
}
