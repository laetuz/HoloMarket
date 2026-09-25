package id.neotica.holomarket.feature.auth.forgotpassword.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import id.neotica.holomarket.R;
import id.neotica.holomarket.feature.auth.forgotpassword.contract.ForgotPasswordView;
import id.neotica.holomarket.feature.auth.forgotpassword.presenter.ForgotPasswordPresenter;
import id.neotica.holomarket.utils.CrashCatcher;
import id.neotica.holomarket.utils.TopBarHelper;

public class ForgotPasswordActivity extends Activity implements ForgotPasswordView {

    private EditText etEmail;
    private ForgotPasswordPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashCatcher.init(this.getApplicationContext());
        setContentView(R.layout.activity_forgot_password);
        CrashCatcher.showCrashLogIfAny(this);

        TopBarHelper.setup(this, "Forgot Password", true);

        presenter = new ForgotPasswordPresenter(this);
        presenter.attach(this);

        etEmail = (EditText) findViewById(R.id.et_forgot_email);
        Button btnSend = (Button) findViewById(R.id.btn_send_reset_link);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.sendResetLink(etEmail.getText().toString().trim());
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
    public void showEmailError() {
        etEmail.setError("Enter email");
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