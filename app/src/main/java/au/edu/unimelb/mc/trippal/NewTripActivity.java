package au.edu.unimelb.mc.trippal;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import au.edu.unimelb.mc.trippal.camera.FaceTrackerActivity;
import au.edu.unimelb.mc.trippal.recommendations.Recommendations;

public class NewTripActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_trip);
    }

    public void startNewTrip(View view) {
        Intent intent = new Intent(this, FaceTrackerActivity.class);
        startActivity(intent);
    }
}
