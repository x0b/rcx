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

import java.util.List;

import ca.pkay.rcloneexplorer.BroadcastReceivers.DownloadCancelAction;
import ca.pkay.rcloneexplorer.Items.FileItem;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;


public class StreamingService extends IntentService {

    private final String CHANNEL_ID = "ca.pkay.rcexplorer.streaming_channel";
    private final String CHANNEL_NAME = "Streaming server";
    public static final String SERVE_PATH_ARG = "ca.pkay.rcexplorer.streaming_service.arg1";
    public static final String REMOTE_ARG = "ca.pkay.rcexplorer.streaming_service.arg2";
    private Rclone rclone;
    private Process runningProcess;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.*
     */
    public StreamingService() {
        super("ca.pkay.rcexplorer.streamingservice");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setNotificationChannel();
        rclone = new Rclone(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Intent foregroundIntent = new Intent(this, StreamingService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, foregroundIntent, 0);

        Intent cancelIntent = new Intent(this, DownloadCancelAction.class); // TODO
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, 0, cancelIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download) // TODO
                .setContentTitle("Streaming Service")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_cancel_download, "Cancel", cancelPendingIntent);

        startForeground(1, builder.build());

        final String servePath = intent.getStringExtra(SERVE_PATH_ARG);
        final String remote = intent.getStringExtra(REMOTE_ARG);

        runningProcess = rclone.serveHttp(remote, servePath);
         try {
             runningProcess.waitFor();
         } catch (InterruptedException e) {
             e.printStackTrace();
         }

        stopForeground(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        runningProcess.destroy();
    }

    private void setNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("File downloads");
            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
