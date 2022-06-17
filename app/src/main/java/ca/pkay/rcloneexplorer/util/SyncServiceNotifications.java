package ca.pkay.rcloneexplorer.util;

import static ca.pkay.rcloneexplorer.Services.SyncService.EXTRA_TASK_ID;
import static ca.pkay.rcloneexplorer.Services.SyncService.TASK_ACTION;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;

import ca.pkay.rcloneexplorer.BroadcastReceivers.SyncCancelAction;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Services.SyncService;

public class SyncServiceNotifications {

    private final String OPERATION_FAILED_GROUP = "ca.pkay.rcexplorer.OPERATION_FAILED_GROUP";
    private final String OPERATION_SUCCESS_GROUP = "ca.pkay.rcexplorer.OPERATION_SUCCESS_GROUP";
    private static final String CHANNEL_ID = "ca.pkay.rcexplorer.sync_service";
    private static final String CHANNEL_NAME = "Sync service";

    public static final int PERSISTENT_NOTIFICATION_ID_FOR_SYNC = 162;
    private static final int OPERATION_FAILED_NOTIFICATION_ID = 89;
    private static final int OPERATION_SUCCESS_NOTIFICATION_ID = 698;

    Context mContext;

    public SyncServiceNotifications(Context context) {
        this.mContext = context;
    }

    public void showFailedNotification(String title, String content, int notificationId, long taskid) {

        Intent i = new Intent(mContext, SyncService.class);
        i.setAction(TASK_ACTION);
        i.putExtra(EXTRA_TASK_ID, taskid);
        PendingIntent retryPendingIntent = PendingIntent.getService(mContext, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_twotone_cloud_error_24)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setGroup(OPERATION_FAILED_GROUP)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .addAction(R.drawable.ic_refresh, mContext.getString(R.string.retry_failed_sync), retryPendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        notificationManager.notify(notificationId, builder.build());
        createSummaryNotificationForFailed();
    }

    public void showSuccessNotification(String content, int notificationId) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_twotone_cloud_done_24)
                .setContentTitle(mContext.getString(R.string.operation_success))
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setGroup(OPERATION_SUCCESS_GROUP)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        notificationManager.notify(notificationId, builder.build());
        createSummaryNotificationForSuccess();
    }

    public void createSummaryNotificationForFailed() {
        Notification summaryNotification =
                new NotificationCompat.Builder(mContext, CHANNEL_ID)
                        .setContentTitle(mContext.getString(R.string.operation_failed))
                        //set content text to support devices running API level < 24
                        .setContentText(mContext.getString(R.string.operation_failed))
                        .setSmallIcon(R.drawable.ic_twotone_cloud_error_24)
                        .setGroup(OPERATION_FAILED_GROUP)
                        .setGroupSummary(true)
                        .setAutoCancel(true)
                        .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        notificationManager.notify(OPERATION_FAILED_NOTIFICATION_ID, summaryNotification);
    }

    public void createSummaryNotificationForSuccess() {
        Notification summaryNotification =
                new NotificationCompat.Builder(mContext, CHANNEL_ID)
                        .setContentTitle(mContext.getString(R.string.operation_success))
                        //set content text to support devices running API level < 24
                        .setContentText(mContext.getString(R.string.operation_success))
                        .setSmallIcon(R.drawable.ic_twotone_cloud_done_24)
                        .setGroup(OPERATION_SUCCESS_GROUP)
                        .setGroupSummary(true)
                        .setAutoCancel(true)
                        .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        notificationManager.notify(OPERATION_SUCCESS_NOTIFICATION_ID, summaryNotification);
    }

    public void setNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(mContext.getString(R.string.sync_service_notification_channel_description));
            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public NotificationCompat.Builder getPersistentNotification(String title) {
        Intent foregroundIntent = new Intent(mContext, SyncService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, foregroundIntent, 0);

        Intent cancelIntent = new Intent(mContext, SyncCancelAction.class);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(mContext, 0, cancelIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_twotone_rounded_cloud_sync_24)
                .setContentTitle(mContext.getString(R.string.syncing_service, title))
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_cancel_download, mContext.getString(R.string.cancel), cancelPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        return builder;
    }

    public void updateNotification(String title, String content, ArrayList<String> bigTextArray, int percent) {
        StringBuilder bigText = new StringBuilder();
        for (int i = 0; i < bigTextArray.size(); i++) {
            bigText.append(bigTextArray.get(i));
            if (i < 4) {
                bigText.append("\n");
            }
        }

        Intent foregroundIntent = new Intent(mContext, SyncService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, foregroundIntent, 0);

        Intent cancelIntent = new Intent(mContext, SyncCancelAction.class);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(mContext, 0, cancelIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_twotone_rounded_cloud_sync_24)
                .setContentTitle(mContext.getString(R.string.syncing_service, title))
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText.toString()))
                .addAction(R.drawable.ic_cancel_download, mContext.getString(R.string.cancel), cancelPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setProgress(100, percent, false);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(mContext);
        notificationManagerCompat.notify(PERSISTENT_NOTIFICATION_ID_FOR_SYNC, builder.build());
    }

}
