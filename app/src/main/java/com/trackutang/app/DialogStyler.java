package com.trackutang.app;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.ViewGroup;
import android.view.Window;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.FrameLayout;

/** Keeps dialogs consistent with the TrackUtang visual language. */
public final class DialogStyler {
    private DialogStyler() { }

    public static void show(AlertDialog dialog) {
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.drawable.dialog_background);
            window.setDimAmount(.52f);
            window.setLayout((int) (contextWidth(dialog) * .92f), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        Button neutral = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        if (positive != null) { positive.setTextColor(Color.WHITE); positive.setAllCaps(false); positive.setPadding(22, 0, 22, 0); positive.setBackground(pill(Color.rgb(20, 143, 143))); }
        if (negative != null) { negative.setTextColor(Color.rgb(41, 68, 95)); negative.setAllCaps(false); }
        if (neutral != null) { neutral.setTextColor(Color.rgb(75, 110, 200)); neutral.setAllCaps(false); }
        if (window != null && window.getDecorView() instanceof ViewGroup) {
            TextView close = new TextView(dialog.getContext()); close.setText("×"); close.setTextSize(26); close.setTextColor(Color.rgb(69,86,99)); close.setGravity(Gravity.CENTER); close.setContentDescription("Close"); close.setOnClickListener(v -> dialog.dismiss());
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(54,54, Gravity.TOP | Gravity.END); params.setMargins(0,10,10,0);
            ((ViewGroup) window.getDecorView()).addView(close, params);
        }
    }

    private static int contextWidth(AlertDialog dialog) { return dialog.getContext().getResources().getDisplayMetrics().widthPixels; }
    private static GradientDrawable pill(int color) { GradientDrawable drawable = new GradientDrawable(); drawable.setOrientation(GradientDrawable.Orientation.TL_BR); drawable.setColors(new int[]{Color.rgb(49, 171, 168), color}); drawable.setCornerRadius(48); return drawable; }
}
