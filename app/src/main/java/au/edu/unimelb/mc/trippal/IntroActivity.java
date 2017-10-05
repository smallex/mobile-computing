package au.edu.unimelb.mc.trippal;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

public class IntroActivity extends AppIntro2 {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Each Intro slide
        AppIntroFragment fragment = AppIntroFragment.newInstance(getString(R.string.splash_screen_location_title), getString(R.string.splash_screen_location), R.drawable.map_colored, ContextCompat.getColor(this, R.color.primary), ContextCompat.getColor(this, R.color.icons), ContextCompat.getColor(this, R.color.icons));
        AppIntroFragment fragment2 = AppIntroFragment.newInstance(getString(R.string.splash_screen_camera_title), getString(R.string.splash_screen_camera), R.drawable.tired_colored, ContextCompat.getColor(this, R.color.primary_dark), ContextCompat.getColor(this, R.color.icons), ContextCompat.getColor(this, R.color.icons));
        AppIntroFragment fragment3 = AppIntroFragment.newInstance(getString(R.string.splash_screen_mic_title), getString(R.string.splash_screen_mic), R.drawable.microphone_colored, ContextCompat.getColor(this, R.color.primary), ContextCompat.getColor(this, R.color.icons), ContextCompat.getColor(this, R.color.icons));
        AppIntroFragment fragment4 = AppIntroFragment.newInstance(getString(R.string.splash_screen_final_title), getString(R.string.splash_screen_final), R.drawable.car_2_colored, ContextCompat.getColor(this, R.color.primary_dark), ContextCompat.getColor(this, R.color.icons), ContextCompat.getColor(this, R.color.icons));

        // Add slides to collection
        addSlide(fragment);
        addSlide(fragment2);
        addSlide(fragment3);
        addSlide(fragment4);

        // Hide Skip/Done button.
        showSkipButton(false);
        setProgressButtonEnabled(true);

        // Turn vibration off
        setVibrate(false);

        // Connect fragment 0, 1, and 2 with permission requests
        askForPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        askForPermissions(new String[]{Manifest.permission.CAMERA}, 2);
        askForPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 3);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        finish();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
    }
}
