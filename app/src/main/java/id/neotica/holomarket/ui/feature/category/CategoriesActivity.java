package id.neotica.holomarket.ui.feature.category;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import id.neotica.holomarket.BuildConfig;
import id.neotica.holomarket.R;
import id.neotica.holomarket.model.CategoryModel;
import id.neotica.holomarket.network.ApiCallback;
import id.neotica.holomarket.network.ApiTask;
import id.neotica.holomarket.ui.components.InfiniteAppsAdapter;
import id.neotica.holomarket.ui.components.SectionListBuilder;
import id.neotica.holomarket.ui.feature.applist.AppListActivity;
import id.neotica.holomarket.utils.CrashCatcher;
import id.neotica.holomarket.utils.TopBarHelper;

/**
 * Created by ryomartin on 26/07/26.
 */

public class CategoriesActivity extends Activity {

    private static final String INTENT_URL_TOPIC = "URL_TOPIC";
    private static final String INTENT_URL_TOPIC_DISPLAY = "URL_TOPIC_DISPLAY";

    private LinearLayout featuredHeaderContainer;
    private LinearLayout sectionContainer;
    private View featuredView;
    private Gallery galleryFeatured;
    private CategoryModel category;
    private String parentSlug;
    private String parentDisplayName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashCatcher.init(this.getApplicationContext());
        setContentView(R.layout.activity_categories);
        CrashCatcher.showCrashLogIfAny(this);

        Intent intent = getIntent();
        parentSlug = intent.getStringExtra(INTENT_URL_TOPIC);
        parentDisplayName = intent.getStringExtra(INTENT_URL_TOPIC_DISPLAY);
        if (parentDisplayName == null) {
            parentDisplayName = parentSlug;
        }
        TopBarHelper.setup(this, parentDisplayName, true);

        featuredHeaderContainer = (LinearLayout) findViewById(R.id.featured_header_container);
        sectionContainer = (LinearLayout) findViewById(R.id.section_container);

        featuredView = getLayoutInflater().inflate(
                R.layout.header_category_featured, featuredHeaderContainer, false);
        galleryFeatured = (Gallery) featuredView.findViewById(R.id.gallery_featured);
        featuredHeaderContainer.addView(featuredView);

        fetchCategory();
        fetchFeaturedApps();
    }

    private void fetchCategory() {
        String targetUrl = BuildConfig.BASE_URL + "/categories/" + parentSlug;

        new ApiTask(this, "GET", targetUrl, null, "Loading...", new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    category = CategoryModel.fromJson(obj);

                    final List<CategoryItem> items = new ArrayList<CategoryItem>();

                    items.add(new CategoryItem("All", parentSlug));

                    if (category.children != null) {
                        for (CategoryModel child : category.children) {
                            items.add(new CategoryItem(child.name, child.slug));
                        }
                    }

                    SectionListBuilder.build(CategoriesActivity.this, sectionContainer, items,
                            new SectionListBuilder.ItemBinder<CategoryItem>() {
                                @Override
                                public void onBind(CategoryItem item, View view) {
                                    ((TextView) view.findViewById(R.id.tv_title)).setText(item.displayName);
                                }

                                @Override
                                public void onClick(CategoryItem item) {
                                    Intent intent = new Intent(CategoriesActivity.this, AppListActivity.class);
                                    intent.putExtra(INTENT_URL_TOPIC, item.slug);
                                    intent.putExtra(INTENT_URL_TOPIC_DISPLAY, item.displayName);
                                    startActivity(intent);
                                }
                            });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String errorMessage) {
            }
        }).execute();
    }

    private void fetchFeaturedApps() {
        String url = BuildConfig.BASE_URL + "/apps/collections/" + parentSlug;

        new ApiTask(this, "GET", url, null, null, new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray dataArray = jsonResponse.getJSONArray("data");

                    if (dataArray.length() > 0) {
                        TextView tvFeaturedTitle = (TextView) featuredView.findViewById(R.id.tv_featured_title);
                        tvFeaturedTitle.setText("Featured in " + parentDisplayName);
                        featuredView.setVisibility(View.VISIBLE);

                        final List<JSONObject> items = new ArrayList<JSONObject>();
                        for (int i = 0; i < dataArray.length(); i++) {
                            items.add(dataArray.getJSONObject(i));
                        }

                        InfiniteAppsAdapter.setupGallery(galleryFeatured, CategoriesActivity.this, items);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String errorMessage) {
            }
        }).execute();
    }

    private static class CategoryItem {
        final String displayName;
        final String slug;

        CategoryItem(String displayName, String slug) {
            this.displayName = displayName;
            this.slug = slug;
        }
    }
}