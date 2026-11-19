package com.trackutang.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class ReminderScheduler {
    private ReminderScheduler() { }

    public static void schedule(Context context, long debtId, String person, double amount, String dueDate) {
        try {
            Date parsed = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dueDate);
            Calendar due = Calendar.getInstance();
            due.setTime(parsed);
            due.set(Calendar.HOUR_OF_DAY, 9);
            due.set(Calendar.MINUTE, 0);
            due.set(Calendar.SECOND, 0);
            if (due.getTimeInMillis() <= System.currentTimeMillis()) return;

            Intent intent = new Intent(context, DueReminderReceiver.class);
            intent.putExtra("debt_id", debtId);
            intent.putExtra("person", person);
            intent.putExtra("amount", String.format(Locale.US, "₱%,.2f", amount));
            PendingIntent alarm = PendingIntent.getBroadcast(context, (int) debtId, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, due.getTimeInMillis(), alarm);
        } catch (Exception ignored) {
            // A blank or invalid optional date simply has no reminder.
        }
    }

    /** A one-time nudge the morning after a due date, with a direct link to the record. */
    public static void scheduleOverdueUpdate(Context context, long debtId, String person, double amount, String dueDate) {
        try {
            Date parsed = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dueDate);
            Calendar reminder = Calendar.getInstance();
            reminder.setTime(parsed);
            reminder.add(Calendar.DAY_OF_YEAR, 1);
            reminder.set(Calendar.HOUR_OF_DAY, 10);
            reminder.set(Calendar.MINUTE, 0);
            reminder.set(Calendar.SECOND, 0);
            if (reminder.getTimeInMillis() <= System.currentTimeMillis()) return;
            Intent intent = new Intent(context, DueReminderReceiver.class);
            intent.putExtra("kind", "overdue_update");
            intent.putExtra("debt_id", debtId);
            intent.putExtra("person", person);
            intent.putExtra("amount", String.format(Locale.US, "₱%,.2f", amount));
            PendingIntent alarm = PendingIntent.getBroadcast(context, (int) (500000 + debtId), intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.getTimeInMillis(), alarm);
        } catch (Exception ignored) { }
    }

    /** Reminds the user every year only while the original debt is still open. */
    public static void scheduleAnniversary(Context context, DebtDatabase.Debt debt) {
        if (debt == null || debt.remaining() <= 0) return;
        Calendar anniversary = Calendar.getInstance();
        anniversary.setTimeInMillis(debt.created);
        anniversary.add(Calendar.YEAR, 1);
        anniversary.set(Calendar.HOUR_OF_DAY, 10);
        anniversary.set(Calendar.MINUTE, 0);
        anniversary.set(Calendar.SECOND, 0);
        while (anniversary.getTimeInMillis() <= System.currentTimeMillis()) anniversary.add(Calendar.YEAR, 1);
        Intent intent = new Intent(context, DueReminderReceiver.class);
        intent.putExtra("kind", "anniversary");
        intent.putExtra("debt_id", debt.id);
        intent.putExtra("person", debt.person);
        PendingIntent alarm = PendingIntent.getBroadcast(context, (int) (900000 + debt.id), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, anniversary.getTimeInMillis(), alarm);
    }
}
