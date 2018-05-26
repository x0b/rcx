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

import java.util.List;

import ca.pkay.rcloneexplorer.Items.FileItem;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;

public class BackgroundService extends IntentService {

    public static final String TASK_TYPE = "ca.pkay.rcexplorer.BACKGROUND_SERVICE_TASK_TYPE";
    public static final int TASK_TYPE_MOVE = 1;
    public static final int TASK_TYPE_DELETE = 2;
    public static final String REMOTE_ARG = "ca.pkay.rcexplorer.BACKGROUND_SERVICE_REMOTE_ARG";
    public static final String MOVE_DEST_PATH = "ca.pkat.rcexplorer.BACKGROUND_SERVICE_MOVE_DEST_ARG";
    public static final String MOVE_ITEM = "ca.pkay.rcexplorer.BACKGROUND_SERVICE_MOVE_ARG";
    public static final String DELETE_ITEM = "ca.pkay.rcexplorer.BACKGROUND_SERVICE_DELETE_ARG";
    public static final String PATH = "ca.pkay.rcexplorer.BACKGROUND_SERVICE_PATH_ARG";
    public static final String PATH2 = "ca.pkay.rcexplorer.BACKGROUND_SERVICE_PATH2_ARG";
    private final String CHANNEL_ID = "ca.pkay.rcexplorer.background_service";
    private final String CHANNEL_NAME = "Background service";
    private final String OPERATION_FAILED_GROUP = "ca.pkay.rcexplorer.OPERATION_FAILED_GROUP";
    private final int PERSISTENT_NOTIFICATION_ID_FOR_MOVE = 43;
    private final int PERSISTENT_NOTIFICATION_ID_FOR_DELETE = 124;
    private final int OPERATION_FAILED_NOTIFICATION_ID = 31;
    private Rclone rclone;


    public BackgroundService() {
        super("ca.pkay.rcexplorer.BACKGROUND_SERVICE");
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
        int taskType = intent.getIntExtra(TASK_TYPE, -1);
        switch (taskType) {
            case TASK_TYPE_MOVE:
                moveTask(intent);
                break;
            case TASK_TYPE_DELETE:
                deleteTask(intent);
                break;
        }
    }

    private void moveTask(Intent intent) {
        final FileItem moveItem = intent.getParcelableExtra(MOVE_ITEM);
        final String moveDestPath = intent.getStringExtra(MOVE_DEST_PATH);
        final String path = intent.getStringExtra(PATH2);
        final String remote = intent.getStringExtra(REMOTE_ARG);

        if (moveItem == null || moveDestPath == null || remote == null || path == null) {
            return;
        }

        String content = moveItem.getName();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.moving_service))
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        startForeground(PERSISTENT_NOTIFICATION_ID_FOR_MOVE, builder.build());

        Boolean success = rclone.moveTo(remote, moveItem, moveDestPath);
        sendUploadFinishedBroadcast(remote, moveDestPath, path);

        if (!success) {
            String errorTitle = "Move operation failed";
            String errorContent = moveItem.getName();
            int notificationId = (int)System.currentTimeMillis();
            showFailedNotification(errorTitle, errorContent, notificationId);
        }
    }

    private void deleteTask(Intent intent) {
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
