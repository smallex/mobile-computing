package au.edu.unimelb.mc.trippal.recommendations;

import android.Manifest;
import android.content.Context;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.CountDownLatch;

import au.edu.unimelb.mc.trippal.R;

public class RecommendationsDetail extends AppCompatActivity implements LocationListener {
    private static final int RC_HANDLE_FINE_LOCATION_PERM = 99;
    private static final String API_KEY = "AIzaSyBf0PRbW8zP5lHcGjfwbevS6CMQYfey20Q";
    private static final String GET_PLACE_ADDRESS = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
    private static final String GET_PHOTO_ADDRESS = "https://maps.googleapis.com/maps/api/place/photo?";
    private static final String LOG_ID = "RecDetActivity";
    private static final int MAX_WIDTH = 500;

    private LocationManager mLocationManager;
    private ArrayList<Place> mDataSet;
    private RecommendationsDetailAdapter mAdapter;
    private RecommendationMapping mRecMapping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendations_detail);

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
        GridView gridView = (GridView) findViewById(R.id.gridview_recommendations_detail);
        mAdapter = new RecommendationsDetailAdapter(this, mDataSet);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(onItemClickListener);

        // TODO Show ActivityIndicator until data loaded

        // Check if location permission granted
        // If yes, get recommendations for current location
        // If not, request it
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getRecommendations();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // TODO Show explanation for needing permission
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, RC_HANDLE_FINE_LOCATION_PERM);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RC_HANDLE_FINE_LOCATION_PERM: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getRecommendations();
                } else {
                    // TODO: Disable recommendations functionality
                }
                return;
            }
        }
    }

    private void getRecommendations() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            android.location.Location locationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            android.location.Location locationNetwork = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (locationGPS != null && locationGPS.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000) {
                startRecs(locationGPS);
            } else if (locationNetwork != null && locationNetwork.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000) {
                startRecs(locationNetwork);
            } else {
                // TODO: Remove one of these.. Preferably Network_Provider -.-
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            }
        }
    }

    public void onLocationChanged(android.location.Location location) {
        if (location != null) {
            Log.v("Location Changed", location.getLatitude() + " and " + location.getLongitude());
            mLocationManager.removeUpdates(this);
            startRecs(location);
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

    private void startRecs(android.location.Location location) {
        // TODO: Different types

        mDataSet = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(mRecMapping.getTypes().length);

        for (final PlaceType type : mRecMapping.getTypes()) {
            // TODO: Recommended radius?
            PlaceRequest request = new PlaceRequest(API_KEY, location.getLatitude(), location.getLongitude(), 500, type.getName());
            startRecommendationsThread(request, latch);
        }

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.setDataSource(mDataSet);
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void startRecommendationsThread(final PlaceRequest request, final CountDownLatch latch) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Transform request object into URI
                    ObjectMapper mapper = new ObjectMapper();
                    String requestURI = mapper.convertValue(request, UriFormat.class).toString();
                    String address = GET_PLACE_ADDRESS + requestURI;

                    ObjectMapper mapper2 = new ObjectMapper();
                    PlaceResponse response = mapper2.readValue(new URL(address), PlaceResponse.class);

                    // TODO: Order by nearest vicinity
                    for (Place place : response.getResults()) {
                        if (place.getOpeningHours() == null || place.getOpeningHours().isOpenNow()) {
                            mDataSet.add(place);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            }
        });
    }

    private final AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
            // Get corresponding RecommendationMapping item
            Place place = mDataSet.get(position);

            // TODO Action on item click
        }
    };

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

            TextView titleTextView = (TextView) rowView.findViewById(R.id.recommendation_detail_label);
            titleTextView.setText(place.getName() + " @ " + place.getVicinity());
            // TODO Calculate & display distance from current location rather than displaying address

            ImageView imageView = (ImageView) rowView.findViewById(R.id.recommendation_detail_icon);
            imageView.setImageResource(mRecMapping.getIcon());

            if (place.getPhotos() != null) {
                if (place.getImage() == null) {
                    Photo photo = place.getPhotos().get(0);
                    PhotoRequest photoRequest = new PhotoRequest(API_KEY, photo.getPhotoReference(), MAX_WIDTH);
                    ObjectMapper mapper = new ObjectMapper();
                    String requestURI = mapper.convertValue(photoRequest, UriFormat.class).toString();
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
                // TODO When erroneous URL response, we get "A connection to https://maps.googleapis.com/ was leaked. Did you forget to close a response body?" log - FIX IT!
                in = new URL(url).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (FileNotFoundException e) {
                Log.e(LOG_ID, "We have exceeded the daily Google API quota");
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
                Bitmap resizedBitmap = Bitmap.createBitmap(result, (result.getWidth() - MAX_WIDTH) / 2, 0, MAX_WIDTH, MAX_WIDTH);

                //bmImage.setImageBitmap(result);
                //mPlace.setImage(result);
                mPlace.setImage(resizedBitmap);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //mAdapter.setDataSource(mDataSet);
                        mAdapter.notifyDataSetChanged();
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
