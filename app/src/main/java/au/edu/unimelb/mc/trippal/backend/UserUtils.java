package au.edu.unimelb.mc.trippal.backend;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import static au.edu.unimelb.mc.trippal.Constants.prefUserId;
import static au.edu.unimelb.mc.trippal.Constants.prefUserInfo;

/**
 * Utility class for retrieving user information
 */
public class UserUtils {
    public static String getUserId(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(prefUserInfo, Context.MODE_PRIVATE);
        String userId = prefs.getString(prefUserId, null);
        if (userId != null) {
            return userId;
        }
        // If not logged in, return demo user id
        return "demoUserID";
    }
}
