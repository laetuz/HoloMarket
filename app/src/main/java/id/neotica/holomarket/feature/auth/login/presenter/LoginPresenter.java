package id.neotica.holomarket.feature.auth.login.presenter;

import android.content.Context;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import id.neotica.holomarket.BuildConfig;
import id.neotica.holomarket.feature.auth.login.contract.LoginView;
import id.neotica.holomarket.feature.auth.login.domain.LoginResult;
import id.neotica.holomarket.network.AnalyticsTracker;
import id.neotica.holomarket.network.ApiCallback;
import id.neotica.holomarket.network.ApiTask;
import id.neotica.holomarket.utils.AuthManager;

/**
 * MVP presenter owning the login flow: validation, token exchange, username fetch, and analytics.
 * Rendering is delegated to a {@link LoginView}; networking stays here.
 */
public class LoginPresenter {

    private final Context context;
    private final AuthManager authManager;

    private LoginView view;

    public LoginPresenter(Context context) {
        this.context = context;
        this.authManager = new AuthManager(context);
    }

    public void attach(LoginView view) {
        this.view = view;
    }

    public void detach() {
        this.view = null;
    }

    public void login(String username, String password) {
        if (username == null || username.length() == 0) {
            if (view != null) {
                view.showUsernameError();
            }
            return;
        }
        if (password == null || password.length() == 0) {
            if (view != null) {
                view.showPasswordError();
            }
            return;
        }

        requestLogin(username, password, new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                LoginResult result;
                try {
                    result = LoginResult.fromJson(new JSONObject(response));
                } catch (Exception e) {
                    if (view != null) {
                        view.showMessage("Error parsing response");
                    }
                    return;
                }

                if (result == null || result.token == null) {
                    if (view != null) {
                        view.showMessage("Login failed: no token in response");
                    }
                    return;
                }

                authManager.saveToken(result.token);
                authManager.saveRefreshToken(result.refreshToken);
                authManager.saveExpirationTime(result.expirationTime);

                AnalyticsTracker.track(context, "feature_use", "user_logged_in");
                fetchUsername();
            }

            @Override
            public void onError(String errorMessage) {
                if (view != null) {
                    view.showMessage("Login failed: " + errorMessage);
                }
            }
        });
    }

    private void fetchUsername() {
        requestUsername(new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                if (response != null) {
                    authManager.saveUsername(response.trim());
                }
                if (view != null) {
                    view.navigateToMain();
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (view != null) {
                    view.navigateToMain();
                }
            }
        });
    }

    // --- I/O seams: overridden in tests to run synchronously ---

    void requestLogin(String username, String password, ApiCallback callback) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("username", username);
        headers.put("password", password);

        new ApiTask(context, "GET", BuildConfig.AUTH_BASE_URL + "/auth/login", null,
                "Logging in...", callback, headers).execute();
    }

    void requestUsername(ApiCallback callback) {
        new ApiTask(context, "GET", BuildConfig.AUTH_BASE_URL + "/auth/user/username", null,
                null, callback, authManager.getAuthHeaders()).execute();
    }
}