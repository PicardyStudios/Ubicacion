package com.picardystudios.ubicacion;

import android.Manifest;



import android.location.Location;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;


import android.content.pm.PackageManager;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {


    private Location location;
    private GoogleApiClient googleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private LocationRequest locationRequest;
    private static final long UPDATE_INTERVAL = 60000, FASTEST_INTERVAL = 60000; // = 5 seconds
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        AndPermission.with(MainActivity.this)
                .runtime()
                .permission(Permission.Group.LOCATION)
                .onGranted(permissions -> {
                    Toast.makeText(MainActivity.this, "Permisos concedidos..", Toast.LENGTH_LONG).show();
                })
                .onDenied(permissions -> {
                    Toast.makeText(MainActivity.this, "Concedenos permiso para utilizar la camara", Toast.LENGTH_LONG).show();
                })
                .start();


        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();

    }






    @Override
    protected void onStart() {
        super.onStart();

        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!checkPlayServices()) {
            Toast.makeText(this, "You need to install Google Play Services to use the App properly", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                finish();
            }

            return false;
        }

        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&  ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Permissions ok, we get last location
        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (location != null) {

            String LaUbica = "Latitude : " + location.getLatitude() + "\nLongitude : " + location.getLongitude();
            Log.e("Ubicación",LaUbica);
            Toast.makeText(this, LaUbica, Toast.LENGTH_SHORT).show();
        }

        startLocationUpdates();
    }

    public void SaveLocation(String latitud, String longitud){

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        String url = "URL";
        try {
            RequestQueue MyRequestQueue = Volley.newRequestQueue(this);

            StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {
                Log.e("Resultado", "-> " + response);

            }, error -> Log.e("Resultado", String.valueOf(error))) {
                protected Map<String, String> getParams() {
                    Map<String, String> MyData = new HashMap<String, String>();
                    MyData.put("lat", latitud);
                    MyData.put("lng", longitud);
                    MyData.put("firebase", refreshedToken);
                    return MyData;
                }
            };

            MyStringRequest.setRetryPolicy(new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 50000;
                }

                @Override
                public int getCurrentRetryCount() {
                    return 50000;
                }

                @Override
                public void retry(VolleyError error) throws VolleyError {

                }
            });

            MyRequestQueue.add(MyStringRequest);
        } catch (Exception e) { Log.e("GuardarUbicacion",e.toString()); }

    }
    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&  ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You need to enable permissions to display location !", Toast.LENGTH_SHORT).show();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            String LaUbica = location.getLatitude() + " | " + location.getLongitude();
            Log.e("Ubicación",LaUbica);
            Toast.makeText(this, LaUbica, Toast.LENGTH_SHORT).show();

            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
            Log.e("Token",refreshedToken);
            SaveLocation(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
        }
    }




}
