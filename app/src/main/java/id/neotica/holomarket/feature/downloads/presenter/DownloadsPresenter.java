package id.neotica.holomarket.feature.downloads.presenter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import id.neotica.holomarket.BuildConfig;
import id.neotica.holomarket.feature.downloads.contract.DownloadsView;
import id.neotica.holomarket.feature.downloads.domain.DownloadInfo;
import id.neotica.holomarket.feature.downloads.domain.InstalledApp;
import id.neotica.holomarket.feature.downloads.service.DownloadService;
import id.neotica.holomarket.network.ApiCallback;
import id.neotica.holomarket.network.ApiTask;

/**
 * MVP presenter for the downloads manager: reads the persisted task state,
 * forwards cancel requests to {@link DownloadService}, and resolves installed
 * store apps via {@code POST /apps/lookup}.
 */
public class DownloadsPresenter {

    private static final int MAX_NAMES_PER_LOOKUP = 500;
    private static final long SCAN_THROTTLE_MS = 60000L;

    private final Context context;

    private DownloadsView view;
    private long lastScanTime = 0L;

    public DownloadsPresenter(Context context) {
        this.context = context;
    }

    public void attach(DownloadsView view) {
        this.view = view;
    }

    public void detach() {
        this.view = null;
    }

    public void refresh() {
        List<DownloadInfo> tasks = new ArrayList<DownloadInfo>();
        try {
            SharedPreferences prefs = context.getSharedPreferences(DownloadService.PREFS_NAME, Context.MODE_PRIVATE);
            String json = prefs.getString(DownloadService.KEY_TASKS, "[]");
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                DownloadInfo info = DownloadInfo.fromJson(arr.optJSONObject(i));
                if (info != null) {
                    tasks.add(info);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (view != null) {
            view.renderTasks(tasks);
        }
    }

    public void cancel(String packageName) {
        sendCancel(packageName);
    }

    /**
     * Looks up which installed packages exist in the store and splits them into
     * "available updates" and "installed" lists. Silent: no loading dialog.
     */
    public void loadInstalledApps() {
        lastScanTime = System.currentTimeMillis();

        final Map<String, Integer> installed = getInstalledVersions();
        if (installed.isEmpty()) {
            if (view != null) {
                view.renderInstalledApps(new ArrayList<InstalledApp>(), new ArrayList<InstalledApp>());
            }
            return;
        }

        final List<String> names = new ArrayList<String>(installed.keySet());
        final List<InstalledApp> updates = new ArrayList<InstalledApp>();
        final List<InstalledApp> installedApps = new ArrayList<InstalledApp>();
        final int chunkCount = (names.size() + MAX_NAMES_PER_LOOKUP - 1) / MAX_NAMES_PER_LOOKUP;
        final AtomicInteger remaining = new AtomicInteger(chunkCount);

        for (int chunk = 0; chunk < chunkCount; chunk++) {
            requestLookup(buildLookupBody(names, chunk), new ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    parseLookup(response, installed, updates, installedApps);
                    if (remaining.decrementAndGet() == 0) {
                        renderInstalledApps(updates, installedApps);
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    if (remaining.decrementAndGet() == 0) {
                        renderInstalledApps(updates, installedApps);
                    }
                }
            });
        }
    }

    /**
     * Re-scans only if the last lookup is older than the throttle window
     * (the endpoint is rate-limited to 60/min).
     */
    public void loadInstalledAppsIfStale() {
        if (System.currentTimeMillis() - lastScanTime >= SCAN_THROTTLE_MS) {
            loadInstalledApps();
        }
    }

    private String buildLookupBody(List<String> names, int chunk) {
        JSONArray array = new JSONArray();
        int start = chunk * MAX_NAMES_PER_LOOKUP;
        int end = Math.min(names.size(), start + MAX_NAMES_PER_LOOKUP);
        for (int i = start; i < end; i++) {
            array.put(names.get(i));
        }

        JSONObject payload = new JSONObject();
        try {
            payload.put("package_names", array);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return payload.toString();
    }

    private void parseLookup(String response, Map<String, Integer> installed,
                             List<InstalledApp> updates, List<InstalledApp> installedApps) {
        try {
            JSONArray arr = new JSONArray(response);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null) {
                    continue;
                }
                String pkg = o.optString("package_name", "");
                Integer installedVersion = installed.get(pkg);
                if (pkg.length() == 0 || installedVersion == null) {
                    continue;
                }
                InstalledApp app = InstalledApp.fromJson(o, installedVersion.intValue());
                if (app == null) {
                    continue;
                }
                if (app.hasUpdate()) {
                    updates.add(app);
                } else {
                    installedApps.add(app);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderInstalledApps(List<InstalledApp> updates, List<InstalledApp> installedApps) {
        sortByTitle(updates);
        sortByTitle(installedApps);
        if (view != null) {
            view.renderInstalledApps(updates, installedApps);
        }
    }

    private static void sortByTitle(List<InstalledApp> apps) {
        Collections.sort(apps, new Comparator<InstalledApp>() {
            @Override
            public int compare(InstalledApp a, InstalledApp b) {
                String titleA = a.title == null ? "" : a.title;
                String titleB = b.title == null ? "" : b.title;
                return titleA.compareToIgnoreCase(titleB);
            }
        });
    }

    // --- I/O seams: overridden in tests ---

    Map<String, Integer> getInstalledVersions() {
        Map<String, Integer> versions = new HashMap<String, Integer>();
        try {
            PackageManager pm = context.getPackageManager();
            List<PackageInfo> packages = pm.getInstalledPackages(0);
            for (int i = 0; i < packages.size(); i++) {
                PackageInfo info = packages.get(i);
                if (info != null && info.packageName != null) {
                    versions.put(info.packageName, info.versionCode);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versions;
    }

    void requestLookup(String jsonBody, ApiCallback callback) {
        new ApiTask(context, "POST", BuildConfig.BASE_URL + "/apps/lookup", jsonBody, null, callback).execute();
    }

    void sendCancel(String packageName) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(DownloadService.ACTION_CANCEL);
        intent.putExtra(DownloadService.EXTRA_PACKAGE, packageName);
        context.startService(intent);
    }
}