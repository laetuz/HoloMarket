package id.neotica.holomarket.model;

/**
 * Created by ryomartin on 21/03/26.
 */

public class AppModel {
    public String packageName;
    public String title;
    public String description;
    public String iconUrl;
    public String category;

    public AppModel(
            String packageName,
            String title,
            String description,
            String iconUrl,
            String category
    ) {
        this.packageName = packageName;
        this.title = title;
        this.description = description;
        this.iconUrl = iconUrl;
        this.category = category;
    }
}
