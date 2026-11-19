package com.trackutang.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public final class BackupReminderScheduler {
    private BackupReminderScheduler() { }
    public static void schedule(Context context) {
        Intent intent = new Intent(context, BackupReminderReceiver.class);
        PendingIntent pending = PendingIntent.getBroadcast(context, 44, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long month = 30L * 24L * 60L * 60L * 1000L;
        alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + month, month, pending);
    }
}
