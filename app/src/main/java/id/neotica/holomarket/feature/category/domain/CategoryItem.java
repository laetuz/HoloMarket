package id.neotica.holomarket.feature.category.domain;

/**
 * A single selectable row on the category screen: the "All" entry plus each child category.
 */
public class CategoryItem {
    public final String displayName;
    public final String slug;

    public CategoryItem(String displayName, String slug) {
        this.displayName = displayName;
        this.slug = slug;
    }
}