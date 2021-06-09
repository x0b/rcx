package ca.pkay.rcloneexplorer.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import ca.pkay.rcloneexplorer.BroadcastReceivers.DeleteCancelAction;
import ca.pkay.rcloneexplorer.Items.FileItem;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import ca.pkay.rcloneexplorer.util.FLog;

public class DeleteService extends IntentService {

    private static final String TAG = "DeleteService";
    public static final String REMOTE_ARG = "ca.pkay.rcexplorer.DELETE_SERVICE_REMOTE_ARG";
    public static final String DELETE_ITEM = "ca.pkay.rcexplorer.DELETE_SERVICE_DELETE_ARG";
    public static final String PATH = "ca.pkay.rcexplorer.DELETE_SERVICE_PATH_ARG";
    private final String CHANNEL_ID = "ca.pkay.rcexplorer.background_service";
    private final String CHANNEL_NAME = "Background service";
    private final String OPERATION_FAILED_GROUP = "ca.pkay.rcexplorer.OPERATION_FAILED_GROUP";
    private final int PERSISTENT_NOTIFICATION_ID_FOR_DELETE = 124;
    private final int OPERATION_FAILED_NOTIFICATION_ID = 31;
    private Rclone rclone;
    private Process currentProcess;

    public DeleteService() {
        super("ca.pkay.rcexplorer.DELETE_SERVICE");
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

        final RemoteItem remote = intent.getParcelableExtra(REMOTE_ARG);
        final String path = intent.getStringExtra(PATH);
        final FileItem deleteItem = intent.getParcelableExtra(DELETE_ITEM);

        if (remote == null || deleteItem == null) {
            return;
        }

        String content = deleteItem.getName();

        Intent foregroundIntent = new Intent(this, DeleteService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, foregroundIntent, 0);

        Intent cancelIntent = new Intent(this, DeleteCancelAction.class);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, 0, cancelIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.delete_service))
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_cancel_download, getString(R.string.cancel), cancelPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        startForeground(PERSISTENT_NOTIFICATION_ID_FOR_DELETE, builder.build());

        currentProcess = rclone.deleteItems(remote, deleteItem);
        if (currentProcess != null) {
            try {
                currentProcess.waitFor();
            } catch (InterruptedException e) {
                FLog.e(TAG, "onHandleIntent: error waiting for process", e);
            }
        }

        sendUploadFinishedBroadcast(remote.getName(), path, null);

        if (currentProcess == null || currentProcess.exitValue() != 0) {
            rclone.logErrorOutput(currentProcess);
            String errorTitle = "Delete operation failed";
            String errorContent = deleteItem.getName();
            int notificationId = (int)System.currentTimeMillis();
            showFailedNotification(errorTitle, errorContent, notificationId);
        }

        stopForeground(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (currentProcess != null) {
            currentProcess.destroy();
        }
    }

    private void sendUploadFinishedBroadcast(String remote, String path, String path2) {
        Intent intent = new Intent();
        intent.setAction(getString(R.string.background_service_broadcast));
        intent.putExtra(getString(R.string.background_service_broadcast_data_remote), remote);
        intent.putExtra(getString(R.string.background_service_broadcast_data_path), path);
        if (path2 != null) {
            intent.putExtra(getString(R.string.background_service_broadcast_data_path2), path2);
        }
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
            channel.setDescription(getString(R.string.background_service_notification_channel_description));
            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
