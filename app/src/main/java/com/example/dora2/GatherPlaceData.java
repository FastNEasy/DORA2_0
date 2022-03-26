package com.example.dora2;

import android.os.AsyncTask;

import android.os.AsyncTask;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class GatherPlaceData extends AsyncTask<Object,String,String> {

    String nearByPlaces;
    GoogleMap googleMap;
    String url;

    @Override
    protected void onPostExecute(String s) {
        try{
            JSONObject jsonObject = new JSONObject(s);
            JSONArray jsonArray = jsonObject.getJSONArray("results"); //saņem JSON formātā visus datus
            googleMap.clear(); //katru reizi kad izsauc, notīra karti un pa jaunam saliek marķierus

            for(int i =0; i<jsonArray.length(); i++){ //kamēr tiek iegūti dati
                JSONObject jsonObject1 = jsonArray.getJSONObject(i); //jauns json objekts
                JSONObject getLocation = jsonObject1.getJSONObject("geometry").getJSONObject("location"); //tiek iegūta vietas lokacija

                String lat = getLocation.getString("lat"); //atgriež lokāciju latitude
                String lng = getLocation.getString("lng"); //atgriež lokāciju longtitude

                JSONObject getName = jsonArray.getJSONObject(i); //jauns jsonobjekts
                String name = getName.getString("name"); //iegūst vietas nosaukumu

                LatLng latlng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)); //parsē lokaciju (lat, lng) no stringa uz double
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.title(name); //pievieno marķierim nosaukumu
                markerOptions.position(latlng); //pievieno pozīciju
                markerOptions.snippet("Lorem Ipsum"); //description lokācijas marķierim
                googleMap.addMarker(markerOptions); //pievieno marķieiri,
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,15)); //pārvieto kameru
            }

        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String doInBackground(Object... objects) {
        try{
            googleMap = (GoogleMap) objects[0];
            url = (String) objects[1];
            DownloadUrl downloadUrl = new DownloadUrl(); //tiek iegūts URL
            nearByPlaces = downloadUrl.getUrl(url);

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return nearByPlaces;
    }
}