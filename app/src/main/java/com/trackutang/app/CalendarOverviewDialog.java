package com.trackutang.app;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Due-date calendar agenda for all local debt records. */
public final class CalendarOverviewDialog {
    private CalendarOverviewDialog() { }
    public static void show(Context context, DebtDatabase db) {
        LinearLayout content = new LinearLayout(context); content.setOrientation(LinearLayout.VERTICAL);
        List<DebtDatabase.Debt> debts = new ArrayList<>(db.debts("", "All"));
        Collections.sort(debts, Comparator.comparing(d -> d.due == null || d.due.isEmpty() ? "9999-12-31" : d.due));
        SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String now = today.format(new Date());
        for (DebtDatabase.Debt debt : debts) {
            if (debt.due == null || debt.due.isEmpty() || debt.remaining() <= 0) continue;
            boolean overdue = debt.due.compareTo(now) < 0;
            LinearLayout row = new LinearLayout(context); row.setOrientation(LinearLayout.VERTICAL); row.setPadding(18,14,18,14);
            GradientDrawable bg = new GradientDrawable(); bg.setColor(overdue ? Color.rgb(253, 241, 238) : Color.rgb(234, 248, 245)); bg.setCornerRadius(18); row.setBackground(bg);
            TextView title = new TextView(context); title.setText(debt.due + "  ·  " + debt.person); title.setTextSize(16); title.setTextColor(Color.rgb(22,50,79));
            TextView detail = new TextView(context); detail.setText((overdue ? "Overdue" : debt.due.equals(now) ? "Due today" : "Upcoming") + "  •  Remaining ₱" + String.format(Locale.US,"%,.2f",debt.remaining())); detail.setTextSize(13); detail.setTextColor(Color.rgb(69,86,99)); detail.setPadding(0,4,0,0);
            row.addView(title); row.addView(detail); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.setMargins(0,0,0,10); content.addView(row,p);
        }
        if(content.getChildCount()==0){TextView empty=new TextView(context);empty.setText("No upcoming due dates. Add a due date to a record to see it here.");empty.setTextSize(15);empty.setTextColor(Color.rgb(69,86,99));empty.setPadding(16,28,16,28);content.addView(empty);}
        final Dialog[] modal=new Dialog[1]; modal[0]=TrackModal.show(context,"Payment calendar","Due dates and overdue balances",content,"Done",()->modal[0].dismiss());
    }
}
