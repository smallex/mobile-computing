package au.edu.unimelb.mc.trippal.other;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

import au.edu.unimelb.mc.trippal.R;

import static au.edu.unimelb.mc.trippal.Constants.prefFirstStart;

/**
 * Activity that introduces the app and asks for required permissions.
 */
public class IntroActivity extends AppIntro2 {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showIntro();
    }


    public void showIntro() {
        // Each Intro slide
        AppIntroFragment fragment = AppIntroFragment.newInstance(getString(R.string
                        .splash_screen_location_title), getString(R.string.splash_screen_location), R
                        .drawable.map_colored, ContextCompat.getColor(this, R.color.primary),
                ContextCompat.getColor(this, R.color.icons), ContextCompat.getColor(this, R.color
                        .icons));
        AppIntroFragment fragment2 = AppIntroFragment.newInstance(getString(R.string
                        .splash_screen_camera_title), getString(R.string.splash_screen_camera), R
                        .drawable.tired_colored, ContextCompat.getColor(this, R.color.primary_dark),
                ContextCompat.getColor(this, R.color.icons), ContextCompat.getColor(this, R.color
                        .icons));
        AppIntroFragment fragment3 = AppIntroFragment.newInstance(getString(R.string
                .splash_screen_mic_title), getString(R.string.splash_screen_mic), R.drawable
                .microphone_colored, ContextCompat.getColor(this, R.color.primary), ContextCompat
                .getColor(this, R.color.icons), ContextCompat.getColor(this, R.color.icons));
        AppIntroFragment fragment4 = AppIntroFragment.newInstance(getString(R.string
                .splash_screen_final_title), getString(R.string.splash_screen_final), R.drawable
                .car_2_colored, ContextCompat.getColor(this, R.color.primary_dark), ContextCompat
                .getColor(this, R.color.icons), ContextCompat.getColor(this, R.color.icons));

        boolean isFirstStart = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean(prefFirstStart, true);
        if (isFirstStart) {
            // Add all slides to collection
            addSlide(fragment);
            addSlide(fragment2);
            addSlide(fragment3);
            addSlide(fragment4);

            // Connect fragment 0, 1, and 2 with permission requests
            askForPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            askForPermissions(new String[]{Manifest.permission.CAMERA}, 2);
            askForPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 3);
        } else {
            // We must be missing a permission --> open intro again until we have them all...
            boolean locationPerm = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
            boolean cameraPerm = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED;
            boolean audioPerm = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED;

            int i = 1;
            if (locationPerm) {
                addSlide(fragment);
                askForPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, i);
                i++;
            }

            if (cameraPerm) {
                addSlide(fragment2);
                askForPermissions(new String[]{Manifest.permission.CAMERA}, i);
                i++;
            }

            if (audioPerm) {
                addSlide(fragment3);
                askForPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, i);
            }

            addSlide(fragment4);
        }

        // Hide Skip/Done button.
        showSkipButton(false);
        setProgressButtonEnabled(true);

        // Turn vibration off
        setVibrate(false);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);

        boolean missingPermission = false;
        String[] neededPermissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
        for (String permission : neededPermissions) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission = true;
                break;
            }
        }

        // Make a new preferences editor
        SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();

        // Edit preference to make it false because we don't want this to run again
        e.putBoolean(prefFirstStart, false);
        e.apply();

        finish();

        if (missingPermission) {
            // Restart activity
            startActivity(getIntent());
        }
    }
}
