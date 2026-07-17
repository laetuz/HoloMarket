package id.neotica.holomarket.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import id.neotica.holomarket.BuildConfig;
import id.neotica.holomarket.utils.AuthManager;

public class AnalyticsTracker {

    private static final String TAG = "AnalyticsTracker";
    static final String EVENTS_URL = BuildConfig.NEOMETRICS_BASE_URL + "/analytics/events";

    public static void track(Context context, String eventType, String eventName) {
        Log.d(TAG, "track(" + eventType + ", " + eventName + ")");

        final AuthManager auth = new AuthManager(context);
        if (!auth.isLoggedIn()) {
            Log.w(TAG, "skip — user not logged in");
            return;
        }

        final String token = auth.getToken();
        final String fEventType = eventType;
        final String fEventName = eventName;
        if (token == null) {
            Log.w(TAG, "skip — token is null");
            return;
        }

        Log.d(TAG, "POST " + EVENTS_URL);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    URL url = new URL(EVENTS_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);

                    String body = "{\"event_type\":\"" + fEventType
                            + "\",\"event_name\":\"" + fEventName
                            + "\",\"source_service\":\"neostore\"}";

                    OutputStream os = conn.getOutputStream();
                    os.write(body.getBytes("UTF-8"));
                    os.flush();
                    os.close();

                    int code = conn.getResponseCode();
                    Log.d(TAG, "response " + code);
                    conn.disconnect();
                } catch (Throwable t) {
                    Log.e(TAG, "request failed", t);
                }
                return null;
            }
        }.execute();
    }
}
