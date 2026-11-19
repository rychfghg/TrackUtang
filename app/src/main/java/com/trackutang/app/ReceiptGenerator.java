package com.trackutang.app;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Creates a shareable PNG receipt without sending financial data online. */
public final class ReceiptGenerator {
    private ReceiptGenerator() { }

    public static void writePng(OutputStream output, DebtDatabase.Debt debt) throws IOException {
        Bitmap bitmap = Bitmap.createBitmap(1080, 1350, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.rgb(247, 250, 255));
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(91, 124, 250));
        canvas.drawRoundRect(60, 70, 1020, 310, 44, 44, paint);
        String receiptNumber = "TU-REF-" + debt.id;
        String paymentReference = "PAY-" + debt.id + "-" + new SimpleDateFormat("HHmmss", Locale.US).format(new Date());
        paint.setColor(Color.WHITE); paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD)); paint.setTextSize(54);
        canvas.drawText("TrackUtang", 110, 155, paint);
        paint.setTypeface(Typeface.DEFAULT); paint.setTextSize(30); canvas.drawText("Payment record", 110, 210, paint);
        paint.setColor(Color.WHITE); paint.setTextSize(26); canvas.drawText("Receipt " + receiptNumber, 110, 265, paint);
        paint.setColor(Color.WHITE); canvas.drawRoundRect(60, 355, 1020, 1170, 44, 44, paint);
        paint.setColor(Color.rgb(41, 68, 95)); paint.setTextSize(34); paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD)); canvas.drawText(debt.person, 115, 450, paint);
        paint.setTypeface(Typeface.DEFAULT); paint.setTextSize(27); paint.setColor(Color.rgb(90, 105, 115)); canvas.drawText(debt.type.equals("Lent") ? "Money received" : "Payment made", 115, 500, paint);
        NumberFormat money = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
        paint.setColor(Color.rgb(56, 178, 157)); paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD)); paint.setTextSize(66); canvas.drawText(money.format(debt.paid), 115, 625, paint);
        paint.setTypeface(Typeface.DEFAULT); paint.setColor(Color.rgb(90, 105, 115)); paint.setTextSize(30);
        canvas.drawText(debt.monthlyAmount > 0 ? "Total payable" : "Original amount", 115, 735, paint); canvas.drawText(money.format(debt.amount), 720, 735, paint);
        canvas.drawText("Remaining balance", 115, 820, paint); canvas.drawText(money.format(debt.remaining()), 720, 820, paint);
        canvas.drawText("Category / provider", 115, 905, paint); canvas.drawText(debt.category == null ? "Personal" : debt.category, 620, 905, paint);
        canvas.drawText("Due date", 115, 990, paint); canvas.drawText(debt.due == null || debt.due.isEmpty() ? "Not set" : debt.due, 720, 990, paint);
        canvas.drawText("Payment reference", 115, 1060, paint); canvas.drawText(paymentReference, 620, 1060, paint);
        paint.setColor(Color.rgb(41, 68, 95)); paint.setTextSize(24); canvas.drawText("Private record • Saved only on your device", 115, 1130, paint);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output); output.close(); bitmap.recycle();
    }
}
