package id.neotica.holomarket.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryomartin on 21/03/26.
 */

public class AppModel {
    public String packageName;
    public String title;
    public String description;
    public String iconUrl;
    public String category;
    public String developer;
    public List<String> categories;
    public List<String> screenshots;

    public AppModel(
            String packageName,
            String title,
            String description,
            String iconUrl,
            String category
    ) {
        this(packageName, title, description, iconUrl, category, "", null, null);
    }

    public AppModel(
            String packageName,
            String title,
            String description,
            String iconUrl,
            String category,
            String developer,
            List<String> categories,
            List<String> screenshots
    ) {
        this.packageName = packageName;
        this.title = title;
        this.description = description;
        this.iconUrl = iconUrl;
        this.category = category;
        this.developer = developer;
        this.categories = categories;
        this.screenshots = screenshots;
    }

    public static AppModel fromJson(JSONObject obj) {
        if (obj == null) {
            return null;
        }

        String iconUrl = obj.isNull("icon_url") ? "" : obj.optString("icon_url", "");

        List<String> categories = new ArrayList<String>();
        JSONArray categoriesArray = obj.optJSONArray("categories");
        if (categoriesArray != null) {
            for (int i = 0; i < categoriesArray.length(); i++) {
                categories.add(categoriesArray.optString(i));
            }
        }

        List<String> screenshots = new ArrayList<String>();
        JSONArray screenshotsArray = obj.optJSONArray("screenshots");
        if (screenshotsArray != null) {
            for (int i = 0; i < screenshotsArray.length(); i++) {
                screenshots.add(screenshotsArray.optString(i));
            }
        }

        return new AppModel(
                obj.optString("package_name", ""),
                obj.optString("title", ""),
                obj.optString("description", ""),
                iconUrl,
                obj.optString("category", ""),
                obj.optString("developer", ""),
                categories,
                screenshots
        );
    }
}