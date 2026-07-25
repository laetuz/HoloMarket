package id.neotica.holomarket.ui.feature.detail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import id.neotica.holomarket.R;
import id.neotica.holomarket.model.VersionModel;

/**
 * Created by ryomartin on 21/03/26.
 */

public class VersionAdapter extends ArrayAdapter<VersionModel> {
    public VersionAdapter(Context context, List<VersionModel> versions) {
        super(context, 0, versions);
    }

    private static class ViewHolder {
        TextView tvVersionName;
        TextView tvMinSdk;
        TextView tvChangelog;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        VersionModel version = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_version, parent, false);
            viewHolder.tvVersionName = (TextView) convertView.findViewById(R.id.tv_version_name);
            viewHolder.tvMinSdk = (TextView) convertView.findViewById(R.id.tv_min_sdk);
            viewHolder.tvChangelog = (TextView) convertView.findViewById(R.id.tv_changelog);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (version != null) {
            viewHolder.tvVersionName.setText("Version " + version.versionName + " (" + version.versionCode + ")");

            viewHolder.tvMinSdk.setText("Min SDK: " + version.minSdk);

            if (version.changelog != null && version.changelog.length() > 0) {
                viewHolder.tvChangelog.setText(version.changelog);
                viewHolder.tvChangelog.setVisibility(View.VISIBLE);
            } else  {
                viewHolder.tvChangelog.setVisibility(View.GONE);
            }
        }

        return convertView;
    }
}
