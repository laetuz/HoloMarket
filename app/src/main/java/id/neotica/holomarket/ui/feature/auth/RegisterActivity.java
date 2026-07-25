package id.neotica.holomarket.ui.feature.auth;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import id.neotica.holomarket.BuildConfig;
import id.neotica.holomarket.R;
import id.neotica.holomarket.network.AnalyticsTracker;
import id.neotica.holomarket.network.ApiCallback;
import id.neotica.holomarket.network.ApiTask;
import id.neotica.holomarket.utils.CrashCatcher;
import id.neotica.holomarket.utils.TopBarHelper;

public class RegisterActivity extends Activity {

    private EditText etUsername, etEmail, etPassword;
    private Button btnRegister;

    private static final String REGISTER_URL = BuildConfig.AUTH_BASE_URL + "/auth/register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashCatcher.init(this.getApplicationContext());
        setContentView(R.layout.activity_register);
        CrashCatcher.showCrashLogIfAny(this);

        TopBarHelper.setup(this, "Create Account", true);

        etUsername = (EditText) findViewById(R.id.et_register_username);
        etEmail = (EditText) findViewById(R.id.et_register_email);
        etPassword = (EditText) findViewById(R.id.et_register_password);
        btnRegister = (Button) findViewById(R.id.btn_register);

        etPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Drawable drawableRight = etPassword.getCompoundDrawables()[2];
                    if (drawableRight != null && event.getRawX() >= (etPassword.getRight() - drawableRight.getBounds().width() - etPassword.getPaddingRight())) {
                        if (etPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_open, 0);
                        } else {
                            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            etPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_closed, 0);
                        }
                        etPassword.setSelection(etPassword.getText().length());
                        return true;
                    }
                }
                return false;
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performRegister();
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

    private void performRegister() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Enter username");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Enter password");
            return;
        }

        try {
            JSONObject payload = new JSONObject();
            payload.put("username", username);
            payload.put("password", password);
            payload.put("email", email);

            new ApiTask(this, "POST", REGISTER_URL, payload.toString(), "Registering...", new ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    try {
                        JSONObject json = new JSONObject(response);
                        String message = json.optString("message", "Account created.");
                        String registeredEmail = json.optString("email", "");
                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                        AnalyticsTracker.track(RegisterActivity.this, "feature_use", "user_registered");
                        finish();
                    } catch (Exception e) {
                        Toast.makeText(RegisterActivity.this, "Error parsing response", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(RegisterActivity.this, "Registration failed: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            }).execute();
        } catch (Exception e) {
            Toast.makeText(this, "Error creating request", Toast.LENGTH_LONG).show();
        }
    }
}
