package id.neotica.holomarket.feature.detail.domain;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import id.neotica.holomarket.model.VersionModel;

/**
 * Parsed model for the app-detail response (`GET /apps/{packageName}`).
 */
public class AppDetailModel {

    public final String packageName;
    public final String title;
    public final String description;
    public final String iconUrl;
    public final String developer;
    public final List<String> categories;
    public final List<String> screenshots;
    public final List<VersionModel> versions;
    public final double averageRating;
    public final int totalReviews;

    public AppDetailModel(String packageName, String title, String description, String iconUrl,
                          String developer, List<String> categories, List<String> screenshots,
                          List<VersionModel> versions, double averageRating, int totalReviews) {
        this.packageName = packageName;
        this.title = title;
        this.description = description;
        this.iconUrl = iconUrl;
        this.developer = developer;
        this.categories = categories;
        this.screenshots = screenshots;
        this.versions = versions;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
    }

    public static AppDetailModel fromJson(String packageName, JSONObject obj) {
        String title = obj.optString("title", "Unknown App");
        String description = obj.optString("description", "No description available.");
        String iconUrl = obj.optString("icon_url", "");
        String developer = obj.optString("developer", "");

        List<String> categories = new ArrayList<String>();
        JSONArray categoriesArray = obj.optJSONArray("categories");
        if (categoriesArray != null) {
            for (int i = 0; i < categoriesArray.length(); i++) {
                String slug = categoriesArray.optString(i);
                if (slug != null && slug.length() > 0) {
                    categories.add(slug);
                }
            }
        }
        if (categories.size() == 0) {
            String category = obj.optString("category", "");
            if (category != null && category.length() > 0) {
                categories.add(category);
            }
        }

        List<String> screenshots = new ArrayList<String>();
        JSONArray screenshotsArray = obj.optJSONArray("screenshots");
        if (screenshotsArray != null) {
            for (int i = 0; i < screenshotsArray.length(); i++) {
                String shot = screenshotsArray.optString(i);
                if (shot != null && shot.length() > 0) {
                    screenshots.add(shot);
                }
            }
        }

        List<VersionModel> versions = new ArrayList<VersionModel>();
        JSONArray versionsArray = obj.optJSONArray("versions");
        if (versionsArray != null) {
            for (int i = 0; i < versionsArray.length(); i++) {
                VersionModel version = VersionModel.fromJson(versionsArray.optJSONObject(i));
                if (version != null) {
                    versions.add(version);
                }
            }
        }

        double averageRating = obj.optDouble("average_rating", 0);
        int totalReviews = obj.optInt("total_reviews", 0);

        return new AppDetailModel(packageName, title, description, iconUrl, developer,
                categories, screenshots, versions, averageRating, totalReviews);
    }

    /**
     * @return the highest version code, or -1 when there are no versions.
     */
    public int latestVersionCode() {
        int max = -1;
        for (int i = 0; i < versions.size(); i++) {
            VersionModel version = versions.get(i);
            if (version != null && version.versionCode > max) {
                max = version.versionCode;
            }
        }
        return max;
    }
}