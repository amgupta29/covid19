package com;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.domainObjects.OperationResponse;
import com.domainObjects.RequestDataObject;
import com.domainObjects.ResponseMapDataObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.gson.Gson;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class GenerateHeatMap {

    private String url ="https://3cwnx8b850.execute-api.eu-west-1.amazonaws.com/prod/open/heatmapNew";

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void addHeatMap(GoogleMap pMap, JSONObject mapData) {
        ObjectMapper objectMapper = new ObjectMapper();
        ResponseMapDataObject responseMapDataObject = null;
        try {
            responseMapDataObject = objectMapper.readValue(String.valueOf(mapData), ResponseMapDataObject.class);
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            Log.e("MAP_DATA_EXCEPTION", sw.toString());
        }

        List<LatLng> fluList = new ArrayList<>();
        List<LatLng> covid19List = new ArrayList<>();
        List<OperationResponse> operationResponseList = responseMapDataObject.getOperationResponse();
        for (OperationResponse operationResponseListObj:
        operationResponseList) {
            LatLng latLng = new LatLng(operationResponseListObj.getValue().getLatitude(), operationResponseListObj.getValue().getLongitude());
            if(operationResponseListObj.getValue().getDiagnosisCovid19() != null && operationResponseListObj.getValue().getDiagnosisCovid19()) {
                covid19List.add(latLng);
                Log.e("COVID19_DATA", fluList.toString());
            } else if(operationResponseListObj.getValue().getDiagnosisFluSymptoms() != null && (operationResponseListObj.getValue().getDiagnosisFluSymptoms() || operationResponseListObj.getValue().getDiagnosisInfluenze())) {
                fluList.add(latLng);
                Log.e("FLU_DATA", fluList.toString());

            }

        }

        Gradient covid19Gradient = new Gradient(covid19Colors, startPoints);
        Gradient fluGradient = new Gradient(fluColors, startPoints);

        if(!covid19List.isEmpty()) {
            HeatmapTileProvider covid19Provider = new HeatmapTileProvider.Builder()
                    .data(covid19List).gradient(covid19Gradient).radius(50)
                    .build();
            // Add a tile overlay to the map, using the heat map tile provider.
            pMap.addTileOverlay(new TileOverlayOptions().tileProvider(covid19Provider));
        }
        if(!fluList.isEmpty()) {
            HeatmapTileProvider fluProvider = new HeatmapTileProvider.Builder()
                    .data(fluList).gradient(fluGradient).radius(50)
                    .build();
            pMap.addTileOverlay(new TileOverlayOptions().tileProvider(fluProvider));
        }
    }

    // Create the gradient.
    int[] covid19Colors = {
            Color.rgb(102, 225, 0), // green
            Color.rgb(255, 0, 0)    // red
    };

    int[] fluColors = {
            Color.rgb(102, 225, 0), // green
            Color.rgb(255, 225, 0)    // red
    };

    float[] startPoints = {
            0.2f, 1f
    };

    public void sendRequest(GoogleMap mMap, RequestDataObject requestDataObject, Context context) {
        Gson gson = new Gson();
        String outBoundMessage = gson.toJson(requestDataObject);

        Log.d("REQUEST_OBJECT", outBoundMessage);

        JSONObject postparams=null;
        try {
            postparams = new JSONObject(outBoundMessage);
        } catch (JSONException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            Log.e("OUT_DATA_EXCEPTION", sw.toString());
        }
        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.PUT,
                url, postparams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Display the first 500 characters of the response string.
                        Log.d("PUT", response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                error.printStackTrace(pw);
                Log.e("PUT_ERROR", sw.toString());
            }
        });

        // Add the request to the RequestQueue.
        RequestQueueSingleton.getInstance(context).addToRequestQueue(jsonObjReq);

        getMapData(mMap, context);
    }

    public void getMapData(final GoogleMap mMap, Context context) {
        Log.d("REQUEST_MAP_DATA", "");

        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("GET", response.toString());
                            addHeatMap(mMap, response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        error.printStackTrace(pw);
                        Log.e("GET_ERROR", sw.toString());

                    }
                });
        // Add the request to the RequestQueue.
        RequestQueueSingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);

    }


    /**
     * Test data
     */
    private ArrayList<LatLng> readCovidItems() throws JSONException {
        ArrayList<LatLng> list = new ArrayList<>();
        String json = "[\n" +
                "   {\n" +
                "      \"lat\":57.708870,\n" +
                "      \"lng\":11.974560\n" +
                "   },\n" +
                "   {\n" +
                "       \"lat\":57.708860,\n" +
                "      \"lng\":11.974560\n" +
                "   },\n" +
                "   {\n" +
                "       \"lat\":57.708870,\n" +
                "      \"lng\":11.974550\n" +
                "   },\n" +
                "   {\n" +
                "       \"lat\":57.708840,\n" +
                "      \"lng\":11.974560\n" +
                "   },\n" +
                "   {\n" +
                "       \"lat\":57.708870,\n" +
                "      \"lng\":11.974540\n" +
                "   }\n" +
                "]";
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            double lat = object.getDouble("lat");
            double lng = object.getDouble("lng");
            list.add(new LatLng(lat, lng));
        }
        return list;
    }

    private ArrayList<LatLng> readFluItems() throws JSONException {
        ArrayList<LatLng> list = new ArrayList<>();
        String json = "[\n" +
                "   {\n" +
                "      \"lat\":57.708870,\n" +
                "      \"lng\":11.975560\n" +
                "   },\n" +
                "   {\n" +
                "       \"lat\":57.708860,\n" +
                "      \"lng\":11.974660\n" +
                "   },\n" +
                "   {\n" +
                "       \"lat\":57.708870,\n" +
                "      \"lng\":11.974750\n" +
                "   },\n" +
                "   {\n" +
                "       \"lat\":57.708840,\n" +
                "      \"lng\":11.978560\n" +
                "   },\n" +
                "   {\n" +
                "       \"lat\":57.708870,\n" +
                "      \"lng\":11.979540\n" +
                "   }\n" +
                "]";
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            double lat = object.getDouble("lat");
            double lng = object.getDouble("lng");
            list.add(new LatLng(lat, lng));
        }
        return list;
    }
}
