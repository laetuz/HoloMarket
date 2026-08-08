package id.neotica.holomarket.model;

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
}