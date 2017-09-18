package au.edu.unimelb.mc.trippal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import au.edu.unimelb.mc.trippal.camera.FaceTrackerActivity;

public class NewTripActivity extends AppCompatActivity {
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private EditText destinationText;
    private TextInputLayout destinationLayout;
    private Place selectedPlace;
    private Button startNewTripButton;
    private EditText durationHours;
    private EditText durationMinutes;
    private SeekBar sleepQuality;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_trip);
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

    public void startNewTrip(View view) {
        Intent intent = new Intent(this, FaceTrackerActivity.class);

        String destinationName = selectedPlace.getName().toString();
        LatLng latLng = selectedPlace.getLatLng();
        intent.putExtra("destinationName", destinationName);
        intent.putExtra("destinationLat", latLng.latitude);
        intent.putExtra("destinationLng", latLng.longitude);
        intent.putExtra("tripStartingTime", new Date().getTime());

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
        }
    }

    public void openBrowser(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.fitbit" +
                ".com/oauth2/authorize?response_type=token&client_id=228LQG&redirect_uri=trippal" +
                "%3A" +
                "%2F%2Fsuccess&scope=sleep&expires_in=604800"));
        startActivity(browserIntent);
    }
}
