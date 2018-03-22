package com.niceattiregames.andrey.funweather;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
//import com.google.android.gms.location.LocationListener;

import com.google.android.gms.gcm.Task;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.tasks.OnCompleteListener;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Created by Andrey on 17.03.2018.
 */

public class GPSService implements LocationListener {

    double longitude;
    double latitude;
    Activity activity;

    DataGPSCallback dataGPSCallback;


    private LocationManager locationManager;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    public Boolean mLocationPermissionGranted;

    public Boolean servicesIsEnabled;

    private static final int MY_PERMISSION_REQUEST_FINE_LOCATION = 101;
    private static final int MY_PERMISSION_REQUEST_COARSE_LOCATION = 102;


    public GPSService(Activity activity) {
        Log.d("1", "GPSService: ");
        //requestPermission();
        this.activity = activity;
        this.dataGPSCallback = (DataGPSCallback) activity;
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        Log.d("2", "GPSService: ");

        boolean procede = checkPermissionsAndServices();

        //mLocationPermissionGranted = true;

        if (procede) {
            Log.d("4", "GPSService: ");

            getDeviceLocation();

            Log.d("GPSService", "GPSService: ");
        }
    }

    private Boolean checkPermissionsAndServices() {
        enableServicesifDisabled();
        if (servicesIsEnabled) {
            if (ActivityCompat.checkSelfPermission(activity, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)  /* && ActivityCompat.checkSelfPermission(activity, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) */ {

                Log.d("3", "GPSService: ");
                requestPermission();
                return false;
            } else {
                mLocationPermissionGranted = true;
            }
        } else {
            if (ActivityCompat.checkSelfPermission(activity, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermission();
                return false;
            } else {
                mLocationPermissionGranted = true;
            }
            dataGPSCallback.onDataReceive(longitude, latitude);
            return false;
        }
        return true;
    }

    public void getDeviceLocation() {

        if (ActivityCompat.checkSelfPermission(activity, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)  /* && ActivityCompat.checkSelfPermission(activity, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) */ {

            Log.d("33", "Don't have permission: ");
            return;
        }
        //requestPermission();
        if (mFusedLocationProviderClient == null) {
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
        }

        try {
            if(mLocationPermissionGranted) {
                final com.google.android.gms.tasks.Task<Location> location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Location> task) {
                        if (task.isSuccessful()) {
                            Log.d("YO", "FOUND LOCATION!" + task.getException());
                            Location currentLocation = (Location) task.getResult();
                            if (currentLocation != null) {
                                Log.d("MY location", "onComplete: longitude " + currentLocation.getLongitude() + " latitude " + currentLocation.getLatitude() + " ^^^ ");

                                longitude = currentLocation.getLongitude();
                                latitude = currentLocation.getLatitude();

                                /*TextView coordinatesTextView = (TextView) activity.findViewById(R.id.coordinates);
                                coordinatesTextView.setText("Координаты: " + currentLocation.getLongitude() + ", " + currentLocation.getLatitude() + " ");*/
                                dataGPSCallback.onDataReceive(longitude, latitude);
                            } else {
                                Log.d("YO", "currentLocation NULL");
                                Toast.makeText(activity,"Устройство не может определить позицию", Toast.LENGTH_LONG).show();
                                dataGPSCallback.gpsDataTimeOut();
                            }
                        } else {
                            Log.d("YO", "Tusk unsuccessfull");
                        }
                    }
                });
            }


        }catch (SecurityException e) {
            Log.e("Error", "SecurityException: " + e.getMessage());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("I AM HERE", "onLocationChanged: ");
        String providerName =  location.getProvider();

        if (providerName.equals(LocationManager.GPS_PROVIDER)) {
            if (location != null) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();

                TextView coordinatesTextView = (TextView) activity.findViewById(R.id.coordinates);
                coordinatesTextView.setText("Координаты: " + longitude + ", " + latitude + " ");
                Log.d("onLocationChanged", "GPS_PROVIDER: ");
            }
        }
        if( providerName.equals(LocationManager.NETWORK_PROVIDER)){
            if (location != null) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();

                /*TextView coordinatesTextView = (TextView) activity.findViewById(R.id.coordinates);
                coordinatesTextView.setText("Координаты: " + longitude + ", " + latitude + " ");*/
                Log.d("onLocationChanged", "NETWORK_PROVIDER: ");

            }
        }
        Log.d("onLocationChanged", "onLocationChanged: ");
    }



    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(activity,"Turn the GPS on", Toast.LENGTH_LONG).show();
        /*Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        context.startActivity(myIntent);*/
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_FINE_LOCATION);
    }


    Context context;
    public boolean isLocationServiceEnabled(){
        LocationManager locationManager = null;
        boolean gps_enabled= false,network_enabled = false;

        if(locationManager ==null)
            locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        try{
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch(Exception ex){
            //do nothing...

        }

        try{
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }catch(Exception ex){
            //do nothing...
        }

        return gps_enabled || network_enabled;

    }

    public void enableServicesifDisabled() {
        if(!isLocationServiceEnabled()) {
            // set default
            servicesIsEnabled = false;
            longitude = 60.5;
            latitude = 60.5;

            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
            dialog.setMessage(activity.getResources().getString(R.string.gps_network_not_enabled));
            dialog.setPositiveButton(activity.getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    activity.startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton(activity.getString(R.string.Cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub

                }
            });
            dialog.show();
        } else {
            servicesIsEnabled = true;
        }
    }


}
