package com.service;

import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.domain.OperationResponse;
import com.domain.ResponseMapDataObject;
import com.domain.Value;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.gson.Gson;
import com.google.maps.android.heatmaps.WeightedLatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenerateHeatMap {

    private static final String TAG = MapDataProcessor.class.getSimpleName();
    public TileOverlay mCovid19TileOverlay;
    public TileOverlay mFluTileOverlay;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean addHeatMap(String mapData, GoogleMap pMap) {
        ObjectMapper objectMapper = new ObjectMapper();
        ResponseMapDataObject responseMapDataObject = null;
        try {
            responseMapDataObject = objectMapper.readValue(mapData, ResponseMapDataObject.class);
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            Log.e("MAP_DATA_EXCEPTION", sw.toString());
        }

        List<LatLng> fluList = new ArrayList<>();
        List<LatLng> covid19List = new ArrayList<>();

        List<OperationResponse> operationResponseList = responseMapDataObject.getOperationResponse();
        Gson gson = new Gson();
        String post_response = gson.toJson(operationResponseList);
        Log.e(TAG, "addHeatMap(), POST_RESPONSE : " + "; size : " + operationResponseList.size() + "; " + post_response);

        Map<String, Value> singlePointData = new HashMap<>();
        for (OperationResponse operationResponseListObj :
                operationResponseList) {
            if (singlePointData.containsKey(operationResponseListObj.getSubmitter().getIdentifier())) {
                if (singlePointData.get(operationResponseListObj.getSubmitter().getIdentifier()).getTimestamp() < operationResponseListObj.getValue().getTimestamp()) {
                    singlePointData.put(operationResponseListObj.getSubmitter().getIdentifier(),
                            createValueObject(operationResponseListObj));
                }
                Log.e(TAG, "REJECTED : " + toString(operationResponseListObj));
            } else {
                singlePointData.put(operationResponseListObj.getSubmitter().getIdentifier(), createValueObject(operationResponseListObj));
            }
            Log.e("PUT_IN_MAP", toString(operationResponseListObj) + ", size : " + singlePointData.size());
        }

        System.out.println(singlePointData);

        if (!post_response.isEmpty()) {
            for (Map.Entry<String, Value> singlePointDataObj :
                    singlePointData.entrySet()) {
                LatLng latLng = new LatLng(singlePointDataObj.getValue().getLatitude(),
                        singlePointDataObj.getValue().getLongitude());
                //WeightedLatLng weightedLatLng = new WeightedLatLng(latLng,10);

                if (singlePointDataObj.getValue().getDiagnosisCovid19() != null
                        && singlePointDataObj.getValue().getDiagnosisCovid19()) {
                    //covid19List.add(latLng);
                    covid19List.add(latLng);
                    Log.d("COVID19_DATA", covid19List.toString()
                            + ";" + singlePointDataObj.getKey()
                            + "; " + singlePointDataObj.getValue().getTimestamp());
                } else if (singlePointDataObj.getValue().getDiagnosisFluSymptoms() != null
                        && singlePointDataObj.getValue().getDiagnosisFluSymptoms()) {
                    //              fluList.add(latLng);
                    fluList.add(latLng);
                    Log.d("FLU_DATA", fluList.toString() + "; " + singlePointDataObj.getKey() + "; " + singlePointDataObj.getValue().getTimestamp());

                }
            }
            Log.e("LIST_SIZE", covid19List.size() + "; " + fluList.size());

        }

        fluList.stream().forEach((LatLng location) -> {
            pMap.addCircle(new CircleOptions()
                    .center(location/*new LatLng(57.708870, 11.974540)*/)
                    .radius(100)
                    .strokeColor(Color.BLUE)
                    .fillColor(Color.YELLOW));

            pMap.addMarker(new MarkerOptions()
                    .position(location)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

        });

        covid19List.stream().forEach((LatLng location) -> {
            pMap.addCircle(new CircleOptions()
                    .center(location/*new LatLng(57.708870, 11.974540)*/)
                    .radius(100)
                    .strokeColor(Color.BLUE)
                    .fillColor(Color.RED));

            pMap.addMarker(new MarkerOptions()
                    .position(location)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        });

        covid19List.clear();
        fluList.clear();
        singlePointData.clear();

        return true;
    }

    private Value createValueObject(OperationResponse operationResponseListObj) {
        boolean flu = false, covid = false;
        if (null != operationResponseListObj.getValue().getDiagnosisCovid19()) {
            covid = operationResponseListObj.getValue().getDiagnosisCovid19();
        }

        if (null != operationResponseListObj.getValue().getDiagnosisFluSymptoms()) {
            flu = operationResponseListObj.getValue().getDiagnosisFluSymptoms();
        }

        return new Value(operationResponseListObj.getValue().getLongitude(),
                operationResponseListObj.getValue().getLatitude(),
                operationResponseListObj.getValue().getTimestamp(),
                covid,
                flu);
    }

    private String toString(OperationResponse obj) {
        return obj.getSubmitter().getIdentifier() + " ; " +
                obj.getValue().getLatitude() + " ; " +
                obj.getValue().getLongitude() + "; " +
                obj.getValue().getTimestamp() + " ; " +
                obj.getValue().getDiagnosisCovid19() + " ; " +
                obj.getValue().getDiagnosisFluSymptoms();
    }

        /*try {
            fluList = readFluItems();
            covid19List = readCovidItems();
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

    //Gradient fluGradient = new Gradient(fluColors, startPoints);
    //Gradient covid19Gradient = new Gradient(covid19Colors, startPoints);

      /*  if(!fluList.isEmpty()) {
            HeatmapTileProvider fluProvider = new HeatmapTileProvider.Builder()
                    .weightedData(fluList).gradient(fluGradient).radius(50).opacity(1).maxIntensity(10)
                    .build();
            mFluTileOverlay = pMap.addTileOverlay(new TileOverlayOptions().tileProvider(fluProvider));
            mFluTileOverlay.setVisible(true);
        }

        if(!covid19List.isEmpty()) {
            HeatmapTileProvider covid19Provider = new HeatmapTileProvider.Builder()
                    .weightedData(covid19List).gradient(covid19Gradient).radius(50).opacity(1).maxIntensity(10)
                    .build();
            // Add a tile overlay to the map, using the heat map tile provider.
            mCovid19TileOverlay = pMap.addTileOverlay(new TileOverlayOptions().tileProvider(covid19Provider));
            mCovid19TileOverlay.setVisible(true);
        }*/
    //return false;
    //}

    // Create the gradient.
    private int[] covid19Colors = {
            Color.rgb(79, 195, 247), // blue
            //Color.rgb(255, 235, 59), // yellow
            //Color.rgb(255, 152, 0), // orange
            //Color.rgb(255, 152, 0), // orange
            //Color.rgb(255, 0, 0),   // red
            Color.rgb(255, 0, 0), //red
            Color.rgb(255, 0, 0), // red
            Color.rgb(255, 0, 0)    // red
    };

    private int[] fluColors = {
            //Color.rgb(225, 215, 0), // green
            //Color.rgb(255, 140, 0)    // orange
            Color.rgb(79, 195, 247), // blue
            //Color.rgb(255, 235, 59), // yellow
            //Color.rgb(255, 235, 59), // yellow
            //Color.rgb(255, 235, 59), // yellow
            Color.rgb(255, 152, 0), // orange
            Color.rgb(255, 152, 0), // orange
            Color.rgb(255, 152, 0) // orange
            //Color.rgb(255, 152, 0), //orange
            // Color.rgb(244, 100, 54)    // red
    };

    private float[] startPoints = {
            0.2f, 0.8f, 0.9f, 1.0f
    };


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

    private ArrayList<WeightedLatLng> readCovidItemsWT() throws JSONException {
        ArrayList<WeightedLatLng> list = new ArrayList<>();
        String json = "[\n" +
                "   {\n" +
                "      \"lat\":57.70,\n" +
                "      \"lng\":11.97\n" +
                "   },\n" +
                "   {\n" +
                "       \"lat\":57.70,\n" +
                "      \"lng\":11.97\n" +
                "   },\n" +
                "   {\n" +
                "       \"lat\":57.70,\n" +
                "      \"lng\":11.97\n" +
                "   },\n" +
                "   {\n" +
                "       \"lat\":57.70,\n" +
                "      \"lng\":11.97\n" +
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
            list.add(new WeightedLatLng(new LatLng(lat, lng), 10));
        }
        return list;
    }

    private ArrayList<WeightedLatLng> readFluItemsWT() throws JSONException {
        ArrayList<WeightedLatLng> list = new ArrayList<>();
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
            list.add(new WeightedLatLng(new LatLng(lat, lng), 10));
        }
        return list;
    }
}
