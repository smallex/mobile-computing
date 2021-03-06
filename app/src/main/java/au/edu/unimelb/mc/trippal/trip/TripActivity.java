/*
 * Copyright (C) The Android Open Source Project
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
package au.edu.unimelb.mc.trippal.trip;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.robinhood.spark.SparkView;
import com.tapadoo.alerter.Alerter;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import au.edu.unimelb.mc.trippal.Constants;
import au.edu.unimelb.mc.trippal.R;
import au.edu.unimelb.mc.trippal.backend.CreateTripTask;
import au.edu.unimelb.mc.trippal.backend.TripEntity;
import au.edu.unimelb.mc.trippal.backend.UserUtils;
import au.edu.unimelb.mc.trippal.recommendations.RecommendationsActivity;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

import static au.edu.unimelb.mc.trippal.Constants.extraCurrentDrowsyLvl;
import static au.edu.unimelb.mc.trippal.Constants.extraDestinationLat;
import static au.edu.unimelb.mc.trippal.Constants.extraDestinationLng;
import static au.edu.unimelb.mc.trippal.Constants.extraDestinationName;
import static au.edu.unimelb.mc.trippal.Constants.extraLastSleepHrs;
import static au.edu.unimelb.mc.trippal.Constants.extraLastSleepQual;
import static au.edu.unimelb.mc.trippal.Constants.extraSpeech;
import static au.edu.unimelb.mc.trippal.Constants.extraStopLocationLat;
import static au.edu.unimelb.mc.trippal.Constants.extraStopLocationLong;
import static au.edu.unimelb.mc.trippal.Constants.extraStopLocationName;
import static au.edu.unimelb.mc.trippal.Constants.extraTripStartTime;
import static au.edu.unimelb.mc.trippal.Constants.mappingBathroom;
import static au.edu.unimelb.mc.trippal.Constants.mappingCoffee;
import static au.edu.unimelb.mc.trippal.Constants.mappingFood;
import static au.edu.unimelb.mc.trippal.Constants.mappingSleep;
import static au.edu.unimelb.mc.trippal.Constants.mappingStretchLegs;
import static au.edu.unimelb.mc.trippal.Constants.mappingSwitchDriver;

/**
 * Activity that is displayed during a trip, showing a map and current trip status.
 */
public final class TripActivity extends AppCompatActivity implements OnMapReadyCallback,
        RecognitionListener {
    private static final String LOG_ID = "TripActivity";
    private static final int RC_HANDLE_GMS = 9001;
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    private static final String KWS_SEARCH = "wakeup";
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 134;
    private static final String UTTERANCE_ID_BREAK = "111";

    private CameraSource mCameraSource = null;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private Marker startLocationMarker;
    private Marker destinationLocationMarker;
    private TextView blinkText;
    private TextView tripDurationText;
    private ProgressBar fatigueLevel;

    private int blinkCount = 0;
    private List<Long> blinkTimes;

    private LatLng destinationLatLng;
    private String destinationName;
    private LatLng startingLatLng;
    private LatLng currentLocation;
    private long tripStartingTime;
    private long lastEyesOpenTime = -1;
    private boolean closedEyesAlertOpen = false;
    private SpeechRecognizer recognizer;
    private TextToSpeech tts;
    private List<LatLng> userLocationList;
    private List<LatLng> stopsLocationList;
    private double distanceTraveledMeters = 0;
    private TextView distanceText;
    private TextView stopsCountText;
    private FloatingActionButton btnBreak;
    private FloatingActionButton playButton;
    private boolean isDriving = true;

    private FatigueModel fatigue;
    private int stopsCount;
    private long currentStopStart;
    private TextView riskText;
    private Polyline routeLine;
    private Polyline userRouteLine;
    private Marker selectedStopMarker;
    private boolean tripFinished = false;
    private SparkView blinkLine;
    private BlinkAdapter blinkAdapter;

    //==============================================================================================
    // Activity Methods
    //==============================================================================================

    /**
     * Initializes the UI and initiates the creation of a face detector.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);

        blinkText = (TextView) findViewById(R.id.blinkText);
        tripDurationText = (TextView) findViewById(R.id.tripDuration);
        fatigueLevel = (ProgressBar) findViewById(R.id.energyLevel);
        distanceText = (TextView) findViewById(R.id.distanceText);
        riskText = (TextView) findViewById(R.id.riskText);
        btnBreak = (FloatingActionButton) findViewById(R.id.btnBreak);
        btnBreak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRecommendations(false);
            }
        });
        blinkLine = (SparkView) findViewById(R.id.sparkview);
        blinkLine.setLineColor(ContextCompat.getColor(this, R.color.accent));
        playButton = (FloatingActionButton) findViewById(R.id.playButton);
        playButton.setOnClickListener(new PlayButtonListener());
        blinkTimes = new ArrayList<>();
        blinkAdapter = new BlinkAdapter();
        blinkLine.setAdapter(blinkAdapter);

        // Show toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_facetracker);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        this.userLocationList = new ArrayList<>();
        this.stopsLocationList = new ArrayList<>();

        ActionBar ab = getSupportActionBar();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.destinationName = extras.getString(extraDestinationName);
            if (ab != null) {
                ab.setTitle(String.format(getString(R.string.tripTo), this.destinationName));
                ab.setDisplayHomeAsUpEnabled(true);
            }

            double lat = extras.getDouble(extraDestinationLat);
            double lng = extras.getDouble(extraDestinationLng);
            this.destinationLatLng = new LatLng(lat, lng);
            this.tripStartingTime = extras.getLong(extraTripStartTime);

            final Handler timerHandler = new Handler();
            final Runnable timeUpdater = new TimeUpdater(timerHandler);
            timerHandler.post(timeUpdater);

            initFatigue(extras);
        }
        requestCurrentLocation();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }

        initializeTextToSpeech();

        runRecognizerSetup();
    }

    private void initializeTextToSpeech() {
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.US);
                    tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String s) {

                        }

                        @Override
                        public void onDone(String s) {
                            if (s.equals(UTTERANCE_ID_BREAK)) {
                                openRecommendations(true);
                            }
                        }

                        @Override
                        public void onError(String s) {

                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initFatigue(Bundle extras) {
        int currentDrowsinessLevel = extras.getInt(extraCurrentDrowsyLvl);
        int lastSleepQuality = extras.getInt(extraLastSleepQual);
        int lastSleepHours = extras.getInt(extraLastSleepHrs);
        this.fatigue = new FatigueModel(currentDrowsinessLevel, lastSleepHours, 0,
                lastSleepQuality);
        this.fatigueLevel.setProgress((int) this.fatigue.getFatigueLevel());
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                .getRecognizer();
        recognizer.addListener(this);

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, getString(R.string.takeABreak));
    }

    private void runRecognizerSetup() {
        // Check if user has given permission to record audio
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest
                .permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                    .RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        } else {

            new AsyncTask<Void, Void, Exception>() {
                @Override
                protected Exception doInBackground(Void... params) {
                    try {
                        Assets assets = new Assets(TripActivity.this);
                        File assetDir = assets.syncAssets();
                        setupRecognizer(assetDir);
                    } catch (IOException e) {
                        Log.d(LOG_ID, "errorvoice: " + e.toString());
                        return e;
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Exception result) {
                    if (result != null) {
                        Log.d(LOG_ID, "errorvoicerecognition: " + result.getMessage());
                    } else {
                        recognizer.stop();
                        recognizer.startListening(KWS_SEARCH);
                    }
                }
            }.execute();
        }
    }

    private void requestCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                    .ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            return;
        }
        locationManager = (LocationManager) this.getSystemService(Context
                .LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, new
                MyLocationListenerGPS());
        Log.d(LOG_ID, "Requesting current location");
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(LOG_ID, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(LOG_ID, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                    PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mCameraSource.start();
        } catch (IOException e) {
            mCameraSource.release();
        }
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
        if (recognizer != null) {
            recognizer.startListening(KWS_SEARCH);
        }
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mCameraSource != null) {
            mCameraSource.stop();
        }
        if (recognizer != null) {
            recognizer.stop();
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        tts.stop();
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        Log.d(LOG_ID, "requestCode == " + requestCode);
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            Log.d(LOG_ID, "requestCode == 1");

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission
                    .ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                    .checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                Log.d(LOG_ID, "Location permission not granted");
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, new
                    MyLocationListenerGPS());
            Log.d(LOG_ID, "requestLocationUpdates");
            return;
        }

        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(LOG_ID, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_ID, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                runRecognizerSetup();
            } else {
                finish();
            }
        }

        Log.e(LOG_ID, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.tripPal)
                .setMessage(R.string
                        .no_camera_permission)
                .setPositiveButton(R
                        .string.ok, listener)
                .show();
    }

    public void openRecommendations(boolean speech) {
        Intent intent = new Intent(TripActivity.this, RecommendationsActivity.class);
        intent.putExtra(extraSpeech, speech);
        startActivity(intent);
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                    PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mCameraSource.start();
        } catch (IOException e) {
            mCameraSource.release();
            mCameraSource = null;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        this.destinationLocationMarker = mMap.addMarker(new MarkerOptions().position(this
                .destinationLatLng).title(this
                .destinationName).icon(BitmapDescriptorFactory.fromResource(R.drawable
                .blue_marker)));
        this.destinationLocationMarker.showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(this.destinationLatLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10f));
        mMap.setPadding(20, 20, 20, 20);

        if (this.startingLatLng != null) {
            createStartLocationMarkers();
            showAllMarkers();
        }
    }

    private void createStartLocationMarkers() {
        this.startLocationMarker = mMap.addMarker(new MarkerOptions().position
                (TripActivity.this
                        .startingLatLng).title(getString(R.string.start)).icon(BitmapDescriptorFactory.fromResource(R
                .drawable.red_marker)));
    }

    private void showAllMarkers() {
        if (this.startingLatLng == null || this.destinationLatLng == null) {
            return;
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        builder.include(this.startingLatLng);
        builder.include(this.destinationLatLng);
        LatLngBounds bounds = builder.build();
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120));
    }

    private void showCurrentStop(LatLng stopLatLng) {
        if (this.currentLocation == null) {
            return;
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        builder.include(this.currentLocation);
        builder.include(stopLatLng);
        LatLngBounds bounds = builder.build();
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 500));
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {
        recognizer.startListening(KWS_SEARCH);
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;
        String text = hypothesis.getHypstr().toLowerCase();
        if (text.equals(getString(R.string.takeABreak))) {
            recognizer.stop();
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            // if keyword was spoken go to next activity and ask user what he wants to do
            String text = hypothesis.getHypstr().toLowerCase();
            tts.speak(getString(R.string.whatPlanningToDo), TextToSpeech.QUEUE_ADD,
                    null, "1");
            tts.playSilentUtterance(300, TextToSpeech.QUEUE_ADD, null);
            tts.speak(getString(R.string.chooseActivity), TextToSpeech.QUEUE_ADD,
                    null, "1");
            tts.playSilentUtterance(300, TextToSpeech.QUEUE_ADD, null);
            tts.setSpeechRate((float) 0.8);
            tts.speak(mappingCoffee, TextToSpeech.QUEUE_ADD,
                    null, "1");
            tts.speak(mappingFood, TextToSpeech.QUEUE_ADD,
                    null, "1");
            tts.speak(mappingBathroom, TextToSpeech.QUEUE_ADD,
                    null, "1");
            tts.speak(mappingSleep, TextToSpeech.QUEUE_ADD,
                    null, "1");
            tts.setSpeechRate((float) 1);

            tts.speak(mappingStretchLegs, TextToSpeech.QUEUE_ADD,
                    null, "1");
            tts.speak(mappingSwitchDriver, TextToSpeech.QUEUE_ADD,
                    null, UTTERANCE_ID_BREAK);
        }
    }

    @Override
    public void onError(Exception e) {
        Log.d("errorvoice", e.toString());
    }

    @Override
    public void onTimeout() {
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode +
                "&key=AIzaSyDQjDkBJd69ZfDHmXbWJd_cX4q5zJYWJl8";

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();
        } catch (Exception e) {
            Log.d(LOG_ID, "Exception: " + e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    private void drawRoute(LatLng from, LatLng to, int i) {
        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(from, to);

        DownloadTask downloadTask = new DownloadTask(i);

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getExtras() != null) {
            String stopLocationName = intent.getExtras().getString(extraStopLocationName);
            double stopLocationLong = intent.getExtras().getDouble(extraStopLocationLong);
            double stopLocationLat = intent.getExtras().getDouble(extraStopLocationLat);

            LatLng stopLocation = new LatLng(stopLocationLat, stopLocationLong);
            if (currentLocation != null) {
                drawRoute(currentLocation, stopLocation, 1);
            }
            selectedStopMarker = mMap.addMarker(new MarkerOptions().title(stopLocationName).position
                    (stopLocation).icon(BitmapDescriptorFactory
                    .fromResource(R.drawable.coffe_marker)));
            selectedStopMarker.showInfoWindow();
            showCurrentStop(selectedStopMarker.getPosition());
        }
    }

    private void finishTrip() {
        tripFinished = true;
        if (routeLine != null) {
            routeLine.setVisible(false);
        }
        if (userRouteLine != null) {
            userRouteLine.setVisible(true);
        }
        if (selectedStopMarker != null) {
            selectedStopMarker.remove();
        }
        drawStopMarkers();
        showAllMarkers();
        saveTrip();
        playButton.setImageResource(R.drawable.ic_check_white_48dp);
        playButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat
                .getColor(this, R.color.primary)));
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TripActivity.this, TripListActivity.class);
                TripActivity.this.startActivity(intent);
            }
        });
    }

    private void saveTrip() {
        TripEntity trip = new TripEntity(UserUtils.getUserId(this));
        trip.setDestinationName(destinationName);
        String duration = tripDurationText.getText().toString().replace("hours", "h").replace
                ("minutes", "m");
        trip.setDuration(duration);
        String distance = distanceText.getText().toString();
        trip.setDistance(distance);
        trip.setTripDate(new Date());

        new CreateTripTask(trip).execute();
    }

    private void drawStopMarkers() {
        for (LatLng stopLatLng : stopsLocationList) {
            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R
                    .drawable.coffe_marker)).position(stopLatLng));
        }
    }

    private class PlayButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            String text;
            int icon;
            if (isDriving) {
                text = getString(R.string.startingBreakNow);
                icon = R.drawable.ic_free_breakfast_white_48dp;
                playButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                stopsCount += 1;
                //stopsCountText.setText(stopsCount + " stops");
                currentStopStart = new Date().getTime();
                if (currentLocation != null) {
                    stopsLocationList.add(currentLocation);
                }
            } else {
                text = getString(R.string.continuingTripNow);
                icon = R.drawable.ic_directions_car_white_24dp;
                playButton.setImageResource(R.drawable.ic_pause_white_24dp);

                long currentStopDuration = new Date().getTime() - currentStopStart;
                fatigue.setCurrentStopDuration(currentStopDuration);
            }
            Alerter.create(TripActivity.this)
                    .setBackgroundColorRes(R.color.accent)
                    .setIcon(icon)
                    .setText(text)
                    .show();
            isDriving = !isDriving;
        }
    }

    private class MyLocationListenerGPS implements LocationListener {

        private boolean insideDestinationRadius = false;

        @Override
        public void onLocationChanged(Location location) {
            if (startingLatLng == null) {
                startingLatLng = new LatLng(location.getLatitude(),
                        location.getLongitude());
                userLocationList.add(startingLatLng);
                if (mMap != null) {
                    createStartLocationMarkers();
                    showAllMarkers();
                    drawRoute(startingLatLng, destinationLatLng, 0);
                }
                Log.d(LOG_ID, "Location: " + startingLatLng);
            } else {
                if (currentLocation != null) {
                    updateCurrentLocation(location);
                    checkIfAtDestination(location);
                } else {
                    currentLocation = new LatLng(location.getLatitude(),
                            location.getLongitude());
                }

                Log.d(LOG_ID, "Location: " + startingLatLng);
            }
        }

        private void checkIfAtDestination(Location location) {
            final float THRESHOLD_DISTANCE = 50.0f;
            float[] distance = new float[]{0};
            Location.distanceBetween(location.getLatitude(),
                    location.getLongitude(), destinationLatLng.latitude, destinationLatLng
                            .longitude, distance);
            if (distance[0] < THRESHOLD_DISTANCE && !insideDestinationRadius && !tripFinished) {
                insideDestinationRadius = true;
                AlertDialog.Builder builder = new AlertDialog.Builder(TripActivity
                        .this);
                builder.setTitle(R.string.youreThere)
                        .setMessage(R.string.reachedDestination)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finishTrip();
                            }
                        })
                        .setNegativeButton(R.string.no, null)
                        .show();
            } else if (distance[0] > THRESHOLD_DISTANCE) {
                insideDestinationRadius = false;
            }
        }

        private void updateCurrentLocation(Location location) {
            if (tripFinished) {
                return;
            }
            if (userRouteLine == null) {
                initUserPolyline();
            }
            LatLng locationLatLng = new LatLng(location.getLatitude(), location
                    .getLongitude());
            if (currentLocation != null && distance(currentLocation, locationLatLng) < 10) {
                Log.d(LOG_ID, "Distance: " + distance(currentLocation, locationLatLng));
                return;
            }

            userLocationList.add(locationLatLng);
            userRouteLine.setPoints(userLocationList);

            CameraPosition currentPos = mMap.getCameraPosition();
            float newBearing = currentPos.bearing;
            float calculatedBearing = calculateBearing(currentLocation, locationLatLng);
            if (distance(calculatedBearing, currentPos.bearing) > 45) {
                newBearing = calculatedBearing;
            }
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(new
                    CameraPosition(locationLatLng, currentPos.zoom, currentPos.tilt,
                    newBearing));
            mMap.animateCamera(update, 2000, null);
            distanceTraveledMeters += distance(currentLocation, new LatLng(location.getLatitude()
                    , location.getLongitude()));
            distanceText.setText(String.format("%.3f", distanceTraveledMeters / 1000.0).replace
                    (",", ".") +
                    " km");
            currentLocation = new LatLng(location.getLatitude(),
                    location.getLongitude());
        }

        private float distance(LatLng l1, LatLng l2) {
            float[] distance = new float[]{0};
            Location.distanceBetween(l1.latitude,
                    l1.longitude, l2.latitude, l2.longitude, distance);
            return distance[0];
        }

        private float distance(float alpha, float beta) {
            float phi = Math.abs(beta - alpha) % 360;
            // This is either the distance or 360 - distance
            float distance = phi > 180 ? 360 - phi : phi;
            return distance;
        }

        private void initUserPolyline() {
            userRouteLine = mMap.addPolyline(new PolylineOptions().width(12).color(Color.argb(125,
                    20, 20, 230)));
            userRouteLine.setVisible(false);
            ;
        }

        private float calculateBearing(LatLng currentLocation, LatLng locationLatLng) {
            double lon1 = degToRad(currentLocation.longitude);
            double lon2 = degToRad(locationLatLng.longitude);
            double lat1 = degToRad(currentLocation.latitude);
            double lat2 = degToRad(locationLatLng.latitude);

            double a = Math.sin(lon2 - lon1) * Math.cos(lat2);
            double b = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math
                    .cos(lon2 - lon1);
            double c = radToDeg(Math.atan2(a, b));
            return (float) c;
        }

        double degToRad(double deg) {
            return deg * Math.PI / 180.0;
        }

        double radToDeg(double rad) {
            rad = rad * (180.0 / Math.PI);
            if (rad < 0) rad = 360.0 + rad;
            return rad;
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker();
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private long EYES_CLOSED_THRESHOLD = TimeUnit.SECONDS.toMillis(2);
        private boolean lastOpen = true;

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            blinkingUpdate(face);
        }

        private void blinkingUpdate(Face face) {
            final float leftProb = face.getIsLeftEyeOpenProbability();
            final float rightProb = face.getIsRightEyeOpenProbability();
            final boolean closed = leftProb > 0 && rightProb > 0 && leftProb < 0.5 && rightProb <
                    0.5;
            if (closed && lastOpen) {
                blinkCount++;
                blinkTimes.add(System.currentTimeMillis());
                TripActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        blinkAdapter.update(blinkTimes);
                    }
                });
            }
            if (!closed) {
                TripActivity.this.lastEyesOpenTime = new Date().getTime();
            }
            if (TripActivity.this.lastEyesOpenTime != -1 && new Date().getTime() -
                    TripActivity.this.lastEyesOpenTime > EYES_CLOSED_THRESHOLD &&
                    !closedEyesAlertOpen) {
                closedEyesAlertOpen = true;
                TripActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogInterface.OnClickListener listener = new DialogInterface
                                .OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                closedEyesAlertOpen = false;
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(TripActivity
                                .this);
                        builder.setTitle(R.string.attention)
                                .setMessage(R.string.eyesClosed)
                                .setPositiveButton(R.string.ok, listener)
                                .setIcon(R.drawable.warning)
                                .show();
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(1000);
                        try {
                            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager
                                    .TYPE_ALARM);
                            final Ringtone r = RingtoneManager.getRingtone
                                    (getApplicationContext(),
                                            notification);
                            r.play();
                            Thread th = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(5000);
                                        if (r.isPlaying()) {
                                            r.stop();
                                            tts.speak(getString(R.string.attention), TextToSpeech.QUEUE_ADD,
                                                    null, "1");
                                            tts.playSilentUtterance(300, TextToSpeech.QUEUE_ADD,
                                                    null);
                                            tts.speak(getString(R.string.eyesClosed),
                                                    TextToSpeech.QUEUE_ADD,
                                                    null, "1"); // for stopping the ringtone
                                        }
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            th.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            lastOpen = !closed;
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation
         * from
         * the overlay.
         */
        @Override
        public void onDone() {
        }
    }

    private class TimeUpdater implements Runnable {
        private final Handler timerHandler;

        public TimeUpdater(Handler timerHandler) {
            this.timerHandler = timerHandler;
        }

        @Override
        public void run() {
            long elapsed = new Date().getTime() - TripActivity.this
                    .tripStartingTime;
            long hours = TimeUnit.MILLISECONDS.toHours(elapsed);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed) % 60;
            if (tripDurationText != null) {
                String text = minutes + " minutes";
                if (hours > 0) {
                    text = hours + " hours " + text;
                }
                tripDurationText.setText(text);
            }
            fatigue.setTimeElapsed(elapsed);
            if (fatigue.isHighRisk()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TripActivity
                        .this);
                tts.speak(getString(R.string.drivingForWhile),
                        TextToSpeech.QUEUE_ADD,
                        null, "1");
                builder.setTitle(R.string.takeBreak)
                        .setMessage(R.string.drivingForWhile)
                        .setPositiveButton(R
                                .string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                openRecommendations(false);
                            }
                        })
                        .setIcon(R.drawable.warning)
                        .show();
            }
            fatigueLevel.setProgress((int) fatigue.getFatigueLevel());
            switch (fatigue.getCurrentRisk()) {
                case LOW:
                    riskText.setText(R.string.lowRisk);
                    break;
                case MEDIUM:
                    riskText.setText(R.string.mediumRisk);
                    break;
                case HIGH:
                    riskText.setText(R.string.highRisk);
                    break;
            }
            blinkAdapter.update(blinkTimes);
            timerHandler.postDelayed(this, TimeUnit.SECONDS.toMillis(5));
        }
    }

    /**
     * Handles parsing route data returned from Directions API and drawing it on the map.
     */
    private class ParserTask extends AsyncTask<String, Integer, DirectionsJSONParser> {

        // Create a stroke pattern of a gap followed by a dash.
        private static final int PATTERN_DASH_LENGTH_PX = 20;
        private static final int PATTERN_GAP_LENGTH_PX = 10;
        private final int type;
        private final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);
        private final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
        private final List<PatternItem> PATTERN_POLYGON_ALPHA = Arrays.asList(GAP, DASH);

        ParserTask(int type) {
            this.type = type;
        }

        // Parsing the data in non-ui thread
        @Override
        protected DirectionsJSONParser doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("JSON", jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser(jObject);
                return parser;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(DirectionsJSONParser parser) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            if (parser == null) {
                return;
            }
            List<List<HashMap<String, String>>> result = parser.parse();
            if (result.isEmpty()) {
                return;
            }

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                if (type == 0) {
                    lineOptions.color(Color.argb(125, 230, 20, 20));
                    lineOptions.width(12);
                } else if (type == 1) {
                    lineOptions.color(Color.BLUE);
                    lineOptions.width(10);
                    lineOptions.pattern(PATTERN_POLYGON_ALPHA);
                }
                lineOptions.geodesic(true);
            }
            // Drawing userRouteLine in the Google Map for the i-th route
            routeLine = mMap.addPolyline(lineOptions);

            final LatLng halfwayLocation = points.get(points.size() / 2);
            int durationSeconds = parser.getDurationSeconds();
            int durationHours = durationSeconds / (60 * 60);
            int durationMinutes = (durationSeconds / 60) % 60;

            String durationText = durationMinutes + " minute";
            if (durationMinutes != 1) {
                durationText += "s";
            }
            if (durationHours > 0) {
                durationText = durationHours + " hours " + durationText;
            }

            BitmapDescriptor transparent = BitmapDescriptorFactory.fromResource(R.drawable
                    .transparent);
            MarkerOptions options = new MarkerOptions()
                    .position(halfwayLocation)
                    .title(parser.getDistanceKM() + " km")
                    .snippet(durationText)
                    .icon(transparent)
                    .anchor((float) 0.5, (float) 0.5); //puts the info window on the
            // userRouteLine

            Marker marker = mMap.addMarker(options);

            if (type == 0) {
                //open the marker's info window
                marker.showInfoWindow();
            }
        }
    }

    /**
     * Handles downloading route data.
     */
    private class DownloadTask extends AsyncTask<String, Void, String> {

        private final int type;

        DownloadTask(int i) {
            this.type = i;
        }

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                Log.d(LOG_ID, "URL: " + url[0]);
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d(LOG_ID, "Background Task: " + e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask(type);

            parserTask.execute(result);
        }
    }
}
