package id.neotica.holomarket.feature.auth.forgotpassword.presenter;

import android.content.Context;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import id.neotica.holomarket.BuildConfig;
import id.neotica.holomarket.feature.auth.forgotpassword.contract.ForgotPasswordView;
import id.neotica.holomarket.network.ApiCallback;
import id.neotica.holomarket.network.ApiTask;

/**
 * MVP presenter owning the forgot-password flow: validation, request, and completion.
 * Rendering is delegated to a {@link ForgotPasswordView}; networking stays here.
 */
public class ForgotPasswordPresenter {

    private final Context context;

    private ForgotPasswordView view;

    public ForgotPasswordPresenter(Context context) {
        this.context = context;
    }

    public void attach(ForgotPasswordView view) {
        this.view = view;
    }

    public void detach() {
        this.view = null;
    }

    public void sendResetLink(String email) {
        if (email == null || email.length() == 0) {
            if (view != null) {
                view.showEmailError();
            }
            return;
        }

        requestForgot(email, new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    String message = json.optString("message", "If the email exists, a reset link has been sent.");
                    if (view != null) {
                        view.showMessage(message);
                    }
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
                    view.showMessage("Request failed: " + errorMessage);
                }
            }
        });
    }

    // --- I/O seam: overridden in tests to run synchronously ---

    void requestForgot(String email, ApiCallback callback) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("email", email);

        new ApiTask(context, "POST", BuildConfig.AUTH_BASE_URL + "/auth/forgot-password", null,
                "Sending...", callback, headers).execute();
    }
}