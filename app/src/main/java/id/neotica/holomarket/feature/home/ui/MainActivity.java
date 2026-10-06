package id.neotica.holomarket.feature.home.ui;

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

import org.json.JSONObject;

import java.util.List;

import id.neotica.holomarket.R;
import id.neotica.holomarket.feature.auth.login.ui.LoginActivity;
import id.neotica.holomarket.feature.home.contract.HomeView;
import id.neotica.holomarket.feature.home.domain.AppTopic;
import id.neotica.holomarket.feature.home.presenter.HomePresenter;
import id.neotica.holomarket.ui.components.InfiniteAppsAdapter;
import id.neotica.holomarket.ui.components.SectionListBuilder;
import id.neotica.holomarket.ui.feature.category.CategoriesActivity;
import id.neotica.holomarket.ui.feature.settings.SettingsActivity;
import id.neotica.holomarket.utils.CrashCatcher;
import id.neotica.holomarket.utils.TopBarHelper;

public class MainActivity extends Activity implements HomeView {

    private LinearLayout featuredContainer;
    private LinearLayout sectionContainer;
    private LinearLayout organizerContainer;
    private View featuredView;
    private Gallery galleryFeatured;

    private HomePresenter presenter;

    private static final String INTENT_TOPIC = "URL_TOPIC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashCatcher.init(this.getApplicationContext());

        setContentView(R.layout.activity_main);
        CrashCatcher.showCrashLogIfAny(this);

        presenter = new HomePresenter(this);
        presenter.attach(this);

        featuredContainer = (LinearLayout) findViewById(R.id.featured_container);
        sectionContainer = (LinearLayout) findViewById(R.id.section_container);
        organizerContainer = (LinearLayout) findViewById(R.id.organizer_container);

        LayoutInflater inflater = getLayoutInflater();
        featuredView = inflater.inflate(R.layout.header_featured_apps, featuredContainer, false);
        galleryFeatured = (Gallery) featuredView.findViewById(R.id.gallery_featured);
        featuredContainer.addView(featuredView);

        presenter.load();
    }

    @Override
    public void showLogin() {
        Button btLogin = (Button) findViewById(R.id.btn_login);
        btLogin.setVisibility(View.VISIBLE);
        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        TopBarHelper.setup(this, "HoloMarket", false);
    }

    @Override
    public void showSettings() {
        TopBarHelper.setup(this, "HoloMarket", false, R.drawable.ic_settings,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    }
                });
    }

    @Override
    public void renderSections(List<AppTopic> topics) {
        SectionListBuilder.build(this, sectionContainer, topics,
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
    }

    @Override
    public void renderFeatured(List<JSONObject> items) {
        featuredView.setVisibility(View.VISIBLE);
        InfiniteAppsAdapter.setupGallery(galleryFeatured, this, items);
    }

    @Override
    public void renderOrganizerSection(String title, List<JSONObject> items) {
        View sectionView = getLayoutInflater().inflate(
                R.layout.header_category_featured, organizerContainer, false);
        ((TextView) sectionView.findViewById(R.id.tv_featured_title)).setText(title);

        Gallery gallery = (Gallery) sectionView.findViewById(R.id.gallery_featured);
        InfiniteAppsAdapter.setupGallery(gallery, this, items);

        sectionView.setVisibility(View.VISIBLE);
        organizerContainer.addView(sectionView);
    }

    @Override
    public void showError(String message) {
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
                        presenter.load();
                    }
                });

        AlertDialog dialog = builder.create();

        SpannableString msg = new SpannableString(
                message
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

    @Override
    protected void onDestroy() {
        if (presenter != null) {
            presenter.detach();
        }
        super.onDestroy();
    }
}