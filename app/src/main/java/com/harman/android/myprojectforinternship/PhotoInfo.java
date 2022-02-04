package com.harman.android.myprojectforinternship;

import android.location.Location;

public class PhotoInfo {

    public double Latitude;
    public double Longitude;
    public String path;
    public String comments;

    public PhotoInfo(double latitude, double longitude, String path, String comments) {
        Latitude = latitude;
        Longitude = longitude;
        this.path = path;
        this.comments = comments;
    }





}