package com.trackutang.app;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DatePickerField extends LinearLayout {
    private final TextView value;
    private final Calendar selected = Calendar.getInstance();
    private boolean hasDate;

    public DatePickerField(Context context) {
        super(context);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setPadding(dp(14), 0, dp(12), 0);
        setBackground(outline());
        setClickable(true);

        value = new TextView(context);
        value.setText("Select due date (optional)");
        value.setTextSize(16);
        value.setTextColor(Color.rgb(88, 104, 114));
        value.setGravity(Gravity.CENTER);
        addView(value, new LayoutParams(0, LayoutParams.MATCH_PARENT, 1));

        TextView icon = new TextView(context);
        icon.setText("▾");
        icon.setTextSize(22);
        icon.setTextColor(Color.rgb(20, 143, 143));
        icon.setGravity(Gravity.CENTER);
        addView(icon, new LayoutParams(dp(34), LayoutParams.MATCH_PARENT));

        View.OnClickListener open = v -> showCalendar();
        setOnClickListener(open);
        icon.setOnClickListener(open);
    }

    public String getDate() {
        return hasDate ? new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selected.getTime()) : "";
    }
    public void setDate(String date) {
        try {
            selected.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date));
            hasDate = true;
            value.setText(new SimpleDateFormat("MMM d, yyyy", Locale.US).format(selected.getTime()));
            value.setTextColor(Color.rgb(22, 50, 79));
        } catch (Exception ignored) { }
    }

    private void showCalendar() {
        new DatePickerDialog(getContext(), (view, year, month, day) -> {
            selected.set(year, month, day);
            hasDate = true;
            value.setText(new SimpleDateFormat("MMM d, yyyy", Locale.US).format(selected.getTime()));
            value.setTextColor(Color.rgb(22, 50, 79));
        }, selected.get(Calendar.YEAR), selected.get(Calendar.MONTH), selected.get(Calendar.DAY_OF_MONTH)).show();
    }

    private GradientDrawable outline() {
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(dp(14));
        background.setStroke(dp(1), Color.rgb(214, 226, 229));
        return background;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + .5f);
    }
}
