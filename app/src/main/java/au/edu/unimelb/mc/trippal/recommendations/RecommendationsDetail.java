package au.edu.unimelb.mc.trippal.recommendations;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.CountDownLatch;

import au.edu.unimelb.mc.trippal.R;
import au.edu.unimelb.mc.trippal.camera.FaceTrackerActivity;

import static android.graphics.Bitmap.createScaledBitmap;

public class RecommendationsDetail extends AppCompatActivity implements LocationListener {
    private static final int RC_HANDLE_FINE_LOCATION_PERM = 99;
    private static final String API_KEY = "AIzaSyBf0PRbW8zP5lHcGjfwbevS6CMQYfey20Q";
    private static final String GET_PLACE_ADDRESS = "https://maps.googleapis" +
            ".com/maps/api/place/nearbysearch/json?";
    private static final String GET_PHOTO_ADDRESS = "https://maps.googleapis" +
            ".com/maps/api/place/photo?";
    private static final String LOG_ID = "RecDetActivity";
    private static final int MAX_WIDTH = 500;
    private static final int MAX_HEIGHT = 500;
    private static final int MIN_RADIUS = 500;
    private static final int MAX_RADIUS = 10000;

    private LocationManager mLocationManager;
    private ArrayList<Place> mDataSet;
    private final AdapterView.OnItemClickListener onItemClickListener = new AdapterView
            .OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
            // Get corresponding RecommendationMapping item
            Place place = mDataSet.get(position);

            Log.d(LOG_ID, place.getName() + place.getDistance());

            Intent redirectIntent = new Intent(RecommendationsDetail.this, FaceTrackerActivity
                    .class);
            redirectIntent.putExtra("stopLocationName", place.getName());
            redirectIntent.putExtra("stopLocationLat", place.getGeometry().getLocation()
                    .getLatitude());
            redirectIntent.putExtra("stopLocationLong", place.getGeometry().getLocation()
                    .getLongitude());
            redirectIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent
                    .FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(redirectIntent);
            finish();
        }
    };
    private RecommendationsDetailAdapter mAdapter;
    private RecommendationMapping mRecMapping;
    private GridView mGridView;
    private ProgressBar mProgressBar;
    private RecommendationsDetail mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendations_detail);

        mActivity = this;

        // Get intent details
        mRecMapping = (RecommendationMapping) getIntent().getSerializableExtra("MAPPING");

        // Show toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_recommendations_detail);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        // Enable up navigation (back arrow)
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(mRecMapping.getActivity().toUpperCase());
        }

        // Initialize grid view
        mDataSet = new ArrayList<>();
        mGridView = (GridView) findViewById(R.id.gridview_recommendations_detail);
        mAdapter = new RecommendationsDetailAdapter(this, mDataSet);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(onItemClickListener);

        // Show progress bar while data loads
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar_recommendations_detail);
        mProgressBar.setVisibility(View.VISIBLE);

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
                // TODO Show explanation for needing permission
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                        .ACCESS_FINE_LOCATION}, RC_HANDLE_FINE_LOCATION_PERM);
            }
        }
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
                startRecs(locationGPS, MIN_RADIUS);
            } else if (locationNetwork != null && locationNetwork.getTime() > Calendar
                    .getInstance().getTimeInMillis() - 2 * 60 * 1000) {
                startRecs(locationNetwork, MIN_RADIUS);
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
            startRecs(location, MIN_RADIUS);
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

    private void startRecs(final android.location.Location location, final int radius) {
        mDataSet = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(mRecMapping.getTypes().length);

        for (final PlaceType type : mRecMapping.getTypes()) {
            PlaceRequest request = new PlaceRequest(API_KEY, location.getLatitude(), location
                    .getLongitude(), radius, type.getName());
            startRecommendationsThread(request, latch, location);
        }

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (mDataSet.isEmpty()) {
                    if (radius < MAX_RADIUS) {
                        int r = radius;
                        startRecs(location, r += MIN_RADIUS);
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mActivity, "There are no points of interest in a "
                                        + MAX_RADIUS + "m radius.", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        });
                    }
                } else {
                    // Sort entries by closest
                    Collections.sort(mDataSet, new Comparator<Place>() {
                        @Override
                        public int compare(Place o1, Place o2) {
                            return Double.compare(o1.getDistance(), o2.getDistance());
                        }
                    });

                    // Update grid view with new data
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBar.setVisibility(View.GONE);
                            mAdapter.setDataSource(mDataSet);
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }

    private void startRecommendationsThread(final PlaceRequest request, final CountDownLatch
            latch, final android.location.Location location) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Transform request object into URI
                    ObjectMapper mapper = new ObjectMapper();
                    String requestURI = mapper.convertValue(request, UriFormat.class).toString();
                    String address = GET_PLACE_ADDRESS + requestURI;

                    ObjectMapper mapper2 = new ObjectMapper();
                    PlaceResponse response = mapper2.readValue(new URL(address), PlaceResponse
                            .class);

                    if (response.getErrorMessage() == null) {
                        for (Place place : response.getResults()) {
                            if (place.getOpeningHours() == null || place.getOpeningHours()
                                    .isOpenNow()) {
                                // Avoid duplicate entries
                                if (!mDataSet.contains(place)) {
                                    calculateHaversineDistance(location, place);
                                    mDataSet.add(place);
                                }
                            }
                        }
                    } else {
                        Log.e(LOG_ID, response.getErrorMessage());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            }
        });
    }

    private void calculateHaversineDistance(android.location.Location location, Place place) {
        double lat1 = location.getLatitude();
        double lon1 = location.getLongitude();
        double lat2 = place.getGeometry().getLocation().getLatitude();
        double lon2 = place.getGeometry().getLocation().getLongitude();

        double R = 6378.137; // Radius of earth in KM
        double dLat = lat2 * Math.PI / 180 - lat1 * Math.PI / 180;
        double dLon = lon2 * Math.PI / 180 - lon1 * Math.PI / 180;
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(lat1 * Math.PI / 180) *
                Math.cos(lat2 * Math.PI / 180) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c;
        place.setDistance(Double.valueOf(d * 1000).intValue()); // meters
    }

    public class RecommendationsDetailAdapter extends BaseAdapter {
        private final Context mContext;
        private final LayoutInflater mInflater;
        private ArrayList<Place> mDataSource;

        RecommendationsDetailAdapter(Context context, ArrayList<Place> items) {
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
            View rowView = mInflater.inflate(R.layout.item_recommendations_detail, parent, false);

            Place place = mDataSource.get(position);

            TextView titleTextView = (TextView) rowView.findViewById(R.id
                    .recommendation_detail_label);
            titleTextView.setText(place.getName() + " (" + place.getDistance() + " m)");

            ImageView imageView = (ImageView) rowView.findViewById(R.id.recommendation_detail_icon);
            imageView.setImageResource(mRecMapping.getIcon());

            if (place.getPhotos() != null) {
                if (place.getImage() == null) {
                    Photo photo = place.getPhotos().get(0);
                    PhotoRequest photoRequest = new PhotoRequest(API_KEY, photo.getPhotoReference
                            (), MAX_HEIGHT);
                    ObjectMapper mapper = new ObjectMapper();
                    String requestURI = mapper.convertValue(photoRequest, UriFormat.class)
                            .toString();
                    String address = GET_PHOTO_ADDRESS + requestURI;
                    new DownloadImageTask(place).execute(address);
                } else {
                    imageView.setImageBitmap(place.getImage());
                }
            }

            return rowView;
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        Place mPlace;

        DownloadImageTask(Place place) {
            this.mPlace = place;
        }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap mIcon11 = null;
            InputStream in = null;
            try {
                in = new URL(url).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e(LOG_ID, e.getMessage());
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                int height = result.getHeight();
                int width = result.getWidth();

                Bitmap resizedBitmap;
                if (height > width) {
                    int y = (height - width) / 2;
                    resizedBitmap = Bitmap.createBitmap(result, 0, y, width, width);
                    resizedBitmap = createScaledBitmap(resizedBitmap, MAX_WIDTH, MAX_HEIGHT, false);
                } else {
                    int x = (width - height) / 2;
                    resizedBitmap = Bitmap.createBitmap(result, x, 0, height, height);
                    resizedBitmap = createScaledBitmap(resizedBitmap, MAX_WIDTH, MAX_HEIGHT, false);
                }

                mPlace.setImage(resizedBitmap);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Set image view of place's image
                        View v = mGridView.getChildAt(mDataSet.indexOf(mPlace) - mGridView
                                .getFirstVisiblePosition());

                        if (v == null)
                            return;

                        ImageView imageView = (ImageView) v.findViewById(R.id
                                .recommendation_detail_icon);
                        imageView.setImageBitmap(mPlace.getImage());
                    }
                });
            }
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