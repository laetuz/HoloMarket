package id.neotica.holomarket.ui.feature.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import id.neotica.holomarket.BuildConfig;
import id.neotica.holomarket.R;
import id.neotica.holomarket.network.ApiCallback;
import id.neotica.holomarket.network.ApiTask;
import id.neotica.holomarket.ui.components.InfiniteAppsAdapter;
import id.neotica.holomarket.ui.components.SectionListBuilder;
import id.neotica.holomarket.ui.feature.auth.LoginActivity;
import id.neotica.holomarket.ui.feature.category.CategoriesActivity;
import id.neotica.holomarket.ui.feature.settings.SettingsActivity;
import id.neotica.holomarket.utils.AuthManager;
import id.neotica.holomarket.utils.CrashCatcher;
import id.neotica.holomarket.utils.TopBarHelper;

public class MainActivity extends Activity {

    private LinearLayout featuredContainer;
    private LinearLayout sectionContainer;
    private LinearLayout organizerContainer;
    private View featuredView;
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

        featuredContainer = (LinearLayout) findViewById(R.id.featured_container);
        sectionContainer = (LinearLayout) findViewById(R.id.section_container);
        organizerContainer = (LinearLayout) findViewById(R.id.organizer_container);

        LayoutInflater inflater = getLayoutInflater();
        featuredView = inflater.inflate(R.layout.header_featured_apps, featuredContainer, false);
        galleryFeatured = (Gallery) featuredView.findViewById(R.id.gallery_featured);
        featuredContainer.addView(featuredView);

        List<AppTopic> topicList = new ArrayList<AppTopic>();
        topicList.add(new AppTopic("Applications", "application"));
        topicList.add(new AppTopic("Games", "game"));
        if (authManager.isAdultContentEnabled()) {
            topicList.add(new AppTopic("Adult", "adult"));
        }

        SectionListBuilder.build(this, sectionContainer, topicList,
                new SectionListBuilder.ItemBinder<AppTopic>() {
                    @Override
                    public void onBind(AppTopic topic, View view) {
                        ((TextView) view.findViewById(R.id.tv_title)).setText(topic.displayName);
                    }

                    @Override
                    public void onClick(AppTopic topic) {
                        Intent intent = new Intent(MainActivity.this, CategoriesActivity.class);
                        intent.putExtra(INTENT_TOPIC, topic.value);
                        intent.putExtra(INTENT_TOPIC + "_DISPLAY", topic.displayName);
                        startActivity(intent);
                    }
                });

        fetchFeaturedApps();

        if (authManager.isLoggedIn()) {
            fetchOrganizer();
        }
    }

    private void fetchOrganizer() {
        String url = BuildConfig.BASE_URL + "/admin/collections/organizer";
        AuthManager auth = new AuthManager(this);
        final Map<String, String> headers = auth.getAuthHeaders();

        new ApiTask(this, "GET", url, null, null, new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray organizer = new JSONArray(response);

                    for (int i = 0; i < organizer.length(); i++) {
                        JSONObject item = organizer.getJSONObject(i);
                        final String slug = item.getString("slug");
                        String title = item.getString("title");

                        View sectionView = getLayoutInflater().inflate(
                                R.layout.header_category_featured, organizerContainer, false);
                        TextView tvTitle = (TextView) sectionView.findViewById(R.id.tv_featured_title);
                        tvTitle.setText(title);
                        sectionView.setVisibility(View.GONE);

                        organizerContainer.addView(sectionView);
                        fetchCollectionApps(slug, sectionView);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String errorMessage) {
            }
        }, headers).execute();
    }

    private void fetchCollectionApps(String slug, final View sectionView) {
        String url = BuildConfig.BASE_URL + "/apps/collections/" + slug;

        new ApiTask(this, "GET", url, null, null, new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray dataArray = jsonResponse.getJSONArray("data");

                    if (dataArray.length() > 0) {
                        final List<JSONObject> items = new ArrayList<JSONObject>();
                        for (int i = 0; i < dataArray.length(); i++) {
                            items.add(dataArray.getJSONObject(i));
                        }

                        Gallery gallery = (Gallery) sectionView.findViewById(R.id.gallery_featured);
                        InfiniteAppsAdapter.setupGallery(gallery, MainActivity.this, items);
                        sectionView.setVisibility(View.VISIBLE);
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

    private void fetchFeaturedApps() {
        String url = BuildConfig.BASE_URL + "/apps/collections/featured";

        new ApiTask(this, "GET", url, null, null, new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray dataArray = jsonResponse.getJSONArray("data");

                    if (dataArray.length() > 0) {
                        featuredView.setVisibility(View.VISIBLE);

                        final List<JSONObject> items = new ArrayList<JSONObject>();
                        for (int i = 0; i < dataArray.length(); i++) {
                            items.add(dataArray.getJSONObject(i));
                        }

                        InfiniteAppsAdapter.setupGallery(galleryFeatured, MainActivity.this, items);
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

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Error")
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
                        });

                AlertDialog dialog = builder.create();

                SpannableString msg = new SpannableString(
                        displayMessage
                        + "\n\nContact support: martin@neotica.id"
                        + "\nOr visit our website"
                );
                Linkify.addLinks(msg, Linkify.EMAIL_ADDRESSES);
                int websiteStart = msg.toString().indexOf("website");
                msg.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://neotica.id/holomarket")));
                    }
                }, websiteStart, websiteStart + "website".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                dialog.setMessage(msg);
                dialog.show();
                TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
                if (messageView != null) {
                    messageView.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }
        }).execute();
    }


}