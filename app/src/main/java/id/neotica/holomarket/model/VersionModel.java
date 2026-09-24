package id.neotica.holomarket.model;

import org.json.JSONObject;

/**
 * Created by ryomartin on 21/03/26.
 */

public class VersionModel {
    public String id;
    public String appId;
    public String versionName;
    public int versionCode;
    public String fileUrl;
    public String changelog;
    public int minSdk;
    public int maxSdk;
    public long createdAt;

    public VersionModel(
            String id,
            String appId,
            String versionName,
            int versionCode,

            String fileUrl,
            String changelog,
            int minSdk,
            int maxSdk,
            long createdAt
    ) {
        this.id = id;
        this.appId = appId;
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.fileUrl = fileUrl;
        this.changelog = changelog;
        this.minSdk = minSdk;
        this.maxSdk = maxSdk;
        this.createdAt = createdAt;
    }

    public static VersionModel fromJson(JSONObject obj) {
        if (obj == null) {
            return null;
        }
        return new VersionModel(
                obj.optString("id", ""),
                obj.optString("app_id", ""),
                obj.optString("version_name", ""),
                obj.optInt("version_code", 0),
                obj.optString("file_url", ""),
                obj.optString("changelog", ""),
                obj.optInt("min_sdk", 0),
                obj.optInt("max_sdk", 0),
                obj.optLong("created_at", 0)
        );
    }
}
