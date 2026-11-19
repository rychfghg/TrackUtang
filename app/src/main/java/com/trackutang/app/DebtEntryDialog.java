package com.trackutang.app;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public final class DebtEntryDialog {
    private DebtEntryDialog() { }

    public static void show(Context context, DebtDatabase database, String type, Runnable onSaved) {
        LinearLayout form = new LinearLayout(context);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(context, 2), 0, dp(context, 2), dp(context, 4));

        EditText person = field(context, type.equals("Borrowed") ? "Lender or provider name *" : "Person's name *", InputType.TYPE_CLASS_TEXT);
        EditText contact = field(context, "Contact number (optional)", InputType.TYPE_CLASS_PHONE);
        EditText amount = field(context, type.equals("Borrowed") ? "Money borrowed (principal) *" : "Money lent (principal) *", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText months = field(context, "Number of months (optional)", InputType.TYPE_CLASS_NUMBER);
        EditText interest = field(context, "Interest per month % (optional)", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText tag = field(context, "Tag (Urgent, Family, Business, optional)", InputType.TYPE_CLASS_TEXT);
        TextView statusLabel = new TextView(context);
        statusLabel.setText("CUSTOM STATUS (OPTIONAL)"); statusLabel.setTextSize(12); statusLabel.setTypeface(null, 1); statusLabel.setTextColor(Color.rgb(20, 143, 143));
        Spinner status = new Spinner(context);
        String[] statuses = new String[]{"No custom status", "Waiting for salary", "Promise to pay", "Under review"};
        status.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, statuses));
        styleDropdown(status, context);
        DatePickerField dueDate = new DatePickerField(context);
        EditText notes = field(context, "Notes (optional)", InputType.TYPE_CLASS_TEXT);

        TextView sourceLabel = new TextView(context);
        sourceLabel.setText(type.equals("Borrowed") ? "BORROWED THROUGH" : "RELATIONSHIP");
        sourceLabel.setTextSize(12); sourceLabel.setTypeface(null, 1);
        sourceLabel.setTextColor(Color.rgb(20, 143, 143));

        Spinner source = new Spinner(context);
        String[] choices = type.equals("Borrowed")
                ? ProviderCatalog.borrowedProviders()
                : new String[]{"Friend", "Family", "Business", "Employee", "Customer", "Personal", "Other"};
        source.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, choices));
        styleDropdown(source, context);

        section(form, "WHO IS THIS WITH?", type.equals("Borrowed") ? "Add the lender or provider." : "Add the person who owes you.", context);
        add(form, person, 54, context);
        add(form, contact, 54, context);
        section(form, "AMOUNT & PLAN", "Leave months blank for a one-time payment.", context);
        add(form, amount, 54, context);
        add(form, months, 54, context);
        add(form, interest, 54, context);
        TextView planPreview = new TextView(context);
        planPreview.setTextSize(14);
        planPreview.setTextColor(Color.rgb(41, 68, 95));
        planPreview.setLineSpacing(dp(context, 3), 1f);
        planPreview.setPadding(dp(context, 15), dp(context, 12), dp(context, 15), dp(context, 12));
        planPreview.setBackground(previewBackground(context));
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(-1, -2); previewParams.setMargins(0, 0, 0, dp(context, 14)); form.addView(planPreview, previewParams);
        Runnable updatePlan = () -> updatePlanPreview(amount, months, interest, planPreview, type);
        updatePlan.run();
        TextWatcher planWatcher = new TextWatcher() { public void beforeTextChanged(CharSequence text, int start, int count, int after) { } public void onTextChanged(CharSequence text, int start, int before, int count) { updatePlan.run(); } public void afterTextChanged(Editable text) { } };
        amount.addTextChangedListener(planWatcher); months.addTextChangedListener(planWatcher); interest.addTextChangedListener(planWatcher);
        section(form, "ORGANIZE", "A tag and due date make records easier to find.", context);
        add(form, tag, 54, context);
        add(form, statusLabel, 30, context);
        add(form, dropdownField(status, context), 48, context);
        add(form, sourceLabel, 30, context);
        add(form, dropdownField(source, context), 48, context);
        add(form, dueDate, 54, context);
        add(form, notes, 54, context);

        final android.app.Dialog[] modal = new android.app.Dialog[1];
        modal[0] = TrackModal.show(context, "New " + type.toLowerCase() + " record", "Your record stays private on this device.", form, "Save record", () -> {
            try {
                double value = Double.parseDouble(amount.getText().toString());
                String name = person.getText().toString().trim();
                if (name.isEmpty() || value <= 0) throw new NumberFormatException();
                int termMonths = months.getText().toString().trim().isEmpty() ? 0 : Integer.parseInt(months.getText().toString());
                double monthlyInterest = interest.getText().toString().trim().isEmpty() ? 0 : Double.parseDouble(interest.getText().toString());
                double monthlyAmount = InstallmentCalculator.monthlyPayment(value, termMonths, monthlyInterest);
                double totalPayable = termMonths > 0 ? Math.round(monthlyAmount * termMonths * 100.0) / 100.0 : value;
                long id = database.addDebt(name, contact.getText().toString().trim(),
                        source.getSelectedItem().toString(), notes.getText().toString().trim(),
                        totalPayable, type, dueDate.getDate());
                if (termMonths > 0) database.applyInstallmentPlan(id, value, totalPayable, monthlyAmount, termMonths, Math.max(0, monthlyInterest));
                database.setTag(id, tag.getText().toString().trim());
                if (status.getSelectedItemPosition() > 0) database.setCustomStatus(id, status.getSelectedItem().toString());
                ReminderScheduler.schedule(context, id, name, value, dueDate.getDate());
                modal[0].dismiss();
                onSaved.run();
            } catch (NumberFormatException error) {
                Toast.makeText(context, "Add a name and valid amount", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static EditText field(Context context, String hint, int inputType) {
        EditText field = new EditText(context);
        field.setHint(hint);
        field.setTextSize(16);
        field.setTextColor(Color.rgb(22, 50, 79));
        field.setHintTextColor(Color.rgb(103, 120, 130));
        field.setInputType(inputType);
        field.setSingleLine();
        field.setPadding(dp(context, 14), 0, dp(context, 14), 0);
        field.setBackground(background(context));
        return field;
    }

    private static void updatePlanPreview(EditText amount, EditText months, EditText interest, TextView view, String type) {
        try {
            double principal = Double.parseDouble(amount.getText().toString());
            int term = Integer.parseInt(months.getText().toString());
            double rate = interest.getText().toString().trim().isEmpty() ? 0 : Double.parseDouble(interest.getText().toString());
            if (principal <= 0 || term <= 0 || rate < 0) throw new NumberFormatException();
            double monthly = InstallmentCalculator.monthlyPayment(principal, term, rate);
            double total = Math.round(monthly * term * 100.0) / 100.0;
            java.text.NumberFormat money = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("en", "PH"));
            view.setText("Monthly payment: " + money.format(monthly) + "\nTotal payable: " + money.format(total) + "\nIncludes interest: " + money.format(total - principal));
        } catch (Exception error) {
            view.setText("Add principal and number of months to see the monthly payment and total payable.");
        }
    }

    private static void add(LinearLayout form, View view, int height, Context context) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(context, height));
        params.setMargins(0, 0, 0, dp(context, 9));
        form.addView(view, params);
    }

    private static GradientDrawable background(Context context) {
        GradientDrawable result = new GradientDrawable();
        result.setColor(Color.WHITE);
        result.setCornerRadius(dp(context, 14));
        result.setStroke(dp(context, 1), Color.rgb(214, 226, 229));
        return result;
    }

    private static GradientDrawable previewBackground(Context context) {
        GradientDrawable result = new GradientDrawable();
        result.setColor(Color.rgb(234, 248, 245));
        result.setCornerRadius(dp(context, 16));
        result.setStroke(dp(context, 1), Color.rgb(210, 235, 230));
        return result;
    }

    private static void section(LinearLayout parent, String title, String detail, Context context) {
        TextView heading = new TextView(context);
        heading.setText(title);
        heading.setTextSize(12);
        heading.setTypeface(null, 1);
        heading.setTextColor(Color.rgb(20, 143, 143));
        LinearLayout.LayoutParams headingParams = new LinearLayout.LayoutParams(-1, -2);
        headingParams.setMargins(0, dp(context, 8), 0, dp(context, 3));
        parent.addView(heading, headingParams);
        TextView caption = new TextView(context);
        caption.setText(detail);
        caption.setTextSize(13);
        caption.setTextColor(Color.rgb(88, 104, 114));
        LinearLayout.LayoutParams captionParams = new LinearLayout.LayoutParams(-1, -2);
        captionParams.setMargins(0, 0, 0, dp(context, 9));
        parent.addView(caption, captionParams);
    }

    private static void styleDropdown(Spinner spinner, Context context) {
        spinner.setPadding(dp(context, 14), 0, dp(context, 14), 0);
        spinner.setBackground(background(context));
        spinner.setPopupBackgroundDrawable(background(context));
    }

    private static FrameLayout dropdownField(Spinner spinner, Context context) {
        FrameLayout field = new FrameLayout(context);
        field.addView(spinner, new FrameLayout.LayoutParams(-1, -1));
        TextView arrow = new TextView(context);
        arrow.setText("⌄"); arrow.setTextSize(22); arrow.setTextColor(Color.rgb(69, 86, 99)); arrow.setGravity(android.view.Gravity.CENTER);
        arrow.setClickable(false); arrow.setFocusable(false);
        FrameLayout.LayoutParams arrowParams = new FrameLayout.LayoutParams(dp(context, 44), -1, android.view.Gravity.END);
        field.addView(arrow, arrowParams);
        return field;
    }

    private static int dp(Context context, int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + .5f);
    }
}
