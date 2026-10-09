package id.neotica.holomarket.feature.category.contract;

import org.json.JSONObject;

import java.util.List;

import id.neotica.holomarket.feature.category.domain.CategoryItem;

/**
 * View contract for the category screen, implemented by the category Activity.
 * {@code CategoryPresenter} drives all rendering through this interface.
 */
public interface CategoryView {

    void renderSections(List<CategoryItem> items);

    void renderFeatured(List<JSONObject> items);
}