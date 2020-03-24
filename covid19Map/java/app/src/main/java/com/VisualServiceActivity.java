package com;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.domainObjects.RequestDataObject;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class VisualServiceActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener  {

    private static final String TAG = VisualServiceActivity.class.getSimpleName();
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private Boolean mRequestingLocationUpdates;
    private String mLastUpdateTime;


    private GoogleMap mMap;
    private Location mLocation;
    private RadioButton mCovid19;
    private RadioButton mSymptom;
    private CheckBox mCough;
    private CheckBox mFever;
    private CheckBox mSoreThroat;
    private CheckBox mBreathless;
    private CheckBox mChestPain;
    private CheckBox mWeakness;
    private CheckBox mDiahorrea;
    private Button mUpdate;
    private Button mHealthy;
    private static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        setContentView(R.layout.syptoms_heatmap);

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            mCurrentLocation = mLocation = location;
                        }
                    }
                });

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        RadioGroup lRadioGroup = (RadioGroup) findViewById(R.id.radioLevels);
        lRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.symptoms) {
                    LinearLayout lInputCheckBoxPanel = (LinearLayout) findViewById(R.id.inputCheckbox);
                    lInputCheckBoxPanel.setVisibility(View.VISIBLE);
                    mSymptom = (RadioButton) findViewById(R.id.symptoms);

                } else if (checkedId == R.id.covid19) {
                    mCovid19 = (RadioButton) findViewById(R.id.covid19);
                    mCovid19.setSelected(true);
                    LinearLayout lInputCheckBoxPanel = (LinearLayout) findViewById(R.id.inputCheckbox);
                    lInputCheckBoxPanel.setVisibility(View.GONE);
                }
            }
        });

        mHealthy = (Button) findViewById(R.id.healthy);
        mHealthy.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LinearLayout lInputPanel = (LinearLayout) findViewById(R.id.inputBox);
                lInputPanel.setVisibility(View.INVISIBLE);

                new GenerateHeatMap().getMapData(mMap, mContext);
            }
        });
        mUpdate = (Button) findViewById(R.id.update);
        mUpdate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LinearLayout lInputPanel = (LinearLayout) findViewById(R.id.inputBox);
                lInputPanel.setVisibility(View.INVISIBLE);

                prepareOutboundData();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        break;
                }
                break;
        }
    }

    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        switch (view.getId()) {
            case R.id.cough:
                if (checked) {
                    mCough = (CheckBox) findViewById(R.id.cough);
                    mCough.setChecked(true);
                }
                break;
            case R.id.fever:
                if (checked) {
                    mFever = (CheckBox) findViewById(R.id.fever);
                    mFever.setChecked(true);
                }
                break;
            case R.id.soreThroat:
                if (checked) {
                    mSoreThroat = (CheckBox) findViewById(R.id.soreThroat);
                    mSoreThroat.setChecked(true);
                }
                break;
            case R.id.breathless:
                if (checked) {
                    mBreathless = (CheckBox) findViewById(R.id.breathless);
                    mBreathless.setChecked(true);
                }
                break;
            case R.id.chestPain:
                if (checked) {
                    mChestPain = (CheckBox) findViewById(R.id.chestPain);
                    mChestPain.setChecked(true);
                }
                break;
            case R.id.severeWeakness:
                if (checked) {
                    mWeakness = (CheckBox) findViewById(R.id.severeWeakness);
                    mWeakness.setChecked(true);
                }
                break;
            case R.id.diahorrea:
                if (checked) {
                    mDiahorrea = (CheckBox) findViewById(R.id.diahorrea);
                    mDiahorrea.setChecked(true);
                }
                break;
        }
    }

    private void prepareOutboundData() {
        RequestDataObject requestDataObject = new RequestDataObject();

        List<String>  symtompsList = new ArrayList<>();
        List<String>  diagnosesList = new ArrayList<>();

        requestDataObject.setId(Utils.id(mContext));
        requestDataObject.setLocation(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
        requestDataObject.setTimestamp(System.currentTimeMillis());

        if (mCovid19 != null && mCovid19.isSelected()) {
            diagnosesList.add(mCovid19.getText().toString());
        }

        if (mSymptom != null && mSymptom.isChecked()) {
            diagnosesList.add(mSymptom.getText().toString());

            if (mCough != null && mCough.isChecked()) {
                symtompsList.add(mCough.getText().toString());
            }
            if (mFever != null && mFever.isChecked()) {
                symtompsList.add(mFever.getText().toString());
            }
            if (mSoreThroat != null && mSoreThroat.isChecked()) {
                symtompsList.add(mSoreThroat.getText().toString());
            }
            if (mBreathless != null && mBreathless.isChecked()) {
                symtompsList.add(mBreathless.getText().toString());
            }
            if (mChestPain != null && mChestPain.isChecked()) {
                symtompsList.add(mChestPain.getText().toString());
            }
            if (mWeakness != null && mWeakness.isChecked()) {
                symtompsList.add(mWeakness.getText().toString());
            }
            if (mDiahorrea != null && mDiahorrea.isChecked()) {
                symtompsList.add(mDiahorrea.getText().toString());
            }

        }

        requestDataObject.setSymptoms(symtompsList);
        requestDataObject.setDiagnoses(diagnosesList);
        new GenerateHeatMap().sendRequest(mMap, requestDataObject, this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

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

            mFusedLocationClient.getLastLocation()
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
            //mPermissionDenied = true;
            // [END_EXCLUDE]
        }
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
        Toast.makeText(this, "Current location:\n" + location.getLatitude() + ", " + location.getLongitude() + ",\nTimestamp: " + location.getTime(), Toast.LENGTH_LONG).show();
    }

}
