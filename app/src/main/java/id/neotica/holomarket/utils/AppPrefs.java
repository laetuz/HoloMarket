package id.neotica.holomarket.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * App-level (non-auth) settings stored in SharedPreferences.
 */
public final class AppPrefs {

    private static final String PREF_NAME = "NeostoreSettings";
    private static final String KEY_ROOT_AUTO_INSTALL = "root_auto_install";

    private AppPrefs() { }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isRootAutoInstallEnabled(Context context) {
        return prefs(context).getBoolean(KEY_ROOT_AUTO_INSTALL, false);
    }

    public static void setRootAutoInstallEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_ROOT_AUTO_INSTALL, enabled).commit();
    }
}