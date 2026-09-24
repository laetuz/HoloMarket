package id.neotica.holomarket.feature.detail.presenter;

import android.content.Context;

import org.json.JSONObject;

import id.neotica.holomarket.BuildConfig;
import id.neotica.holomarket.feature.detail.domain.AppDetailModel;
import id.neotica.holomarket.model.VersionModel;
import id.neotica.holomarket.network.AnalyticsTracker;
import id.neotica.holomarket.network.ApiCallback;
import id.neotica.holomarket.network.ApiTask;
import id.neotica.holomarket.network.DownloadTask;
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
                    view.renderInstallState(installState);
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
        startDownload(fileName, appTitle, downloadUrl);
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

    void startDownload(String fileName, String appTitle, String downloadUrl) {
        new DownloadTask(context, fileName, appTitle).execute(downloadUrl);
    }
}