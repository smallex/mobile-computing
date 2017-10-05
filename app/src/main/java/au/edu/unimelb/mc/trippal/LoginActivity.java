package au.edu.unimelb.mc.trippal;

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

import au.edu.unimelb.mc.trippal.trip.TripListActivity;

public class LoginActivity extends AppCompatActivity {

    private final String AZURE_AUTHENTICATION_URL = "https://trippal.azurewebsites.net";
    private final int GOOGLE_SIGNIN_CODE = 1;
    private final int MICROSOFT_SIGNIN_CODE = 2;

    private GoogleSignInButton googleButton;
    private Button microsoftButton;

    private MobileServiceClient mClient;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        googleButton = (GoogleSignInButton) findViewById(R.id.googleSignInButton);
        microsoftButton = (Button) findViewById(R.id.microsoftSigninButton);
        toolbar = (Toolbar) findViewById(R.id.toolbar_login);
        toolbar.setTitle("Login");

        try {
            mClient = new MobileServiceClient(AZURE_AUTHENTICATION_URL, this);
        } catch (MalformedURLException e) {
        }

        if (loadUserData(mClient)) {
            showTripList();
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

    private void showTripList() {
        Intent intent = new Intent(this, TripListActivity.class);
        intent.putExtra("loginSuccess", true);
        startActivity(intent);
    }

    private boolean loadUserData(MobileServiceClient mClient) {
        SharedPreferences prefs = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        String userId = prefs.getString("userid", null);
        if (userId == null)
            return false;
        String token = prefs.getString("token", null);
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
                    Log.d("USERID", mClient.getCurrentUser().getUserId());
                    showTripList();
                } else {
                    Log.d("ERROR", "Error during login");
                    Alerter.create(this).enableIconPulse(false).setIcon(R.drawable.warning).show();
                }
            }
        }
    }

    private void cacheUserData(MobileServiceUser user) {
        SharedPreferences prefs = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("userid", user.getUserId());
        editor.putString("token", user.getAuthenticationToken());
        editor.commit();
    }
}
