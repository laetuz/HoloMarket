package id.neotica.holomarket.feature.auth.login.domain;

import org.json.JSONObject;

/**
 * Parsed login response (`GET /auth/login`).
 */
public class LoginResult {

    public final String token;
    public final String refreshToken;
    public final long expirationTime;

    public LoginResult(String token, String refreshToken, long expirationTime) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.expirationTime = expirationTime;
    }

    public static LoginResult fromJson(JSONObject obj) {
        if (obj == null) {
            return null;
        }
        return new LoginResult(
                obj.optString("token", null),
                obj.optString("refreshToken", null),
                obj.optLong("expirationTime", 0)
        );
    }
}