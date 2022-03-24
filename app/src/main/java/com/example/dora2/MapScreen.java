package com.example.dora2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MapScreen extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private FusedLocationProviderClient client;
    private static final int RequesCode = 101;
    private double lat,lng;
    ImageButton cafe, shops, pharmacy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        cafe = findViewById(R.id.restaurants_near); //atrod pogas
        shops = findViewById(R.id.markets_near);
        pharmacy = findViewById(R.id.pharmacy_near);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapAPI); //kartes fragments
        mapFragment.getMapAsync(this);
        client = LocationServices.getFusedLocationProviderClient(this.getApplicationContext());

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        getCurrentLocation();
    }

    private void getCurrentLocation(){ //ja nav iedoti permissioni jautā pēc permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},RequesCode);
            return;
        }

        map.setMyLocationEnabled(true);
        LocationRequest locationRequest = LocationRequest.create(); //pieprasa lokāciju
        locationRequest.setInterval(60000); //uzliek intervālu ar kādu tiks updeitota lokācija milisekundēs
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //uzliek prioritāti high_accuracy
        locationRequest.setFastestInterval(5000); //kontrolē ātrumu ar kādu aplikācija saņems ātrāko updaite milisekundēs

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                // Toast.makeText(getApplicationContext(), "location result is =" + locationResult, Toast.LENGTH_LONG).show();

                if(locationRequest == null){ //pārbauda vai ir kāda lokācija
                    //Toast.makeText(getApplicationContext(), "Current location is null", Toast.LENGTH_LONG).show();
                    return;
                }
                for(Location location:locationResult.getLocations()){ //updeitotās lokācijas
                    if(location != null){
                        //Toast.makeText(getApplicationContext(), "location result is =" + locationResult, Toast.LENGTH_LONG).show();
                    }
                }
            }
        };
        client.requestLocationUpdates(locationRequest,locationCallback,null);

        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location !=null){
                    lat = location.getLatitude();
                    lng = location.getLongitude();
                    LatLng latLng = new LatLng(lat,lng);

                    //map.addMarker(new MarkerOptions().position(latLng).title("Current Location"));
                    map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                }
            }
        });
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (RequesCode){
            case RequesCode:
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){ //ja ir iedoti permissioni tad izsauc getCurrentLocation
                    getCurrentLocation();
                }
        }
    }

    public void onButtonClick(View view){
        switch(view.getId()){
            case R.id.restaurants_near:
                //map.clear();
                String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                        "?location="+lat+","+lng+"&radius=5000"+"&types=restaurant"+"&sensor=true"+
                        "&key=" + getResources().getString(R.string.maps_api_key);
                Log.i("link", url);
                Object dataFetch[] = new Object[2];
                dataFetch[0] = map;
                dataFetch[1] = url;
                GatherPlaceData gpd = new GatherPlaceData();
                gpd.execute(dataFetch);
                break;

            case R.id.markets_near:
                //map.clear();
                String url1 = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                        "?location="+lat+","+lng+"&radius=5000"+"&types=store"+"&sensor=true"+
                        "&key=" + getResources().getString(R.string.maps_api_key);
                Object dataFetch1[] = new Object[2];
                dataFetch1[0] = map;
                dataFetch1[1] = url1;

                GatherPlaceData gpd1 = new GatherPlaceData();
                gpd1.execute(dataFetch1);
                break;
            case R.id.pharmacy_near:
                //map.clear();
                String url2 = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                        "?location="+lat+","+lng+"&radius=5000"+"&types=pharmacy"+"&sensor=true"+
                        "&key=" + getResources().getString(R.string.maps_api_key);
                Object dataFetch2[] = new Object[2];
                dataFetch2[0] = map;
                dataFetch2[1] = url2;

                GatherPlaceData gpd2 = new GatherPlaceData();
                gpd2.execute(dataFetch2);
        }
    }
}

