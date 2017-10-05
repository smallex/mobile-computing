package au.edu.unimelb.mc.trippal.trip;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.tapadoo.alerter.Alerter;

import java.util.ArrayList;
import java.util.List;

import au.edu.unimelb.mc.trippal.R;
import au.edu.unimelb.mc.trippal.backend.QueryTask;
import au.edu.unimelb.mc.trippal.backend.TripEntity;

public class TripListActivity extends AppCompatActivity {

    private ListView tripListView;
    private Toolbar toolbar;
    private TextView noTripsText;
    private ProgressBar progress;
    private TripListAdapter adapter;
    private MobileServiceClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_list);

        noTripsText = (TextView) findViewById(R.id.no_trip_text);
        progress = (ProgressBar) findViewById(R.id.trip_list_progress);
        toolbar = (Toolbar) findViewById(R.id.toolbar_trip_list);
        toolbar.setTitle("Your Trips");
        adapter = new TripListAdapter(this, R.layout
                .activity_trip_list_item, new ArrayList<TripEntity>());

        tripListView = (ListView) findViewById(R.id.trip_list_view);
        tripListView.setAdapter(adapter);

        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("loginSuccess", false)) {
            Alerter.create(this).setText("Login successful!").setBackgroundColorRes(R.color
                    .accent).enableIconPulse(false).setDuration(500).show();
        }

        new QueryTask(this).execute();
    }

    public void loadTrips(List<TripEntity> tripEntities) {
        adapter.clear();
        adapter.addAll(tripEntities);
        progress.setVisibility(View.GONE);
        if (tripEntities.isEmpty()) {
            noTripsText.setVisibility(View.VISIBLE);
        }
    }

    public void addTrip(View view) {
        Intent intent = new Intent(this, NewTripActivity.class);
        startActivity(intent);
    }
}
