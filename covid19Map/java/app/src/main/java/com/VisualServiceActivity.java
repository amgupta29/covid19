package com;/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.domainObjects.DataObject;
import com.domainObjects.Symptom;

/**
 * This demonstrates how to add a tile overlay to a map.
 */
public class VisualServiceActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;
    private Location mLocation;
    private RadioButton mCovid;
    private RadioButton mFlu;
    private RadioButton mNormal;
    private Button mUpdate;
    private CheckBox mCough;
    private CheckBox mFever;
    private CheckBox mSoreThroat;
    private CheckBox mDiahorrea;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.syptoms_heatmap);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        RadioGroup lRadioGroup = (RadioGroup) findViewById(R.id.radioLevels);
        lRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged (RadioGroup group,int checkedId) {
                if (checkedId == R.id.flu) {
                    LinearLayout lInputCheckBoxPanel = (LinearLayout) findViewById(R.id.inputCheckbox);
                    lInputCheckBoxPanel.setVisibility(View.VISIBLE);
                    mCough = (CheckBox) findViewById(R.id.cough);
                    mFever = (CheckBox) findViewById(R.id.fever);
                    mFlu = (RadioButton) findViewById(R.id.flu);
                    mSoreThroat = (CheckBox) findViewById(R.id.soreThroat);
                    mDiahorrea = (CheckBox) findViewById(R.id.diahorrea);
                } else if (checkedId == R.id.covid19) {
                    mCovid = (RadioButton) findViewById(R.id.covid19);
                    LinearLayout lInputCheckBoxPanel = (LinearLayout) findViewById(R.id.inputCheckbox);
                    lInputCheckBoxPanel.setVisibility(View.GONE);
                } else {
                    mNormal = (RadioButton) findViewById(R.id.normal);
                    LinearLayout lInputCheckBoxPanel = (LinearLayout) findViewById(R.id.inputCheckbox);
                    lInputCheckBoxPanel.setVisibility(View.GONE);
                }
            }
        });

        mUpdate = (Button) findViewById(R.id.update);
        mUpdate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addHeatMap();

                LinearLayout lInputPanel = (LinearLayout) findViewById(R.id.inputBox);
                lInputPanel.setVisibility(View.INVISIBLE);

                prepareOutboundData();
            }
        });
    }

    private void prepareOutboundData() {
        DataObject dataObject = new DataObject();
        dataObject.setLocation(new LatLng(mLocation.getLatitude(),  mLocation.getLongitude()));
        dataObject.setTimestamp(System.currentTimeMillis());
        if(mCovid != null) {
            dataObject.setCovid(mCovid.getText().toString());
        }
        if(mNormal != null) {
            dataObject.setHealthy(mNormal.getText().toString());
        }

        if(mFlu != null) {
            Symptom flu = new Symptom();
            if(mCough != null) {
                flu.setCough(mCough.getText().toString());
            }
            if(mFever != null) {
                flu.setFever(mFever.getText().toString());
            }
            if(mSoreThroat != null) {
                flu.setSoreThroat(mSoreThroat.getText().toString());
            }
            if(mDiahorrea != null) {
                flu.setDiahorrea(mDiahorrea.getText().toString());
            }

            dataObject.setFlu(flu);
        }

        prepareJson(dataObject);
    }

    private void prepareJson(DataObject dataObject) {
        Gson gson = new Gson();
        String outBoundMessage = gson.toJson(dataObject);

        System.out.println(outBoundMessage);

        //Todo: Send the data to the server
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //addHeatMap();

        map.setOnMyLocationButtonClickListener(this);
        map.setOnMyLocationClickListener(this);

        enableMyLocation();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        // [START maps_check_location_permission]
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                mLocation = location;
                            }
                        }
                    });

        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
        // [END maps_check_location_permission]
    }

    // [START maps_check_location_permission_result]
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Permission was denied. Display an error message
            // [START_EXCLUDE]
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
            // [END_EXCLUDE]
        }
    }
    // [END maps_check_location_permission_result]


    private void addHeatMap() {
        //TODO: Load the data from the server
        List<LatLng> fluList = null;
        List<LatLng> covidList = null;

        try {
            covidList = readCovidItems();
            fluList = readFluItems();
        } catch (JSONException e) {
            Toast.makeText(this, "Problem reading list of locations.", Toast.LENGTH_LONG).show();
        }

        Gradient covidGradient = new Gradient(covidColors, startPoints);
        Gradient fluGradient = new Gradient(fluColors, startPoints);

        HeatmapTileProvider covidProvider = new HeatmapTileProvider.Builder()
                .data(covidList).gradient(covidGradient).radius(50)
                .build();
        HeatmapTileProvider fluProvider = new HeatmapTileProvider.Builder()
                .data(fluList).gradient(fluGradient).radius(25)
                .build();
        // Add a tile overlay to the map, using the heat map tile provider.
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(covidProvider));
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(fluProvider));
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

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "Taking you to your current location", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location.getLatitude()+", "+location.getLongitude()+",\nTimestamp: "+location.getTime(), Toast.LENGTH_LONG).show();
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
