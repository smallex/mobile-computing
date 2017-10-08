package au.edu.unimelb.mc.trippal.trip;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import au.edu.unimelb.mc.trippal.R;
import au.edu.unimelb.mc.trippal.backend.TripEntity;

/**
 * Custom ArrayAdapter for TripEntities for display inside a ListView.
 */
class TripListAdapter extends ArrayAdapter<TripEntity> {
    TripListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull
            List<TripEntity> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getContext()
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.activity_trip_list_item, null, true);
        }

        TripEntity entity = getItem(position);
        TextView destination = (TextView) convertView.findViewById(R.id.trip_destination);
        destination.setText(entity.getDestinationName());

        TextView duration = (TextView) convertView.findViewById(R.id.trip_duration);
        duration.setText(entity.getDuration());

        TextView distance = (TextView) convertView.findViewById(R.id.trip_distance);
        distance.setText(entity.getDistance());

        TextView date = (TextView) convertView.findViewById(R.id.trip_date);
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yy");
        String dateText = entity.getTripDate() != null ? df.format(entity.getTripDate()) : "";
        date.setText(dateText);

        return convertView;
    }
}
