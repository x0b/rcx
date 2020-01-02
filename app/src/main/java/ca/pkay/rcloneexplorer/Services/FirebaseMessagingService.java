package ca.pkay.rcloneexplorer.Services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;


import ca.pkay.rcloneexplorer.R;

public class FirebaseMessagingService  {

    private final String CHANNEL_ID = "ca.pkay.rcexplorer.app_updates";
    private final String CHANNEL_NAME = "App updates";



    private void setNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("App updates notification");
            // Register the channel with the system

        }
    }
}
