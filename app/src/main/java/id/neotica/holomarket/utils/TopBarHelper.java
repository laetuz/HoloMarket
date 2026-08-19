package id.neotica.holomarket.utils;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import id.neotica.holomarket.R;

public class TopBarHelper {

    public static void setup(Activity activity, String title, boolean showBack) {
        setup(activity, title, showBack, 0, null);
    }

    public static void setup(final Activity activity, String title, boolean showBack,
                             int actionIconRes, View.OnClickListener actionListener) {
        TextView tvTitle = (TextView) activity.findViewById(R.id.top_bar_title);
        ImageButton btnBack = (ImageButton) activity.findViewById(R.id.top_bar_back);
        ImageButton btnAction = (ImageButton) activity.findViewById(R.id.top_bar_action);

        if (tvTitle != null) {
            tvTitle.setText(title);
        }

        if (btnBack != null) {
            if (showBack) {
                btnBack.setVisibility(View.VISIBLE);
                btnBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.onBackPressed();
                    }
                });
            } else {
                btnBack.setVisibility(View.GONE);
            }
        }

        if (btnAction != null) {
            if (actionIconRes != 0 && actionListener != null) {
                btnAction.setImageResource(actionIconRes);
                btnAction.setVisibility(View.VISIBLE);
                btnAction.setOnClickListener(actionListener);
            } else {
                btnAction.setVisibility(View.GONE);
            }
        }

        int tintColor = Color.WHITE;

        if (btnBack != null) {
            tintDrawable(btnBack, tintColor);
        }
        if (btnAction != null && btnAction.getVisibility() == View.VISIBLE) {
            tintDrawable(btnAction, tintColor);
        }
    }

    private static void tintDrawable(ImageButton btn, int color) {
        Drawable d = btn.getDrawable();
        if (d != null) {
            d.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
    }
}