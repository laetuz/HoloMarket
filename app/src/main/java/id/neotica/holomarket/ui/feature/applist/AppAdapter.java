package id.neotica.holomarket.ui.feature.applist;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import id.neotica.holomarket.R;
import id.neotica.holomarket.model.AppModel;
import id.neotica.holomarket.utils.ImageUrlHelper;

/**
 * Created by ryomartin on 21/03/26.
 */

public class AppAdapter extends ArrayAdapter<AppModel> {
    public AppAdapter(Context context, List<AppModel> apps) {
        super(context, 0, apps);
    }

    private static class ViewHolder {
        TextView tvtitle;
        ImageView ivIcon;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AppModel app = getItem(position);

        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_app, parent, false);

            viewHolder.tvtitle = (TextView) convertView.findViewById(R.id.tv_title);
            viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (app != null) {
            viewHolder.tvtitle.setText(app.title);

            if (!TextUtils.isEmpty(app.iconUrl)) {

                String fullImageUrl = ImageUrlHelper.build(app.iconUrl);

                // Fire the ImageLoader
                ImageLoader.getInstance().displayImage(fullImageUrl, viewHolder.ivIcon);

            } else {
                // If the app has no icon, cancel any pending image load on this recycled view
                // and set it to a default system icon
                ImageLoader.getInstance().cancelDisplayTask(viewHolder.ivIcon);
                viewHolder.ivIcon.setImageResource(android.R.drawable.sym_def_app_icon);
            }
        }

        return convertView;
    }
}
