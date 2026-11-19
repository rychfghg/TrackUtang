package com.trackutang.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/** A quiet local notification that offers fast entry points without requiring an account. */
public final class QuickAddNotifier {
    private static final String CHANNEL_ID = "quick_add";
    private static final int NOTIFICATION_ID = 709;

    private QuickAddNotifier() { }

    public static void show(Context context) {
        createChannel(context);
        Intent open = new Intent(context, MainActivity.class);
        open.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent content = PendingIntent.getActivity(context, 700, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent lend = action(context, "Lent", 701);
        PendingIntent borrow = action(context, "Borrowed", 702);
        android.app.Notification.Builder notification = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new android.app.Notification.Builder(context, CHANNEL_ID)
                : new android.app.Notification.Builder(context);
        notification.setSmallIcon(R.drawable.ic_home)
                .setContentTitle("TrackUtang quick add")
                .setContentText("Add a lent or borrowed record in one tap.")
                .setPriority(android.app.Notification.PRIORITY_LOW)
                .setOngoing(true)
                .setContentIntent(content)
                .addAction(new android.app.Notification.Action.Builder(0, "Lend", lend).build())
                .addAction(new android.app.Notification.Action.Builder(0, "Borrow", borrow).build());
        context.getSystemService(NotificationManager.class).notify(NOTIFICATION_ID, notification.build());
    }

    private static PendingIntent action(Context context, String type, int requestCode) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("quick_add_type", type);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Quick add", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("One-tap shortcuts for adding TrackUtang records");
            context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }
}
