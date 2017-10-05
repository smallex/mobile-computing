package au.edu.unimelb.mc.trippal.recommendations;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.edu.unimelb.mc.trippal.R;
import au.edu.unimelb.mc.trippal.trip.TripActivity;
import info.debatty.java.stringsimilarity.Levenshtein;

import static android.graphics.Bitmap.createScaledBitmap;

public class RecommendationsDetailActivity extends AppCompatActivity implements LocationListener {
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

    private RecommendationsDetailAdapter mAdapter;
    private RecommendationMapping mRecMapping;
    private GridView mGridView;
    private ProgressBar mProgressBar;
    private RecommendationsDetailActivity mActivity;
    private TextToSpeech tts;
    private static final String UTTERANCE_ID_LOC = "111";
    private int REQ_CODE_SPEECH_INPUT_Location = 111;

    private void startSelection(int position) {
        Place place = mDataSet.get(position);

        Log.d(LOG_ID, place.getName() + place.getDistance());

        Intent redirectIntent = new Intent(RecommendationsDetailActivity.this, TripActivity
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

    private final AdapterView.OnItemClickListener onItemClickListener = new AdapterView
            .OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
            // Get corresponding RecommendationMapping item
            startSelection(position);
        }
    };


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

        // Add location manager
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Start recommendation-getting process
        getRecommendations();
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

    private void getRecommendations() {
        // Check if we have the location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            // Get the last known location from either GPS or network provider
            android.location.Location locationGPS = mLocationManager.getLastKnownLocation
                    (LocationManager.GPS_PROVIDER);
            android.location.Location locationNetwork = mLocationManager.getLastKnownLocation
                    (LocationManager.NETWORK_PROVIDER);

            // Check if the last known locations are somewhat recent
            int maxRecent = 2 * 60 * 1000;
            if (locationGPS != null && locationGPS.getTime() > Calendar.getInstance()
                    .getTimeInMillis() - maxRecent) {
                startRecs(locationGPS, MIN_RADIUS);
            } else if (locationNetwork != null && locationNetwork.getTime() > Calendar
                    .getInstance().getTimeInMillis() - maxRecent) {
                startRecs(locationNetwork, MIN_RADIUS);
            } else {
                // Not recent --> Request location updates
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
                        this);
            }
        } else {
            // We don't have the location permission --> Show error message
            Toast.makeText(mActivity, R.string.enableLocationServices, Toast.LENGTH_LONG).show();
        }
    }

    public void onLocationChanged(android.location.Location location) {
        if (location != null) {
            // Only ask for one location update
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
    }

    private void startRecs(final android.location.Location location, final int radius) {
        mDataSet = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(mRecMapping.getTypes().length);

        // Get Google API places for each mapping type
        for (final PlaceType type : mRecMapping.getTypes()) {
            PlaceRequest request = new PlaceRequest(API_KEY, location.getLatitude(), location
                    .getLongitude(), radius, type.getName());
            startRecommendationsThread(request, latch, location);
        }

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Wait until all threads have returned
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (mDataSet.isEmpty()) {
                    if (radius < MAX_RADIUS) {
                        // Recursively call this function until MAX_RADIUS is reached
                        int r = radius;
                        startRecs(location, r += MIN_RADIUS);
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Display error message
                                Toast.makeText(mActivity, String.format(getString(R.string.noPlacesInRadius)
                                        , MAX_RADIUS), Toast.LENGTH_LONG).show();
                                if (getIntent().getExtras().getBoolean("speech")) {
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
                                                        if (s.equals(UTTERANCE_ID_LOC)) {
                                                        }
                                                    }

                                                    @Override
                                                    public void onError(String s) {
                                                    }
                                                });
                                                tts.speak(String.format(getString(R.string.noPlacesInRadius)
                                                        , MAX_RADIUS), TextToSpeech.QUEUE_ADD,
                                                        null, "1");
                                            } else {
                                            }
                                        }
                                    });
                                }
                                finish();
                            }
                        });
                    }
                } else {
                    if (getIntent().getExtras().getBoolean("speech")) {
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
                                            if (s.equals(UTTERANCE_ID_LOC)) {
                                                Intent intent = new Intent(RecognizerIntent
                                                        .ACTION_RECOGNIZE_SPEECH);
                                                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                                                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale
                                                        .getDefault());
                                                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Choose location?");

                                                try {
                                                    startActivityForResult(intent, REQ_CODE_SPEECH_INPUT_Location);
                                                } catch (ActivityNotFoundException a) {
                                                }
                                            }
                                        }

                                        @Override
                                        public void onError(String s) {
                                        }
                                    });
                                    tts.speak(getString(R.string.TTSLocationsAvailable), TextToSpeech.QUEUE_ADD,
                                            null, "1");
                                    tts.playSilentUtterance(300, TextToSpeech.QUEUE_ADD, null);
                                    for (int i = 0; i < mDataSet.size(); i++) {
                                        tts.speak(mDataSet.get(i).getName(), TextToSpeech.QUEUE_ADD,
                                                null, "1");
                                        tts.playSilentUtterance(300, TextToSpeech.QUEUE_ADD, null);
                                    }
                                    tts.playSilentUtterance(300, TextToSpeech.QUEUE_ADD, null);
                                    tts.speak(getString(R.string.TTSChooseActivity), TextToSpeech.QUEUE_ADD,
                                            null, UTTERANCE_ID_LOC);

                                } else {
                                    Log.d("errorrrr", String.valueOf(status));
                                }
                            }
                        });
                    }

                    // Sort entries by nearest to current location
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_SPEECH_INPUT_Location) {
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent
                        .EXTRA_RESULTS);
                String res = result.get(0).toLowerCase();
                Pattern pattern = Pattern.compile("\\s");
                Matcher matcher = pattern.matcher(res);
                boolean found = matcher.find();

                Levenshtein l = new Levenshtein();
                Map<String, Double> results = new HashMap<>();
                for (int i = 0; i < mDataSet.size(); i++) {
                    results.put(mDataSet.get(i).getName().toLowerCase(), l.distance(res, mDataSet.get(i).getName().toLowerCase()));
                }

                Map.Entry<String, Double> min = null;
                for (Map.Entry<String, Double> entry : results.entrySet()) {
                    if (min == null || min.getValue() > entry.getValue()) {
                        min = entry;
                    }
                }

                res = min.getKey();
                int loc = 0;
                for (int i = 0; i < mDataSet.size(); i++) {
                    if (res.equals(mDataSet.get(i).getName().toLowerCase())) {
                        loc = i;
                    }
                }
                startSelection(loc);
            }
        }
    }

    private void startRecommendationsThread(final PlaceRequest request, final CountDownLatch
            latch, final android.location.Location location) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Transform Request object into URI and append to URL string
                    ObjectMapper mapper = new ObjectMapper();
                    String requestURI = mapper.convertValue(request, UriFormat.class).toString();
                    String address = GET_PLACE_ADDRESS + requestURI;

                    // Read value at URL into Response object
                    ObjectMapper mapper2 = new ObjectMapper();
                    PlaceResponse response = mapper2.readValue(new URL(address), PlaceResponse
                            .class);

                    if (response.getErrorMessage() == null) {
                        for (Place place : response.getResults()) {
                            // Only add open places or ones without opening hours
                            if (place.getOpeningHours() == null || place.getOpeningHours()
                                    .isOpenNow()) {
                                // Avoid duplicate entries
                                if (!mDataSet.contains(place)) {
                                    // Calculate distance from current location to place
                                    calculateHaversineDistance(location, place);

                                    // Add response results to our list of results
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
                    // Signal waiting functions that this thread is done
                    latch.countDown();
                }
            }
        });
    }

    private void calculateHaversineDistance(android.location.Location location, Place place) {
        // Calculate Harversine distance between two points
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

        // Set place distance value to d in meters
        place.setDistance(Double.valueOf(d * 1000).intValue());
    }

    private static class ViewHolder {
        private TextView titleTextView;
        private ImageView imageView;
    }

    private class RecommendationsDetailAdapter extends BaseAdapter {
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

        void setDataSource(ArrayList<Place> dataSource) {
            mDataSource = dataSource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            // Inflate new view or reuse old one
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_recommendations_detail,
                        parent, false);
                holder = new ViewHolder();
                holder.titleTextView = (TextView) convertView.findViewById(R.id.recommendation_detail_label);
                holder.imageView = (ImageView) convertView.findViewById(R.id.recommendation_detail_icon);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            // Place in question
            Place place = mDataSource.get(position);

            // Set title as name & distance
            holder.titleTextView.setText(String.format(getString(R.string.recommendationName), place.getName(), place.getDistance()));

            // Add dummy image while loading the real one
            holder.imageView.setImageResource(mRecMapping.getIcon());

            if (place.getPhotos() != null) {
                if (place.getImage() == null) {
                    // Image not yet downloaded --> build and send PhotoRequest
                    Photo photo = place.getPhotos().get(0);
                    PhotoRequest photoRequest = new PhotoRequest(API_KEY, photo.getPhotoReference
                            (), MAX_HEIGHT);
                    ObjectMapper mapper = new ObjectMapper();
                    String requestURI = mapper.convertValue(photoRequest, UriFormat.class)
                            .toString();
                    String address = GET_PHOTO_ADDRESS + requestURI;

                    // Download image in separate thread
                    new DownloadImageTask(place).execute(address);
                } else {
                    // Image already downloaded --> show it
                    holder.imageView.setImageBitmap(place.getImage());
                }
            }

            return convertView;
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
                // Download image
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
                // Resize image to be square-shaped, max 500x500
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
                        // Set image view of place's image without reloading the whole grid view
                        View v = mGridView.getChildAt(mDataSet.indexOf(mPlace) - mGridView
                                .getFirstVisiblePosition());

                        if (v == null) {
                            return;
                        }

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