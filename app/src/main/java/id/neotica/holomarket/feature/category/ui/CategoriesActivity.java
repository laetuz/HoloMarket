package id.neotica.holomarket.feature.category.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.List;

import id.neotica.holomarket.R;
import id.neotica.holomarket.feature.applist.ui.AppListActivity;
import id.neotica.holomarket.feature.category.contract.CategoryView;
import id.neotica.holomarket.feature.category.domain.CategoryItem;
import id.neotica.holomarket.feature.category.presenter.CategoryPresenter;
import id.neotica.holomarket.ui.components.InfiniteAppsAdapter;
import id.neotica.holomarket.ui.components.SectionListBuilder;
import id.neotica.holomarket.utils.CrashCatcher;
import id.neotica.holomarket.utils.TopBarHelper;

/**
 * Created by ryomartin on 26/07/26.
 */

public class CategoriesActivity extends Activity implements CategoryView {

    private static final String INTENT_URL_TOPIC = "URL_TOPIC";
    private static final String INTENT_URL_TOPIC_DISPLAY = "URL_TOPIC_DISPLAY";

    private LinearLayout featuredHeaderContainer;
    private LinearLayout sectionContainer;
    private View featuredView;
    private Gallery galleryFeatured;
    private String parentSlug;
    private String parentDisplayName;

    private CategoryPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashCatcher.init(this.getApplicationContext());
        setContentView(R.layout.activity_categories);
        CrashCatcher.showCrashLogIfAny(this);

        presenter = new CategoryPresenter(this);
        presenter.attach(this);

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

        presenter.load(parentSlug);
    }

    @Override
    public void renderSections(List<CategoryItem> items) {
        SectionListBuilder.build(this, sectionContainer, items,
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
    }

    @Override
    public void renderFeatured(List<JSONObject> items) {
        TextView tvFeaturedTitle = (TextView) featuredView.findViewById(R.id.tv_featured_title);
        tvFeaturedTitle.setText("Featured in " + parentDisplayName);
        featuredView.setVisibility(View.VISIBLE);

        InfiniteAppsAdapter.setupGallery(galleryFeatured, this, items);
    }

    @Override
    protected void onDestroy() {
        if (presenter != null) {
            presenter.detach();
        }
        super.onDestroy();
    }
}