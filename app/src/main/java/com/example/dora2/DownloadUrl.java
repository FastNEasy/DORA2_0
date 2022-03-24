package com.example.dora2;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadUrl {

    public String getUrl(String url) throws IOException {
        String urlString = "";
        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;

        try{
            URL url1 = new URL(url);
            httpURLConnection = (HttpURLConnection) url1.openConnection();
            httpURLConnection.connect();

            inputStream = httpURLConnection.getInputStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer sb = new StringBuffer();

            String line = "";

            while((line = bufferedReader.readLine()) !=null){
                sb.append(line);
            }
            urlString = sb.toString();
            bufferedReader.close();
            httpURLConnection.disconnect();
            inputStream.close();
        }catch (Exception e){
            Log.d("Exception", e.toString());
        }
        return urlString;
    }
}
