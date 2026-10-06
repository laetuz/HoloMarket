package id.neotica.holomarket.feature.settings.presenter;

import android.content.Context;

import org.json.JSONObject;

import id.neotica.holomarket.BuildConfig;
import id.neotica.holomarket.feature.settings.contract.SettingsView;
import id.neotica.holomarket.model.VersionModel;
import id.neotica.holomarket.network.ApiCallback;
import id.neotica.holomarket.network.ApiTask;
import id.neotica.holomarket.network.DownloadTask;
import id.neotica.holomarket.utils.AuthManager;

/**
 * MVP presenter owning the settings screen: profile, logout, adult toggle, and self-update check.
 * Rendering is delegated to a {@link SettingsView}.
 */
public class SettingsPresenter {

    private static final String SELF_PACKAGE = "id.neotica.holomarket";
    private static final String ADULT_PASSWORD = "adult";

    private final Context context;
    private final AuthManager authManager;

    private SettingsView view;
    private String latestFileName;
    private String latestDownloadUrl;

    public SettingsPresenter(Context context) {
        this.context = context;
        this.authManager = new AuthManager(context);
    }

    public void attach(SettingsView view) {
        this.view = view;
    }

    public void detach() {
        this.view = null;
    }

    public void load() {
        String username = authManager.getUsernameFromToken();
        if (view != null) {
            view.renderProfile(username != null ? username : "User");
            view.renderAdultContent(authManager.isAdultContentEnabled());
            view.renderVersion(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);
        }
    }

    public void logout() {
        authManager.clear();
        if (view != null) {
            view.navigateToMain();
        }
    }

    public void setAdultContent(boolean enabled) {
        authManager.saveAdultContentEnabled(enabled);
    }

    public boolean verifyAdultPassword(String password) {
        return ADULT_PASSWORD.equals(password);
    }

    public void checkForUpdates() {
        requestLatest(new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    VersionModel latest = VersionModel.fromJson(new JSONObject(response));
                    if (latest != null && latest.versionCode > BuildConfig.VERSION_CODE) {
                        latestFileName = "holomarket_v" + latest.versionName + ".apk";
                        latestDownloadUrl = BuildConfig.FILE_BASE_URL + latest.fileUrl;
                        if (view != null) {
                            view.showUpdateAvailable(latest.versionName);
                        }
                    } else if (view != null) {
                        view.showUpToDate();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (view != null) {
                        view.showMessage("Error checking for updates.");
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (view != null) {
                    view.showMessage("Failed to check for updates: " + errorMessage);
                }
            }
        });
    }

    public void downloadUpdate() {
        if (latestDownloadUrl == null) {
            if (view != null) {
                view.showMessage("No update available.");
            }
            return;
        }
        startDownload(latestFileName, latestDownloadUrl);
    }

    // --- I/O seams: overridden in tests to run synchronously ---

    void requestLatest(ApiCallback callback) {
        new ApiTask(context, "GET", BuildConfig.BASE_URL + "/apps/" + SELF_PACKAGE + "/latest",
                null, "Checking for updates...", callback).execute();
    }

    void startDownload(String fileName, String downloadUrl) {
        new DownloadTask(context, fileName).execute(downloadUrl);
    }
}