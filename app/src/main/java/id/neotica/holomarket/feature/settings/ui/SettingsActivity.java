package id.neotica.holomarket.feature.settings.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import id.neotica.holomarket.R;
import id.neotica.holomarket.feature.home.ui.MainActivity;
import id.neotica.holomarket.feature.settings.contract.SettingsView;
import id.neotica.holomarket.feature.settings.presenter.SettingsPresenter;
import id.neotica.holomarket.utils.CrashCatcher;
import id.neotica.holomarket.utils.TopBarHelper;

public class SettingsActivity extends Activity implements SettingsView {

    private TextView tvUsername;
    private TextView tvVersion;
    private CheckBox cbAdultContent;
    private boolean ignoreCheckedChange;

    private SettingsPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashCatcher.init(this.getApplicationContext());
        setContentView(R.layout.activity_settings);
        CrashCatcher.showCrashLogIfAny(this);

        TopBarHelper.setup(this, getString(R.string.settings_title), true);

        presenter = new SettingsPresenter(this);
        presenter.attach(this);

        tvUsername = (TextView) findViewById(R.id.tv_settings_username);
        tvVersion = (TextView) findViewById(R.id.tv_version);
        cbAdultContent = (CheckBox) findViewById(R.id.cb_adult_content);
        Button btnLogout = (Button) findViewById(R.id.btn_logout);

        cbAdultContent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (ignoreCheckedChange) return;

                if (isChecked) {
                    final EditText input = new EditText(SettingsActivity.this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                    new AlertDialog.Builder(SettingsActivity.this)
                            .setTitle(R.string.settings_adult_title)
                            .setMessage(R.string.settings_adult_prompt)
                            .setView(input)
                            .setPositiveButton(R.string.common_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String password = input.getText().toString();
                                    if (presenter.verifyAdultPassword(password)) {
                                        presenter.setAdultContent(true);
                                    } else {
                                        Toast.makeText(SettingsActivity.this, R.string.settings_wrong_password, Toast.LENGTH_SHORT).show();
                                        setAdultChecked(false);
                                    }
                                }
                            })
                            .setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    setAdultChecked(false);
                                    dialog.cancel();
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    setAdultChecked(false);
                                }
                            })
                            .show();
                } else {
                    presenter.setAdultContent(false);
                }
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.logout();
            }
        });

        TextView tvChangelog = (TextView) findViewById(R.id.tv_changelog);
        tvChangelog.setText(R.string.settings_changelog_text);

        TextView tvCredits = (TextView) findViewById(R.id.tv_credits);
        tvCredits.setText(R.string.settings_credits_text);

        Button btnCheckUpdate = (Button) findViewById(R.id.btn_check_update);
        btnCheckUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.checkForUpdates();
            }
        });

        presenter.load();
    }

    private void setAdultChecked(boolean checked) {
        ignoreCheckedChange = true;
        cbAdultContent.setChecked(checked);
        ignoreCheckedChange = false;
    }

    @Override
    public void renderProfile(String username) {
        tvUsername.setText(username);
    }

    @Override
    public void renderAdultContent(boolean enabled) {
        setAdultChecked(enabled);
    }

    @Override
    public void renderVersion(String versionName, int versionCode) {
        tvVersion.setText(getString(R.string.settings_version, versionName, versionCode));
    }

    @Override
    public void showUpToDate() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_up_to_date_title)
                .setMessage(R.string.settings_up_to_date_message)
                .setPositiveButton(R.string.common_ok, null)
                .show();
    }

    @Override
    public void showUpdateAvailable(String versionName) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_update_available_title)
                .setMessage(getString(R.string.settings_update_available_message, versionName))
                .setPositiveButton(R.string.common_download, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        presenter.downloadUpdate();
                    }
                })
                .setNegativeButton(R.string.common_cancel, null)
                .show();
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToMain() {
        Toast.makeText(SettingsActivity.this, R.string.settings_logged_out, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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