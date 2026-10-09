package id.neotica.holomarket.feature.auth.register.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import id.neotica.holomarket.R;
import id.neotica.holomarket.feature.auth.common.ui.PasswordEyeToggle;
import id.neotica.holomarket.feature.auth.register.contract.RegisterView;
import id.neotica.holomarket.feature.auth.register.presenter.RegisterPresenter;
import id.neotica.holomarket.utils.CrashCatcher;
import id.neotica.holomarket.utils.TopBarHelper;

public class RegisterActivity extends Activity implements RegisterView {

    private EditText etUsername, etEmail, etPassword;
    private RegisterPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashCatcher.init(this.getApplicationContext());
        setContentView(R.layout.activity_register);
        CrashCatcher.showCrashLogIfAny(this);

        TopBarHelper.setup(this, getString(R.string.auth_register_title), true);

        presenter = new RegisterPresenter(this);
        presenter.attach(this);

        etUsername = (EditText) findViewById(R.id.et_register_username);
        etEmail = (EditText) findViewById(R.id.et_register_email);
        etPassword = (EditText) findViewById(R.id.et_register_password);
        Button btnRegister = (Button) findViewById(R.id.btn_register);

        PasswordEyeToggle.attach(etPassword, R.drawable.ic_eye_open, R.drawable.ic_eye_closed);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.register(
                        etUsername.getText().toString().trim(),
                        etEmail.getText().toString().trim(),
                        etPassword.getText().toString().trim());
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

    @Override
    public void showUsernameError() {
        etUsername.setError(getString(R.string.common_error_enter_username));
    }

    @Override
    public void showEmailError() {
        etEmail.setError(getString(R.string.common_error_enter_email));
    }

    @Override
    public void showPasswordError() {
        etPassword.setError(getString(R.string.common_error_enter_password));
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void finishScreen() {
        finish();
    }

    @Override
    protected void onDestroy() {
        if (presenter != null) {
            presenter.detach();
        }
        super.onDestroy();
    }
}