package id.neotica.holomarket.ui.feature.category;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import id.neotica.holomarket.BuildConfig;
import id.neotica.holomarket.R;
import id.neotica.holomarket.model.CategoryModel;
import id.neotica.holomarket.network.ApiCallback;
import id.neotica.holomarket.network.ApiTask;
import id.neotica.holomarket.ui.feature.applist.AppListActivity;
import id.neotica.holomarket.ui.feature.detail.AppDetailActivity;
import id.neotica.holomarket.utils.CrashCatcher;
import id.neotica.holomarket.utils.TopBarHelper;

/**
 * Created by ryomartin on 26/07/26.
 */

public class CategoriesActivity extends Activity {

    private static final String INTENT_URL_TOPIC = "URL_TOPIC";
    private static final String INTENT_URL_TOPIC_DISPLAY = "URL_TOPIC_DISPLAY";

    private ListView listView;
    private View headerView;
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

        listView = (ListView) findViewById(R.id.lv_main);

        LayoutInflater inflater = getLayoutInflater();
        headerView = inflater.inflate(R.layout.header_category_featured, listView, false);
        galleryFeatured = (Gallery) headerView.findViewById(R.id.gallery_featured);
        listView.addHeaderView(headerView);

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

                    final List<String> displayItems = new ArrayList<String>();
                    final List<String> slugItems = new ArrayList<String>();

                    displayItems.add("All");
                    slugItems.add(parentSlug);

                    if (category.children != null) {
                        for (CategoryModel child : category.children) {
                            displayItems.add(child.name);
                            slugItems.add(child.slug);
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            CategoriesActivity.this,
                            android.R.layout.simple_list_item_1,
                            displayItems
                    );
                    listView.setAdapter(adapter);

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            int headerCount = listView.getHeaderViewsCount();
                            int dataPosition = position - headerCount;
                            if (dataPosition < 0) return;

                            String slug = slugItems.get(dataPosition);
                            String displayName = displayItems.get(dataPosition);

                            Intent intent = new Intent(CategoriesActivity.this, AppListActivity.class);
                            intent.putExtra(INTENT_URL_TOPIC, slug);
                            intent.putExtra(INTENT_URL_TOPIC_DISPLAY, displayName);
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
                        TextView tvFeaturedTitle = (TextView) headerView.findViewById(R.id.tv_featured_title);
                        tvFeaturedTitle.setText("Featured in " + parentDisplayName);
                        headerView.setVisibility(View.VISIBLE);

                        final List<JSONObject> items = new ArrayList<JSONObject>();
                        for (int i = 0; i < dataArray.length(); i++) {
                            items.add(dataArray.getJSONObject(i));
                        }

                        final FeaturedAppsAdapter adapter = new FeaturedAppsAdapter(
                                CategoriesActivity.this, items);
                        galleryFeatured.setAdapter(adapter);

                        int midPos = Integer.MAX_VALUE / 2;
                        int startPos = midPos - (midPos % items.size());
                        galleryFeatured.setSelection(startPos);

                        galleryFeatured.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                JSONObject appObj = items.get(position % items.size());
                                String packageName = appObj.optString("package_name", "");
                                if (!TextUtils.isEmpty(packageName)) {
                                    Intent intent = new Intent(CategoriesActivity.this, AppDetailActivity.class);
                                    intent.putExtra("PACKAGE_NAME", packageName);
                                    startActivity(intent);
                                }
                            }
                        });
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

    private static class FeaturedAppsAdapter extends BaseAdapter {
        private Context context;
        private List<JSONObject> items;

        FeaturedAppsAdapter(Context context, List<JSONObject> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public Object getItem(int position) {
            return items.get(position % items.size());
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = LayoutInflater.from(context).inflate(R.layout.item_featured_app, parent, false);
            } else {
                view = convertView;
            }

            JSONObject appObj = items.get(position % items.size());
            String title = appObj.optString("title", "");
            String iconUrl = appObj.optString("icon_url", "");

            TextView tvFeaturedTitle = (TextView) view.findViewById(R.id.tv_featured_title);
            ImageView ivFeaturedIcon = (ImageView) view.findViewById(R.id.iv_featured_icon);

            tvFeaturedTitle.setText(title);

            if (!TextUtils.isEmpty(iconUrl)) {
                String fullImageUrl = BuildConfig.FILE_BASE_URL + "/buckets" + iconUrl;
                ImageLoader.getInstance().displayImage(fullImageUrl, ivFeaturedIcon);
            } else {
                ImageLoader.getInstance().cancelDisplayTask(ivFeaturedIcon);
                ivFeaturedIcon.setImageResource(android.R.drawable.sym_def_app_icon);
            }

            return view;
        }
    }
}