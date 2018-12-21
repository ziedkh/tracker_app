package com.ziedkh.tracker;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class TrackLocation implements LocationListener {

    public  static Location location;
    public static boolean isRunning =false;

    public  TrackLocation(){
        isRunning=true;
        location = new Location("not defined");
        location.setLatitude(-34);
        location.setLongitude(151);
    }
    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
