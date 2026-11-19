package com.trackutang.app;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Responsive, app-owned modal used for the core financial forms. */
public final class TrackModal {
    private TrackModal() { }

    public static Dialog show(Context context, String title, String subtitle, View form, String action, Runnable onAction) {
        Dialog dialog = new Dialog(context);
        LinearLayout sheet = new LinearLayout(context);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setPadding(dp(context, 22), dp(context, 22), dp(context, 22), dp(context, 18));
        sheet.setBackground(surface(context));

        LinearLayout header = new LinearLayout(context); header.setGravity(Gravity.CENTER_VERTICAL);
        TextView heading = new TextView(context); heading.setText(title); heading.setTextSize(22); heading.setTextColor(Color.rgb(22, 50, 79)); heading.setTypeface(android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)); heading.setLetterSpacing(-.02f);
        TextView close = new TextView(context); close.setText("×"); close.setTextSize(30); close.setTextColor(Color.rgb(69, 86, 99)); close.setGravity(Gravity.CENTER); close.setContentDescription("Close"); close.setOnClickListener(v -> dialog.dismiss()); GradientDrawable cb = new GradientDrawable(); cb.setShape(GradientDrawable.OVAL); cb.setColor(Color.rgb(240, 245, 246)); close.setBackground(cb); close.setTextSize(22);
        header.addView(heading, new LinearLayout.LayoutParams(0, dp(context, 44), 1)); header.addView(close, new LinearLayout.LayoutParams(dp(context, 44), dp(context, 44)));
        TextView description = new TextView(context); description.setText(subtitle); description.setTextSize(14); description.setTextColor(Color.rgb(69, 86, 99)); description.setLineSpacing(dp(context, 3), 1f); description.setPadding(0, dp(context, 3), 0, dp(context, 18));
        sheet.addView(header); sheet.addView(description);
        boolean compact = form instanceof EditText || (form instanceof LinearLayout && ((LinearLayout) form).getChildCount() <= 2);
        ScrollView scroll = new ScrollView(context); scroll.setFillViewport(true); scroll.addView(form); sheet.addView(scroll, compact ? new LinearLayout.LayoutParams(-1, -2) : new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout footer = new LinearLayout(context); footer.setPadding(0, dp(context, 16), 0, 0);
        Button cancel = button(context, "Cancel", false); Button save = button(context, action, true);
        cancel.setOnClickListener(v -> dialog.dismiss()); save.setOnClickListener(v -> onAction.run());
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(0, dp(context, 50), 1); cancelParams.setMargins(0, 0, dp(context, 8), 0);
        footer.addView(cancel, cancelParams); footer.addView(save, new LinearLayout.LayoutParams(0, dp(context, 50), 1)); sheet.addView(footer);

        int width = (int)(context.getResources().getDisplayMetrics().widthPixels * .90f);
        int maxHeight = (int)(context.getResources().getDisplayMetrics().heightPixels * .72f);
        dialog.setContentView(sheet); Window window = dialog.getWindow(); if (window != null) { window.setBackgroundDrawableResource(android.R.color.transparent); window.setLayout(width, compact ? ViewGroup.LayoutParams.WRAP_CONTENT : maxHeight); window.setGravity(Gravity.CENTER); }
        dialog.show(); Window shown = dialog.getWindow(); if (shown != null) shown.setLayout(width, compact ? ViewGroup.LayoutParams.WRAP_CONTENT : maxHeight);
        return dialog;
    }
    private static Button button(Context c, String label, boolean primary) { Button b = new Button(c); b.setText(label); b.setTextSize(15); b.setAllCaps(false); b.setTypeface(null, 1); b.setTextColor(primary ? Color.WHITE : Color.rgb(22, 50, 79)); b.setBackground(buttonBackground(c, primary)); return b; }
    private static GradientDrawable surface(Context c) { GradientDrawable g = new GradientDrawable(); g.setColor(Color.WHITE); g.setCornerRadius(dp(c, 28)); return g; }
    private static GradientDrawable buttonBackground(Context c, boolean primary) { GradientDrawable g = new GradientDrawable(); if (primary) { g.setOrientation(GradientDrawable.Orientation.TL_BR); g.setColors(new int[]{Color.rgb(49, 171, 168), Color.rgb(20, 143, 143)}); } else { g.setColor(Color.rgb(240, 245, 246)); g.setStroke(dp(c, 1), Color.rgb(222, 231, 233)); } g.setCornerRadius(dp(c, 16)); return g; }
    private static int dp(Context c, int value) { return (int)(value * c.getResources().getDisplayMetrics().density + .5f); }
}
