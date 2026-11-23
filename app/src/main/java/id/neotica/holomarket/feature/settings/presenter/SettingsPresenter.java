package id.neotica.holomarket.feature.settings.presenter;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONObject;

import id.neotica.holomarket.BuildConfig;
import id.neotica.holomarket.feature.settings.contract.SettingsView;
import id.neotica.holomarket.model.VersionModel;
import id.neotica.holomarket.network.ApiCallback;
import id.neotica.holomarket.network.ApiTask;
import id.neotica.holomarket.feature.downloads.DownloadStarter;
import id.neotica.holomarket.utils.AppPrefs;
import id.neotica.holomarket.utils.AuthManager;
import id.neotica.holomarket.utils.CacheCleaner;

/**
 * MVP presenter owning the settings screen: profile, logout, adult toggle, cache clearing,
 * and self-update check. Rendering is delegated to a {@link SettingsView}.
 */
public class SettingsPresenter {

    public interface CacheClearCallback {
        void onCacheCleared();
    }

    public interface CacheSizeCallback {
        void onCacheSize(String text);
    }

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
            view.renderRootAutoInstall(AppPrefs.isRootAutoInstallEnabled(context));
            view.renderVersion(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);
        }
        refreshCacheSize();
    }

    public void clearCache() {
        clearCachesAsync(new CacheClearCallback() {
            @Override
            public void onCacheCleared() {
                if (view != null) {
                    view.showMessage("Cache cleared");
                }
                refreshCacheSize();
            }
        });
    }

    public void refreshCacheSize() {
        cacheSizeAsync(new CacheSizeCallback() {
            @Override
            public void onCacheSize(String text) {
                if (view != null) {
                    view.renderCacheSize(text);
                }
            }
        });
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

    public void setRootAutoInstall(boolean enabled) {
        AppPrefs.setRootAutoInstallEnabled(context, enabled);
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
        startDownload(SELF_PACKAGE, latestFileName, "HoloMarket", "", latestDownloadUrl);
    }

    // --- I/O seams: overridden in tests to run synchronously ---

    void requestLatest(ApiCallback callback) {
        new ApiTask(context, "GET", BuildConfig.BASE_URL + "/apps/" + SELF_PACKAGE + "/latest",
                null, "Checking for updates...", callback).execute();
    }

    void startDownload(String pkg, String fileName, String appTitle, String icon, String downloadUrl) {
        DownloadStarter.start(context, pkg, fileName, appTitle, icon, downloadUrl);
    }

    void clearCachesAsync(CacheClearCallback callback) {
        new ClearCacheTask(context, callback).execute();
    }

    void cacheSizeAsync(CacheSizeCallback callback) {
        new CacheSizeTask(context, callback).execute();
    }

    private static class ClearCacheTask extends AsyncTask<Void, Void, Void> {
        private final Context context;
        private final CacheClearCallback callback;

        ClearCacheTask(Context context, CacheClearCallback callback) {
            this.context = context;
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(Void... params) {
            CacheCleaner.clear(context);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            callback.onCacheCleared();
        }
    }

    private static class CacheSizeTask extends AsyncTask<Void, Void, String> {
        private final Context context;
        private final CacheSizeCallback callback;

        CacheSizeTask(Context context, CacheSizeCallback callback) {
            this.context = context;
            this.callback = callback;
        }

        @Override
        protected String doInBackground(Void... params) {
            return CacheCleaner.formatSize(context, CacheCleaner.size(context));
        }

        @Override
        protected void onPostExecute(String text) {
            callback.onCacheSize(text);
        }
    }
}