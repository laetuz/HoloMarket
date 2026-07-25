package id.neotica.holomarket.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryomartin on 26/07/26.
 */

public class CategoryModel {
    public final String slug;
    public final String name;
    public final String parentSlug;
    public final List<CategoryModel> children;

    public CategoryModel(String slug, String name, String parentSlug, List<CategoryModel> children) {
        this.slug = slug;
        this.name = name;
        this.parentSlug = parentSlug;
        this.children = children;
    }

    public static CategoryModel fromJson(JSONObject obj) {
        String slug = obj.optString("slug", "");
        String name = obj.optString("name", "");
        String parentSlug = obj.isNull("parent_slug") ? null : obj.optString("parent_slug", null);
        List<CategoryModel> children = new ArrayList<CategoryModel>();
        JSONArray childrenArray = obj.optJSONArray("children");
        if (childrenArray != null) {
            for (int i = 0; i < childrenArray.length(); i++) {
                children.add(fromJson(childrenArray.optJSONObject(i)));
            }
        }
        return new CategoryModel(slug, name, parentSlug, children);
    }
}