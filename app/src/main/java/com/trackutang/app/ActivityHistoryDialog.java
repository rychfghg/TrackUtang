package com.trackutang.app;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Locale;

/** General activity ledger: payments and deleted records across Lent and Borrowed. */
public final class ActivityHistoryDialog {
    private ActivityHistoryDialog() { }

    public static void show(Context context, DebtDatabase db) {
        ScrollView scroll = new ScrollView(context);
        LinearLayout list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(28, 8, 28, 8);
        SimpleDateFormat format = new SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault());
        for (DebtDatabase.Activity event : db.activity()) {
            LinearLayout card = new LinearLayout(context);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(20, 18, 20, 18);
            card.setBackgroundColor(Color.rgb(242, 249, 255));
            TextView title = new TextView(context); title.setText(event.title); title.setTextSize(16); title.setTextColor(Color.rgb(41, 68, 95));
            TextView detail = new TextView(context); detail.setText(event.detail + "\n" + format.format(event.created)); detail.setTextSize(13); detail.setTextColor(Color.rgb(88, 104, 114)); detail.setPadding(0, 5, 0, 0);
            card.addView(title); card.addView(detail);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2); params.setMargins(0, 0, 0, 12); list.addView(card, params);
        }
        if (list.getChildCount() == 0) { TextView empty = new TextView(context); empty.setText("No activity yet. Payments and deleted records will appear here."); empty.setTextSize(15); empty.setTextColor(Color.rgb(88, 104, 114)); empty.setGravity(Gravity.CENTER); empty.setPadding(12, 50, 12, 50); list.addView(empty); }
        scroll.addView(list);
        AlertDialog dialog = new AlertDialog.Builder(context).setTitle("Transaction activity").setMessage("All payment and record activity").setView(scroll).setPositiveButton("Done", null).create();
        DialogStyler.show(dialog);
    }
}
