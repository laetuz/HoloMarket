package id.neotica.holomarket.ui.feature.detail;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import id.neotica.holomarket.BuildConfig;
import id.neotica.holomarket.R;
import id.neotica.holomarket.model.AppModel;
import id.neotica.holomarket.model.VersionModel;
import id.neotica.holomarket.network.AnalyticsTracker;
import id.neotica.holomarket.network.ApiCallback;
import id.neotica.holomarket.network.ApiTask;
import id.neotica.holomarket.network.DownloadTask;
import id.neotica.holomarket.utils.TopBarHelper;

public class AppDetailActivity extends Activity {

    private TextView tvTitle, tvDesc;
    private ImageView ivIcon;
    private ListView lvVersions;
    private VersionAdapter adapter;
    private Button btDownload;
    private String currentPackageName;
    private boolean isOpenMode = false;

    private static final String INTENT_PACKAGE_NAME = "PACKAGE_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_detail);

        TopBarHelper.setup(this, "App Detail", true);

        tvTitle = (TextView) findViewById(R.id.tv_detail_title);
        tvDesc = (TextView) findViewById(R.id.tv_detail_desc);
        ivIcon = (ImageView) findViewById(R.id.iv_detail_icon);
        lvVersions = (ListView) findViewById(R.id.lv_versions);
        btDownload = (Button) findViewById(R.id.bt_download);

        btDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isOpenMode && currentPackageName != null && currentPackageName.length() > 0) {
                    android.content.Intent launchIntent = getPackageManager().getLaunchIntentForPackage(currentPackageName);
                    if (launchIntent != null) {
                        startActivity(launchIntent);
                    } else {
                        Toast.makeText(AppDetailActivity.this, "Unable to open the app.", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

                if (adapter.getCount() == 0) {
                    Toast.makeText(AppDetailActivity.this, "No versions available.", Toast.LENGTH_SHORT).show();
                    return;
                }

                VersionModel latestVersion = null;
                int maxVersionCode = -1;

                for (int i = 0; i < adapter.getCount(); i++) {
                    VersionModel current = adapter.getItem(i);
                    if (current != null && current.versionCode > maxVersionCode) {
                        maxVersionCode = current.versionCode;
                        latestVersion = current;
                    }
                }

                if (latestVersion != null && latestVersion.fileUrl != null && latestVersion.fileUrl.length() > 0) {

                    String downloadUrl = BuildConfig.FILE_BASE_URL + latestVersion.fileUrl;
                    String fileName = latestVersion.fileUrl.substring(latestVersion.fileUrl.lastIndexOf('/') + 1);

                    if (fileName.length() == 0 || !fileName.endsWith(".apk")) {
                        fileName = "update_v" + latestVersion.versionCode + ".apk";
                    }

                    AnalyticsTracker.track(AppDetailActivity.this, "download", "app_downloaded");

                    String appTitle = tvTitle.getText().toString();
                    new DownloadTask(AppDetailActivity.this, fileName, appTitle).execute(downloadUrl);

                } else {
                    Toast.makeText(AppDetailActivity.this, "Download link not available for the latest version.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        adapter = new VersionAdapter(this, new ArrayList<VersionModel>());
        lvVersions.setAdapter(adapter);

        lvVersions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                VersionModel clickedVersion = adapter.getItem(position);

                if (clickedVersion != null && clickedVersion.fileUrl != null && clickedVersion.fileUrl.length() > 0) {

                    String downloadUrl = BuildConfig.FILE_BASE_URL + clickedVersion.fileUrl;

                    String fileName = clickedVersion.fileUrl.substring(clickedVersion.fileUrl.lastIndexOf('/') + 1);

                    if (fileName.length() == 0 || !fileName.endsWith(".apk")) {
                        fileName = "update_v" + clickedVersion.versionCode + ".apk";
                    }

                    AnalyticsTracker.track(AppDetailActivity.this, "download", "app_downloaded");

                    // start downloading.
                    String appTitle = tvTitle.getText().toString();
                    new DownloadTask(AppDetailActivity.this, fileName, appTitle).execute(downloadUrl);

                } else {
                    Toast.makeText(AppDetailActivity.this, "Download link not available for this version.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        String packageName = getIntent().getStringExtra(INTENT_PACKAGE_NAME);

        if (packageName != null) {
            currentPackageName = packageName;
            fetchAppDetails(packageName);
        } else {
            Toast.makeText(this, "Error: No package provided.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private int getInstalledVersionCode(String packageName) {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(packageName, 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }

    private void updateInstallState(String packageName, int latestVersionCode) {
        btDownload.setEnabled(true);
        int installedVersion = getInstalledVersionCode(packageName);
        if (installedVersion < 0) {
            isOpenMode = false;
            btDownload.setVisibility(View.VISIBLE);
            btDownload.setText("Download");
        } else if (installedVersion >= latestVersionCode) {
            isOpenMode = true;
            btDownload.setVisibility(View.VISIBLE);
            btDownload.setText("Open");
        } else {
            isOpenMode = false;
            btDownload.setVisibility(View.VISIBLE);
            btDownload.setText("Update");
        }
    }

    private void fetchAppDetails(final String packageName) {
        btDownload.setEnabled(false);
        btDownload.setText("Loading...");
        String targetUrl = BuildConfig.BASE_URL + "/apps/" + packageName;

        new ApiTask(this, "GET", targetUrl, null, "Loading details...", new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject root = new JSONObject(response);

                    String appTitle = root.optString("title", "Unknown App");
                    tvTitle.setText(appTitle);

                    TextView topBarTitle = (TextView) findViewById(R.id.top_bar_title);
                    if (topBarTitle != null) {
                        topBarTitle.setText(appTitle);
                    }

                    tvDesc.setText(root.optString("description", "No description available."));

                    String iconUrl = root.optString("icon_url", "");
                    if (!TextUtils.isEmpty(iconUrl)) {
                        String fullImageUrl = BuildConfig.FILE_BASE_URL + "/buckets" + iconUrl;
                        ImageLoader.getInstance().displayImage(fullImageUrl, ivIcon);
                    } else {
                        ivIcon.setImageResource(android.R.drawable.sym_def_app_icon);
                    }

                    int maxVersionCode = -1;
                    JSONArray versionsArray = root.optJSONArray("versions");
                    if (versionsArray != null) {
                        adapter.clear();
                        for (int i = 0; i < versionsArray.length(); i++) {
                            JSONObject vObj = versionsArray.getJSONObject(i);
                            int vc = vObj.optInt("version_code", 0);
                            if (vc > maxVersionCode) {
                                maxVersionCode = vc;
                            }
                            adapter.add(new VersionModel(
                                    vObj.optString("id", ""),
                                    vObj.optString("app_id", ""),
                                    vObj.optString("version_name", ""),
                                    vc,
                                    vObj.optString("file_url", ""),
                                    vObj.optString("changelog", ""),
                                    vObj.optInt("min_sdk", 0),
                                    vObj.optInt("max_sdk", 0),
                                    vObj.optLong("created_at", 0)
                            ));
                        }
                    }

                    updateInstallState(packageName, maxVersionCode);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(AppDetailActivity.this, "Error parsing app details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                String displayMessage = errorMessage;
                if (errorMessage.contains("|")) {
                    displayMessage = errorMessage.substring(errorMessage.indexOf("|") + 1);
                }

                new AlertDialog.Builder(AppDetailActivity.this)
                        .setTitle("Error")
                        .setMessage(displayMessage)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setNegativeButton("Reload", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                fetchAppDetails(packageName);
                            }
                        })
                        .show();
            }
        }).execute();
    }

    /**
     * Created by ryomartin on 21/03/26.
     */

    public static class VersionAdapter extends ArrayAdapter<VersionModel> {
        public VersionAdapter(Context context, List<VersionModel> versions) {
            super(context, 0, versions);
        }

        private static class ViewHolder {
            TextView tvVersionName;
            TextView tvMinSdk;
            TextView tvChangelog;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            VersionModel version = getItem(position);
            ViewHolder viewHolder;

            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_version, parent, false);
            viewHolder.tvVersionName = (TextView) convertView.findViewById(R.id.tv_version_name);
            viewHolder.tvMinSdk = (TextView) convertView.findViewById(R.id.tv_min_sdk);
            viewHolder.tvChangelog = (TextView) convertView.findViewById(R.id.tv_changelog);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (version != null) {
                viewHolder.tvVersionName.setText("Version " + version.versionName + " (" + version.versionCode + ")");

                viewHolder.tvMinSdk.setText("Min SDK: " + version.minSdk);

                if (version.changelog != null && version.changelog.length() > 0) {
                    viewHolder.tvChangelog.setText(version.changelog);
                    viewHolder.tvChangelog.setVisibility(View.VISIBLE);
                } else  {
                    viewHolder.tvChangelog.setVisibility(View.GONE);
                }
            }

            return convertView;
        }
    }
}
