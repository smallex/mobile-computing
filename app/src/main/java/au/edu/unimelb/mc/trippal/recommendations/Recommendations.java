package au.edu.unimelb.mc.trippal.recommendations;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

import javax.net.ssl.HttpsURLConnection;

import au.edu.unimelb.mc.trippal.R;

public class Recommendations extends Activity implements LocationListener {
    private static final int RC_HANDLE_FINE_LOCATION_PERM = 99;
    private static final String API_KEY = "AIzaSyBf0PRbW8zP5lHcGjfwbevS6CMQYfey20Q";
    private static final String GET_ADDRESS = "https://maps.googleapis" +
            ".com/maps/api/place/nearbysearch/json?";
    private static final String LOG_ID = "RecommendationsActivity";
    private LocationManager mLocationManager;
    private ArrayList<Place> mDataSet;
    private RecommendationsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendations);

        // Initialize places and list view
        mDataSet = new ArrayList<>();
        ListView mListView = (ListView) findViewById(R.id.listview_recommendations);
        mAdapter = new RecommendationsAdapter(this, mDataSet);
        mListView.setAdapter(mAdapter);

        // Check if location permission granted
        // If yes, get recommendations for current location
        // If not, request it
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            getRecommendations();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                    .ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                // TODO Show explanation for needing permission
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                        .ACCESS_FINE_LOCATION}, RC_HANDLE_FINE_LOCATION_PERM);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[]
            grantResults) {
        switch (requestCode) {
            case RC_HANDLE_FINE_LOCATION_PERM: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager
                        .PERMISSION_GRANTED) {
                    getRecommendations();
                } else {
                    // TODO: Disable recommendations functionality
                }
                return;
            }
        }
    }

    private void getRecommendations() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            android.location.Location locationGPS = mLocationManager.getLastKnownLocation
                    (LocationManager.GPS_PROVIDER);
            android.location.Location locationNetwork = mLocationManager.getLastKnownLocation
                    (LocationManager.NETWORK_PROVIDER);
            if (locationGPS != null && locationGPS.getTime() > Calendar.getInstance()
                    .getTimeInMillis() - 2 * 60 * 1000) {
                // TODO: Recommended radius?
                // TODO: Different types
                PlaceRequest request = new PlaceRequest(API_KEY, locationGPS.getLatitude(),
                        locationGPS.getLongitude(), 500, "restaurant");
                startRecommendationsThread(request);
            } else if (locationNetwork != null && locationNetwork.getTime() > Calendar
                    .getInstance().getTimeInMillis() - 2 * 60 * 1000) {
                // TODO: Recommended radius?
                // TODO: Different types
                PlaceRequest request = new PlaceRequest(API_KEY, locationNetwork.getLatitude(),
                        locationNetwork.getLongitude(), 500, "restaurant");
                startRecommendationsThread(request);
            } else {
                // TODO: Remove one of these.. Preferably Network_Provider -.-
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
                        this);
            }
        }
    }

    public void onLocationChanged(android.location.Location location) {
        if (location != null) {
            Log.v("Location Changed", location.getLatitude() + " and " + location.getLongitude());
            mLocationManager.removeUpdates(this);
            PlaceRequest request = new PlaceRequest(API_KEY, location.getLatitude(), location
                    .getLongitude(), 500, "restaurant");
            startRecommendationsThread(request);
        }
    }

    // Required functions
    public void onProviderDisabled(String arg0) {
    }

    public void onProviderEnabled(String arg0) {
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.AVAILABLE:
                Log.i(LOG_ID, "GPS available again");
                break;
            case LocationProvider.OUT_OF_SERVICE:
                Log.i(LOG_ID, "GPS out of service");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.i(LOG_ID, "GPS temporarily unavailable");
                break;
        }
    }

    private void startRecommendationsThread(final PlaceRequest request) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Transform request object into URI
                    ObjectMapper mapper = new ObjectMapper();
                    String requestURI = mapper.convertValue(request, UriFormat.class).toString();
                    String address = GET_ADDRESS + requestURI;

                    // Create URL
                    URL githubEndpoint = new URL(address);

                    // Create connection
                    HttpsURLConnection myConnection = (HttpsURLConnection) githubEndpoint
                            .openConnection();

                    if (myConnection.getResponseCode() == 200) {
                        // Success
                        ObjectMapper mapper2 = new ObjectMapper();
                        PlaceResponse response = mapper2.readValue(new URL(address),
                                PlaceResponse.class);

                        mDataSet = new ArrayList<>();

                        for (Place place : response.getResults()) {
                            if (place.getOpeningHours().isOpenNow()) {
                                mDataSet.add(place);
                            }
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.setDataSource(mDataSet);
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                    } else {
                        // Error handling code goes here
                        // TODO: Error handling
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public class RecommendationsAdapter extends BaseAdapter {
        private final Context mContext;
        private final LayoutInflater mInflater;
        private ArrayList<Place> mDataSource;

        RecommendationsAdapter(Context context, ArrayList<Place> items) {
            mContext = context;
            mDataSource = items;
            mInflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mDataSource.size();
        }

        @Override
        public Object getItem(int position) {
            return mDataSource.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void setDataSource(ArrayList<Place> dataSource) {
            mDataSource = dataSource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = mInflater.inflate(R.layout.item_recommendations, parent, false);

            Place place = mDataSource.get(position);
            TextView titleTextView = (TextView) rowView.findViewById(R.id.recommendation_label);

            titleTextView.setText(place.getName() + " @ " + place.getVicinity());

            return rowView;
        }
    }
}

class UriFormat {

    private StringBuilder builder = new StringBuilder();

    @JsonAnySetter
    public void addToUri(String name, Object property) {
        if (builder.length() > 0) {
            builder.append("&");
        }
        builder.append(name).append("=").append(property);
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
