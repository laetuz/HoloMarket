package id.neotica.holomarket.feature.category.presenter;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import id.neotica.holomarket.BuildConfig;
import id.neotica.holomarket.feature.category.contract.CategoryView;
import id.neotica.holomarket.feature.category.domain.CategoryItem;
import id.neotica.holomarket.model.CategoryModel;
import id.neotica.holomarket.network.ApiCallback;
import id.neotica.holomarket.network.ApiTask;

/**
 * MVP presenter owning the category screen: the category tree (as section rows) and the
 * category's featured collection. Rendering is delegated to a {@link CategoryView}.
 */
public class CategoryPresenter {

    private final Context context;

    private CategoryView view;
    private String parentSlug;

    public CategoryPresenter(Context context) {
        this.context = context;
    }

    public void attach(CategoryView view) {
        this.view = view;
    }

    public void detach() {
        this.view = null;
    }

    public void load(String parentSlug) {
        this.parentSlug = parentSlug;
        loadCategory();
        loadFeatured();
    }

    private void loadCategory() {
        requestCategory(parentSlug, new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    CategoryModel category = CategoryModel.fromJson(new JSONObject(response));

                    List<CategoryItem> items = new ArrayList<CategoryItem>();
                    items.add(new CategoryItem("All", parentSlug));
                    if (category.children != null) {
                        for (int i = 0; i < category.children.size(); i++) {
                            CategoryModel child = category.children.get(i);
                            items.add(new CategoryItem(child.name, child.slug));
                        }
                    }

                    if (view != null) {
                        view.renderSections(items);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Category tree failure is non-fatal (as before).
            }
        });
    }

    private void loadFeatured() {
        requestCollection(parentSlug, new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                List<JSONObject> items = parseData(response);
                if (view != null && items.size() > 0) {
                    view.renderFeatured(items);
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Featured failure is non-fatal (as before).
            }
        });
    }

    private List<JSONObject> parseData(String response) {
        List<JSONObject> items = new ArrayList<JSONObject>();
        try {
            JSONObject root = new JSONObject(response);
            JSONArray data = root.optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject obj = data.optJSONObject(i);
                    if (obj != null) {
                        items.add(obj);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    // --- I/O seams: overridden in tests to run synchronously ---

    void requestCategory(String slug, ApiCallback callback) {
        new ApiTask(context, "GET", BuildConfig.BASE_URL + "/categories/" + slug,
                null, "Loading...", callback).execute();
    }

    void requestCollection(String slug, ApiCallback callback) {
        new ApiTask(context, "GET", BuildConfig.BASE_URL + "/apps/collections/" + slug,
                null, null, callback).execute();
    }
}