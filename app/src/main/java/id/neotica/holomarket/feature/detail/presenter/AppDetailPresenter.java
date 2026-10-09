package id.neotica.holomarket.feature.detail.presenter;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import id.neotica.holomarket.BuildConfig;
import id.neotica.holomarket.feature.detail.domain.AppDetailModel;
import id.neotica.holomarket.model.VersionModel;
import id.neotica.holomarket.network.AnalyticsTracker;
import id.neotica.holomarket.network.ApiCallback;
import id.neotica.holomarket.network.ApiTask;
import id.neotica.holomarket.feature.downloads.DownloadStarter;
import id.neotica.holomarket.feature.downloads.domain.DownloadInfo;
import id.neotica.holomarket.feature.downloads.service.DownloadService;
import id.neotica.holomarket.feature.detail.contract.AppDetailView;
import id.neotica.holomarket.feature.detail.domain.InstallState;

/**
 * MVP presenter owning the app-detail fetch/parse, install state, and download logic.
 * Rendering is delegated to an {@link AppDetailView}; networking stays here.
 */
public class AppDetailPresenter {

    private final Context context;

    private AppDetailView view;
    private String packageName;
    private AppDetailModel detail;
    private InstallState installState = InstallState.DOWNLOAD;

    public AppDetailPresenter(Context context) {
        this.context = context;
    }

    public void attach(AppDetailView view) {
        this.view = view;
    }

    public void detach() {
        this.view = null;
    }

    public void load(String packageName) {
        this.packageName = packageName;
        if (view != null) {
            view.showLoading();
        }

        requestAppDetail(packageName, new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject root = new JSONObject(response);
                    detail = AppDetailModel.fromJson(AppDetailPresenter.this.packageName, root);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (view != null) {
                        view.showLoadError("Error parsing app details.");
                    }
                    return;
                }

                installState = computeInstallState(detail);
                if (view != null) {
                    view.renderAppDetail(detail);
                    refreshDownloadState();
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (view != null) {
                    view.showLoadError(extractMessage(errorMessage));
                }
            }
        });
    }

    /**
     * Primary Download/Update/Open button.
     */
    public void onDownloadClicked() {
        if (installState == InstallState.OPEN && packageName != null && packageName.length() > 0) {
            if (view != null) {
                view.openApp(packageName);
            }
            return;
        }

        if (detail == null || detail.versions.size() == 0) {
            if (view != null) {
                view.showNoVersions();
            }
            return;
        }

        VersionModel latest = latestVersion();
        if (latest != null && latest.fileUrl != null && latest.fileUrl.length() > 0) {
            downloadVersion(latest);
        } else {
            if (view != null) {
                view.showNoDownloadLink();
            }
        }
    }

    /**
     * Per-version Download button.
     */
    public void downloadVersion(VersionModel version) {
        if (version == null || version.fileUrl == null || version.fileUrl.length() == 0) {
            if (view != null) {
                view.showNoDownloadLink();
            }
            return;
        }

        String downloadUrl = BuildConfig.FILE_BASE_URL + version.fileUrl;

        String fileName = version.fileUrl.substring(version.fileUrl.lastIndexOf('/') + 1);
        if (fileName.length() == 0 || !fileName.endsWith(".apk")) {
            fileName = "update_v" + version.versionCode + ".apk";
        }

        AnalyticsTracker.track(context, "download", "app_downloaded");

        String appTitle = (detail != null && detail.title != null) ? detail.title : "App";
        String icon = (detail != null && detail.iconUrl != null) ? detail.iconUrl : "";
        startDownload(packageName, fileName, appTitle, icon, downloadUrl);

        if (view != null) {
            view.showDownloadProgress(0, null, true);
        }
    }

    /**
     * Reconciles the download UI with the persisted task state for this package.
     * Called on resume and on every {@link DownloadService} progress broadcast.
     */
    public void refreshDownloadState() {
        if (packageName == null || packageName.length() == 0 || view == null) {
            return;
        }

        DownloadInfo task = readDownloadTask(packageName);
        if (task != null) {
            view.showDownloadProgress(task.percent, task.statusText, task.installing);
            return;
        }

        view.hideDownloadProgress();
        if (detail != null) {
            installState = computeInstallState(detail);
            view.renderInstallState(installState);
        }
    }

    /**
     * Cancel button next to the inline download progress.
     */
    public void cancelDownload() {
        if (packageName != null && packageName.length() > 0) {
            sendCancel(packageName);
        }
    }

    private VersionModel latestVersion() {
        if (detail == null) {
            return null;
        }
        VersionModel latest = null;
        int maxVersionCode = -1;
        for (int i = 0; i < detail.versions.size(); i++) {
            VersionModel current = detail.versions.get(i);
            if (current != null && current.versionCode > maxVersionCode) {
                maxVersionCode = current.versionCode;
                latest = current;
            }
        }
        return latest;
    }

    private InstallState computeInstallState(AppDetailModel detail) {
        int latestVersionCode = detail.latestVersionCode();
        int installedVersion = getInstalledVersionCode(detail.packageName);
        if (installedVersion < 0) {
            return InstallState.DOWNLOAD;
        } else if (installedVersion >= latestVersionCode) {
            return InstallState.OPEN;
        } else {
            return InstallState.UPDATE;
        }
    }

    private String extractMessage(String errorMessage) {
        if (errorMessage != null && errorMessage.contains("|")) {
            return errorMessage.substring(errorMessage.indexOf("|") + 1);
        }
        return errorMessage;
    }

    // --- I/O seams: overridden in tests to run synchronously ---

    void requestAppDetail(String pkg, ApiCallback callback) {
        String url = BuildConfig.BASE_URL + "/apps/" + pkg;
        new ApiTask(context, "GET", url, null, "Loading details...", callback).execute();
    }

    int getInstalledVersionCode(String pkg) {
        try {
            return context.getPackageManager().getPackageInfo(pkg, 0).versionCode;
        } catch (Exception e) {
            return -1;
        }
    }

    void startDownload(String pkg, String fileName, String appTitle, String icon, String downloadUrl) {
        DownloadStarter.start(context, pkg, fileName, appTitle, icon, downloadUrl);
    }

    DownloadInfo readDownloadTask(String pkg) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(DownloadService.PREFS_NAME, Context.MODE_PRIVATE);
            String json = prefs.getString(DownloadService.KEY_TASKS, "[]");
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                DownloadInfo info = DownloadInfo.fromJson(arr.optJSONObject(i));
                if (info != null && pkg.equals(info.packageName)) {
                    return info;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    void sendCancel(String pkg) {
        DownloadStarter.cancel(context, pkg);
    }
}