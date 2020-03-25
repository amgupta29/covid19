package com;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.domain.RequestDataObject;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.service.LocationUpdatesService;
import com.utils.PermissionUtils;
import com.utils.Utils;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener  {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 0x1;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 0x2;

    // UI elements.
    private Button mUpdatesButton;
    private Button mStopButton;
    private LinearLayout mStopButtonPanel;
    private LinearLayout mInputPanel;
    private GoogleMap mMap;
    private Location mCurrentLocation;
    private Location mLocation;
    private FusedLocationProviderClient mFusedLocationClient;
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
    private GenerateHeatMap mGenerateHeatMap;
    private RequestDataObject mRequestDataObject = new RequestDataObject();

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;

    // A reference to the service used to get location updates.
    private LocationUpdatesService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(Utils.KEY_REQUESTING_LOCATION_UPDATES)) {
            setButtonsState(sharedPreferences.getBoolean(Utils.KEY_REQUESTING_LOCATION_UPDATES,
                    false));
        }
    }

    private void setButtonsState(boolean requestingLocationUpdates) {
        if (requestingLocationUpdates) {
            mStopButtonPanel.setVisibility(View.VISIBLE);
            mInputPanel.setVisibility(View.INVISIBLE);
        } else {
            mStopButtonPanel.setVisibility(View.INVISIBLE);
            mInputPanel.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Receiver for broadcasts sent by {@link LocationUpdatesService}.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null) {
                Toast.makeText(MainActivity.this, Utils.getLocationText(location),
                        Toast.LENGTH_SHORT).show();

                mCurrentLocation = location;
                mRequestDataObject = prepareOutboundData();
                mService.initialiseDataObject(mRequestDataObject, mContext);
                //mGenerateHeatMap.sendRequest(mRequestDataObject, mContext);
                //mGenerateHeatMap.getMapData(mContext, mMap);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();
        setContentView(R.layout.syptoms_heatmap);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Check that the user hasn't revoked permissions by going to Settings.
        if (Utils.requestingLocationUpdates(this)) {
            if (!checkPermissions()) {
                requestPermissions();
            }
        }

        myReceiver = new MyReceiver();
        mGenerateHeatMap = new GenerateHeatMap();

        UIInitialisation();
    }


    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        mUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkPermissions()) {
                    requestPermissions();
                } else {
                    mService.requestLocationUpdates();
                    //prepareOutboundData();
                    mGenerateHeatMap.getMapData(mContext, mMap);
                }
            }
        });

        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mService.removeLocationUpdates();
                resetUI();
            }
        });

        mHealthy.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!checkPermissions()) {
                    requestPermissions();

                } else {
                    mService.requestLocationUpdates();
                    //prepareOutboundData();
                    mGenerateHeatMap.getMapData(mContext, mMap);
                }

            }
        });

        // Restore the state of the buttons when the activity (re)launches.
        setButtonsState(Utils.requestingLocationUpdates(this));

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);

        super.onStop();
    }

    private boolean checkPermissions() {
        return  PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    findViewById(R.id.map_container),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                enableMyLocation();
                //mService.requestLocationUpdates();
                //setButtonsState(Utils.requestingLocationUpdates(this));
            } else {
                // Permission denied.
                setButtonsState(false);
                Snackbar.make(
                        findViewById(R.id.map_container),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }

    private void UIInitialisation() {
        mStopButtonPanel = (LinearLayout) findViewById(R.id.stopButtonPanel);
        mInputPanel = (LinearLayout) findViewById(R.id.inputBox);
        mUpdatesButton = (Button) findViewById(R.id.update);
        mStopButton = (Button) findViewById(R.id.stopButton);
        RadioGroup lRadioGroup = (RadioGroup) findViewById(R.id.radioLevels);
        mCovid19 = (RadioButton) findViewById(R.id.covid19);
        mSymptom = (RadioButton) findViewById(R.id.symptoms);
        mHealthy = (Button) findViewById(R.id.healthy);

        if( !mCovid19.isSelected() && !mSymptom.isSelected()) {
            mUpdatesButton.setEnabled(false);
        } else {
            mUpdatesButton.setEnabled(true);
        }

        lRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.symptoms) {
                    LinearLayout lInputCheckBoxPanel = (LinearLayout) findViewById(R.id.inputCheckbox);
                    lInputCheckBoxPanel.setVisibility(View.VISIBLE);
                } else if (checkedId == R.id.covid19) {
                    mCovid19.setSelected(true);
                    LinearLayout lInputCheckBoxPanel = (LinearLayout) findViewById(R.id.inputCheckbox);
                    lInputCheckBoxPanel.setVisibility(View.GONE);
                }
                mUpdatesButton.setEnabled(true);
            }
        });

        /*mHealthy.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //mGenerateHeatMap.getMapData(mMap, mContext);
            }
        });*/
    }

    private void resetUI() {
        mSymptom = (RadioButton) findViewById(R.id.symptoms);
        mSymptom.setSelected(false);

        mCough = (CheckBox) findViewById(R.id.cough);
        mCough.setChecked(false);
        mFever = (CheckBox) findViewById(R.id.fever);
        mFever.setChecked(false);
        mSoreThroat = (CheckBox) findViewById(R.id.soreThroat);
        mSoreThroat.setChecked(false);
        mBreathless = (CheckBox) findViewById(R.id.breathless);
        mBreathless.setChecked(false);
        mBreathless = (CheckBox) findViewById(R.id.breathless);
        mBreathless.setChecked(false);
        mChestPain = (CheckBox) findViewById(R.id.chestPain);
        mChestPain.setChecked(false);
        mWeakness = (CheckBox) findViewById(R.id.severeWeakness);
        mWeakness.setChecked(false);
        mDiahorrea = (CheckBox) findViewById(R.id.diahorrea);
        mDiahorrea.setChecked(false);

        mCovid19 = (RadioButton) findViewById(R.id.covid19);
        mCovid19.setSelected(false);

        //reset map overlay
        if(null != mGenerateHeatMap.mCovid19TileOverlay)
            mGenerateHeatMap.mCovid19TileOverlay.remove();
        if(null != mGenerateHeatMap.mFluTileOverlay)
            mGenerateHeatMap.mFluTileOverlay.remove();
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

    private RequestDataObject prepareOutboundData() {
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

//        mGenerateHeatMap.sendRequest(requestDataObject, this);
//        mGenerateHeatMap.getMapData(mContext, mMap);

        return requestDataObject;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);

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

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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
