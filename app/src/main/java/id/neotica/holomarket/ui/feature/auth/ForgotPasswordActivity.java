package id.neotica.holomarket.ui.feature.auth;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import id.neotica.holomarket.BuildConfig;
import id.neotica.holomarket.R;
import id.neotica.holomarket.network.ApiCallback;
import id.neotica.holomarket.network.ApiTask;
import id.neotica.holomarket.utils.CrashCatcher;
import id.neotica.holomarket.utils.TopBarHelper;

public class ForgotPasswordActivity extends Activity {

    private EditText etEmail;
    private Button btnSend;

    private static final String FORGOT_PASSWORD_URL = BuildConfig.AUTH_BASE_URL + "/auth/forgot-password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashCatcher.init(this.getApplicationContext());
        setContentView(R.layout.activity_forgot_password);
        CrashCatcher.showCrashLogIfAny(this);

        TopBarHelper.setup(this, "Forgot Password", true);

        etEmail = (EditText) findViewById(R.id.et_forgot_email);
        btnSend = (Button) findViewById(R.id.btn_send_reset_link);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performForgotPassword();
            }
        });

        findViewById(R.id.layout_root).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return false;
            }
        });
    }

    private void performForgotPassword() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter email");
            return;
        }

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("email", email);

        new ApiTask(this, "POST", FORGOT_PASSWORD_URL, null, "Sending...", new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    String message = json.optString("message", "If the email exists, a reset link has been sent.");
                    Toast.makeText(ForgotPasswordActivity.this, message, Toast.LENGTH_LONG).show();
                    finish();
                } catch (Exception e) {
                    Toast.makeText(ForgotPasswordActivity.this, "Error parsing response", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(ForgotPasswordActivity.this, "Request failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        }, headers).execute();
    }
}
