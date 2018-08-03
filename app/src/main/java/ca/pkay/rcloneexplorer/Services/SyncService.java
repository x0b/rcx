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
import android.support.v4.content.LocalBroadcastManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import ca.pkay.rcloneexplorer.BroadcastReceivers.SyncCancelAction;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;

public class SyncService extends IntentService {

    public static final String REMOTE_ARG = "ca.pkay.rcexplorer.SYNC_SERVICE_REMOTE_ARG";
    public static final String REMOTE_PATH_ARG = "ca.pkay.rcexplorer.SYNC_SERVICE_REMOTE_PATH_ARG";
    public static final String LOCAL_PATH_ARG = "ca.pkay.rcexplorer.SYNC_LOCAL_PATH_ARG";
    public static final String SYNC_DIRECTION_ARG = "ca.pkay.rcexplorer.SYNC_DIRECTION_ARG";
    private final String OPERATION_FAILED_GROUP = "ca.pkay.rcexplorer.OPERATION_FAILED_GROUP";
    private final String CHANNEL_ID = "ca.pkay.rcexplorer.sync_service";
    private final String CHANNEL_NAME = "Sync service";
    private final int PERSISTENT_NOTIFICATION_ID_FOR_SYNC = 162;
    private final int OPERATION_FAILED_NOTIFICATION_ID = 89;
    private Rclone rclone;
    Process currentProcess;

    public SyncService() {
        super("ca.pkay.rcexplorer.SYNC_SERCVICE");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setNotificationChannel();
        rclone = new Rclone(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        final RemoteItem remoteItem = intent.getParcelableExtra(REMOTE_ARG);
        final String remotePath = intent.getStringExtra(REMOTE_PATH_ARG);
        final String localPath = intent.getStringExtra(LOCAL_PATH_ARG);
        final int syncDirection = intent.getIntExtra(SYNC_DIRECTION_ARG, 1);

        String title;
        int slashIndex = remotePath.lastIndexOf("/");
        if (slashIndex >= 0) {
            title = remotePath.substring(slashIndex + 1);
        } else {
            title = remotePath;
        }

        Intent foregroundIntent = new Intent(this, SyncService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, foregroundIntent, 0);

        Intent cancelIntent = new Intent(this, SyncCancelAction.class);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, 0, cancelIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.syncing_service, title))
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_cancel_download, getString(R.string.cancel), cancelPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        startForeground(PERSISTENT_NOTIFICATION_ID_FOR_SYNC, builder.build());

        currentProcess = rclone.sync(remoteItem, remotePath, localPath, syncDirection);
        if (currentProcess != null) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(currentProcess.getErrorStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Transferred:") && !line.matches("Transferred:\\s+\\d+$")) {
                        updateNotification(title, line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                currentProcess.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        sendUploadFinishedBroadcast(remoteItem.getName(), remotePath);

        if (currentProcess == null || currentProcess.exitValue() != 0) {
            rclone.logErrorOutput(currentProcess);
            String errorTitle = "Sync operation failed";
            int notificationId = (int)System.currentTimeMillis();
            showFailedNotification(errorTitle, title, notificationId);
        }

        stopForeground(true);
    }

    private void updateNotification(String title, String transferred) {
        Intent foregroundIntent = new Intent(this, SyncService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, foregroundIntent, 0);

        Intent cancelIntent = new Intent(this, SyncCancelAction.class);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, 0, cancelIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.syncing_service, title))
                .setContentText(transferred)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_cancel_download, getString(R.string.cancel), cancelPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(PERSISTENT_NOTIFICATION_ID_FOR_SYNC, builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (currentProcess != null) {
            currentProcess.destroy();
        }
    }

    private void sendUploadFinishedBroadcast(String remote, String path) {
        Intent intent = new Intent();
        intent.setAction(getString(R.string.background_service_broadcast));
        intent.putExtra(getString(R.string.background_service_broadcast_data_remote), remote);
        intent.putExtra(getString(R.string.background_service_broadcast_data_path), path);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void showFailedNotification(String title, String content, int notificationId) {
        createSummaryNotificationForFailed();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle(title)
                .setContentText(content)
                .setGroup(OPERATION_FAILED_GROUP)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, builder.build());

    }

    private void createSummaryNotificationForFailed() {
        Notification summaryNotification =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle(getString(R.string.operation_failed))
                        //set content text to support devices running API level < 24
                        .setContentText(getString(R.string.operation_failed))
                        .setSmallIcon(android.R.drawable.stat_sys_warning)
                        .setGroup(OPERATION_FAILED_GROUP)
                        .setGroupSummary(true)
                        .setAutoCancel(true)
                        .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(OPERATION_FAILED_NOTIFICATION_ID, summaryNotification);
    }

    private void setNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(getString(R.string.sync_service_notification_channel_description));
            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
