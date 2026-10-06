package id.neotica.holomarket.feature.home.presenter;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import id.neotica.holomarket.BuildConfig;
import id.neotica.holomarket.feature.home.contract.HomeView;
import id.neotica.holomarket.feature.home.domain.AppTopic;
import id.neotica.holomarket.network.ApiCallback;
import id.neotica.holomarket.network.ApiTask;
import id.neotica.holomarket.utils.AuthManager;

/**
 * MVP presenter owning the home screen: auth gate, section topics, featured collection,
 * and the collection organizer. Rendering is delegated to a {@link HomeView}.
 */
public class HomePresenter {

    private final Context context;
    private final AuthManager authManager;

    private HomeView view;

    public HomePresenter(Context context) {
        this.context = context;
        this.authManager = new AuthManager(context);
    }

    public void attach(HomeView view) {
        this.view = view;
    }

    public void detach() {
        this.view = null;
    }

    public void load() {
        boolean loggedIn = authManager.isLoggedIn();

        if (view != null) {
            if (loggedIn) {
                view.showSettings();
            } else {
                view.showLogin();
            }
            view.renderSections(buildTopics());
        }

        loadFeatured();

        if (loggedIn) {
            loadOrganizer();
        }
    }

    private List<AppTopic> buildTopics() {
        List<AppTopic> topics = new ArrayList<AppTopic>();
        topics.add(new AppTopic("Applications", "application"));
        topics.add(new AppTopic("Games", "game"));
        if (authManager.isAdultContentEnabled()) {
            topics.add(new AppTopic("Adult", "adult"));
        }
        return topics;
    }

    private void loadFeatured() {
        requestFeatured(new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                List<JSONObject> items = parseData(response);
                if (view != null && items.size() > 0) {
                    view.renderFeatured(items);
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (view != null) {
                    view.showError(extractMessage(errorMessage));
                }
            }
        });
    }

    private void loadOrganizer() {
        requestOrganizer(new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray organizer = new JSONArray(response);
                    for (int i = 0; i < organizer.length(); i++) {
                        JSONObject item = organizer.getJSONObject(i);
                        final String slug = item.optString("slug", "");
                        final String title = item.optString("title", "");
                        if (slug.length() == 0) {
                            continue;
                        }

                        requestCollection(slug, new ApiCallback() {
                            @Override
                            public void onSuccess(String collectionResponse) {
                                List<JSONObject> items = parseData(collectionResponse);
                                if (view != null && items.size() > 0) {
                                    view.renderOrganizerSection(title, items);
                                }
                            }

                            @Override
                            public void onError(String errorMessage) {
                                // Organizer collections fail silently (as before).
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Organizer failures are non-fatal (as before).
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

    private String extractMessage(String errorMessage) {
        if (errorMessage != null && errorMessage.contains("|")) {
            return errorMessage.substring(errorMessage.indexOf("|") + 1);
        }
        return errorMessage;
    }

    // --- I/O seams: overridden in tests to run synchronously ---

    void requestFeatured(ApiCallback callback) {
        new ApiTask(context, "GET", BuildConfig.BASE_URL + "/apps/collections/featured",
                null, null, callback).execute();
    }

    void requestOrganizer(ApiCallback callback) {
        Map<String, String> headers = authManager.getAuthHeaders();
        new ApiTask(context, "GET", BuildConfig.BASE_URL + "/admin/collections/organizer",
                null, null, callback, headers).execute();
    }

    void requestCollection(String slug, ApiCallback callback) {
        new ApiTask(context, "GET", BuildConfig.BASE_URL + "/apps/collections/" + slug,
                null, null, callback).execute();
    }
}