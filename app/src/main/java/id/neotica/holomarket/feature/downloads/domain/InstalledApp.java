package id.neotica.holomarket.feature.downloads.domain;

import org.json.JSONObject;

/**
 * A store app that is installed on the device, built by matching a
 * {@code POST /apps/lookup} item against the installed package version.
 */
public class InstalledApp {

    public final String packageName;
    public final String title;
    public final String iconUrl;
    public final int latestVersionCode;
    public final int installedVersionCode;

    public InstalledApp(String packageName, String title, String iconUrl,
                        int latestVersionCode, int installedVersionCode) {
        this.packageName = packageName;
        this.title = title;
        this.iconUrl = iconUrl;
        this.latestVersionCode = latestVersionCode;
        this.installedVersionCode = installedVersionCode;
    }

    public static InstalledApp fromJson(JSONObject o, int installedVersionCode) {
        if (o == null) {
            return null;
        }
        String packageName = o.optString("package_name", "");
        if (packageName.length() == 0) {
            return null;
        }
        String iconUrl = o.isNull("icon_url") ? "" : o.optString("icon_url", "");
        return new InstalledApp(
                packageName,
                o.optString("title", ""),
                iconUrl,
                o.optInt("version_code", 0),
                installedVersionCode
        );
    }

    public boolean hasUpdate() {
        return latestVersionCode > 0 && installedVersionCode > 0
                && latestVersionCode > installedVersionCode;
    }
}