package id.neotica.holomarket.ui.feature.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
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
import id.neotica.holomarket.network.ApiCallback;
import id.neotica.holomarket.network.ApiTask;
import id.neotica.holomarket.ui.feature.applist.AppListActivity;
import id.neotica.holomarket.ui.feature.auth.LoginActivity;
import id.neotica.holomarket.ui.feature.detail.AppDetailActivity;
import id.neotica.holomarket.ui.feature.settings.SettingsActivity;
import id.neotica.holomarket.utils.AuthManager;
import id.neotica.holomarket.utils.CrashCatcher;
import id.neotica.holomarket.utils.TopBarHelper;

public class MainActivity extends Activity {

    private ListView listView;
    private SectionAdapter adapter;

    private View headerView;
    private Gallery galleryFeatured;

    private static final String INTENT_TOPIC = "URL_TOPIC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashCatcher.init(this.getApplicationContext());

        setContentView(R.layout.activity_main);
        CrashCatcher.showCrashLogIfAny(this);

        final TextView tvWelcome = (TextView) findViewById(R.id.tv_welcome);
        final Button btLogin = (Button) findViewById(R.id.btn_login);
        final AuthManager authManager = new AuthManager(this);

        int actionIcon = 0;
        View.OnClickListener actionListener = null;
        if (authManager.isLoggedIn()) {
            actionIcon = R.drawable.ic_settings;
            actionListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                }
            };
        } else {
            tvWelcome.setVisibility(View.GONE);
            btLogin.setVisibility(View.VISIBLE);
            btLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            });
        }
        TopBarHelper.setup(this, "HoloMarket", false, actionIcon, actionListener);

        listView = (ListView) findViewById(R.id.lv_main);

        // 1. Inflate and add the Header BEFORE setting the adapter
        LayoutInflater inflater = getLayoutInflater();
        headerView = inflater.inflate(R.layout.header_featured_apps, listView, false);
        galleryFeatured = (Gallery) headerView.findViewById(R.id.gallery_featured);
        listView.addHeaderView(headerView);

        List<String> topicList = new ArrayList<String>();
        topicList.add("application");
        topicList.add("game");
        if (authManager.isAdultContentEnabled()) {
            topicList.add("adult");
        }

        adapter = new SectionAdapter(this, topicList);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);

                if (item instanceof String) {
                    String clickedApp = (String) item;

                    Intent intent = new Intent(MainActivity.this, AppListActivity.class);
                    intent.putExtra(INTENT_TOPIC, clickedApp);
                    startActivity(intent);
                }
            }
        });

        fetchFeaturedApps();
    }

    private void fetchFeaturedApps() {
        String url = BuildConfig.BASE_URL + "/apps/collections/featured";

        new ApiTask(this, "GET", url, null, null, new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray dataArray = jsonResponse.getJSONArray("data");

                    if (dataArray.length() > 0) {
                        headerView.setVisibility(View.VISIBLE);

                        final List<JSONObject> items = new ArrayList<JSONObject>();
                        for (int i = 0; i < dataArray.length(); i++) {
                            items.add(dataArray.getJSONObject(i));
                        }

                        final FeaturedGalleryAdapter adapter = new FeaturedGalleryAdapter(
                                MainActivity.this, items);
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
                                    Intent intent = new Intent(MainActivity.this, AppDetailActivity.class);
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
                String displayMessage = errorMessage;
                if (errorMessage.contains("|")) {
                    displayMessage = errorMessage.substring(errorMessage.indexOf("|") + 1);
                }

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Error")
                        .setMessage(displayMessage)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setNegativeButton("Reload", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                fetchFeaturedApps();
                            }
                        })
                        .show();
            }
        }).execute();
    }

    private static class FeaturedGalleryAdapter extends BaseAdapter {
        private Context context;
        private List<JSONObject> items;

        FeaturedGalleryAdapter(Context context, List<JSONObject> items) {
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