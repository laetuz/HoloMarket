package id.neotica.holomarket.ui.components;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONObject;

import java.util.List;

import id.neotica.holomarket.BuildConfig;
import id.neotica.holomarket.R;
import id.neotica.holomarket.ui.feature.detail.AppDetailActivity;

/**
 * Created by ryomartin on 26/07/26.
 */

public class InfiniteAppsAdapter extends BaseAdapter {

    private Context context;
    private List<JSONObject> items;

    public InfiniteAppsAdapter(Context context, List<JSONObject> items) {
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

    public static void setupGallery(Gallery gallery, final Activity activity,
                                     final List<JSONObject> items) {
        InfiniteAppsAdapter adapter = new InfiniteAppsAdapter(activity, items);
        gallery.setAdapter(adapter);

        int midPos = Integer.MAX_VALUE / 2;
        int startPos = midPos - (midPos % items.size());
        gallery.setSelection(startPos);

        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONObject appObj = items.get(position % items.size());
                String packageName = appObj.optString("package_name", "");
                if (!TextUtils.isEmpty(packageName)) {
                    Intent intent = new Intent(activity, AppDetailActivity.class);
                    intent.putExtra("PACKAGE_NAME", packageName);
                    activity.startActivity(intent);
                }
            }
        });
    }
}