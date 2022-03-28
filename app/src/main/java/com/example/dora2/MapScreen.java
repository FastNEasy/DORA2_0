package com.example.dora2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;

public class MapScreen extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private FusedLocationProviderClient client;
    private static final int RequesCode = 101;
    private double lat,lng;
    ImageButton showRoute;
    MarkerOptions marker;
    LatLng markerPosition;
    String chosenFilter = "church";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        showRoute = findViewById(R.id.show_route); //atrod pogu kas zime celu

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapAPI); //kartes fragments
        mapFragment.getMapAsync(this);
        client = LocationServices.getFusedLocationProviderClient(this.getApplicationContext());

        ListView listView = new ListView(this);
        List<String> data = new ArrayList<>();
        data.add("museums");
        data.add("restaurants");
        data.add("parks");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,data);
        listView.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(MapScreen.this);
        builder.setCancelable(true);
        builder.setView(listView);
        final AlertDialog dialog = builder.create();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.mapSelection);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.mapSelection:
                        return true;
                    case R.id.profile:
                        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.logout:
                        return true;
                }
                return false;
            }
        });

        showRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParserTask pt = new ParserTask();
                PolylineOptions pOptions = new PolylineOptions();
                pOptions.addAll(pt.points);
                Log.i("POINTS", pt.coords()+ "");
                pOptions.width(5);
                pOptions.color(Color.RED);
                map.addPolyline(pOptions);
                //for showing nearby things
                if(pt.points.size() > 0){
                    LatLng fromPlace = new LatLng(lat,lng);
                    LatLng toPlace = pt.points.get(pt.points.size()-1);
                    double dist = SphericalUtil.computeDistanceBetween(fromPlace,toPlace);
                    LatLng tripCenter = getPolylineCentroid(pOptions);
                    setNearbySpots(tripCenter, dist/2);
                }
            }
        });

        ImageButton openDialogue = findViewById(R.id.open_bottom_sheet);
        openDialogue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                long itemPosition = adapter.getItemId(position);
                Log.i("POZICIJA", ""+itemPosition);
                if(itemPosition == 0){
                    chosenFilter = "museum";
                    dialog.dismiss();
                    Toast.makeText(MapScreen.this, "Museums selected", Toast.LENGTH_SHORT).show();
                }
                if(itemPosition == 1){
                    chosenFilter = "restaurant";
                    dialog.dismiss();
                    Toast.makeText(MapScreen.this, "Restaurants selected", Toast.LENGTH_SHORT).show();
                }
                if(itemPosition == 2){
                    chosenFilter = "park";
                    dialog.dismiss();
                    Toast.makeText(MapScreen.this, "Parks selected", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public void setNearbySpots(@NonNull LatLng center, double d){
        //later pass here choices from the filter
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location="+center.latitude+","+center.longitude+"&radius="+d+"&types="+chosenFilter+"&sensor=true"+
                "&key=" + getResources().getString(R.string.maps_api_key);
        Object dataFetch[] = new Object[2];
        Log.i("link", url);
        dataFetch[0] = map;
        dataFetch[1] = url;

        GatherPlaceData gpd = new GatherPlaceData();
        gpd.execute(dataFetch);
    }
    public LatLng getPolylineCentroid(@NonNull PolylineOptions p) {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(int i = 0; i < p.getPoints().size(); i++){
            builder.include(p.getPoints().get(i));
        }

        LatLngBounds bounds = builder.build();
        return bounds.getCenter();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapClickListener(this);
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
                if(locationRequest == null){ //pārbauda vai ir kāda lokācija
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



    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        map.clear();
        marker = new MarkerOptions().position(latLng).title("Lat" + latLng.latitude +" Lng"+ latLng.longitude).icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        map.addMarker(marker);
        markerPosition = marker.getPosition();
        String url = getUrl(marker.getPosition(), "driving");//dabū url
        Log.i("url", url);
        DownloadTask dTask = new DownloadTask();
        dTask.execute(url);

    }

    private String getUrl(LatLng destinationPoint, String directionMode){ //dabū url priekš directions
        String start_origin = "origin=" + lat+","+lng; //sakuma pozicija
        String dest_origin = "destination="+ destinationPoint.latitude+","+destinationPoint.longitude; //destination
        String mode = "mode="+directionMode; //parvietosanas veids
        String parameters = start_origin+"&"+dest_origin+"&"+mode; //viss salikst kopa
        String output = "json"; //output formats
        String url ="https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters+"&key="+getString(R.string.maps_api_key);
        return url;
    }



}

