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

/**
 * Created by alexandrafritzen on 12/09/2017.
 */

public class RecommendationsActivity extends AppCompatActivity {
    private ArrayList<RecommendationMapping> mDataSet;
    private TextToSpeech tts;
    private static final String UTTERANCE_ID_ACT = "666";
    private int REQ_CODE_SPEECH_INPUT_ACT = 600;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendations);
        Log.d("take", String.valueOf(getIntent().getExtras().getBoolean(Constants.extraSpeech)));

        // Show toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_recommendations);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        // Enable up navigation (back arrow)
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(R.string.WhatToDo);
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

        if (getIntent().getExtras().getBoolean(Constants.extraSpeech)) {
            Intent intent = new Intent(RecognizerIntent
                    .ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale
                    .getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, Constants.extraWhichActivity);

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
        i.putExtra(Constants.extraMapping, mapping);
        i.putExtra(Constants.extraSpeech, speech);
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
                if (res.contains(Constants.mappingCoffee)) {
                    goToSelection(0, true);
                } else if (res.contains(Constants.mappingFood)) {
                    goToSelection(1, true);
                } else if (res.contains(Constants.mappingBathroom)) {
                    goToSelection(2, true);
                } else if (res.contains(Constants.mappingSleep)) {
                    goToSelection(3, true);
                } else if (res.contains(Constants.mappingStretchLegs)) {
                    goToSelection(4, true);
                } else if (res.contains(Constants.mappingSwitchDriver)) {
                    goToSelection(5, true);
                } else {
                    Levenshtein l = new Levenshtein();
                    Map<String, Double> results = new HashMap<>();
                    results.put(Constants.mappingCoffee, l.distance(res, Constants.mappingCoffee));
                    results.put(Constants.mappingFood, l.distance(res, Constants.mappingFood));
                    results.put(Constants.mappingBathroom, l.distance(res, Constants.mappingBathroom));
                    results.put(Constants.mappingSleep, l.distance(res, Constants.mappingSleep));
                    results.put(Constants.mappingStretchLegs, l.distance(res, Constants.mappingStretchLegs));
                    results.put(Constants.mappingSwitchDriver, l.distance(res, Constants.mappingSwitchDriver));

                    Map.Entry<String, Double> min = null;
                    for (Map.Entry<String, Double> entry : results.entrySet()) {
                        if (min == null || min.getValue() > entry.getValue()) {
                            min = entry;
                        }
                    }
                    res = min.getKey();
                    if (res.contains(Constants.mappingCoffee)) {
                        goToSelection(0, true);
                    } else if (res.contains(Constants.mappingFood)) {
                        goToSelection(1, true);
                    } else if (res.contains(Constants.mappingBathroom)) {
                        goToSelection(2, true);
                    } else if (res.contains(Constants.mappingSleep)) {
                        goToSelection(3, true);
                    } else if (res.contains(Constants.mappingStretchLegs)) {
                        goToSelection(4, true);
                    } else if (res.contains(Constants.mappingSwitchDriver)) {
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
            i.putExtra(Constants.extraMapping, mapping);
            startActivity(i);
        }
    };

    private static class ViewHolder {
        private TextView titleTextView;
        private ImageView imageView;
    }

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