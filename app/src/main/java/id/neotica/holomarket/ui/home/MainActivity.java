package id.neotica.holomarket.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import id.neotica.holomarket.BuildConfig;
import id.neotica.holomarket.R;
import id.neotica.holomarket.network.ApiCallback;
import id.neotica.holomarket.network.ApiTask;
import id.neotica.holomarket.ui.auth.LoginActivity;
import id.neotica.holomarket.ui.detail.AppDetailActivity;
import id.neotica.holomarket.ui.settings.SettingsActivity;
import id.neotica.holomarket.utils.AuthManager;
import id.neotica.holomarket.utils.CrashCatcher;

public class MainActivity extends Activity {

    private ListView listView;
    private SectionAdapter adapter;

    private View headerView;
    private LinearLayout llFeaturedContainer;

    private static final String INTENT_TOPIC = "URL_TOPIC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashCatcher.init(this.getApplicationContext());

        setContentView(R.layout.activity_main);
        CrashCatcher.showCrashLogIfAny(this);

        final TextView tvTitle = (TextView) findViewById(R.id.tv_title);
        final Button btLogin = (Button) findViewById(R.id.btn_login);
        final AuthManager authManager = new AuthManager(this);

        ImageView ivSettings = (ImageView) findViewById(R.id.iv_settings);
        TypedArray ta = obtainStyledAttributes(new int[]{android.R.attr.textColorPrimary});
        int tintColor = ta.getColor(0, 0xFF888888);
        ta.recycle();
        ivSettings.setColorFilter(tintColor);

        if (authManager.isLoggedIn()) {
            btLogin.setVisibility(View.GONE);
            tvTitle.setVisibility(View.VISIBLE);
            ivSettings.setVisibility(View.VISIBLE);
            String username = authManager.getUsernameFromToken();
            if (username != null) {
                tvTitle.setText("Welcome " + username + "!");
            } else {
                tvTitle.setText("Welcome User!");
            }
        } else {
            btLogin.setVisibility(View.VISIBLE);
            tvTitle.setVisibility(View.GONE);
            ivSettings.setVisibility(View.GONE);
            btLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            });
        }

        listView = (ListView) findViewById(R.id.lv_main);

        // 1. Inflate and add the Header BEFORE setting the adapter
        LayoutInflater inflater = getLayoutInflater();
        headerView = inflater.inflate(R.layout.header_featured_apps, listView, false);
        llFeaturedContainer = (LinearLayout) headerView.findViewById(R.id.ll_featured_container);
        listView.addHeaderView(headerView);

        List<String> topicList = new ArrayList<String>();
        topicList.add("APPLICATION");
        topicList.add("GAME");
        if (authManager.isAdultContentEnabled()) {
            topicList.add("ADULT");
        }

        adapter = new SectionAdapter(this, topicList);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);

                // 2. Make sure they didn't click the header itself
                if (item instanceof String) {
                    String clickedApp = (String) item;

                    Intent intent = new Intent(MainActivity.this, AppListActivity.class);
                    intent.putExtra(INTENT_TOPIC, clickedApp);
                    startActivity(intent);
                }
            }
        });

        ivSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });

        fetchFeaturedApps();
    }

    private void fetchFeaturedApps() {
        String url = BuildConfig.BASE_URL + "/apps/collections/featured";

        // We pass null for the loading message so it fetches quietly in the background
        new ApiTask(this, "GET", url, null, null, new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray dataArray = jsonResponse.getJSONArray("data");

                    if (dataArray.length() > 0) {
                        // Make the header visible since we have data
                        headerView.setVisibility(View.VISIBLE);
                        llFeaturedContainer.removeAllViews(); // Clear any old items

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject appObj = dataArray.getJSONObject(i);
                            final String packageName = appObj.getString("package_name");
                            String title = appObj.getString("title");
                            String iconUrl = appObj.optString("icon_url", "");

                            // Inflate the individual item view
                            View itemView = getLayoutInflater().inflate(R.layout.item_featured_app, llFeaturedContainer, false);

                            TextView tvFeaturedTitle = (TextView) itemView.findViewById(R.id.tv_featured_title);
                            ImageView ivFeaturedIcon = (ImageView) itemView.findViewById(R.id.iv_featured_icon);

                            tvFeaturedTitle.setText(title);

                            if (!TextUtils.isEmpty(iconUrl)) {
                                String fullImageUrl = BuildConfig.FILE_BASE_URL + "/buckets" + iconUrl;
                                ImageLoader.getInstance().displayImage(fullImageUrl, ivFeaturedIcon);
                            } else {
                                // Fallback to system default if no icon exists
                                ImageLoader.getInstance().cancelDisplayTask(ivFeaturedIcon);
                                ivFeaturedIcon.setImageResource(android.R.drawable.sym_def_app_icon);
                            }
                            // Set click listener to go straight to the Detail screen
                            itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(MainActivity.this, AppDetailActivity.class);
                                    intent.putExtra("PACKAGE_NAME", packageName);
                                    startActivity(intent);
                                }
                            });

                            // Add the view to the horizontal scroll container
                            llFeaturedContainer.addView(itemView);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // Keep header hidden if parsing fails
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Keep header hidden on network error
                // We don't need a toast here so it fails silently if the network is down
            }
        }).execute();
    }

}