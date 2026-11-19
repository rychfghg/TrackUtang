package com.trackutang.app;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/** First-run onboarding only. Kept separate so it does not affect the app's other dialogs. */
public final class WelcomeDialog {
    public interface Listener { void onContinue(String name); }
    private WelcomeDialog() { }

    public static void show(Context context, Listener listener) {
        Dialog dialog = new Dialog(context);
        LinearLayout sheet = new LinearLayout(context);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setPadding(dp(context, 24), dp(context, 22), dp(context, 24), dp(context, 20));
        sheet.setBackground(round(Color.WHITE, dp(context, 28)));

        ImageView taka = new ImageView(context);
        taka.setImageResource(R.drawable.taka_cutout);
        taka.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        LinearLayout.LayoutParams takaParams = new LinearLayout.LayoutParams(-1, dp(context, 88));
        sheet.addView(taka, takaParams);

        TextView title = text(context, "Welcome to\nTrackUtang", 25, Color.rgb(22, 50, 79));
        title.setTypeface(null, 1);
        title.setGravity(Gravity.CENTER);
        title.setLineSpacing(dp(context, 2), 1f);
        sheet.addView(title);

        TextView subtitle = text(context, "Let’s personalize your private money space.", 14, Color.rgb(69, 86, 99));
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, dp(context, 7), 0, dp(context, 20));
        sheet.addView(subtitle);

        TextView label = text(context, "WHAT SHOULD WE CALL YOU?", 12, Color.rgb(20, 143, 143));
        label.setTypeface(null, 1);
        label.setPadding(dp(context, 4), 0, 0, dp(context, 7));
        sheet.addView(label);

        EditText name = new EditText(context);
        name.setHint("Your first name or nickname");
        name.setTextSize(17);
        name.setSingleLine();
        name.setPadding(dp(context, 16), 0, dp(context, 16), 0);
        GradientDrawable field = round(Color.rgb(247, 250, 255), dp(context, 16));
        field.setStroke(dp(context, 1), Color.rgb(164, 207, 204));
        name.setBackground(field);
        sheet.addView(name, new LinearLayout.LayoutParams(-1, dp(context, 56)));

        TextView privateNote = text(context, "Your name stays only on this device. No account or login is needed.", 13, Color.rgb(69, 86, 99));
        privateNote.setLineSpacing(dp(context, 3), 1f);
        privateNote.setPadding(dp(context, 4), dp(context, 12), dp(context, 4), dp(context, 18));
        sheet.addView(privateNote);

        Button continueButton = new Button(context);
        continueButton.setText("Let’s get started");
        continueButton.setAllCaps(false);
        continueButton.setTypeface(null, 1);
        continueButton.setTextColor(Color.WHITE);
        continueButton.setTextSize(15);
        continueButton.setBackground(round(Color.rgb(20, 143, 143), dp(context, 16)));
        continueButton.setOnClickListener(v -> { listener.onContinue(name.getText().toString().trim()); dialog.dismiss(); });
        sheet.addView(continueButton, new LinearLayout.LayoutParams(-1, dp(context, 52)));

        dialog.setContentView(sheet);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout((int) (context.getResources().getDisplayMetrics().widthPixels * .90f), ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
        }
        dialog.setCancelable(false);
        dialog.show();
        if (dialog.getWindow() != null) dialog.getWindow().setLayout((int) (context.getResources().getDisplayMetrics().widthPixels * .90f), ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private static TextView text(Context context, String content, int size, int color) {
        TextView view = new TextView(context);
        view.setText(content);
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }

    private static GradientDrawable round(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private static int dp(Context context, int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + .5f);
    }
}
