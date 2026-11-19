package com.trackutang.app;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Readable modal for histories, notices, and long record text. */
public final class TextModal {
    private TextModal() { }
    public static void show(Context context, String title, String subtitle, String body) {
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        TextView panel = new TextView(context);
        panel.setText(body);
        panel.setTextSize(15);
        panel.setTextColor(Color.rgb(22, 50, 79));
        panel.setLineSpacing(dp(context, 6), 1f);
        panel.setPadding(dp(context, 18), dp(context, 18), dp(context, 18), dp(context, 18));
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(234, 248, 245));
        background.setCornerRadius(dp(context, 18));
        panel.setBackground(background);
        content.addView(panel);
        final Dialog[] modal = new Dialog[1];
        modal[0] = TrackModal.show(context, title, subtitle, content, "Done", () -> modal[0].dismiss());
    }
    private static int dp(Context c, int value) { return (int)(value * c.getResources().getDisplayMetrics().density + .5f); }
}
