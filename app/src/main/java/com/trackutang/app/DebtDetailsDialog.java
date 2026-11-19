package com.trackutang.app;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Expanded, readable view of one debt record and its local payment timeline. */
public final class DebtDetailsDialog {
    private DebtDetailsDialog() { }
    public static void show(Context context, DebtDatabase db, DebtDatabase.Debt debt, Runnable onChanged) {
        LinearLayout content = new LinearLayout(context); content.setOrientation(LinearLayout.VERTICAL);
        NumberFormat money = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
        TextView amount = new TextView(context); amount.setText("Remaining balance\n" + money.format(debt.remaining())); amount.setTextSize(22); amount.setTypeface(null, 1); amount.setTextColor(Color.rgb(22, 50, 79)); amount.setPadding(20, 20, 20, 20); GradientDrawable summary = new GradientDrawable(); summary.setColor(Color.rgb(234, 248, 245)); summary.setCornerRadius(20); amount.setBackground(summary); content.addView(amount);
        String installment = debt.monthlyAmount > 0 ? "\nTerm: " + debt.termMonths + " months\nMonthly interest: " + debt.monthlyInterest + "%\nCalculated monthly payment: " + money.format(debt.monthlyAmount) + "\nPayments remaining: " + debt.installmentsRemaining : "";
        TextView details = new TextView(context); details.setText("Original amount: " + money.format(debt.amount) + "\nPaid so far: " + money.format(debt.paid) + installment + "\nProvider / category: " + (debt.category == null ? "Personal" : debt.category) + "\nDue date: " + (debt.due == null || debt.due.isEmpty() ? "Not set" : debt.due) + (debt.note == null || debt.note.isEmpty() ? "" : "\nNotes: " + debt.note)); details.setTextSize(15); details.setTextColor(Color.rgb(22, 50, 79)); details.setLineSpacing(6,1f); details.setPadding(4,20,4,16); content.addView(details);
        TextView history = new TextView(context); history.setText("PAYMENT TIMELINE"); history.setTextSize(12); history.setTypeface(null,1); history.setTextColor(Color.rgb(20,143,143)); content.addView(history);
        SimpleDateFormat date = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        for (DebtDatabase.Payment payment : db.payments(debt.id)) { TextView row = new TextView(context); row.setText(money.format(payment.amount) + "  ·  " + date.format(new Date(payment.paidAt))); row.setTextSize(14); row.setTextColor(Color.rgb(22,50,79)); row.setPadding(0,10,0,4); content.addView(row); }
        if (db.payments(debt.id).isEmpty()) { TextView none = new TextView(context); none.setText("No payments recorded yet."); none.setTextColor(Color.rgb(69,86,99)); none.setPadding(0,10,0,0); content.addView(none); }
        final Dialog[] modal = new Dialog[1]; modal[0] = TrackModal.show(context, debt.person, "Debt details and payment timeline", content, debt.remaining() > 0 ? "Record payment" : "Done", () -> { if (debt.remaining() > 0) { modal[0].dismiss(); PaymentDialog.show(context, db, debt, onChanged); } else modal[0].dismiss(); });
    }
}
