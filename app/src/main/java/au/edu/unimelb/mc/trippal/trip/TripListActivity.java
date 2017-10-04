package au.edu.unimelb.mc.trippal.trip;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import au.edu.unimelb.mc.trippal.R;
import au.edu.unimelb.mc.trippal.backend.QueryTask;
import au.edu.unimelb.mc.trippal.backend.TripEntity;

public class TripListActivity extends AppCompatActivity {

    private ListView tripListView;
    private Toolbar toolbar;
    private TripListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_list);

        toolbar = (Toolbar) findViewById(R.id.toolbar_trip_list);
        toolbar.setTitle("Your Trips");
        adapter = new TripListAdapter(this, R.layout
                .activity_trip_list_item, new ArrayList<TripEntity>());

        tripListView = (ListView) findViewById(R.id.trip_list_view);
        tripListView.setAdapter(adapter);

        new QueryTask(this).execute();
    }

    public void loadTrips(List<TripEntity> tripEntities) {
        adapter.clear();
        adapter.addAll(tripEntities);
    }

    public void addTrip(View view) {
        Intent intent = new Intent(this, NewTripActivity.class);
        startActivity(intent);
    }
}
