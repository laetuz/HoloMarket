package id.neotica.holomarket.feature.auth.register.presenter;

import android.content.Context;

import org.json.JSONObject;

import id.neotica.holomarket.BuildConfig;
import id.neotica.holomarket.feature.auth.register.contract.RegisterView;
import id.neotica.holomarket.network.AnalyticsTracker;
import id.neotica.holomarket.network.ApiCallback;
import id.neotica.holomarket.network.ApiTask;

/**
 * MVP presenter owning the registration flow: validation, request, analytics, and completion.
 * Rendering is delegated to a {@link RegisterView}; networking stays here.
 */
public class RegisterPresenter {

    private final Context context;

    private RegisterView view;

    public RegisterPresenter(Context context) {
        this.context = context;
    }

    public void attach(RegisterView view) {
        this.view = view;
    }

    public void detach() {
        this.view = null;
    }

    public void register(String username, String email, String password) {
        if (username == null || username.length() == 0) {
            if (view != null) {
                view.showUsernameError();
            }
            return;
        }
        if (email == null || email.length() == 0) {
            if (view != null) {
                view.showEmailError();
            }
            return;
        }
        if (password == null || password.length() == 0) {
            if (view != null) {
                view.showPasswordError();
            }
            return;
        }

        requestRegister(username, password, email, new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    String message = json.optString("message", "Account created.");
                    if (view != null) {
                        view.showMessage(message);
                    }
                    AnalyticsTracker.track(context, "feature_use", "user_registered");
                    if (view != null) {
                        view.finishScreen();
                    }
                } catch (Exception e) {
                    if (view != null) {
                        view.showMessage("Error parsing response");
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (view != null) {
                    view.showMessage("Registration failed: " + errorMessage);
                }
            }
        });
    }

    // --- I/O seam: overridden in tests to run synchronously ---

    void requestRegister(String username, String password, String email, ApiCallback callback) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("username", username);
            payload.put("password", password);
            payload.put("email", email);

            new ApiTask(context, "POST", BuildConfig.AUTH_BASE_URL + "/auth/register",
                    payload.toString(), "Registering...", callback).execute();
        } catch (Exception e) {
            callback.onError("Error creating request");
        }
    }
}