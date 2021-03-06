package au.edu.unimelb.mc.trippal.other;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.microsoft.windowsazure.mobileservices.MobileServiceActivityResult;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.shobhitpuri.custombuttons.GoogleSignInButton;
import com.tapadoo.alerter.Alerter;

import java.net.MalformedURLException;

import au.edu.unimelb.mc.trippal.R;
import au.edu.unimelb.mc.trippal.trip.TripListActivity;

import static au.edu.unimelb.mc.trippal.Constants.extraLoginSuccess;
import static au.edu.unimelb.mc.trippal.Constants.prefToken;
import static au.edu.unimelb.mc.trippal.Constants.prefUserId;
import static au.edu.unimelb.mc.trippal.Constants.prefUserInfo;

public class LoginActivity extends AppCompatActivity {
    private final String LOG_ID = "LoginActivity";
    private final String AZURE_AUTHENTICATION_URL = "https://trippal.azurewebsites.net";
    private final int GOOGLE_SIGNIN_CODE = 1;
    private final int MICROSOFT_SIGNIN_CODE = 2;

    private MobileServiceClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        GoogleSignInButton googleButton = (GoogleSignInButton) findViewById(R.id.googleSignInButton);
        Button microsoftButton = (Button) findViewById(R.id.microsoftSigninButton);

        try {
            mClient = new MobileServiceClient(AZURE_AUTHENTICATION_URL, this);
        } catch (MalformedURLException e) {
            Log.d(LOG_ID, e.getLocalizedMessage());
            e.printStackTrace();
        }

        if (loadUserData(mClient)) {
            showTripList(false);
        }

        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mClient != null) {
                    mClient.login("Google", "trippal", GOOGLE_SIGNIN_CODE);
                }
            }
        });
        microsoftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mClient != null) {
                    mClient.login("MicrosoftAccount", "trippal", MICROSOFT_SIGNIN_CODE);
                }
            }
        });
    }

    private void showTripList(boolean isNewLogin) {
        Intent intent = new Intent(this, TripListActivity.class);
        intent.putExtra(extraLoginSuccess, isNewLogin);
        startActivity(intent);
    }

    private boolean loadUserData(MobileServiceClient mClient) {
        SharedPreferences prefs = getSharedPreferences(prefUserInfo, Context.MODE_PRIVATE);
        String userId = prefs.getString(prefUserId, null);
        if (userId == null)
            return false;
        String token = prefs.getString(prefToken, null);
        if (token == null)
            return false;

        MobileServiceUser user = new MobileServiceUser(userId);
        user.setAuthenticationToken(token);
        mClient.setCurrentUser(user);

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // When request completes
        if (resultCode == RESULT_OK) {
            // Check the request code matches the one we send in the login request
            if (requestCode == GOOGLE_SIGNIN_CODE || requestCode == MICROSOFT_SIGNIN_CODE) {
                MobileServiceActivityResult result = mClient.onActivityResult(data);
                if (result.isLoggedIn()) {
                    cacheUserData(mClient.getCurrentUser());
                    Log.d(LOG_ID, "USERID: " + mClient.getCurrentUser().getUserId());
                    showTripList(true);
                } else {
                    Log.d(LOG_ID, "ERROR: Error during login");
                    Alerter.create(this).setText(R.string.unableToLogIn).setBackgroundColorRes(R.color.accent).enableIconPulse(false).setIcon(R.drawable.warning).show();
                }
            }
        }
    }

    private void cacheUserData(MobileServiceUser user) {
        SharedPreferences prefs = getSharedPreferences(prefUserInfo, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(prefUserId, user.getUserId());
        editor.putString(prefToken, user.getAuthenticationToken());
        editor.commit();
    }
}
