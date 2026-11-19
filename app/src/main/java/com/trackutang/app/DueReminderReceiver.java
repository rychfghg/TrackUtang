package com.trackutang.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/** Posts local due-date, overdue-review, and annual open-debt reminders. */
public class DueReminderReceiver extends BroadcastReceiver {
    public static final String CHANNEL_ID = "due_reminders";

    @Override public void onReceive(Context context, Intent intent) {
        createChannel(context);
        String person = intent.getStringExtra("person");
        String amount = intent.getStringExtra("amount");
        String kind = intent.getStringExtra("kind");
        long debtId = intent.getLongExtra("debt_id", -1);

        Intent openApp = new Intent(context, MainActivity.class);
        openApp.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (debtId > 0) openApp.putExtra("open_debt_id", debtId);
        PendingIntent pending = PendingIntent.getActivity(context, (int) debtId, openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String title = "TrackUtang due today";
        String message = person + " is due today" + (amount == null ? "" : " · " + amount);
        int notificationId = debtId > 0 ? (int) debtId : (int) System.currentTimeMillis();
        if ("overdue_update".equals(kind)) {
            title = "Update this overdue record?";
            message = person + " is still open. Review the due date or record a payment.";
            notificationId += 200000;
        } else if ("anniversary".equals(kind)) {
            DebtDatabase.Debt debt = new DebtDatabase(context).debt(debtId);
            if (debt == null || debt.remaining() <= 0) return;
            title = "Open-debt anniversary";
            message = person + " has remained open for one year. Review this record when ready.";
            notificationId += 100000;
            ReminderScheduler.scheduleAnniversary(context, debt);
        }

        android.app.Notification.Builder notification = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new android.app.Notification.Builder(context, CHANNEL_ID)
                : new android.app.Notification.Builder(context);
        notification.setSmallIcon(R.drawable.ic_borrowed)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new android.app.Notification.BigTextStyle().bigText(message))
                .setPriority(android.app.Notification.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pending);
        context.getSystemService(NotificationManager.class).notify(notificationId, notification.build());
    }

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Due-date reminders", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Local reminders for TrackUtang due dates and open records");
            context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }
}
