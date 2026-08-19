package id.neotica.holomarket.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by ryomartin on 21/03/26.
 */

public class CrashCatcher {

    private static final String PREF_NAME = "CrashLogs";
    private static final String KEY_LAST_CRASH = "last_crash";

    /**
     * Call this once to start listening for app crashes.
     */
    public static void init(final Context context) {
        final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                // 1. Extract the red text (stack trace)
                StringWriter sw = new StringWriter();
                throwable.printStackTrace(new PrintWriter(sw));
                String stackTrace = sw.toString();

                // 2. Save it to memory instantly
                SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                prefs.edit().putString(KEY_LAST_CRASH, stackTrace).commit();

                // 3. Let the OS kill the app normally
                if (defaultHandler != null) {
                    defaultHandler.uncaughtException(thread, throwable);
                }
            }
        });
    }

    /**
     * Call this in your Activity to pop up the dialog if a crash was saved.
     */
    public static void showCrashLogIfAny(final Activity activity) {
        String lastCrash = getLastCrash(activity);

        if (lastCrash != null) {
            new AlertDialog.Builder(activity)
                    .setTitle("Crash Detected!")
                    .setMessage(lastCrash)
                    .setPositiveButton("Clear & Close", null)
                    .show();

            // Clear the log so it only shows once
            clearCrashLog(activity);
        }
    }

    public static String getLastCrash(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LAST_CRASH, null);
    }

    public static void clearCrashLog(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_LAST_CRASH).commit();
    }
}