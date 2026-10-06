package id.neotica.holomarket.feature.applist.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

import id.neotica.holomarket.R;
import id.neotica.holomarket.feature.applist.contract.AppListView;
import id.neotica.holomarket.feature.applist.presenter.AppListPresenter;
import id.neotica.holomarket.feature.detail.ui.AppDetailActivity;
import id.neotica.holomarket.model.AppModel;
import id.neotica.holomarket.utils.CrashCatcher;
import id.neotica.holomarket.utils.TopBarHelper;

public class AppListActivity extends Activity implements AppListView {

    private ListView listView;
    private AppAdapter adapter;
    private List<AppModel> appList;

    private EditText etSearch;
    private Button btnSearch;
    private Button btnLoadMore;
    private TextView tvAllLoaded;
    private View footerView;

    private AppListPresenter presenter;

    private static final String INTENT_URL_TOPIC = "URL_TOPIC";
    private static final String INTENT_PACKAGE_NAME = "PACKAGE_NAME";
    public static final String INTENT_SEARCH_QUERY = "SEARCH_QUERY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashCatcher.init(this.getApplicationContext());
        setContentView(R.layout.activity_app_list);
        CrashCatcher.showCrashLogIfAny(this);

        presenter = new AppListPresenter(this);
        presenter.attach(this);

        Intent intent = getIntent();
        String currentCategory = "";
        if (intent != null && intent.hasExtra(INTENT_URL_TOPIC)) {
            currentCategory = intent.getStringExtra(INTENT_URL_TOPIC);
        }

        String initialQuery = null;
        if (intent != null && intent.hasExtra(INTENT_SEARCH_QUERY)) {
            initialQuery = intent.getStringExtra(INTENT_SEARCH_QUERY);
        }

        String categoryTitle = "App List";
        if (currentCategory != null && currentCategory.length() > 0) {
            String displayName = intent.getStringExtra(INTENT_URL_TOPIC + "_DISPLAY");
            if (displayName != null && displayName.length() > 0) {
                categoryTitle = displayName;
            } else {
                categoryTitle = currentCategory;
            }
        } else if (initialQuery != null && initialQuery.length() > 0) {
            categoryTitle = initialQuery;
        }
        TopBarHelper.setup(this, categoryTitle, true);

        boolean developerMode = initialQuery != null && initialQuery.length() > 0;

        View topBarBack = findViewById(R.id.top_bar_back);
        topBarBack.setNextFocusDownId(developerMode ? R.id.lv_main : R.id.et_search);

        etSearch = (EditText) findViewById(R.id.et_search);
        btnSearch = (Button) findViewById(R.id.btn_search);

        View searchRow = findViewById(R.id.search_row);
        if (developerMode) {
            searchRow.setVisibility(View.GONE);
        }

        listView = (ListView) findViewById(R.id.lv_main);
        appList = new ArrayList<AppModel>();

        footerView = getLayoutInflater().inflate(R.layout.footer_load_more, null);
        btnLoadMore = (Button) footerView.findViewById(R.id.btn_load_more);
        tvAllLoaded = (TextView) footerView.findViewById(R.id.tv_all_loaded);
        listView.addFooterView(footerView);

        adapter = new AppAdapter(this, appList);
        listView.setAdapter(adapter);

        btnLoadMore.setVisibility(View.GONE);
        tvAllLoaded.setVisibility(View.GONE);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performSearch();
            }
        });

        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
                if (position < 0 || position >= adapter.getCount()) { return; }
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
                presenter.loadMore();
            }
        });

        presenter.load(currentCategory, initialQuery);
    }

    private void performSearch() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromInputMethod(etSearch.getWindowToken(), 0);
        presenter.search(etSearch.getText().toString().trim());
    }

    @Override
    public void renderApps(List<AppModel> apps, boolean append) {
        if (!append) {
            adapter.clear();
        }
        for (int i = 0; i < apps.size(); i++) {
            adapter.add(apps.get(i));
        }
    }

    @Override
    public void showLoadMore(boolean hasMore) {
        if (hasMore) {
            btnLoadMore.setVisibility(View.VISIBLE);
            tvAllLoaded.setVisibility(View.GONE);
        } else {
            btnLoadMore.setVisibility(View.GONE);
            tvAllLoaded.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        if (presenter != null) {
            presenter.detach();
        }
        super.onDestroy();
    }
}