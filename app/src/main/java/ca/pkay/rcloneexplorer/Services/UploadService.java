package ca.pkay.rcloneexplorer.Services;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

import ca.pkay.rcloneexplorer.BroadcastReceivers.DownloadCancelAction;
import ca.pkay.rcloneexplorer.BroadcastReceivers.UploadCancelAction;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;


public class UploadService extends IntentService {

    private final String CHANNEL_ID = "ca.pkay.rcexplorer.upload_channel";
    private final String CHANNEL_NAME = "Uploads";
    public static final String UPLOAD_PATH_ARG = "ca.pkay.rcexplorer.upload_service.arg1";
    public static final String LOCAL_PATH_ARG = "ca.pkay.rcexplorer.upload_service.arg2";
    public static final String REMOTE_ARG = "ca.pkay.rcexplorer.upload_service.arg3";
    private Rclone rclone;
    private List<Process> runningProcesses;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.*
     */
    public UploadService() {
        super("ca.pkay.rcexplorer.uploadservice");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setNotificationChannel();
        rclone = new Rclone(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Intent foregroundIntent = new Intent(this, UploadService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, foregroundIntent, 0);

        Intent cancelIntent = new Intent(this, UploadCancelAction.class);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, 0, cancelIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_upload)
                .setContentTitle(getString(R.string.upload_service_notification_title))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_cancel_download, getString(R.string.cancel), cancelPendingIntent);

        startForeground(10, builder.build());

        if (intent == null) {
            return;
        }
        final String uploadPath = intent.getStringExtra(UPLOAD_PATH_ARG);
        final ArrayList<String> uploadList = intent.getStringArrayListExtra(LOCAL_PATH_ARG);
        final String remote = intent.getStringExtra(REMOTE_ARG);

        runningProcesses = rclone.uploadFiles(remote, uploadPath, uploadList);

        for (Process process : runningProcesses) {
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        stopForeground(true);

        NotificationCompat.Builder builder1 = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_upload_done)
                .setContentTitle(getString(R.string.upload_complete))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(20, builder1.build());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (Process process : runningProcesses) {
            process.destroy();
        }
    }

    private void setNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(getString(R.string.upload_service_notification_channel_description));
            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
