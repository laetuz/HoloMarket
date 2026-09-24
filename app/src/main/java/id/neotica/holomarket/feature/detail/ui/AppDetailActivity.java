package id.neotica.holomarket.feature.detail.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.neotica.holomarket.R;
import id.neotica.holomarket.feature.detail.domain.AppDetailModel;
import id.neotica.holomarket.model.VersionModel;
import id.neotica.holomarket.ui.feature.auth.LoginActivity;
import id.neotica.holomarket.feature.detail.contract.AppDetailView;
import id.neotica.holomarket.feature.detail.domain.InstallState;
import id.neotica.holomarket.feature.detail.contract.RatingsView;
import id.neotica.holomarket.feature.detail.presenter.AppDetailPresenter;
import id.neotica.holomarket.feature.detail.presenter.RatingsPresenter;
import id.neotica.holomarket.utils.ImageUrlHelper;
import id.neotica.holomarket.utils.TopBarHelper;

public class AppDetailActivity extends Activity implements RatingsView, AppDetailView {

    private TextView tvTitle, tvDesc, tvDeveloper, tvCategories, tvScreenshotsLabel, tvReadMore;
    private ImageView ivIcon;
    private LinearLayout llVersions;
    private HorizontalScrollView hsvScreenshots;
    private LinearLayout llScreenshots;
    private Button btDownload;
    private RatingBar rbRating;
    private TextView tvRatingInfo;
    private Button btnDeleteReview;
    private RatingsPresenter ratingsPresenter;
    private AppDetailPresenter appDetailPresenter;
    private List<String> previewScreenshots = new ArrayList<String>();
    private boolean descExpanded = false;
    private String currentPackageName;

    private static final String INTENT_PACKAGE_NAME = "PACKAGE_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_detail);

        TopBarHelper.setup(this, "App Detail", true);

        View topBarBack = findViewById(R.id.top_bar_back);
        topBarBack.setNextFocusDownId(R.id.bt_download);

        tvTitle = (TextView) findViewById(R.id.tv_detail_title);
        tvDesc = (TextView) findViewById(R.id.tv_detail_desc);
        tvDeveloper = (TextView) findViewById(R.id.tv_detail_developer);
        tvCategories = (TextView) findViewById(R.id.tv_detail_categories);
        ivIcon = (ImageView) findViewById(R.id.iv_detail_icon);
        llVersions = (LinearLayout) findViewById(R.id.ll_versions);
        btDownload = (Button) findViewById(R.id.bt_download);
        tvScreenshotsLabel = (TextView) findViewById(R.id.tv_screenshots_label);
        hsvScreenshots = (HorizontalScrollView) findViewById(R.id.hsv_screenshots);
        llScreenshots = (LinearLayout) findViewById(R.id.ll_screenshots);
        tvReadMore = (TextView) findViewById(R.id.tv_read_more);

        ratingsPresenter = new RatingsPresenter(this);
        ratingsPresenter.attach(this);

        appDetailPresenter = new AppDetailPresenter(this);
        appDetailPresenter.attach(this);

        rbRating = (RatingBar) findViewById(R.id.rb_rating);
        tvRatingInfo = (TextView) findViewById(R.id.tv_rating_info);
        btnDeleteReview = (Button) findViewById(R.id.btn_delete_review);

        rbRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (fromUser) {
                    ratingsPresenter.submitRating((int) rating);
                }
            }
        });

        btnDeleteReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ratingsPresenter.deleteReview();
            }
        });

        tvReadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (descExpanded) {
                    tvDesc.setMaxLines(3);
                    tvDesc.setEllipsize(TextUtils.TruncateAt.END);
                    tvReadMore.setText("Read more");
                } else {
                    tvDesc.setMaxLines(Integer.MAX_VALUE);
                    tvDesc.setEllipsize(null);
                    tvReadMore.setText("Show less");
                }
                descExpanded = !descExpanded;
            }
        });

        btDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appDetailPresenter.onDownloadClicked();
            }
        });

        String packageName = getIntent().getStringExtra(INTENT_PACKAGE_NAME);

        if (packageName != null) {
            currentPackageName = packageName;
            appDetailPresenter.load(packageName);
        } else {
            Toast.makeText(this, "Error: No package provided.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void showLoading() {
        btDownload.setEnabled(false);
        btDownload.setText("Loading...");
    }

    @Override
    public void renderAppDetail(AppDetailModel detail) {
        tvTitle.setText(detail.title);

        TextView topBarTitle = (TextView) findViewById(R.id.top_bar_title);
        if (topBarTitle != null) {
            topBarTitle.setText(detail.title);
        }

        tvDesc.setText(detail.description);
        tvDesc.post(new Runnable() {
            @Override
            public void run() {
                if (tvDesc.getLineCount() > 3) {
                    tvDesc.setMaxLines(3);
                    tvDesc.setEllipsize(TextUtils.TruncateAt.END);
                    tvReadMore.setVisibility(View.VISIBLE);
                } else {
                    tvReadMore.setVisibility(View.GONE);
                }
            }
        });

        if (!TextUtils.isEmpty(detail.iconUrl)) {
            ImageLoader.getInstance().displayImage(ImageUrlHelper.build(detail.iconUrl), ivIcon);
        } else {
            ivIcon.setImageResource(android.R.drawable.sym_def_app_icon);
        }

        if (detail.developer != null && detail.developer.length() > 0) {
            tvDeveloper.setText(detail.developer);
            tvDeveloper.setVisibility(View.VISIBLE);
        } else {
            tvDeveloper.setVisibility(View.GONE);
        }

        if (detail.categories.size() > 0) {
            tvCategories.setText("Categories: " + join(detail.categories));
            tvCategories.setVisibility(View.VISIBLE);
        } else {
            tvCategories.setVisibility(View.GONE);
        }

        renderScreenshots(detail.screenshots);
        renderVersions(detail.versions);

        ratingsPresenter.load(detail.packageName, detail.averageRating, detail.totalReviews);
    }

    @Override
    public void renderInstallState(InstallState state) {
        btDownload.setEnabled(true);
        btDownload.setVisibility(View.VISIBLE);
        if (state == InstallState.OPEN) {
            btDownload.setText("Open");
        } else if (state == InstallState.UPDATE) {
            btDownload.setText("Update");
        } else {
            btDownload.setText("Download");
        }
    }

    @Override
    public void showLoadError(String message) {
        final String packageName = currentPackageName;
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
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
                        if (packageName != null) {
                            appDetailPresenter.load(packageName);
                        }
                    }
                })
                .show();
    }

    @Override
    public void openApp(String packageName) {
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            startActivity(launchIntent);
        } else {
            Toast.makeText(this, "Unable to open the app.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showNoVersions() {
        Toast.makeText(this, "No versions available.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showNoDownloadLink() {
        Toast.makeText(this, "Download link not available for this version.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void renderRating(boolean loggedIn, int myRating, double averageRating, int totalReviews) {
        rbRating.setIsIndicator(!loggedIn);

        if (loggedIn && myRating > 0) {
            rbRating.setRating(myRating);
        } else {
            rbRating.setRating((float) averageRating);
        }

        String summary;
        if (totalReviews > 0) {
            summary = String.format(Locale.US, "%.1f", averageRating) + " \u00b7 " + totalReviews + " reviews";
        } else {
            summary = "No ratings yet";
        }

        if (!loggedIn) {
            tvRatingInfo.setText(summary);
        } else if (myRating > 0) {
            tvRatingInfo.setText("Your rating: " + myRating + " \u00b7 " + summary);
        } else {
            tvRatingInfo.setText("Tap a star to rate \u00b7 " + summary);
        }

        btnDeleteReview.setVisibility((loggedIn && myRating > 0) ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showRatingSaved() {
        Toast.makeText(this, "Rating saved", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showReviewDeleted() {
        Toast.makeText(this, "Review deleted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showRatingError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUnauthorized() {
        Toast.makeText(this, "Please log in to rate.", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
    }

    @Override
    protected void onDestroy() {
        if (ratingsPresenter != null) {
            ratingsPresenter.detach();
        }
        if (appDetailPresenter != null) {
            appDetailPresenter.detach();
        }
        super.onDestroy();
    }

    private void renderVersions(List<VersionModel> versions) {
        llVersions.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < versions.size(); i++) {
            final VersionModel vm = versions.get(i);
            if (vm == null) {
                continue;
            }

            View row = inflater.inflate(R.layout.item_version, llVersions, false);
            TextView tvVersionName = (TextView) row.findViewById(R.id.tv_version_name);
            TextView tvMinSdk = (TextView) row.findViewById(R.id.tv_min_sdk);
            TextView tvChangelog = (TextView) row.findViewById(R.id.tv_changelog);
            Button btnVersionDownload = (Button) row.findViewById(R.id.btn_version_download);

            tvVersionName.setText("Version " + vm.versionName + " (" + vm.versionCode + ")");
            tvMinSdk.setText("Min SDK: " + vm.minSdk);

            if (vm.changelog != null && vm.changelog.length() > 0) {
                tvChangelog.setText(vm.changelog);
                tvChangelog.setVisibility(View.VISIBLE);
            } else {
                tvChangelog.setVisibility(View.GONE);
            }

            btnVersionDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    appDetailPresenter.downloadVersion(vm);
                }
            });

            llVersions.addView(row);
        }
    }

    private String join(List<String> items) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(items.get(i));
        }
        return sb.toString();
    }

    private void renderScreenshots(List<String> urls) {
        previewScreenshots = urls;
        llScreenshots.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        final float density = getResources().getDisplayMetrics().density;
        final int longSide = (int) (350 * density);
        final int maxWidth = getResources().getDisplayMetrics().widthPixels - (int) (32 * density);
        final int marginRight = (int) (8 * density);

        for (int i = 0; i < urls.size(); i++) {
            final int index = i;
            final ImageView imageView = (ImageView) inflater.inflate(R.layout.item_screenshot, llScreenshots, false);
            String fullImageUrl = ImageUrlHelper.build(urls.get(i));

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showScreenshotPreview(index);
                }
            });

            if (!TextUtils.isEmpty(fullImageUrl)) {
                ImageLoader.getInstance().loadImage(fullImageUrl, new ImageSize(longSide, longSide),
                        new SimpleImageLoadingListener() {
                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                if (loadedImage == null) {
                                    imageView.setImageResource(android.R.drawable.picture_frame);
                                    return;
                                }

                                int w = loadedImage.getWidth();
                                int h = loadedImage.getHeight();
                                int viewWidth;
                                int viewHeight;
                                if (h >= w) {
                                    viewHeight = longSide;
                                    viewWidth = Math.round(longSide * (float) w / h);
                                } else {
                                    viewWidth = Math.min(longSide, maxWidth);
                                    viewHeight = Math.round(viewWidth * (float) h / w);
                                }
                                viewWidth = Math.max(1, viewWidth);
                                viewHeight = Math.max(1, viewHeight);

                                LinearLayout.LayoutParams lp;
                                ViewGroup.LayoutParams params = imageView.getLayoutParams();
                                if (params instanceof LinearLayout.LayoutParams) {
                                    lp = (LinearLayout.LayoutParams) params;
                                } else {
                                    lp = new LinearLayout.LayoutParams(viewWidth, viewHeight);
                                    lp.rightMargin = marginRight;
                                }
                                lp.width = viewWidth;
                                lp.height = viewHeight;
                                imageView.setLayoutParams(lp);
                                imageView.setImageBitmap(loadedImage);
                            }
                        });
            } else {
                imageView.setImageResource(android.R.drawable.picture_frame);
            }

            llScreenshots.addView(imageView);
        }
        boolean show = urls.size() > 0;
        tvScreenshotsLabel.setVisibility(show ? View.VISIBLE : View.GONE);
        hsvScreenshots.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private class PreviewGalleryAdapter extends BaseAdapter {
        private final List<String> screenshotUrls;

        PreviewGalleryAdapter(List<String> screenshotUrls) {
            this.screenshotUrls = screenshotUrls;
        }

        @Override
        public int getCount() {
            return screenshotUrls.size();
        }

        @Override
        public Object getItem(int position) {
            return screenshotUrls.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(AppDetailActivity.this);
                imageView = (ImageView) inflater.inflate(R.layout.item_screenshot_preview, parent, false);
            } else {
                imageView = (ImageView) convertView;
            }

            String fullImageUrl = ImageUrlHelper.build(screenshotUrls.get(position));
            if (!TextUtils.isEmpty(fullImageUrl)) {
                ImageLoader.getInstance().displayImage(fullImageUrl, imageView);
            } else {
                imageView.setImageResource(android.R.drawable.picture_frame);
            }
            return imageView;
        }
    }

    private void showScreenshotPreview(int position) {
        final Dialog dialog = new Dialog(this);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_screenshot_preview);

        final Gallery galleryPreview = (Gallery) dialog.findViewById(R.id.gallery_preview);
        galleryPreview.setAdapter(new PreviewGalleryAdapter(previewScreenshots));
        galleryPreview.setSelection(position);

        galleryPreview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                galleryPreview.setSelection(pos);
            }
        });

        dialog.findViewById(R.id.iv_close_preview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.copyFrom(dialog.getWindow().getAttributes());
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(params);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));

        dialog.show();
    }
}