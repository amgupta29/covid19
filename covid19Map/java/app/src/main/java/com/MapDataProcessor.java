package com;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.domain.RequestDataObject;
import com.google.android.gms.maps.GoogleMap;
import com.google.gson.Gson;
import com.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;

public class MapDataProcessor {

    private String url ="https://3cwnx8b850.execute-api.eu-west-1.amazonaws.com/prod/open/heatmapNew";
    private GenerateHeatMap mGenerateHeatMap = new GenerateHeatMap();

    public void getMapData(Context context, final GoogleMap pMap) {
        Log.e("REQUEST_MAP_DATA", "context :"+context);
        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("GET", response.toString());
                        mGenerateHeatMap.addHeatMap(response.toString(), pMap);

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
        //return responseMapData;
    }

    public void sendRequest(RequestDataObject requestDataObject, Context context) {
        Gson gson = new Gson();
        String outBoundMessage = gson.toJson(requestDataObject);

        Log.d("REQUEST_OBJECT", outBoundMessage);

        JSONObject postparams = null;
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
    }
}
