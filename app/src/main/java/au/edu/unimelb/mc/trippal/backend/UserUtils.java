package au.edu.unimelb.mc.trippal.backend;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Utility class for retrieving user information
 */
public class UserUtils {
    public static String getUserId(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        String userId = prefs.getString("userid", null);
        if (userId != null) {
            return userId;
        }
        // If not logged in, return demo user id
        return "demoUserID";
    }
}
