package id.neotica.holomarket.ui.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import java.util.List;

import id.neotica.holomarket.R;

/**
 * Created by ryomartin on 26/07/26.
 */

public class SectionListBuilder {

    public interface ItemBinder<T> {
        void onBind(T item, View view);
        void onClick(T item);
    }

    public static <T> void build(Context context, LinearLayout container,
                                  List<T> items, final ItemBinder<T> binder) {
        for (int i = 0; i < items.size(); i++) {
            final T item = items.get(i);

            View divider = new View(context);
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(context.getResources().getColor(R.color.grey100));
            container.addView(divider);

            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_section, container, false);
            binder.onBind(item, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    binder.onClick(item);
                }
            });
            container.addView(itemView);
        }

        if (items.size() > 0) {
            View divider = new View(context);
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(context.getResources().getColor(R.color.grey100));
            container.addView(divider);
        }
    }
}