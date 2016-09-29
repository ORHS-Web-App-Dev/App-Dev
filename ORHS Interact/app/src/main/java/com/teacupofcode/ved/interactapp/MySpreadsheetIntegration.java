package com.teacupofcode.ved.interactapp;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;


import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *  Created by VasanthSadhasivan on 10/30/2015.
 */
class MySpreadsheetIntegration extends AsyncTask<MainActivity.FragmentMaker, Void, MainActivity.FragmentMaker>{

    static ArrayList<Event> eventList;

    private final static String TAG = "MySpreadsheetI";
    static String upcoming;

    @Override
    protected MainActivity.FragmentMaker doInBackground(MainActivity.FragmentMaker... a){
        try {
            String url = "https://spreadsheets.google.com/tq?key=1lCS6UsbWjTD0J-M7LACeKhd42BZFn8M4QNHnGRVl_cQ";
            MySpreadsheetIntegration.setEvents(url);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return a[0];
    }

    @Override
    protected void onPostExecute(MainActivity.FragmentMaker a){
        MainActivity.runFragment(a);
    }

    private static void setEvents(String url)  throws IOException, JSONException {
        MySpreadsheetIntegration data = new MySpreadsheetIntegration();
        ArrayList<Event> events = new ArrayList<>();

        String jsonData = data.downloadUrl(url);

        JSONObject jsonObject = data.getJSONObject(jsonData);

        JSONArray rows =jsonObject.getJSONObject("table").getJSONArray("rows");

        JSONArray namedata = rows.getJSONObject(1).getJSONArray("c");
        JSONArray datetime = rows.getJSONObject(2).getJSONArray("c");
        JSONArray linkdata = rows.getJSONObject(3).getJSONArray("c");
        JSONArray locationdata = rows.getJSONObject(4).getJSONArray("c");
        JSONArray descriptiondata = rows.getJSONObject(5).getJSONArray("c");

        for(int i=1; i<namedata.length(); i++) {
            Event tempEvent = new Event();
            events.add(tempEvent);
            try {
                String a = datetime.getJSONObject(i).getString("v");
                @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("h:mma MMMM dd, yyyy");
                tempEvent.setDate(formatter.parse(a));
                tempEvent.setName(namedata.getJSONObject(i).getString("v"));
                tempEvent.setLink(linkdata.getJSONObject(i).getString("v"));
                tempEvent.setLocation(locationdata.getJSONObject(i).getString("v"));
                tempEvent.setDescription(descriptiondata.getJSONObject(i).getString("v"));
            }
            catch (Exception e){
                Log.e(TAG, e.toString());
            }
        }

        upcoming = events.get(0).getName();
        Log.w(TAG, events.get(0) +" events.get(0)");
        Log.w(TAG,""+events.size() +" events.get(0)");
        MySpreadsheetIntegration.eventList = events;

    }

    private String downloadUrl(String urlString) throws IOException {
        InputStream is = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            Log.w(TAG, "CONN>CONECT");
            conn.connect();
            Log.w(TAG, "CONN>DONE");
            //int responseCode = conn.getResponseCode();
            is = conn.getInputStream();

            return convertStreamToString(is);
        } finally {
            if (is != null)
                //noinspection ThrowFromFinallyBlock
                is.close();
        }
    }

    private JSONObject getJSONObject(String result) throws JSONException {
        // remove the unnecessary parts from the response and construct a JSON
        JSONObject table;
        int start = result.indexOf("{", 0);
        int end = result.lastIndexOf("}")+1;
        String jsonResponse = result.substring(start, end);
        table = new JSONObject(jsonResponse);
        return table;
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.v(TAG, sb.toString());
        return sb.toString();
    }

}