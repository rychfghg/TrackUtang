package com.trackutang.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/** Sends a calm monthly local-backup reminder only when a backup is overdue. */
public class BackupReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL = "backup_reminders";
    private static final long MONTH = 30L * 24L * 60L * 60L * 1000L;

    @Override public void onReceive(Context context, Intent intent) {
        long lastBackup = context.getSharedPreferences("trackutang", 0).getLong("last_backup", 0);
        if (lastBackup > 0 && System.currentTimeMillis() - lastBackup < MONTH) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL, "Backup reminders", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Gentle reminders to create a local TrackUtang backup");
            context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        Intent open = new Intent(context, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(context, 44, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        android.app.Notification.Builder notice = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new android.app.Notification.Builder(context, CHANNEL)
                : new android.app.Notification.Builder(context);
        notice.setSmallIcon(R.drawable.ic_home).setContentTitle("Create a local backup")
                .setContentText("Keep your TrackUtang records safe with a CSV backup.")
                .setAutoCancel(true).setContentIntent(pending);
        context.getSystemService(NotificationManager.class).notify(4401, notice.build());
    }
}
