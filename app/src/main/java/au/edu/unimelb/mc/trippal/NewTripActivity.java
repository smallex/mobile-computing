package au.edu.unimelb.mc.trippal;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import au.edu.unimelb.mc.trippal.camera.FaceTrackerActivity;
import info.debatty.java.stringsimilarity.Levenshtein;

public class NewTripActivity extends AppCompatActivity {
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final int REQ_CODE_SPEECH_INPUT_Location = 1000;
    private static final int REQ_CODE_SPEECH_INPUT_Feeling = 2000;
    private String UTTERANCE_ID_LOCATION = "100";
    private String UTTERANCE_ID_FEELINGS = "200";
    private EditText destinationText;
    private TextInputLayout destinationLayout;
    private Place selectedPlace;
    private Button startNewTripButton;
    private EditText durationHours;
    private EditText durationMinutes;
    private SeekBar sleepQuality;
    private ImageButton mSpeakBtn;
    private TextToSpeech tts;
    private SeekBar seekBar;
    private List<Address> address;
    private String finalDestination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_trip);

        // Show toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_newtrip);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        // Enable up navigation (back arrow)
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle("New Trip");
        }

        // Open splash screen on first start
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean isFirstStart = sharedPreferences.getBoolean("FIRST_START", true);

        // If the activity has never started before...
        if (isFirstStart) {
            // Open 'Welcome' screens
            Intent i = new Intent(this, IntroActivity.class);
            startActivity(i);

            // Make a new preferences editor
            SharedPreferences.Editor e = sharedPreferences.edit();

            // Edit preference to make it false because we don't want this to run again
            e.putBoolean("FIRST_START", false);
            e.apply();
        }

        startNewTripButton = (Button) findViewById(R.id.startNewTripButton);
        destinationText = (EditText) findViewById(R.id.destination);
        destinationLayout = (TextInputLayout) findViewById(R.id.destinationWrapper);
        durationHours = (EditText) findViewById(R.id.durationHours);
        durationMinutes = (EditText) findViewById(R.id.durationMinutes);
        sleepQuality = (SeekBar) findViewById(R.id.sleepQuality);

        destinationLayout.clearFocus();
        destinationText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPlaceAutocomplete();
            }
        });

        seekBar = (SeekBar) findViewById(R.id.drowsinessSeekBar);
        mSpeakBtn = (ImageButton) findViewById(R.id.btnMic);
        mSpeakBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startVoiceOutput("Hello, where do you wanna travel", UTTERANCE_ID_LOCATION);
            }
        });

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
                            if (s.equals(UTTERANCE_ID_LOCATION)) {
                                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, where do you wanna travel?");

                                try {
                                    startActivityForResult(intent, REQ_CODE_SPEECH_INPUT_Location);
                                } catch (ActivityNotFoundException a) {

                                }
                            }
                            if (s.equals(UTTERANCE_ID_FEELINGS)) {
                                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "How tired are you feeling?");

                                try {
                                    startActivityForResult(intent, REQ_CODE_SPEECH_INPUT_Feeling);
                                } catch (ActivityNotFoundException a) {

                                }
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

    private void startVoiceOutput(String s, String id) {
        tts.speak(s, TextToSpeech.QUEUE_FLUSH, null, id);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getExtras() != null) {
            loadSleepData(intent.getExtras());
        }
    }

    private void startPlaceAutocomplete() {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete
                            .MODE_FULLSCREEN)
                            .zzlf(destinationText.getText().toString())
                            .build(NewTripActivity.this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    private void loadSleepData(Bundle bundle) {
        final String accessToken = bundle.getString("accessToken");
        String userID = bundle.getString("userID");

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api.fitbit.com/1.2/user/" + userID +
                "/sleep/date/2017-08-17.json";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new
                Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int totalMinutesAsleep = response.getJSONObject
                                    ("summary").getInt("totalMinutesAsleep");
                            durationHours.setText(totalMinutesAsleep / 60 + "");
                            durationMinutes.setText(totalMinutesAsleep % 60 + "");
                            int sleepQualityLevel = response.getJSONArray("sleep").getJSONObject
                                    (0).getInt("efficiency");
                            sleepQuality.setProgress(sleepQualityLevel);
                        } catch (JSONException e) {
                            Log.d("TripPal", e.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("TripPal", error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + accessToken);
                return map;
            }
        };
        queue.add(request);
    }

    public List<Address> getLatLongFromPlace(String place) {
        try {
            Geocoder selected_place_geocoder = new Geocoder(this);
            List<Address> address;

            address = selected_place_geocoder.getFromLocationName(place, 5);
            return address;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void startNewTrip(View view) {
        Intent intent = new Intent(this, FaceTrackerActivity.class);
        if (selectedPlace != null) {
            String destinationName = selectedPlace.getName().toString();
            LatLng latLng = selectedPlace.getLatLng();
            intent.putExtra("destinationName", destinationName);
            intent.putExtra("destinationLat", latLng.latitude);
            intent.putExtra("destinationLng", latLng.longitude);
            intent.putExtra("tripStartingTime", new Date().getTime());
        } else {
            if (address == null) {

            } else {
                Address location = address.get(0);
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                intent.putExtra("destinationName", finalDestination);
                intent.putExtra("destinationLat", lat);
                intent.putExtra("destinationLng", lng);
                intent.putExtra("tripStartingTime", new Date().getTime());
            }
        }

        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i("APP", "Place: " + place.getName());
                selectedPlace = place;
                destinationText.setText(place.getName());
                destinationText.clearFocus();
                startNewTripButton.setEnabled(true);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i("APP", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        } else if (requestCode == REQ_CODE_SPEECH_INPUT_Location) {
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                Log.d("test", result.get(0));
                destinationText.setText(result.get(0));
                finalDestination = result.get(0);
                address = getLatLongFromPlace(result.get(0));
                startVoiceOutput("How tired are you feeling right now .. Rate on a scale between 1 and 5 .. where 1 is Not at all tired . and 5 is extremely tired", UTTERANCE_ID_FEELINGS);
            }
        } else if (requestCode == REQ_CODE_SPEECH_INPUT_Feeling) {
            if (resultCode == RESULT_OK && null != data) {

                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                Log.d("test", result.get(0));

                if (result.get(0).equals("one") || result.get(0).equals("1")) {
                    seekBar.setProgress(0);
                } else if (result.get(0).equals("two") || result.get(0).equals("2")) {
                    seekBar.setProgress(1);
                } else if (result.get(0).equals("three") || result.get(0).equals("3")) {
                    seekBar.setProgress(2);
                } else if (result.get(0).equals("four") || result.get(0).equals("4")) {
                    seekBar.setProgress(3);
                } else if (result.get(0).equals("five") || result.get(0).equals("5")) {
                    seekBar.setProgress(4);
                } else {
                    Levenshtein l = new Levenshtein();
                    Map<String, Double> results = new HashMap<>();
                    results.put("1", l.distance(result.get(0), "one"));
                    results.put("2", l.distance(result.get(0), "two"));
                    results.put("3", l.distance(result.get(0), "three"));
                    results.put("4", l.distance(result.get(0), "four"));
                    results.put("5", l.distance(result.get(0), "five"));

                    Map.Entry<String, Double> min = null;
                    for (Map.Entry<String, Double> entry : results.entrySet()) {
                        if (min == null || min.getValue() > entry.getValue()) {
                            min = entry;
                        }
                    }
                    seekBar.setProgress(Integer.valueOf(min.getKey()) - 1);

                }
                startNewTripButton.setEnabled(true);
            }
        }
    }

    public void loadSleepData(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.fitbit" +
                ".com/oauth2/authorize?response_type=token&client_id=228LQG&redirect_uri=trippal" +
                "%3A" +
                "%2F%2Fsuccess&scope=sleep&expires_in=604800"));
        startActivity(browserIntent);
    }
}
