package com.example.dora2;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> > {

    GoogleMap googleMap;
    public static ArrayList<LatLng> points = null;  //arraylists, kas satur punktu koordinātes ceļa zīmēšanai



    //tiek darbināts tad kad ir parsēti iegūtie dati
    @Override
    protected void onPostExecute(List<List<HashMap<String, String>>> result) {

        points = new ArrayList<LatLng>(); //inicializē arraylistu

        for(int i=0;i<result.size();i++){ //iet cauri visiem route

            List<HashMap<String, String>> path = result.get(i); //dabū konkrēto route


            for(int j=0;j<path.size();j++){ //dabū visus route punktus
                HashMap<String,String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);
                points.add(position); //pievieno punktu sarakstam
            }
        }
    }


    // tiek parsēti visi iegūtie json dati
    @Override
    protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;
        try{
            jObject = new JSONObject(jsonData[0]); //jsonobjekts
            DirectionsJSONParser parser = new DirectionsJSONParser(); //izsaukta json parser metode
            routes = parser.parse(jObject); //parsē datus
        }catch(Exception e){
            e.printStackTrace();
        }
        return routes;
    }

    public static ArrayList<LatLng> coords(){ //atgriež punktu sarakstu ar koordinātēm
        return points;
    }
}