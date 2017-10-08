package au.edu.unimelb.mc.trippal.other;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import au.edu.unimelb.mc.trippal.trip.NewTripActivity;

import static au.edu.unimelb.mc.trippal.Constants.extraAccessToken;
import static au.edu.unimelb.mc.trippal.Constants.extraUserId;

/**
 * Redirects to NewTripActivity after Fitbit data synchronization.
 */
public class RedirectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            // Replace '#' by '?' so query parameters can be parsed
            uri = Uri.parse(uri.toString().replace("#", "?"));
            final String accessToken = uri.getQueryParameter("access_token");
            String userID = uri.getQueryParameter("user_id");

            Intent redirectIntent = new Intent(this, NewTripActivity.class);
            redirectIntent.putExtra(extraUserId, userID);
            redirectIntent.putExtra(extraAccessToken, accessToken);
            redirectIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent
                    .FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(redirectIntent);
            finish();
        }
    }
}
