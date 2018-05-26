package ca.pkay.rcloneexplorer.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import ca.pkay.rcloneexplorer.BroadcastReceivers.DownloadCancelAction;
import ca.pkay.rcloneexplorer.Items.FileItem;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;


public class DownloadService extends IntentService {

    public static final String DOWNLOAD_ITEM_ARG = "ca.pkay.rcexplorer.download_service.arg1";
    public static final String DOWNLOAD_PATH_ARG = "ca.pkay.rcexplorer.download_service.arg2";
    public static final String REMOTE_ARG = "ca.pkay.rcexplorer.download_service.arg3";
    private final String CHANNEL_ID = "ca.pkay.rcexplorer.DOWNLOAD_CHANNEL";
    private final String DOWNLOAD_FINISHED_GROUP = "ca.pkay.rcexplorer.DOWNLOAD_FINISHED_GROUP";
    private final String DOWNLOAD_FAILED_GROUP = "ca.pkay.rcexplorer.DOWNLOAD_FAILED_GROUP";
    private final String CHANNEL_NAME = "Downloads";
    private final int PERSISTENT_NOTIFICATION_ID = 167;
    private final int FAILED_DOWNLOAD_NOTIFICATION_ID = 138;
    private final int DOWNLOAD_FINISHED_NOTIFICATION_ID = 80;
    private Rclone rclone;
    private Process currentProcess;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.*
     */
    public DownloadService() {
        super("ca.pkay.rcexplorer.downloadservice");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setNotificationChannel();
        rclone = new Rclone(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Intent foregroundIntent = new Intent(this, DownloadService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, foregroundIntent, 0);

        Intent cancelIntent = new Intent(this, DownloadCancelAction.class);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, 0, cancelIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(getString(R.string.download_service_notification_title))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_cancel_download, getString(R.string.cancel), cancelPendingIntent);

        startForeground(PERSISTENT_NOTIFICATION_ID, builder.build());

        if (intent == null) {
            return;
        }

        final FileItem downloadItem = intent.getParcelableExtra(DOWNLOAD_ITEM_ARG);
        final String downloadPath = intent.getStringExtra(DOWNLOAD_PATH_ARG);
        final String remote = intent.getStringExtra(REMOTE_ARG);

        currentProcess = rclone.downloadFile(remote, downloadItem, downloadPath);
        try {
            currentProcess.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int notificationId = (int)System.currentTimeMillis();

        if (currentProcess.exitValue() == 0) {
            showDownloadFinishedNotification(notificationId, downloadItem.getName());
        } else {
            showDownloadFailedNotification(notificationId, downloadItem.getName());
        }

        stopForeground(true);
    }

    private void showDownloadFinishedNotification(int notificationID, String contentText) {
        createSummaryNotificationForFinished();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle(getString(R.string.download_complete))
                .setContentText(contentText)
                .setGroup(DOWNLOAD_FINISHED_GROUP)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationID, builder.build());
    }

    private void createSummaryNotificationForFinished() {
        Notification summaryNotification =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle(getString(R.string.download_complete))
                        //set content text to support devices running API level < 24
                        .setContentText(getString(R.string.download_complete))
                        .setSmallIcon(android.R.drawable.stat_sys_download_done)
                        .setGroup(DOWNLOAD_FINISHED_GROUP)
                        .setGroupSummary(true)
                        .setAutoCancel(true)
                        .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(DOWNLOAD_FINISHED_NOTIFICATION_ID, summaryNotification);
    }

    private void showDownloadFailedNotification(int notificationId, String contentText) {
        createSummaryNotificationForFailed();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle(getString(R.string.download_failed))
                .setContentText(contentText)
                .setGroup(DOWNLOAD_FAILED_GROUP)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, builder.build());
    }

    private void createSummaryNotificationForFailed() {
        Notification summaryNotification =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle(getString(R.string.download_failed))
                        //set content text to support devices running API level < 24
                        .setContentText(getString(R.string.download_failed))
                        .setSmallIcon(android.R.drawable.stat_sys_warning)
                        .setGroup(DOWNLOAD_FAILED_GROUP)
                        .setGroupSummary(true)
                        .setAutoCancel(true)
                        .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(FAILED_DOWNLOAD_NOTIFICATION_ID, summaryNotification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (currentProcess != null) {
            currentProcess.destroy();
        }
    }

    private void setNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(getString(R.string.download_service_notification_description));
            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
