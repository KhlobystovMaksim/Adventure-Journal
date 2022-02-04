package com.harman.android.myprojectforinternship;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.exifinterface.media.ExifInterface;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ChooseAndTakePhoto {


    public ChooseAndTakePhoto(Activity currentActivity) {
        this.currentActivity = currentActivity;
    }

    private Activity currentActivity;

    private ArrayList<String> getAllShownImagesPath(Activity activity) {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        cursor = activity.getContentResolver().query(uri, projection, null,
                null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);

            listOfAllImages.add(absolutePathOfImage);
        }
        return listOfAllImages;
    }

    public List<PhotoInfo> getPhotosWithLocationsFromGallery() {
        List<PhotoInfo> result = new ArrayList<>();
        List<String> ImagesUri = getAllShownImagesPath(currentActivity);
        for (String uri : ImagesUri) {
            try {
                PhotoInfo photoInfo = getPhotoInfoByURI(uri);
                if(photoInfo != null) {
                    result.add(photoInfo);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public PhotoInfo getPhotoInfoByURI(String uri) throws IOException {
        PhotoInfo photoInfo = null;
        ExifInterface exif = new ExifInterface(uri);
        double[] location = exif.getLatLong();
        if(location != null && location.length >= 2) {
            photoInfo = new PhotoInfo(location[0],location[1],uri,"");
        }
        return photoInfo;
    }
}

