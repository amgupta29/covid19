package com.service;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.domain.CustomLocation;
import com.domain.RequestDataObject;
import com.google.android.gms.maps.GoogleMap;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;

public class MapDataProcessor {

    private static final String TAG = MapDataProcessor.class.getSimpleName();
    private String getDataUrl_POST ="https://3cwnx8b850.execute-api.eu-west-1.amazonaws.com/prod/open/heatmapGet";
    private String sendDataUrl_PUT = "https://3cwnx8b850.execute-api.eu-west-1.amazonaws.com/prod/open/heatmapNew";
    private GenerateHeatMap mGenerateHeatMap = new GenerateHeatMap();

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void getMapData(Context pContext, final GoogleMap pMap, Location pLocation) {
        Log.e(TAG, "context : "+pContext);

        CustomLocation customLocation = new CustomLocation();
        com.domain.Location location = new com.domain.Location();
        location.setLongitude(pLocation.getLongitude());
        location.setLatitude(pLocation.getLatitude());
        customLocation.setLocation(location);

        Gson gson = new Gson();
        String outBoundMessage = gson.toJson(customLocation);
        Log.e(TAG, "LOCATION_OBJECT : " + outBoundMessage);

        JSONObject postparams = null;
        try {
            postparams = new JSONObject(outBoundMessage);
        } catch (JSONException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            Log.e(TAG, "getMapData() PARSING_EXCEPTION : " + sw.toString());
        }

        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, getDataUrl_POST, postparams, response -> {
                    Log.e(TAG, "getMapData(), POST_REQUEST_OBJECT : " + response.toString());
                    mGenerateHeatMap.addHeatMap(response.toString(), pMap);

                }, error -> {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    error.printStackTrace(pw);
                    Log.e(TAG, "getMapData(), POST_ERROR : " + sw.toString());
                });

        // Add the request to the RequestQueue.
        RequestQueueSingleton.getInstance(pContext).addToRequestQueue(jsonObjectRequest);

        Toast toast = Toast.makeText(pContext, "Map is loading...",
                Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM, 0, 400);
        toast.show();
    }

    public void sendRequest(RequestDataObject requestDataObject, Context context) {
        Gson gson = new Gson();
        String outBoundMessage = gson.toJson(requestDataObject);
        Log.d(TAG,"sendRequest(), POST_REQUEST_OBJECT : " + outBoundMessage);

        JSONObject postparams = null;
        try {
            postparams = new JSONObject(outBoundMessage);
        } catch (JSONException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            Log.e(TAG, "sendRequest(), PARSING_EXCEPTION : " + sw.toString());
        }
        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.PUT,
                sendDataUrl_PUT, postparams,
                response -> {
                    // Display the first 500 characters of the response string.
                    Log.d(TAG, "POST_REQUEST_OBJECT : " + response.toString());
                }, error -> {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    error.printStackTrace(pw);
                    Log.e(TAG, "sendRequest(), POST_ERROR : " + sw.toString());
                });

        // Add the request to the RequestQueue.
        RequestQueueSingleton.getInstance(context).addToRequestQueue(jsonObjReq);
    }
}
