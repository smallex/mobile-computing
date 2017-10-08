package au.edu.unimelb.mc.trippal.recommendations;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import au.edu.unimelb.mc.trippal.Constants;
import au.edu.unimelb.mc.trippal.R;
import info.debatty.java.stringsimilarity.Levenshtein;

import static au.edu.unimelb.mc.trippal.Constants.extraMapping;
import static au.edu.unimelb.mc.trippal.Constants.extraSpeech;
import static au.edu.unimelb.mc.trippal.Constants.extraWhichActivity;
import static au.edu.unimelb.mc.trippal.Constants.mappingBathroom;
import static au.edu.unimelb.mc.trippal.Constants.mappingCoffee;
import static au.edu.unimelb.mc.trippal.Constants.mappingFood;
import static au.edu.unimelb.mc.trippal.Constants.mappingSleep;
import static au.edu.unimelb.mc.trippal.Constants.mappingStretchLegs;
import static au.edu.unimelb.mc.trippal.Constants.mappingSwitchDriver;

/**
 * Created by alexandrafritzen on 12/09/2017.
 */

public class RecommendationsActivity extends AppCompatActivity {
    private ArrayList<RecommendationMapping> mDataSet;
    private int REQ_CODE_SPEECH_INPUT_ACT = 600;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendations);
        Log.d("take", String.valueOf(getIntent().getExtras().getBoolean(extraSpeech)));

        // Show toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_recommendations);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        // Enable up navigation (back arrow)
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(R.string.whatToDo);
        }

        // Initialize mappings
        mDataSet = new ArrayList<>();
        mDataSet.add(RecommendationMapping.COFFEE);
        mDataSet.add(RecommendationMapping.FOOD);
        mDataSet.add(RecommendationMapping.BATHROOM);
        mDataSet.add(RecommendationMapping.SLEEP);
        mDataSet.add(RecommendationMapping.STRETCH_LEGS);
        mDataSet.add(RecommendationMapping.SWITCH_DRIVER);

        // Initialize grid view
        GridView gridView = (GridView) findViewById(R.id.gridview_recommendations);
        RecommendationsAdapter adapter = new RecommendationsAdapter(this, mDataSet);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(onItemClickListener);

        if (getIntent().getExtras().getBoolean(extraSpeech)) {
            Intent intent = new Intent(RecognizerIntent
                    .ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale
                    .getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, extraWhichActivity);

            try {
                startActivityForResult(intent, REQ_CODE_SPEECH_INPUT_ACT);
            } catch (ActivityNotFoundException a) {

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

    public void goToSelection(int position, boolean speech) {
        // Get corresponding RecommendationMapping item
        RecommendationMapping mapping = mDataSet.get(position);

        // Open RecommendationsDetailActivity page for the mapping
        Intent i = new Intent(RecommendationsActivity.this, RecommendationsDetailActivity.class);
        i.putExtra(extraMapping, mapping);
        i.putExtra(extraSpeech, speech);
        startActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_SPEECH_INPUT_ACT) {
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent
                        .EXTRA_RESULTS);
                String res = result.get(0).toLowerCase();
                Log.d("resultSleep", res);

                // check if speechrecognition result contains keywords if not choose closest one based on levensthein distance
                if (res.contains(mappingCoffee)) {
                    goToSelection(0, true);
                } else if (res.contains(mappingFood)) {
                    goToSelection(1, true);
                } else if (res.contains(mappingBathroom)) {
                    goToSelection(2, true);
                } else if (res.contains(mappingSleep)) {
                    goToSelection(3, true);
                } else if (res.contains(mappingStretchLegs)) {
                    goToSelection(4, true);
                } else if (res.contains(mappingSwitchDriver)) {
                    goToSelection(5, true);
                } else {
                    Levenshtein l = new Levenshtein();
                    Map<String, Double> results = new HashMap<>();
                    results.put(mappingCoffee, l.distance(res, mappingCoffee));
                    results.put(mappingFood, l.distance(res, mappingFood));
                    results.put(mappingBathroom, l.distance(res, mappingBathroom));
                    results.put(mappingSleep, l.distance(res, mappingSleep));
                    results.put(mappingStretchLegs, l.distance(res, mappingStretchLegs));
                    results.put(mappingSwitchDriver, l.distance(res, mappingSwitchDriver));

                    Map.Entry<String, Double> min = null;
                    for (Map.Entry<String, Double> entry : results.entrySet()) {
                        if (min == null || min.getValue() > entry.getValue()) {
                            min = entry;
                        }
                    }
                    res = min.getKey();
                    if (res.contains(mappingCoffee)) {
                        goToSelection(0, true);
                    } else if (res.contains(mappingFood)) {
                        goToSelection(1, true);
                    } else if (res.contains(mappingBathroom)) {
                        goToSelection(2, true);
                    } else if (res.contains(mappingSleep)) {
                        goToSelection(3, true);
                    } else if (res.contains(mappingStretchLegs)) {
                        goToSelection(4, true);
                    } else if (res.contains(mappingSwitchDriver)) {
                        goToSelection(5, true);
                    }
                }
            }
        }
    }

    private final AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
            // Get corresponding RecommendationMapping item
            RecommendationMapping mapping = mDataSet.get(position);

            // Open RecommendationsDetailActivity page for the mapping
            Intent i = new Intent(RecommendationsActivity.this, RecommendationsDetailActivity.class);
            i.putExtra(extraMapping, mapping);
            startActivity(i);
        }
    };

    /**
     * Helper class to recycle views with the RecommendationsAdapter
     */
    private static class ViewHolder {
        private TextView titleTextView;
        private ImageView imageView;
    }

    /**
     * The BaseAdapter that controls everything to do with the grid view
     */
    private class RecommendationsAdapter extends BaseAdapter {
        private final Context mContext;
        private final LayoutInflater mInflater;
        private ArrayList<RecommendationMapping> mDataSource;

        RecommendationsAdapter(Context context, ArrayList<RecommendationMapping> items) {
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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            // Inflate new view or reuse old one
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_recommendations,
                        parent, false);
                holder = new ViewHolder();
                holder.titleTextView = (TextView) convertView.findViewById(R.id.recommendation_label);
                holder.imageView = (ImageView) convertView.findViewById(R.id.recommendation_icon);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            // The mapping we're talking about
            RecommendationMapping mapping = mDataSource.get(position);

            // Set title and icon
            holder.titleTextView.setText(mapping.getActivity().toUpperCase());
            holder.imageView.setImageResource(mapping.getIcon());

            return convertView;
        }
    }
}