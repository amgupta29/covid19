package com;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GenerateHeatMap {

    public void addHeatMap(GoogleMap pMap) throws JSONException {
        //TODO: Load the data from the server
        List<LatLng> fluList = null;
        List<LatLng> covidList = null;

            covidList = readCovidItems();
            fluList = readFluItems();


        Gradient covidGradient = new Gradient(covidColors, startPoints);
        Gradient fluGradient = new Gradient(fluColors, startPoints);

        HeatmapTileProvider covidProvider = new HeatmapTileProvider.Builder()
                .data(covidList).gradient(covidGradient).radius(50)
                .build();
        HeatmapTileProvider fluProvider = new HeatmapTileProvider.Builder()
                .data(fluList).gradient(fluGradient).radius(25)
                .build();
        // Add a tile overlay to the map, using the heat map tile provider.
        pMap.addTileOverlay(new TileOverlayOptions().tileProvider(covidProvider));
        pMap.addTileOverlay(new TileOverlayOptions().tileProvider(fluProvider));
    }

    // Create the gradient.
    int[] covidColors = {
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
