
package se.kjellstrand.awp.plasma.prefs;

import se.kjellstrand.awp.plasma.PlasmaWallpaper;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public final class Settings {

    private static final String PREFERENCES_FILE = "RecommendationPreferencesFile";

    private static final String LOG_TAG = Settings.class.getCanonicalName();

    private static final String PREFS_ZOOM = "zoom";

    private static SharedPreferences.Editor openSharedPreferencesForEditing(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_FILE,
                Context.MODE_PRIVATE);
        return prefs.edit();
    }

    private static SharedPreferences loadSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    public static void setZoom(Context context, float zoom) {
        SharedPreferences.Editor editor = openSharedPreferencesForEditing(context);
        editor.putFloat(PREFS_ZOOM, zoom);
        editor.apply();
    }

    public static float getZoom(Context context) {
        return loadSharedPreferences(context).getFloat(PREFS_ZOOM, PlasmaWallpaper.SCALE);
    }

    public static void reset(Context context) {
        if (context != null) {
            SharedPreferences.Editor editor = openSharedPreferencesForEditing(context);
            editor.clear();
            editor.apply();
        } else {
            Log.e(LOG_TAG,
                    "Reset: context == null.");
        }
    }

}
