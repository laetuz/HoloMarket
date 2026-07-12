package id.neotica.holomarket.ui.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import id.neotica.holomarket.BuildConfig;
import id.neotica.holomarket.R;
import id.neotica.holomarket.model.AppModel;
import id.neotica.holomarket.network.AnalyticsTracker;
import id.neotica.holomarket.network.ApiCallback;
import id.neotica.holomarket.network.ApiTask;
import id.neotica.holomarket.ui.detail.AppDetailActivity;
import id.neotica.holomarket.utils.CrashCatcher;
import id.neotica.holomarket.utils.TopBarHelper;

public class AppListActivity extends Activity {

    private ListView listView;
    private AppDetailActivity.AppAdapter adapter;
    private List<AppModel> appList;

    // Pagination State
    private EditText etSearch;
    private Button btnSearch;
    private String currentSearchQuery = "";
    private String currentCategory = "";
    private int currentPage = 1;
    private int totalPages = 1;
    private Button btnLoadMore;
    private View footerView;

    private static final String INTENT_URL_TOPIC = "URL_TOPIC";
    private static final String INTENT_PACKAGE_NAME = "PACKAGE_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashCatcher.init(this.getApplicationContext());
        setContentView(R.layout.activity_app_list);
        CrashCatcher.showCrashLogIfAny(this);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(INTENT_URL_TOPIC)) {
            currentCategory = intent.getStringExtra(INTENT_URL_TOPIC);
        }

        String categoryTitle = "App List";
        if (currentCategory != null && currentCategory.length() > 0) {
            categoryTitle = currentCategory;
        }
        TopBarHelper.setup(this, categoryTitle, true);

        etSearch = (EditText) findViewById(R.id.et_search);
        btnSearch = (Button) findViewById(R.id.btn_search);

        listView = (ListView) findViewById(R.id.lv_main);
        appList = new ArrayList<>();

        footerView = getLayoutInflater().inflate(R.layout.footer_load_more, null);
        btnLoadMore = (Button) footerView.findViewById(R.id.btn_load_more);
        listView.addFooterView(footerView);

        adapter = new AppDetailActivity.AppAdapter(this, appList);
        listView.setAdapter(adapter);

        btnLoadMore.setVisibility(View.GONE);

        btnSearch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                performSearch();
            }
        });

        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    return true;
                }
                return false;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AppModel clickedApp = adapter.getItem(position);
                if (clickedApp != null) {
                    Intent intent = new Intent(AppListActivity.this, AppDetailActivity.class);
                    intent.putExtra(INTENT_PACKAGE_NAME, clickedApp.packageName);
                    startActivity(intent);
                }
            }
        });

        btnLoadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentPage < totalPages) {
                    currentPage++;
                    fetchApps(currentPage);
                }
            }
        });

        fetchApps(currentPage);
    }

    private void performSearch() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromInputMethod(etSearch.getWindowToken(), 0);

        currentSearchQuery = etSearch.getText().toString().trim();
        currentPage = 1;
        AnalyticsTracker.track(AppListActivity.this, "feature_use", "search_performed");
        fetchApps(currentPage);
    }

    private void fetchApps(final int pageToLoad) {
        String baseEndpoint;
        if ("adult".equals(currentCategory)) {
            baseEndpoint = "/apps/adult-feed";
        } else {
            baseEndpoint = "/apps/feed";
        }
        String targetUrl = BuildConfig.BASE_URL + baseEndpoint + "?page=" + pageToLoad;

        try {
            if (!TextUtils.isEmpty(currentCategory) && !"adult".equals(currentCategory)) {
                targetUrl += "&category=" + URLEncoder.encode(currentCategory, "UTF-8");
            }

            if (!TextUtils.isEmpty(currentSearchQuery)) {
                targetUrl += "&search=" + URLEncoder.encode(currentSearchQuery, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        new ApiTask(this, "GET", targetUrl, null, "Loading Neostore...", new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {

                    JSONObject root = new JSONObject(response);

                    currentPage = root.optInt("page", 1);
                    totalPages = root.optInt("total_pages", 1);

                    JSONArray dataArray = root.optJSONArray("data");

                    if (pageToLoad == 1) adapter.clear();

                    if (dataArray != null) {
                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject appObj = dataArray.getJSONObject(i);

                            String packageName = appObj.optString("package_name", "");
                            String title = appObj.optString("title", "");
                            String desc = appObj.optString("description", "");
                            String iconUrl = appObj.isNull("icon_url") ? "" : appObj.optString("icon_url", "");

                            adapter.add(new AppModel(packageName, title, desc, iconUrl));
                        }
                    }

                    if (currentPage >= totalPages) {
                        btnLoadMore.setVisibility(View.GONE);
                    } else {
                        btnLoadMore.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(AppListActivity.this, "Error parsing server data: " + e.toString(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(AppListActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                if (pageToLoad > 1) {
                    currentPage--;
                }
            }
        }).execute();
    }
}
