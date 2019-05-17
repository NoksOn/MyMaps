package com.example.googlemaps;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.MainThread;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.Serializable;

public class GpsService extends Service implements LocationListener {

    private LocationManager locationManager;
    private IBinder binder = new MyServiceBinder();
    private MapCallback serviceCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Service","OnCreate");
        locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,15f,this);
        }catch (SecurityException e){

        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Service","OnDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onLocationChanged(Location location) {
       Log.d("Service","On LocationChanged");
       if(location!=null){
           serviceCallback.setCurrentLatLng(new LatLng(location.getLatitude(),location.getLongitude()));
           serviceCallback.refresh();
       }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
       Log.d("Service","On Status chaged");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Service","Provider Enabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Service","Provider Disabled");
    }

    public class MyServiceBinder extends Binder{
          public GpsService getService(){
              return GpsService.this;
          }
    }

    public void setServiceCallBack(MapCallback serviceCallBack){
        this.serviceCallback = serviceCallBack;
    }

}
