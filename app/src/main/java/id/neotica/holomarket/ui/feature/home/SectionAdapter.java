package id.neotica.holomarket.ui.feature.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import id.neotica.holomarket.R;

/**
 * Created by ryomartin on 21/03/26.
 */

public class SectionAdapter extends ArrayAdapter<String> {
    public SectionAdapter(Context context, List<String> title) {
        super(context, 0, title);
    }

    private static class ViewHolder {
        TextView tvTitle;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String topic = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_section, parent, false);
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (topic != null) {
            viewHolder.tvTitle.setText(topic);
        }

        return convertView;
    }
}