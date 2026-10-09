package id.neotica.holomarket.feature.downloads.domain;

import org.json.JSONObject;

/**
 * A download task shown by the downloads manager.
 */
public class DownloadInfo {

    public final String packageName;
    public final String appName;
    public final String icon;
    public final int percent;
    public final long speedBytesPerSecond;
    public final boolean installing;
    public final String statusText;
    public final String fileName;
    public final String filePath;

    public DownloadInfo(String packageName, String appName, String icon, int percent,
                        long speedBytesPerSecond, boolean installing, String statusText,
                        String fileName, String filePath) {
        this.packageName = packageName;
        this.appName = appName;
        this.icon = icon;
        this.percent = percent;
        this.speedBytesPerSecond = speedBytesPerSecond;
        this.installing = installing;
        this.statusText = statusText;
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public static DownloadInfo fromJson(JSONObject o) {
        if (o == null) {
            return null;
        }
        return new DownloadInfo(
                o.optString("package", ""),
                o.optString("app_name", ""),
                o.optString("icon", ""),
                o.optInt("percent", 0),
                o.optLong("speed_bps", 0),
                o.optBoolean("installing", false),
                o.optString("status_text", ""),
                o.optString("file_name", ""),
                o.optString("file_path", "")
        );
    }
}