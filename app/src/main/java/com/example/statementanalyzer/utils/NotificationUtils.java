package com.example.statementanalyzer.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.Manifest;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.statementanalyzer.R;
import com.example.statementanalyzer.activities.MainActivity;
import com.example.statementanalyzer.data.PreferenceManager;

public class NotificationUtils {

    private static final String CHANNEL_ID = "statement_analyzer_channel";
    private static final String CHANNEL_NAME = "Statement Analyzer Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for Statement Analyzer app";

    // Create notification channel for Android O and above
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESCRIPTION);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // Show a notification
    public static void showNotification(Context context, String title, String message, int notificationId) {
        PreferenceManager preferenceManager = new PreferenceManager(context);

        // Check if notifications are enabled in app preferences
        if (!preferenceManager.isNotificationEnabled()) {
            return;
        }

        // Check if notification permission is granted (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                    android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return; // Permission not granted, do not show notification
            }
        }

        // Create intent for notification tap action
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(context.getResources().getColor(R.color.primary));

        // Show notification if the NotificationManager is available
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (notificationManager.areNotificationsEnabled()) {
            notificationManager.notify(notificationId, builder.build());
        }
    }

    // Show analysis complete notification
    public static void showAnalysisCompleteNotification(Context context) {
        showNotification(
                context,
                "Analysis Complete",
                "Your financial statement analysis is ready to view",
                1001
        );
    }

    // Show insights notification
    public static void showInsightsNotification(Context context, String insight) {
        showNotification(
                context,
                "New Financial Insight",
                insight,
                1002
        );
    }
}
