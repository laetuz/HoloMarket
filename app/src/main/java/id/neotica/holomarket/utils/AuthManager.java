package id.neotica.holomarket.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AuthManager {

    private static final String PREF_NAME = "NeostoreAuth";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_EXPIRATION_TIME = "expiration_time";
    private static final String KEY_ADULT_CONTENT = "adult_content_enabled";
    private static final String KEY_USERNAME = "username";

    private SharedPreferences prefs;

    public AuthManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).commit();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void saveRefreshToken(String refreshToken) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, refreshToken).commit();
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public void saveExpirationTime(long expirationTime) {
        prefs.edit().putLong(KEY_EXPIRATION_TIME, expirationTime).commit();
    }

    public long getExpirationTime() {
        return prefs.getLong(KEY_EXPIRATION_TIME, 0);
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public String getAuthorizationHeader() {
        String token = getToken();
        if (token != null) {
            return "Bearer " + token;
        }
        return null;
    }

    public void saveUsername(String username) {
        prefs.edit().putString(KEY_USERNAME, username).commit();
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    public void clear() {
        prefs.edit()
                .remove(KEY_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .remove(KEY_EXPIRATION_TIME)
                .remove(KEY_USERNAME)
                .commit();
    }

    public void saveAdultContentEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ADULT_CONTENT, enabled).commit();
    }

    public boolean isAdultContentEnabled() {
        return prefs.getBoolean(KEY_ADULT_CONTENT, false);
    }

    public Map<String, String> getAuthHeaders() {
        String token = getToken();
        if (token != null) {
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Authorization", "Bearer " + token);
            return headers;
        }
        return null;
    }

    public String getUsernameFromToken() {
        String stored = getUsername();
        if (stored != null) return stored;
        String token = getToken();
        if (token == null) return null;
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;
            byte[] decoded = Base64.decode(parts[1], Base64.URL_SAFE);
            String json = new String(decoded, "UTF-8");
            JSONObject payload = new JSONObject(json);
            if (payload.has("sub")) return payload.optString("sub");
            if (payload.has("username")) return payload.optString("username");
            if (payload.has("name")) return payload.optString("name");
            if (payload.has("preferred_username")) return payload.optString("preferred_username");
            if (payload.has("email")) return payload.optString("email");
            if (payload.has("id")) return payload.optString("id");
            return "User";
        } catch (Throwable e) {
            return null;
        }
    }
}