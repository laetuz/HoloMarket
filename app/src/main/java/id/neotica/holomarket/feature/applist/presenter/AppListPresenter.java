package id.neotica.holomarket.feature.applist.presenter;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import id.neotica.holomarket.BuildConfig;
import id.neotica.holomarket.feature.applist.contract.AppListView;
import id.neotica.holomarket.model.AppModel;
import id.neotica.holomarket.network.AnalyticsTracker;
import id.neotica.holomarket.network.ApiCallback;
import id.neotica.holomarket.network.ApiTask;

/**
 * MVP presenter owning the paginated app-list feed: paging state, search, and parsing.
 * Rendering is delegated to an {@link AppListView}; networking stays here.
 */
public class AppListPresenter {

    private final Context context;

    private AppListView view;
    private String category = "";
    private String query = "";
    private int page = 1;
    private int totalPages = 1;

    public AppListPresenter(Context context) {
        this.context = context;
    }

    public void attach(AppListView view) {
        this.view = view;
    }

    public void detach() {
        this.view = null;
    }

    public void load(String category, String query) {
        this.category = category == null ? "" : category;
        this.query = query == null ? "" : query;
        this.page = 1;
        request(buildUrl(page), false);
    }

    public void loadMore() {
        if (page >= totalPages) {
            return;
        }
        page++;
        request(buildUrl(page), true);
    }

    public void search(String query) {
        this.query = query == null ? "" : query.trim();
        this.page = 1;
        AnalyticsTracker.track(context, "feature_use", "search_performed");
        request(buildUrl(page), false);
    }

    private void request(String url, final boolean append) {
        requestFeed(url, new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject root = new JSONObject(response);
                    page = root.optInt("page", page);
                    totalPages = root.optInt("total_pages", 1);

                    List<AppModel> apps = new ArrayList<AppModel>();
                    JSONArray data = root.optJSONArray("data");
                    if (data != null) {
                        for (int i = 0; i < data.length(); i++) {
                            AppModel app = AppModel.fromJson(data.optJSONObject(i));
                            if (app != null) {
                                apps.add(app);
                            }
                        }
                    }

                    if (view != null) {
                        view.renderApps(apps, append);
                        view.showLoadMore(page < totalPages);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (view != null) {
                        view.showError("Error parsing server data.");
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (append && page > 1) {
                    page--;
                }
                if (view != null) {
                    view.showError(errorMessage);
                }
            }
        });
    }

    private String buildUrl(int page) {
        String baseEndpoint = "adult".equals(category) ? "/apps/adult-feed" : "/apps/feed";
        String url = BuildConfig.BASE_URL + baseEndpoint + "?page=" + page;
        try {
            if (category.length() > 0 && !"adult".equals(category)) {
                url += "&category=" + URLEncoder.encode(category, "UTF-8");
            }
            if (query.length() > 0) {
                url += "&search=" + URLEncoder.encode(query, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }

    // --- I/O seam: overridden in tests to run synchronously ---

    void requestFeed(String url, ApiCallback callback) {
        new ApiTask(context, "GET", url, null, "Loading Neostore...", callback).execute();
    }
}