package com.example.dora2;

import android.os.AsyncTask;
import android.util.Log;

public class DownloadTask extends AsyncTask<String, Void, String> {

    //datu noladēšanas process notiek backgrounda
    @Override
    protected String doInBackground(String... url) {
        String data = "";
        try{
            DownloadUrl downloadUrl = new DownloadUrl();
            data = downloadUrl.getUrl(url[0]); //nolādē api datus
            Log.i("DATA", data);
        }catch(Exception e){
            Log.d("Background Task",e.toString());
        }
        return data;
    }


    //izpildās kad ir pabeigta datu lādēšana
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        ParserTask parserTask = new ParserTask(); //tiek izsaukta klase parserTask
        parserTask.execute(result);
    }
}
