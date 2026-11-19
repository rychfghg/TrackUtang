package com.trackutang.app;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.text.NumberFormat;
import java.util.Locale;

/** A compact payment form that explains exactly what is due before saving. */
public final class PaymentDialog {
    private PaymentDialog() { }

    public static void show(Context context, DebtDatabase db, DebtDatabase.Debt debt, Runnable onSaved) {
        LinearLayout form = new LinearLayout(context);
        form.setOrientation(LinearLayout.VERTICAL);
        NumberFormat money = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));

        if (debt.monthlyAmount > 0) {
            form.addView(summary(context,
                    "Pay now",
                    money.format(debt.monthlyBalance()),
                    "Normal monthly payment: " + money.format(debt.monthlyAmount) + "\nTotal balance left: " + money.format(debt.remaining())));
        } else {
            form.addView(summary(context,
                    "Balance left",
                    money.format(debt.remaining()),
                    "Enter the amount received or paid."));
        }

        EditText amount = paymentField(context);
        if (debt.monthlyAmount > 0) {
            amount.setText(String.format(Locale.US, "%.2f", debt.monthlyBalance()));
        }
        LinearLayout.LayoutParams fieldParams = new LinearLayout.LayoutParams(-1, dp(context, 58));
        fieldParams.setMargins(0, dp(context, 16), 0, 0);
        form.addView(amount, fieldParams);

        EditText note = paymentNoteField(context);
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(-1, dp(context, 58));
        noteParams.setMargins(0, dp(context, 10), 0, 0);
        form.addView(note, noteParams);

        TextView result = new TextView(context);
        result.setTextSize(14);
        result.setTextColor(Color.rgb(69, 86, 99));
        result.setLineSpacing(dp(context, 3), 1f);
        result.setPadding(dp(context, 4), dp(context, 12), dp(context, 4), 0);
        form.addView(result);

        Runnable refresh = () -> updateResult(amount, result, debt, money);
        refresh.run();
        amount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence text, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence text, int start, int before, int count) { refresh.run(); }
            @Override public void afterTextChanged(Editable text) { }
        });

        final Dialog[] modal = new Dialog[1];
        String subtitle = debt.monthlyAmount > 0
                ? "Pay the amount due now, or enter a different amount."
                : "Your remaining balance updates right away.";
        modal[0] = TrackModal.show(context, "Record payment", subtitle, form, "Save payment", () -> {
            try {
                double entered = Double.parseDouble(amount.getText().toString());
                if (entered <= 0) throw new NumberFormatException();
                String paymentNote = note.getText().toString().trim();
                db.addPayment(debt.id, Math.min(entered, debt.remaining()), paymentNote);
                modal[0].dismiss();
                onSaved.run();
            } catch (NumberFormatException error) {
                Toast.makeText(context, "Enter a valid payment amount", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static void updateResult(EditText field, TextView result, DebtDatabase.Debt debt, NumberFormat money) {
        try {
            double entered = Double.parseDouble(field.getText().toString());
            double applied = Math.min(Math.max(0, entered), debt.remaining());
            if (debt.monthlyAmount <= 0) {
                result.setText("Balance after payment: " + money.format(debt.remaining() - applied));
                return;
            }
            double dueNow = debt.monthlyBalance();
            String message;
            if (applied < dueNow - .005) {
                message = money.format(dueNow - applied) + " still due for this month.";
            } else if (Math.abs(applied - dueNow) < .005) {
                message = "This month's payment is complete.";
            } else {
                message = "This month is complete. The extra reduces next month's amount.";
            }
            result.setText(message + "\nNext amount due: " + money.format(debt.monthlyBalanceAfter(applied)));
        } catch (Exception error) {
            result.setText("Enter an amount to see what will be due next.");
        }
    }

    private static TextView summary(Context context, String label, String amount, String detail) {
        TextView view = new TextView(context);
        view.setText(label + "\n" + amount + "\n" + detail);
        view.setTextSize(17);
        view.setTextColor(Color.rgb(41, 68, 95));
        view.setLineSpacing(dp(context, 4), 1f);
        view.setPadding(dp(context, 20), dp(context, 18), dp(context, 20), dp(context, 18));
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(237, 242, 253));
        background.setCornerRadius(dp(context, 20));
        view.setBackground(background);
        return view;
    }

    private static EditText paymentField(Context context) {
        EditText field = new EditText(context);
        field.setHint("Amount to pay");
        field.setTextSize(17);
        field.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        field.setSingleLine();
        field.setPadding(dp(context, 18), 0, dp(context, 18), 0);
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(dp(context, 16));
        background.setStroke(dp(context, 1), Color.rgb(208, 220, 233));
        field.setBackground(background);
        return field;
    }

    private static EditText paymentNoteField(Context context) {
        EditText field = paymentField(context);
        field.setHint("Paid via GCash, reference no. (optional)");
        field.setInputType(InputType.TYPE_CLASS_TEXT);
        return field;
    }

    private static int dp(Context context, int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + .5f);
    }
}
