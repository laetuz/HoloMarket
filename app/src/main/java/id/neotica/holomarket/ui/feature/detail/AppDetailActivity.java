package id.neotica.holomarket.ui.feature.detail;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import id.neotica.holomarket.model.VersionModel;
import id.neotica.holomarket.network.AnalyticsTracker;
import id.neotica.holomarket.network.ApiCallback;
import id.neotica.holomarket.network.ApiTask;
import id.neotica.holomarket.network.DownloadTask;
import id.neotica.holomarket.utils.ImageUrlHelper;
import id.neotica.holomarket.utils.TopBarHelper;

public class AppDetailActivity extends Activity {

    private TextView tvTitle, tvDesc, tvDeveloper, tvCategories, tvScreenshotsLabel, tvReadMore;
    private ImageView ivIcon;
    private LinearLayout llVersions;
    private List<VersionModel> versionList;
    private HorizontalScrollView hsvScreenshots;
    private LinearLayout llScreenshots;
    private Button btDownload;
    private List<String> previewScreenshots = new ArrayList<String>();
    private boolean descExpanded = false;
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
        tvDeveloper = (TextView) findViewById(R.id.tv_detail_developer);
        tvCategories = (TextView) findViewById(R.id.tv_detail_categories);
        ivIcon = (ImageView) findViewById(R.id.iv_detail_icon);
        llVersions = (LinearLayout) findViewById(R.id.ll_versions);
        btDownload = (Button) findViewById(R.id.bt_download);
        tvScreenshotsLabel = (TextView) findViewById(R.id.tv_screenshots_label);
        hsvScreenshots = (HorizontalScrollView) findViewById(R.id.hsv_screenshots);
        llScreenshots = (LinearLayout) findViewById(R.id.ll_screenshots);
        tvReadMore = (TextView) findViewById(R.id.tv_read_more);
        versionList = new ArrayList<VersionModel>();

        tvReadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (descExpanded) {
                    tvDesc.setMaxLines(3);
                    tvDesc.setEllipsize(TextUtils.TruncateAt.END);
                    tvReadMore.setText("Read more");
                } else {
                    tvDesc.setMaxLines(Integer.MAX_VALUE);
                    tvDesc.setEllipsize(null);
                    tvReadMore.setText("Show less");
                }
                descExpanded = !descExpanded;
            }
        });

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

                if (versionList.size() == 0) {
                    Toast.makeText(AppDetailActivity.this, "No versions available.", Toast.LENGTH_SHORT).show();
                    return;
                }

                VersionModel latestVersion = null;
                int maxVersionCode = -1;

                for (int i = 0; i < versionList.size(); i++) {
                    VersionModel current = versionList.get(i);
                    if (current != null && current.versionCode > maxVersionCode) {
                        maxVersionCode = current.versionCode;
                        latestVersion = current;
                    }
                }

                if (latestVersion != null) {
                    downloadVersion(latestVersion);
                } else {
                    Toast.makeText(AppDetailActivity.this, "Download link not available for the latest version.", Toast.LENGTH_SHORT).show();
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
                    tvDesc.post(new Runnable() {
                        @Override
                        public void run() {
                            if (tvDesc.getLineCount() > 3) {
                                tvDesc.setMaxLines(3);
                                tvDesc.setEllipsize(TextUtils.TruncateAt.END);
                                tvReadMore.setVisibility(View.VISIBLE);
                            } else {
                                tvReadMore.setVisibility(View.GONE);
                            }
                        }
                    });

                    String iconUrl = root.optString("icon_url", "");
                    if (!TextUtils.isEmpty(iconUrl)) {
                        String fullImageUrl = ImageUrlHelper.build(iconUrl);
                        ImageLoader.getInstance().displayImage(fullImageUrl, ivIcon);
                    } else {
                        ivIcon.setImageResource(android.R.drawable.sym_def_app_icon);
                    }

                    String developer = root.optString("developer", "");
                    if (developer != null && developer.length() > 0) {
                        tvDeveloper.setText(developer);
                        tvDeveloper.setVisibility(View.VISIBLE);
                    } else {
                        tvDeveloper.setVisibility(View.GONE);
                    }

                    List<String> categoriesList = new ArrayList<String>();
                    JSONArray categoriesArray = root.optJSONArray("categories");
                    if (categoriesArray != null) {
                        for (int i = 0; i < categoriesArray.length(); i++) {
                            String slug = categoriesArray.optString(i);
                            if (slug != null && slug.length() > 0) {
                                categoriesList.add(slug);
                            }
                        }
                    }
                    if (categoriesList.size() == 0) {
                        String category = root.optString("category", "");
                        if (category != null && category.length() > 0) {
                            categoriesList.add(category);
                        }
                    }
                    if (categoriesList.size() > 0) {
                        tvCategories.setText("Categories: " + join(categoriesList));
                        tvCategories.setVisibility(View.VISIBLE);
                    } else {
                        tvCategories.setVisibility(View.GONE);
                    }

                    List<String> screenshotsList = new ArrayList<String>();
                    JSONArray screenshotsArray = root.optJSONArray("screenshots");
                    if (screenshotsArray != null) {
                        for (int i = 0; i < screenshotsArray.length(); i++) {
                            String shot = screenshotsArray.optString(i);
                            if (shot != null && shot.length() > 0) {
                                screenshotsList.add(shot);
                            }
                        }
                    }
                    renderScreenshots(screenshotsList);

                    int maxVersionCode = -1;
                    JSONArray versionsArray = root.optJSONArray("versions");
                    if (versionsArray != null) {
                        for (int i = 0; i < versionsArray.length(); i++) {
                            int vc = versionsArray.getJSONObject(i).optInt("version_code", 0);
                            if (vc > maxVersionCode) {
                                maxVersionCode = vc;
                            }
                        }
                    }

                    renderVersions(versionsArray);
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

    private void downloadVersion(VersionModel version) {
        if (version != null && version.fileUrl != null && version.fileUrl.length() > 0) {
            String downloadUrl = BuildConfig.FILE_BASE_URL + version.fileUrl;

            String fileName = version.fileUrl.substring(version.fileUrl.lastIndexOf('/') + 1);

            if (fileName.length() == 0 || !fileName.endsWith(".apk")) {
                fileName = "update_v" + version.versionCode + ".apk";
            }

            AnalyticsTracker.track(AppDetailActivity.this, "download", "app_downloaded");

            String appTitle = tvTitle.getText().toString();
            new DownloadTask(AppDetailActivity.this, fileName, appTitle).execute(downloadUrl);
        } else {
            Toast.makeText(AppDetailActivity.this, "Download link not available for this version.", Toast.LENGTH_SHORT).show();
        }
    }

    private void renderVersions(JSONArray versionsArray) {
        llVersions.removeAllViews();
        versionList.clear();

        if (versionsArray == null) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < versionsArray.length(); i++) {
            final VersionModel vm;
            try {
                JSONObject vObj = versionsArray.getJSONObject(i);
                vm = new VersionModel(
                        vObj.optString("id", ""),
                        vObj.optString("app_id", ""),
                        vObj.optString("version_name", ""),
                        vObj.optInt("version_code", 0),
                        vObj.optString("file_url", ""),
                        vObj.optString("changelog", ""),
                        vObj.optInt("min_sdk", 0),
                        vObj.optInt("max_sdk", 0),
                        vObj.optLong("created_at", 0)
                );
            } catch (JSONException e) {
                continue;
            }
            versionList.add(vm);

            View row = inflater.inflate(R.layout.item_version, llVersions, false);
            TextView tvVersionName = (TextView) row.findViewById(R.id.tv_version_name);
            TextView tvMinSdk = (TextView) row.findViewById(R.id.tv_min_sdk);
            TextView tvChangelog = (TextView) row.findViewById(R.id.tv_changelog);
            Button btnVersionDownload = (Button) row.findViewById(R.id.btn_version_download);

            tvVersionName.setText("Version " + vm.versionName + " (" + vm.versionCode + ")");
            tvMinSdk.setText("Min SDK: " + vm.minSdk);

            if (vm.changelog != null && vm.changelog.length() > 0) {
                tvChangelog.setText(vm.changelog);
                tvChangelog.setVisibility(View.VISIBLE);
            } else {
                tvChangelog.setVisibility(View.GONE);
            }

            btnVersionDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    downloadVersion(vm);
                }
            });

            llVersions.addView(row);
        }
    }

    private String join(List<String> items) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(items.get(i));
        }
        return sb.toString();
    }

    private void renderScreenshots(List<String> urls) {
        previewScreenshots = urls;
        llScreenshots.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < urls.size(); i++) {
            final int index = i;
            ImageView imageView = (ImageView) inflater.inflate(R.layout.item_screenshot, llScreenshots, false);
            String fullImageUrl = ImageUrlHelper.build(urls.get(i));
            if (!TextUtils.isEmpty(fullImageUrl)) {
                ImageLoader.getInstance().displayImage(fullImageUrl, imageView);
            } else {
                imageView.setImageResource(android.R.drawable.picture_frame);
            }
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showScreenshotPreview(index);
                }
            });
            llScreenshots.addView(imageView);
        }
        boolean show = urls.size() > 0;
        tvScreenshotsLabel.setVisibility(show ? View.VISIBLE : View.GONE);
        hsvScreenshots.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private class PreviewGalleryAdapter extends BaseAdapter {
        private final List<String> screenshotUrls;

        PreviewGalleryAdapter(List<String> screenshotUrls) {
            this.screenshotUrls = screenshotUrls;
        }

        @Override
        public int getCount() {
            return screenshotUrls.size();
        }

        @Override
        public Object getItem(int position) {
            return screenshotUrls.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(AppDetailActivity.this);
                imageView = (ImageView) inflater.inflate(R.layout.item_screenshot_preview, parent, false);
            } else {
                imageView = (ImageView) convertView;
            }

            String fullImageUrl = ImageUrlHelper.build(screenshotUrls.get(position));
            if (!TextUtils.isEmpty(fullImageUrl)) {
                ImageLoader.getInstance().displayImage(fullImageUrl, imageView);
            } else {
                imageView.setImageResource(android.R.drawable.picture_frame);
            }
            return imageView;
        }
    }

    private void showScreenshotPreview(int position) {
        final Dialog dialog = new Dialog(this);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_screenshot_preview);

        final Gallery galleryPreview = (Gallery) dialog.findViewById(R.id.gallery_preview);
        galleryPreview.setAdapter(new PreviewGalleryAdapter(previewScreenshots));
        galleryPreview.setSelection(position);

        galleryPreview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                galleryPreview.setSelection(pos);
            }
        });

        dialog.findViewById(R.id.iv_close_preview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.copyFrom(dialog.getWindow().getAttributes());
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(params);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));

        dialog.show();
    }
}
