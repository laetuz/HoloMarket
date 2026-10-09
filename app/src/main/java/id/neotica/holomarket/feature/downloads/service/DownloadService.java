package id.neotica.holomarket.feature.downloads.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import id.neotica.holomarket.R;
import id.neotica.holomarket.feature.downloads.ui.DownloadsActivity;
import id.neotica.holomarket.utils.AppPrefs;

/**
 * Foreground download service: concurrent per-app APK downloads with progress
 * notifications, persisted task state, cancel support, and auto-install.
 *
 * Installs are user-confirmed via the system package installer, except when the
 * optional "root auto-install" setting is enabled and {@code su} is available.
 */
public class DownloadService extends Service {

    public static final String ACTION_START = "id.neotica.holomarket.DOWNLOAD_START";
    public static final String ACTION_CANCEL = "id.neotica.holomarket.DOWNLOAD_CANCEL";
    public static final String ACTION_PROGRESS = "id.neotica.holomarket.DOWNLOAD_PROGRESS";

    public static final String EXTRA_PACKAGE = "package";
    public static final String EXTRA_OPEN_PACKAGE = "PACKAGE_NAME";
    public static final String EXTRA_URL = "url";
    public static final String EXTRA_FILE_NAME = "file_name";
    public static final String EXTRA_APP_NAME = "app_name";
    public static final String EXTRA_ICON = "icon";
    public static final String EXTRA_DONE = "done";
    public static final String EXTRA_ERROR = "error";
    public static final String EXTRA_CANCELLED = "cancelled";

    public static final String PREFS_NAME = "DownloadState";
    public static final String KEY_TASKS = "tasks_json";
    private static final String APK_DIR = "NeoStore";

    private static class TaskInfo {
        String packageName = "";
        String appName = "";
        String icon = "";
        volatile boolean cancel = false;
        int percent = 0;
        long speed = 0;
        boolean installing = false;
        String statusText = "0%";
        String fileName = "";
        String filePath = "";
    }

    private static final ConcurrentHashMap<String, TaskInfo> TASKS =
            new ConcurrentHashMap<String, TaskInfo>();

    private boolean foregroundStarted = false;

    private static final Class<?>[] START_FOREGROUND_SIG = new Class[] {
            int.class, Notification.class
    };
    private static final Class<?>[] STOP_FOREGROUND_SIG = new Class[] {
            boolean.class
    };

    private Method mStartForeground;
    private Method mStopForeground;
    private Method mSetForeground;

    private final Object[] mStartForegroundArgs = new Object[2];
    private final Object[] mStopForegroundArgs = new Object[1];
    private final Object[] mSetForegroundArgs = new Object[1];

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            mStartForeground = getClass().getMethod("startForeground", START_FOREGROUND_SIG);
            mStopForeground = getClass().getMethod("stopForeground", STOP_FOREGROUND_SIG);
        } catch (NoSuchMethodException e) {
            mStartForeground = null;
            mStopForeground = null;
        }

        try {
            mSetForeground = getClass().getMethod("setForeground", new Class[] { boolean.class });
        } catch (NoSuchMethodException e) {
            mSetForeground = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_NOT_STICKY;
        }
        String action = intent.getAction();

        if (ACTION_CANCEL.equals(action)) {
            String pkg = intent.getStringExtra(EXTRA_PACKAGE);
            if (pkg != null) {
                TaskInfo t = TASKS.get(pkg);
                if (t != null) {
                    t.cancel = true;
                }
            }
            return START_NOT_STICKY;
        }

        if (ACTION_START.equals(action)) {
            final String pkg = intent.getStringExtra(EXTRA_PACKAGE);
            final String url = intent.getStringExtra(EXTRA_URL);
            final String fileName = intent.getStringExtra(EXTRA_FILE_NAME);
            final String appName = intent.getStringExtra(EXTRA_APP_NAME);
            final String icon = intent.getStringExtra(EXTRA_ICON);

            if (pkg == null || pkg.length() == 0 || url == null || url.length() == 0) {
                return START_NOT_STICKY;
            }
            if (TASKS.containsKey(pkg)) {
                return START_STICKY;
            }

            final TaskInfo t = new TaskInfo();
            t.packageName = pkg;
            t.appName = appName == null ? "" : appName;
            t.icon = icon == null ? "" : icon;
            t.fileName = fileName == null ? (pkg.replace('.', '_') + ".apk") : fileName;

            TASKS.put(pkg, t);
            persistTasks();
            sendStateBroadcast(t, false, false, false);

            new Thread(new Runnable() {
                public void run() {
                    runDownload(t, url);
                }
            }).start();
        }

        return START_STICKY;
    }

    private SharedPreferences prefs() {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private synchronized void persistTasks() {
        JSONArray arr = new JSONArray();
        try {
            for (Map.Entry<String, TaskInfo> e : TASKS.entrySet()) {
                TaskInfo t = e.getValue();
                JSONObject o = new JSONObject();
                o.put("package", t.packageName);
                o.put("app_name", t.appName);
                o.put("icon", t.icon);
                o.put("percent", t.percent);
                o.put("speed_bps", t.speed);
                o.put("installing", t.installing);
                o.put("status_text", t.statusText);
                o.put("file_name", t.fileName);
                o.put("file_path", t.filePath);
                arr.put(o);
            }
        } catch (Exception ex) {
        }
        prefs().edit().putString(KEY_TASKS, arr.toString()).commit();
    }

    private void sendStateBroadcast(TaskInfo t, boolean done, boolean error, boolean cancelled) {
        Intent p = new Intent(ACTION_PROGRESS);
        p.putExtra(EXTRA_PACKAGE, t.packageName);
        p.putExtra("percent", t.percent);
        p.putExtra("speed_bps", t.speed);
        p.putExtra(EXTRA_DONE, done);
        p.putExtra(EXTRA_ERROR, error);
        p.putExtra(EXTRA_CANCELLED, cancelled);
        p.putExtra("active", !(done || error || cancelled));
        p.putExtra("installing", t.installing);
        p.putExtra(EXTRA_APP_NAME, t.appName);
        p.putExtra("status_text", t.statusText);
        p.putExtra(EXTRA_ICON, t.icon);
        if (t.filePath != null && t.filePath.length() > 0) {
            p.putExtra("file_path", t.filePath);
        }
        sendBroadcast(p);
    }

    private File outFile(String fileName) {
        File dir;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            dir = new File(Environment.getExternalStorageDirectory(), APK_DIR);
        } else {
            dir = getCacheDir();
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, fileName);
    }

    private int progressNotifId(TaskInfo t) {
        return 1000 + (t.packageName.hashCode() & 0x7fffffff) % 100000;
    }

    private int completeNotifId(TaskInfo t) {
        return 2000 + (t.packageName.hashCode() & 0x7fffffff) % 100000;
    }

    /**
     * Opens the downloads manager, which then forwards to the app detail screen,
     * so "back" from the detail returns to the downloads list.
     */
    private PendingIntent detailPendingIntent(TaskInfo t) {
        Intent open = new Intent(this, DownloadsActivity.class);
        open.putExtra(EXTRA_OPEN_PACKAGE, t.packageName);
        open.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(this, progressNotifId(t), open, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent completePendingIntent(TaskInfo t) {
        Intent open = new Intent(Intent.ACTION_VIEW);
        open.setDataAndType(Uri.fromFile(new File(t.filePath)), "application/vnd.android.package-archive");
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(this, completeNotifId(t), open, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent cancelPendingIntent(TaskInfo t) {
        Intent cancel = new Intent(this, DownloadService.class);
        cancel.setAction(ACTION_CANCEL);
        cancel.putExtra(EXTRA_PACKAGE, t.packageName);
        return PendingIntent.getService(this, progressNotifId(t), cancel, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Notification makeProgressNotification(TaskInfo t) {
        Notification n = new Notification(android.R.drawable.stat_sys_download, null, System.currentTimeMillis());
        n.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_ONLY_ALERT_ONCE;
        n.contentIntent = detailPendingIntent(t);

        RemoteViews rv = new RemoteViews(getPackageName(), R.layout.notification_download);
        rv.setTextViewText(R.id.notif_title,
                t.appName == null || t.appName.length() == 0 ? getString(R.string.downloads_downloading) : t.appName);
        rv.setTextViewText(R.id.notif_text, t.statusText);
        rv.setProgressBar(R.id.notif_progress, 100, Math.max(0, Math.min(100, t.percent)), t.installing);
        rv.setOnClickPendingIntent(R.id.notif_cancel, cancelPendingIntent(t));
        n.contentView = rv;
        return n;
    }

    private Notification makeSimpleNotification(String title, String text, PendingIntent pi, int iconRes) {
        Notification n = new Notification(iconRes, null, System.currentTimeMillis());
        n.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONLY_ALERT_ONCE;
        n.contentIntent = pi;

        RemoteViews rv = new RemoteViews(getPackageName(), R.layout.notification_simple);
        rv.setTextViewText(R.id.notif_title, title);
        rv.setTextViewText(R.id.notif_text, text);
        n.contentView = rv;
        return n;
    }

    private void compatStartForeground(int id, Notification notification) {
        if (mStartForeground != null) {
            mStartForegroundArgs[0] = Integer.valueOf(id);
            mStartForegroundArgs[1] = notification;
            try {
                mStartForeground.invoke(this, mStartForegroundArgs);
                return;
            } catch (Exception e) {
            }
        }

        if (mSetForeground != null) {
            try {
                mSetForegroundArgs[0] = Boolean.TRUE;
                mSetForeground.invoke(this, mSetForegroundArgs);
            } catch (Exception e) {
            }
        }

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(id, notification);
    }

    private void compatStopForeground() {
        if (mStopForeground != null) {
            mStopForegroundArgs[0] = Boolean.TRUE;
            try {
                mStopForeground.invoke(this, mStopForegroundArgs);
                return;
            } catch (Exception e) {
            }
        }

        if (mSetForeground != null) {
            try {
                mSetForegroundArgs[0] = Boolean.FALSE;
                mSetForeground.invoke(this, mSetForegroundArgs);
            } catch (Exception e) {
            }
        }
    }

    private void notifyProgress(TaskInfo t) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification n = makeProgressNotification(t);
        nm.notify(progressNotifId(t), n);
        if (!foregroundStarted) {
            compatStartForeground(progressNotifId(t), n);
            foregroundStarted = true;
        }
    }

    private void notifyComplete(TaskInfo t) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(
                completeNotifId(t),
                makeSimpleNotification(
                        getString(R.string.downloads_complete),
                        t.appName == null || t.appName.length() == 0
                                ? getString(R.string.downloads_complete)
                                : t.appName,
                        completePendingIntent(t),
                        android.R.drawable.stat_sys_download_done
                )
        );
    }

    private void notifyInstalled(TaskInfo t) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String text = (t.appName == null || t.appName.length() == 0)
                ? getString(R.string.downloads_installed)
                : getString(R.string.downloads_installed_named, t.appName);

        nm.notify(
                completeNotifId(t),
                makeSimpleNotification(
                        getString(R.string.downloads_installed),
                        text,
                        detailPendingIntent(t),
                        android.R.drawable.stat_sys_download_done
                )
        );
    }

    private void launchPackageInstaller(String filePath) {
        try {
            Intent open = new Intent(Intent.ACTION_VIEW);
            open.setDataAndType(Uri.fromFile(new File(filePath)), "application/vnd.android.package-archive");
            open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(open);
        } catch (Exception e) {
        }
    }

    private void runDownload(TaskInfo t, String urlStr) {
        HttpURLConnection conn = null;
        InputStream in = null;
        FileOutputStream out = null;
        File f = outFile(t.fileName);

        long lastNotifyTime = 0L;
        int lastNotifyPercent = -1;
        int lastBroadcastPercent = -1;

        try {
            notifyProgress(t);

            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout(12000);
            conn.setReadTimeout(30000);
            conn.connect();

            int len = conn.getContentLength();
            in = conn.getInputStream();
            out = new FileOutputStream(f);

            byte[] buf = new byte[8192];
            long total = 0;
            long speedWindowStartTime = System.currentTimeMillis();
            long speedWindowStartBytes = 0;

            int r;
            while ((r = in.read(buf)) != -1) {
                if (t.cancel) {
                    throw new RuntimeException("cancel");
                }

                out.write(buf, 0, r);
                total += r;

                int percent = (len > 0) ? (int) (total * 100 / len) : 0;
                if (percent < t.percent) {
                    percent = t.percent;
                } else {
                    t.percent = percent;
                }

                long now = System.currentTimeMillis();
                long dt = now - speedWindowStartTime;
                long db = total - speedWindowStartBytes;
                if (dt > 0) {
                    t.speed = (db * 1000L) / dt;
                }

                String speedText = (t.speed >= 1024 * 1024)
                        ? String.format("%.1f MB/s", (t.speed / 1024f / 1024f))
                        : Math.max(1, (t.speed / 1024)) + " KB/s";
                t.statusText = t.percent + "%  -  " + speedText;

                if (t.percent >= 100 || ((now - lastNotifyTime) >= 2500 && t.percent >= lastNotifyPercent + 5)) {
                    lastNotifyTime = now;
                    lastNotifyPercent = t.percent;
                    notifyProgress(t);
                }

                if (t.percent >= 100 || t.percent >= lastBroadcastPercent + 1) {
                    lastBroadcastPercent = t.percent;
                    persistTasks();
                    sendStateBroadcast(t, false, false, false);
                }

                if (dt >= 1000) {
                    speedWindowStartTime = now;
                    speedWindowStartBytes = total;
                }
            }

            t.filePath = f.getAbsolutePath();

            if (AppPrefs.isRootAutoInstallEnabled(this)) {
                t.installing = true;
                t.statusText = getString(R.string.downloads_installing);
                persistTasks();
                notifyProgress(t);
                sendStateBroadcast(t, false, false, false);

                boolean installed = installSilently(t.filePath);
                finishTask(t);
                if (installed) {
                    notifyInstalled(t);
                } else {
                    notifyComplete(t);
                    launchPackageInstaller(t.filePath);
                }
                sendStateBroadcast(t, true, false, false);
            } else {
                finishTask(t);
                notifyComplete(t);
                launchPackageInstaller(t.filePath);
                sendStateBroadcast(t, true, false, false);
            }

            maybeStopSelf();
        } catch (Exception e) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            try {
                nm.cancel(progressNotifId(t));
            } catch (Exception ex) {
            }
            finishTask(t);
            sendStateBroadcast(t, false, !t.cancel, t.cancel);
            maybeStopSelf();
        } finally {
            try { if (out != null) out.close(); } catch (Exception e) { }
            try { if (in != null) in.close(); } catch (Exception e) { }
            try { if (conn != null) conn.disconnect(); } catch (Exception e) { }
        }
    }

    private void finishTask(TaskInfo t) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        try {
            nm.cancel(progressNotifId(t));
        } catch (Exception e) {
        }
        TASKS.remove(t.packageName);
        persistTasks();
    }

    private void maybeStopSelf() {
        if (TASKS.isEmpty()) {
            try {
                compatStopForeground();
            } catch (Exception e) {
            }
            foregroundStarted = false;
            stopSelf();
        }
    }

    private boolean installSilently(String apkPath) {
        Process p = null;
        DataOutputStream os = null;
        try {
            String safePath = apkPath.replace("'", "'\\''");
            p = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("pm install -r '" + safePath + "'\n");
            os.writeBytes("exit\n");
            os.flush();
            int rc = p.waitFor();
            return rc == 0;
        } catch (Exception e) {
            return false;
        } finally {
            try { if (os != null) os.close(); } catch (Exception e) { }
            try { if (p != null) p.destroy(); } catch (Exception e) { }
        }
    }
}