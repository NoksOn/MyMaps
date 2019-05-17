package com.example.googlemaps;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.Placeholder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements View.OnClickListener, TextView.OnEditorActionListener, View.OnFocusChangeListener,Serializable {


    private transient SupportMapFragment supportMapFragment;
    private transient MapCallback mapCallback;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private boolean PermissionGranted = false;
    private transient Button addMarker;
    private transient ArrayList<Marker> markers = new ArrayList<>();
    private transient EditText getStreet;
    private transient RelativeLayout mLayout;
    private boolean PointIsCreated;
    private boolean FragmentIsOpened = false;
    private transient SearchFragment fragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.MapFragment);
        addMarker = (Button) findViewById(R.id.add);
        mLayout = (RelativeLayout) findViewById(R.id.RelLay);
        getStreet = (EditText) findViewById(R.id.TextField);
        getStreet.setOnEditorActionListener(this);
        getStreet.setOnFocusChangeListener(this);
        getStreet.setOnClickListener(this);
        addMarker.setOnClickListener(this);

        if(CheckServices()){
            CheckPermission();
        }

    }


    public void initMap(){
        mapCallback = new MapCallback(this,PermissionGranted);
        supportMapFragment.getMapAsync(mapCallback);
    }


    public void CheckPermission(){
       String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION};
       if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
           if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                   PermissionGranted = true;
                   initMap();
           }
           else {
               ActivityCompat.requestPermissions(this,permissions,LOCATION_PERMISSION_REQUEST_CODE);
           }
       }
       else {
           ActivityCompat.requestPermissions(this,permissions,LOCATION_PERMISSION_REQUEST_CODE);
       }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:
                if(grantResults.length>0){
                    for(int i: grantResults){
                        if(i != PackageManager.PERMISSION_GRANTED) {
                            PermissionGranted = false;
                            return;
                        }
                    }
                    PermissionGranted = true;
                    initMap();
                }
                break;
        }
    }


    public boolean CheckServices(){
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if(available == ConnectionResult.SUCCESS){
            Log.d("Permission","Connection to google maps Api succes");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            Log.d("Permission","an error but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this,available,9001);
            dialog.show();
        }
        else {
            Toast.makeText(MainActivity.this,"Cannot connect to Map Api",Toast.LENGTH_LONG).show();
        }
        return false;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.add:
                mLayout.setVisibility(View.VISIBLE);
                if(mapCallback.getCurrentLatLng()!=null){
                    Marker marker = mapCallback.addMarker(mapCallback.getCurrentLatLng(),true,"My Point");
                    markers.add(marker);
                    PointIsCreated = true;
                }
                else {
                    Toast.makeText(this,"Please set your location",Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.TextField:
                if(!FragmentIsOpened){
                    OpenFragment();
                }
                break;
        }

    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if(actionId== EditorInfo.IME_ACTION_SEARCH){
            Log.d("Text","Search");
            GeoLocate(String.valueOf(getStreet.getText()));
            return true;
        }
        return false;
    }

    public void GeoLocate(String text){
        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses = new ArrayList<>();
        try{
              addresses = geocoder.getFromLocationName(text,30);
                Log.d("List","List size "+addresses.size());
        }catch (IOException e){
            Log.e("Ex",e.getMessage());
        }
        if(addresses.size()>0){
              fragment.CreateRecyclerView(addresses);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
       OpenFragment();
    }

    public void OpenFragment(){
        fragment = SearchFragment.newInstance(this,this);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_to_down,R.anim.exit_to_up,R.anim.enter_to_down,R.anim.exit_to_up);
        transaction.addToBackStack(null);
        transaction.add(R.id.FrameContainer,fragment,"BLANK_FRAGMENT").commit();
        addMarker.setVisibility(View.INVISIBLE);
        FragmentIsOpened = true;
    }

    public void setFragmentIsOpened(boolean fragmentIsOpened) {
        FragmentIsOpened = fragmentIsOpened;
    }

    public Button getAddMarker() {
        return addMarker;
    }

    public ArrayList<Marker> getMarkers() {
        return markers;
    }
    public boolean isPointIsCreated() {
        return PointIsCreated;
    }

    public void setPointIsCreated(boolean pointIsCreated) {
        PointIsCreated = pointIsCreated;
    }

    public MapCallback getMapCallback() {
        return mapCallback;
    }

}
