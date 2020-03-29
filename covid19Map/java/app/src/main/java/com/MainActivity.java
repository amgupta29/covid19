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
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.domain.RequestDataObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.service.LocationUpdatesService;
import com.service.MapDataProcessor;
import com.utils.PermissionUtils;
import com.utils.Utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 0x1, REQUEST_PERMISSIONS_REQUEST_CODE = 0x2;
    private static Context mContext;

    // UI elements.
    private Button mUpdatesButton, mStopButton, mHealthy, mReloadButton;
    private LinearLayout mStopButtonPanel, mInputPanel, mInputCheckBoxPanel;
    private RadioButton mCovid19, mSymptom;
    private CheckBox mCough, mFever, mSoreThroat, mBreathless, mChestPain, mWeakness, mDiahorrea;
    private RadioGroup mRadioGroup;

    //Location
    private GoogleMap mMap;
    private Location mCurrentLocation, mLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private String mId = "";


    //private GenerateHeatMap mGenerateHeatMap = new GenerateHeatMap();
    private MapDataProcessor mMapDataProcessor;
    private RequestDataObject mRequestDataObject;

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;

    // A reference to the service used to get location updates.
    private LocationUpdatesService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;

    //ObjectMapper objectMapper = new ObjectMapper();

    Gson gson = new Gson();

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            SharedPreferences sharedPrefs = mContext.getSharedPreferences(
                    "RequestDataObject", Context.MODE_PRIVATE);
            if(null != sharedPrefs.getString("RequestDataObject", null)){
                String dataString = sharedPrefs.getString("RequestDataObject", null);

                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    mRequestDataObject = objectMapper.readValue(dataString, RequestDataObject.class);
                } catch (IOException e) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    Log.e(TAG, "onServiceConnected(), PARSING_EXCEPTION : " + sw.toString());
                }

                //.initialiseDataObject(mRequestDataObject);

                Log.e(TAG, "onServiceConnected : "+mRequestDataObject);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        mId = Utils.id(mContext);
        setContentView(R.layout.syptoms_heatmap);
        restartMapInitialise();

        UIInitialisation();

        Log.e("ID", mId);

        // Check that the user hasn't revoked permissions by going to Settings.
        if (Utils.requestingLocationUpdates(this)) {
            if (!checkPermissions()) {
                requestPermissions();
            }
        }

        myReceiver = new MyReceiver();
        mMapDataProcessor = new MapDataProcessor();
        mRequestDataObject = new RequestDataObject();


    }

    private void restartMapInitialise() {
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

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
                mLocation = mCurrentLocation = location;
                //mRequestDataObject = prepareOutboundData();
                mService.initialiseDataObject(mRequestDataObject);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart() {
        Log.e(TAG, "onStart");
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        onButtonActions();

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        //buttonListenerActions();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();

        SharedPreferences sharedPrefs = mContext.getSharedPreferences(
                "RequestDataObject", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("RequestDataObject", gson.toJson(mRequestDataObject));
        editor.commit();

        //mService.initialiseDataObject(mRequestDataObject);
        Log.e(TAG, "onPause : "+mRequestDataObject);

    }

    @Override
    protected void onStop() {
        Log.e(TAG, "onStop");
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            //buttonListenerActions();
            unbindService(mServiceConnection);
            mBound = false;
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);

        super.onStop();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void onButtonActions() {
        if (mUpdatesButton != null) {

            mUpdatesButton.setOnClickListener(view -> {
                if (!checkPermissions()) {
                    requestPermissions();
                } else {
                    mService.requestLocationUpdates();
                    getLastKnownOrLatestLocation();
                    if(mLocation != null) {
                        mRequestDataObject = prepareOutboundData();
                        mMapDataProcessor.sendRequest(mRequestDataObject, mContext);

                        mService.initialiseDataObject(mRequestDataObject);

                        /*Toast toast_error = Toast.makeText(MainActivity.this, "Updating your details...",
                                Toast.LENGTH_LONG);
                        toast_error.setGravity(Gravity.BOTTOM, 0, 500);
                        toast_error.show();*/
                        postMapData();

                    } else if(mRequestDataObject.getId() == null) {
                        Toast toast_error = Toast.makeText(MainActivity.this,
                                "Error getting your location please try again.",
                                Toast.LENGTH_LONG);
                        toast_error.setGravity(Gravity.BOTTOM, 0, 500);
                        toast_error.show();

                        mService.removeLocationUpdates();
                        resetUI();
                        }
                }
            });
        }

        if (mHealthy != null) {
            mHealthy.setOnClickListener(v -> {
                if (!checkPermissions()) {
                    requestPermissions();
                } else {
                    mService.requestLocationUpdates();
                    getLastKnownOrLatestLocation();
                    if(mLocation != null) {
                        mRequestDataObject = prepareOutboundData();
                        mMapDataProcessor.sendRequest(mRequestDataObject, mContext);

                        mService.initialiseDataObject(mRequestDataObject);

                        /*Toast toast_error = Toast.makeText(MainActivity.this, "Updating your details...",
                                Toast.LENGTH_LONG);
                        toast_error.setGravity(Gravity.BOTTOM, 0, 500);
                        toast_error.show();*/

                        postMapData();
                    } else if(mRequestDataObject.getId() == null) {
                        Toast toast_error = Toast.makeText(MainActivity.this,
                                "Error getting your location please try again.",
                                Toast.LENGTH_LONG);
                        toast_error.setGravity(Gravity.BOTTOM, 0, 500);
                        toast_error.show();

                        mService.removeLocationUpdates();
                        resetUI();
                    }

                    //postMapData();
                }
            });
        }

        if (mReloadButton != null) {

            mReloadButton.setOnClickListener(v -> {
                if (!checkPermissions()) {
                    requestPermissions();
                } else {
                    //mService.requestLocationUpdates();
                    mMap.clear();
                    mService.requestLocationUpdates();
                    getLastKnownOrLatestLocation();
                    if(mLocation != null) {
                            //mRequestDataObject = prepareOutboundData();
                            mRequestDataObject.setLocation(new com.domain.Location(mLocation.getLatitude(),
                                    mLocation.getLongitude()));
                            mMapDataProcessor.sendRequest(mRequestDataObject, mContext);

                            mService.initialiseDataObject(mRequestDataObject);

                        /*Toast toast_error = Toast.makeText(MainActivity.this, "Updating your details...",
                                Toast.LENGTH_LONG);
                        toast_error.setGravity(Gravity.BOTTOM, 0, 500);
                        toast_error.show();*/
                            postMapData();

                    }
                    //mMap.
                }
            });
        }

        if (mStopButton != null) {
            mStopButton.setOnClickListener(view -> {
                mService.removeLocationUpdates();
                mMap.clear();
                resetUI();
            });
        }

        // Restore the state of the buttons when the activity (re)launches.
        setButtonsState(Utils.requestingLocationUpdates(this));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void postMapData() {
        if (mLocation != null) {
            mMapDataProcessor.getMapData(mContext, mMap, mLocation);
        } else {
            Toast toast_error = Toast.makeText(MainActivity.this, "Unable to get your location, please turn your location service on and try again.",
                    Toast.LENGTH_LONG);
            toast_error.setGravity(Gravity.BOTTOM, 0, 500);
            toast_error.show();
        }
    }

    private boolean checkPermissions() {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
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
                //if(mMap == null) {
                //restartMapInitialise();
                enableMyLocation();
                //resetUI();
                //}
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
        mRadioGroup = (RadioGroup) findViewById(R.id.radioLevels);
        mCovid19 = (RadioButton) findViewById(R.id.covid19);
        mSymptom = (RadioButton) findViewById(R.id.symptoms);
        mHealthy = (Button) findViewById(R.id.healthy);
        mSymptom = (RadioButton) findViewById(R.id.symptoms);
        mCough = (CheckBox) findViewById(R.id.cough);
        mFever = (CheckBox) findViewById(R.id.fever);
        mSoreThroat = (CheckBox) findViewById(R.id.soreThroat);
        mBreathless = (CheckBox) findViewById(R.id.breathless);
        mBreathless = (CheckBox) findViewById(R.id.breathless);
        mChestPain = (CheckBox) findViewById(R.id.chestPain);
        mWeakness = (CheckBox) findViewById(R.id.severeWeakness);
        mDiahorrea = (CheckBox) findViewById(R.id.diahorrea);
        mCovid19 = (RadioButton) findViewById(R.id.covid19);
        mReloadButton = (Button) findViewById(R.id.reloadButton);
        mInputCheckBoxPanel = (LinearLayout) findViewById(R.id.inputCheckbox);

        resetUI();

    }

    private void resetUI() {

        mSymptom.setChecked(false);
        mCough.setChecked(false);
        mFever.setChecked(false);
        mSoreThroat.setChecked(false);
        mBreathless.setChecked(false);
        mChestPain.setChecked(false);
        mWeakness.setChecked(false);
        mDiahorrea.setChecked(false);
        mCovid19.setChecked(false);

        mUpdatesButton.setEnabled(false);
        mHealthy.setEnabled(true);

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

    public void onCovid19Clicked(View v) {
        mCovid19.setChecked(true);


        mInputCheckBoxPanel.setVisibility(View.GONE);
        mSymptom.setChecked(false);
        mCough.setChecked(false);
        mFever.setChecked(false);
        mSoreThroat.setChecked(false);
        mBreathless.setChecked(false);
        mBreathless.setChecked(false);
        mChestPain.setChecked(false);
        mWeakness.setChecked(false);
        mDiahorrea.setChecked(false);

        mUpdatesButton.setEnabled(true);

    }

    public void onFluSymptomsClicked(View v) {
        //mSymptom.setSelected(true);
        mSymptom.setChecked(true);
        mInputCheckBoxPanel.setVisibility(View.VISIBLE);

        mCovid19.setChecked(false);

        mUpdatesButton.setEnabled(true);
    }

    private RequestDataObject prepareOutboundData() {
        RequestDataObject requestDataObject = new RequestDataObject();
        List<String> symtompsList = new ArrayList<>();
        List<String> diagnosesList = new ArrayList<>();

        requestDataObject.setId(mId);
        requestDataObject.setLocation(new com.domain.Location(mCurrentLocation.getLatitude(),
                mCurrentLocation.getLongitude()));
        requestDataObject.setTimestamp(System.currentTimeMillis());

        if (mCovid19 != null && mCovid19.isChecked()) {
            diagnosesList.add("Covid19 Positive");
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

        Log.e(TAG, gson.toJson(requestDataObject));
        return requestDataObject;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        Log.e(TAG, "onMapReady");
        mMap = map;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        map.setOnMyLocationButtonClickListener(this);
        map.setOnMyLocationClickListener(this);

        enableMyLocation();

        mMap.setOnMapLoadedCallback(() -> mMap.setMyLocationEnabled(true));
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        // [START maps_check_location_permission]
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getLastKnownOrLatestLocation();
            //mMap.setMyLocationEnabled(true);

        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
        // [END maps_check_location_permission]
    }

    private void getLastKnownOrLatestLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Log.e(TAG, "Fused Location : " + location.toString());
                            mCurrentLocation = mLocation = location;
                        }
                    }
                });
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast toast = Toast.makeText(MainActivity.this, "Taking you to your current location.",
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 500);
        toast.show();

        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast toast_error = Toast.makeText(this, "Your current location :\n "
                + location.getLatitude() + ", " + location.getLongitude() + ", \nTime: "
                + new Date(location.getTime()), Toast.LENGTH_LONG);
        toast_error.setGravity(Gravity.BOTTOM, 0, 500);
        toast_error.show();

    }

}
