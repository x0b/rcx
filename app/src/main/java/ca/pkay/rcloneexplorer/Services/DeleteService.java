package ca.pkay.rcloneexplorer.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;

import ca.pkay.rcloneexplorer.Items.FileItem;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;

public class DeleteService extends IntentService {

    public static final String REMOTE_ARG = "ca.pkay.rcexplorer.DELETE_SERVICE_REMOTE_ARG";
    public static final String DELETE_ITEM = "ca.pkay.rcexplorer.DELETE_SERVICE_DELETE_ARG";
    public static final String PATH = "ca.pkay.rcexplorer.DELETE_SERVICE_PATH_ARG";
    private final String CHANNEL_ID = "ca.pkay.rcexplorer.background_service";
    private final String CHANNEL_NAME = "Background service";
    private final String OPERATION_FAILED_GROUP = "ca.pkay.rcexplorer.OPERATION_FAILED_GROUP";
    private final int PERSISTENT_NOTIFICATION_ID_FOR_DELETE = 124;
    private final int OPERATION_FAILED_NOTIFICATION_ID = 31;
    private Rclone rclone;


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

        final String remote = intent.getStringExtra(REMOTE_ARG);
        final String path = intent.getStringExtra(PATH);
        final FileItem deleteItem = intent.getParcelableExtra(DELETE_ITEM);

        if (remote == null || deleteItem == null) {
            return;
        }

        String content = deleteItem.getName();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.delete_service))
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        startForeground(PERSISTENT_NOTIFICATION_ID_FOR_DELETE, builder.build());

        Boolean success = rclone.deleteItems(remote, deleteItem);
        sendUploadFinishedBroadcast(remote, path, null);

        if (!success) {
            String errorTitle = "Delete operation failed";
            String errorContent = deleteItem.getName();
            int notificationId = (int)System.currentTimeMillis();
            showFailedNotification(errorTitle, errorContent, notificationId);
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
