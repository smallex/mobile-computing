package au.edu.unimelb.mc.trippal;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;

import au.edu.unimelb.mc.trippal.camera.FaceTrackerActivity;

public class NewTripActivity extends AppCompatActivity {
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private EditText destinationText;
    private TextInputLayout destinationLayout;
    private Place selectedPlace;
    private Button startNewTripButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_trip);
        startNewTripButton = (Button) findViewById(R.id.startNewTripButton);
        destinationText = (EditText) findViewById(R.id.destination);
        destinationLayout = (TextInputLayout) findViewById(R.id.destinationWrapper);

        destinationLayout.clearFocus();
        destinationText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .zzlf(destinationText.getText().toString())
                                    .build(NewTripActivity.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
            }
        });
    }

    public void startNewTrip(View view) {
        Intent intent = new Intent(this, FaceTrackerActivity.class);

        String destinationName = selectedPlace.getName().toString();
        LatLng latLng = selectedPlace.getLatLng();
        intent.putExtra("destinationName", destinationName);
        intent.putExtra("destinationLat", latLng.latitude);
        intent.putExtra("destinationLng", latLng.longitude);

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
}
