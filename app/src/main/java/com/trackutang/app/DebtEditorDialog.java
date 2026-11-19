package com.trackutang.app;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/** Editor matching the add-record form, with no raw date typing required. */
public final class DebtEditorDialog {
    private DebtEditorDialog() { }

    public static void show(Context context, DebtDatabase db, DebtDatabase.Debt debt, Runnable onSaved) {
        LinearLayout form = new LinearLayout(context);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(context, 2), 0, dp(context, 2), dp(context, 4));

        EditText name = field(context, "Name *", debt.person, InputType.TYPE_CLASS_TEXT);
        EditText contact = field(context, "Contact number", debt.contact, InputType.TYPE_CLASS_PHONE);
        EditText amount = field(context, "Amount in PHP *", String.valueOf(debt.amount), InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText notes = field(context, "Notes", debt.note, InputType.TYPE_CLASS_TEXT);
        DatePickerField dueDate = new DatePickerField(context);
        dueDate.setDate(debt.due);

        TextView sourceLabel = new TextView(context);
        sourceLabel.setText(debt.type.equals("Borrowed") ? "BORROWED THROUGH" : "RELATIONSHIP");
        sourceLabel.setTextSize(12); sourceLabel.setTypeface(null, 1);
        sourceLabel.setTextColor(Color.rgb(20, 143, 143));
        Spinner source = new Spinner(context);
        String[] choices = debt.type.equals("Borrowed")
                ? ProviderCatalog.borrowedProviders()
                : new String[]{"Friend", "Family", "Business", "Employee", "Customer", "Personal", "Other"};
        source.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, choices));
        for (int i = 0; i < choices.length; i++) if (choices[i].equalsIgnoreCase(debt.category)) source.setSelection(i);
        styleDropdown(source, context);

        TextView statusLabel = new TextView(context);
        statusLabel.setText("CUSTOM STATUS (OPTIONAL)");
        statusLabel.setTextSize(12); statusLabel.setTypeface(null, 1);
        statusLabel.setTextColor(Color.rgb(20, 143, 143));
        Spinner status = new Spinner(context);
        String[] statuses = new String[]{"No custom status", "Waiting for salary", "Promise to pay", "Under review"};
        status.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, statuses));
        for (int i = 0; i < statuses.length; i++) if (statuses[i].equalsIgnoreCase(debt.customStatus)) status.setSelection(i);
        styleDropdown(status, context);

        section(form, "RECORD DETAILS", "Update the person and amount carefully.", context);
        add(form, name, 54, context); add(form, contact, 54, context); add(form, amount, 54, context);
        section(form, "ORGANIZE", "Keep the relationship, status, and due date up to date.", context);
        add(form, sourceLabel, 28, context); add(form, dropdownField(source, context), 48, context); add(form, statusLabel, 28, context); add(form, dropdownField(status, context), 48, context); add(form, dueDate, 54, context); add(form, notes, 54, context);

        final Dialog[] modal = new Dialog[1];
        modal[0] = TrackModal.show(context, "Edit record", "Update this record. Your payment history stays intact.", form, "Save changes", () -> {
            try {
                double value = Double.parseDouble(amount.getText().toString());
                String person = name.getText().toString().trim();
                if (person.isEmpty() || value <= 0) throw new NumberFormatException();
                db.updateDebt(debt.id, person, contact.getText().toString().trim(), source.getSelectedItem().toString(), notes.getText().toString().trim(), value, dueDate.getDate());
                db.setCustomStatus(debt.id, status.getSelectedItemPosition() == 0 ? "" : status.getSelectedItem().toString());
                modal[0].dismiss(); onSaved.run();
            } catch (NumberFormatException error) { Toast.makeText(context, "Add a valid name and amount", Toast.LENGTH_SHORT).show(); }
        });
    }
    private static EditText field(Context context, String hint, String value, int input) { EditText view = new EditText(context); view.setHint(hint); view.setText(value == null ? "" : value); view.setTextSize(16); view.setTextColor(Color.rgb(22, 50, 79)); view.setHintTextColor(Color.rgb(103, 120, 130)); view.setInputType(input); view.setSingleLine(); view.setPadding(dp(context, 14), 0, dp(context, 14), 0); view.setBackground(background(context)); return view; }
    private static void add(LinearLayout parent, View view, int height, Context context) { LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(context, height)); params.setMargins(0, 0, 0, dp(context, 9)); parent.addView(view, params); }
    private static GradientDrawable background(Context context) { GradientDrawable shape = new GradientDrawable(); shape.setColor(Color.WHITE); shape.setCornerRadius(dp(context, 14)); shape.setStroke(dp(context, 1), Color.rgb(214, 226, 229)); return shape; }
    private static void section(LinearLayout parent, String title, String detail, Context context) { TextView heading = new TextView(context); heading.setText(title); heading.setTextSize(12); heading.setTypeface(null, 1); heading.setTextColor(Color.rgb(20, 143, 143)); LinearLayout.LayoutParams headingParams = new LinearLayout.LayoutParams(-1, -2); headingParams.setMargins(0, dp(context, 8), 0, dp(context, 3)); parent.addView(heading, headingParams); TextView caption = new TextView(context); caption.setText(detail); caption.setTextSize(13); caption.setTextColor(Color.rgb(88, 104, 114)); LinearLayout.LayoutParams captionParams = new LinearLayout.LayoutParams(-1, -2); captionParams.setMargins(0, 0, 0, dp(context, 9)); parent.addView(caption, captionParams); }
    private static void styleDropdown(Spinner spinner, Context context) { spinner.setPadding(dp(context, 14), 0, dp(context, 14), 0); spinner.setBackground(background(context)); spinner.setPopupBackgroundDrawable(background(context)); }
    private static FrameLayout dropdownField(Spinner spinner, Context context) { FrameLayout field = new FrameLayout(context); field.addView(spinner, new FrameLayout.LayoutParams(-1, -1)); TextView arrow = new TextView(context); arrow.setText("⌄"); arrow.setTextSize(22); arrow.setTextColor(Color.rgb(69, 86, 99)); arrow.setGravity(android.view.Gravity.CENTER); arrow.setClickable(false); arrow.setFocusable(false); field.addView(arrow, new FrameLayout.LayoutParams(dp(context, 44), -1, android.view.Gravity.END)); return field; }
    private static int dp(Context context, int value) { return (int) (value * context.getResources().getDisplayMetrics().density + .5f); }
}
