package id.neotica.holomarket.ui.feature.settings;

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

import org.json.JSONException;
import org.json.JSONObject;

import id.neotica.holomarket.BuildConfig;
import id.neotica.holomarket.R;
import id.neotica.holomarket.network.ApiCallback;
import id.neotica.holomarket.network.ApiTask;
import id.neotica.holomarket.network.DownloadTask;
import id.neotica.holomarket.ui.feature.home.MainActivity;
import id.neotica.holomarket.utils.AuthManager;
import id.neotica.holomarket.utils.CrashCatcher;
import id.neotica.holomarket.utils.TopBarHelper;

public class SettingsActivity extends Activity {

    private AuthManager authManager;
    private boolean ignoreCheckedChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashCatcher.init(this.getApplicationContext());
        setContentView(R.layout.activity_settings);
        CrashCatcher.showCrashLogIfAny(this);

        TopBarHelper.setup(this, "Settings", true);

        authManager = new AuthManager(this);

        TextView tvUsername = (TextView) findViewById(R.id.tv_settings_username);
        Button btnLogout = (Button) findViewById(R.id.btn_logout);

        String username = authManager.getUsernameFromToken();
        if (username != null) {
            tvUsername.setText(username);
        } else {
            tvUsername.setText("User");
        }

        final CheckBox cbAdultContent = (CheckBox) findViewById(R.id.cb_adult_content);
        cbAdultContent.setChecked(authManager.isAdultContentEnabled());
        cbAdultContent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (ignoreCheckedChange) return;

                if (isChecked) {
                    final EditText input = new EditText(SettingsActivity.this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                    new AlertDialog.Builder(SettingsActivity.this)
                            .setTitle("18+ Content")
                            .setMessage("Enter password to enable 18+ content:")
                            .setView(input)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String password = input.getText().toString();
                                    if ("adult".equals(password)) {
                                        authManager.saveAdultContentEnabled(true);
                                    } else {
                                        Toast.makeText(SettingsActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
                                        ignoreCheckedChange = true;
                                        cbAdultContent.setChecked(false);
                                        ignoreCheckedChange = false;
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ignoreCheckedChange = true;
                                    cbAdultContent.setChecked(false);
                                    ignoreCheckedChange = false;
                                    dialog.cancel();
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    ignoreCheckedChange = true;
                                    cbAdultContent.setChecked(false);
                                    ignoreCheckedChange = false;
                                }
                            })
                            .show();
                } else {
                    authManager.saveAdultContentEnabled(false);
                }
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authManager.clear();
                Toast.makeText(SettingsActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        TextView tvVersion = (TextView) findViewById(R.id.tv_version);
        tvVersion.setText("Version " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")");

        TextView tvChangelog = (TextView) findViewById(R.id.tv_changelog);
        tvChangelog.setText("- Optimized for Android 1.5+\n- New category browsing with galleries\n- Performance improvements");

        TextView tvCredits = (TextView) findViewById(R.id.tv_credits);
        tvCredits.setText("HoloMarket\nPowered by Neotica\n© 2026 Neotica");

        Button btnCheckUpdate = (Button) findViewById(R.id.btn_check_update);
        btnCheckUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkForUpdate();
            }
        });
    }

    private void checkForUpdate() {
        String targetUrl = BuildConfig.BASE_URL + "/apps/id.neotica.holomarket/latest";

        new ApiTask(this, "GET", targetUrl, null, "Checking for updates...", new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject root = new JSONObject(response);
                    int latestVersionCode = root.optInt("version_code", 0);
                    String latestVersionName = root.optString("version_name", "");
                    String fileUrl = root.optString("file_url", "");

                    if (latestVersionCode == BuildConfig.VERSION_CODE) {
                        new AlertDialog.Builder(SettingsActivity.this)
                                .setTitle("Up to date")
                                .setMessage("HoloMarket is up to date.")
                                .setPositiveButton("OK", null)
                                .show();
                    } else if (latestVersionCode > BuildConfig.VERSION_CODE) {
                        final String downloadUrl = BuildConfig.FILE_BASE_URL + fileUrl;
                        final String fileName = "holomarket_v" + latestVersionName + ".apk";

                        new AlertDialog.Builder(SettingsActivity.this)
                                .setTitle("Update available")
                                .setMessage("Version " + latestVersionName + " is available.")
                                .setPositiveButton("Download", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        new DownloadTask(SettingsActivity.this, fileName).execute(downloadUrl);
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    } else {
                        new AlertDialog.Builder(SettingsActivity.this)
                                .setTitle("Up to date")
                                .setMessage("HoloMarket is up to date.")
                                .setPositiveButton("OK", null)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(SettingsActivity.this, "Error checking for updates.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(SettingsActivity.this, "Failed to check for updates: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        }).execute();
    }
}
