package au.edu.unimelb.mc.trippal.recommendations;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import au.edu.unimelb.mc.trippal.R;

/**
 * Created by alexandrafritzen on 12/09/2017.
 */

public class Recommendations extends AppCompatActivity {
    private static final String LOG_ID = "RecommendationsActivity";
    private ArrayList<RecommendationMapping> mDataSet;
    private RecommendationsAdapter mAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendations);

        // Show toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_recommendations);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        // Enable up navigation (back arrow)
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle("WHAT TO DO?");
        }

        // Initialize places and list view
        mDataSet = new ArrayList<>();
        mDataSet.add(RecommendationMapping.COFFEE);
        mDataSet.add(RecommendationMapping.FOOD);
        mDataSet.add(RecommendationMapping.BATHROOM);
        mDataSet.add(RecommendationMapping.SLEEP);
        mDataSet.add(RecommendationMapping.STRETCH_LEGS);
        ListView mListView = (ListView) findViewById(R.id.listview_recommendations);
        mAdapter = new Recommendations.RecommendationsAdapter(this, mDataSet);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(onItemClickListener);
    }

    private final AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
            // Get corresponding RecommendationMapping item
            RecommendationMapping mapping = mDataSet.get(position);

            // Open RecommendationsDetail page for the mapping
            Intent i = new Intent(Recommendations.this, RecommendationsDetail.class);
            i.putExtra("MAPPING", mapping);
            startActivity(i);
        }
    };

    public class RecommendationsAdapter extends BaseAdapter {
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

        public void setDataSource(ArrayList<RecommendationMapping> dataSource) {
            mDataSource = dataSource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = mInflater.inflate(R.layout.item_recommendations, parent, false);

            RecommendationMapping mapping = mDataSource.get(position);
            TextView titleTextView = (TextView) rowView.findViewById(R.id.recommendation_label);

            titleTextView.setText(mapping.getActivity());

            return rowView;
        }
    }
}