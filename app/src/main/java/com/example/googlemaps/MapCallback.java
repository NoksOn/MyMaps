package com.example.googlemaps;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;

public class MapCallback implements OnMapReadyCallback,ServiceCallback{

    private Context mContext;
    private GoogleMap mMap;
    private boolean PermissionIsGranted = false;
    private LocationManager locationManager;
    private GpsService gpsService;


    public void setCurrentLatLng(LatLng currentLatLng) {
        this.currentLatLng = currentLatLng;
    }

    private LatLng currentLatLng;




    public MapCallback(Context mContext, boolean PermissionIsGranted) {
        this.PermissionIsGranted = PermissionIsGranted;
        this.mContext = mContext;
        locationManager = (LocationManager) mContext.getSystemService(mContext.LOCATION_SERVICE);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        if (PermissionIsGranted) {
            if (CheckGPSTracker()) {
                try {
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                    mMap.getUiSettings().setCompassEnabled(true);
                    GetMyLocation();
                } catch (SecurityException e) {
                    Log.d("Permission", e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("Permission", e.getMessage());
                }
            }
            else {
                Intent intent = new Intent(mContext,GpsService.class);
                mContext.bindService(intent,serviceConnection,Context.BIND_AUTO_CREATE);
            }
        } else {
            return;
        }
    }


    public void GetMyLocation() throws IOException, SecurityException {
        Log.d("Service","GetMyLocation");
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
         Task location = fusedLocationProviderClient.getLastLocation();
        location.addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()&&task.getResult()!=null) {
                        Location currentLocation = (Location) task.getResult();
                        Log.d("Service","Current Location is "+currentLocation);
                            LatLng currentPosition = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            MoveCamera(currentPosition);
                    }
            }
        });
    }

    public void MoveCamera(LatLng latLng) {
        Log.d("Service","MoveCamera");
        this.currentLatLng = latLng;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
    }


    public boolean CheckGPSTracker() {
        boolean IsGpsOn = false;
            if (locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)) {
                IsGpsOn = true;
            }
            else {
                new AlertDialog.Builder(mContext)
                        .setMessage(R.string.gps_not_enabled)
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mContext.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        })
                        .setNegativeButton("Cancel",null).show();
             }
        return IsGpsOn;
    }

    @Override
    public void refresh() {
        Log.d("Service","Map Refresh");
            this.onMapReady(this.mMap);
    }

    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            gpsService = ((GpsService.MyServiceBinder) service).getService();
            gpsService.setServiceCallBack(MapCallback.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public LatLng getCurrentLatLng() {
        return currentLatLng;
    }

    public Marker addMarker(LatLng position,boolean draggable,String title){
        Marker marker = mMap.addMarker(new MarkerOptions()
        .position(position)
        .draggable(draggable)
        .title(title));

        return marker;
    }
}
