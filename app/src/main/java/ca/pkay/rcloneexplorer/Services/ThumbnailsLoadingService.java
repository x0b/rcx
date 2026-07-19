package ca.pkay.rcloneexplorer.Services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;
import androidx.preference.PreferenceManager;

import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import ca.pkay.rcloneexplorer.util.FLog;

/**
 * Foreground service that runs an rclone HTTP serve against the current
 * remote so the file list RecyclerView can load image thumbnails via
 * 127.0.0.1:&lt;port&gt;/&lt;auth&gt;/...
 * <p>
 * Replaces the legacy {@code IntentService} implementation which Android 8+
 * would kill within ~5 seconds because it never promoted itself to the
 * foreground. On Android 14+ a foreground service must declare a type
 * ({@code dataSync} here) and call {@link ServiceCompat#startForeground}
 * with a matching {@link ServiceInfo} type.
 */
public class ThumbnailsLoadingService extends Service {

    private static final String TAG = "ThumbnailsLoadingSvc";
    private static final String CHANNEL_ID = "ca.pkay.rcexplorer.thumbnail_channel";
    private static final String CHANNEL_NAME = "Thumbnails";
    private static final int NOTIFICATION_ID = 314;

    public static final String REMOTE_ARG = "ca.pkay.rcexplorer.ThumbnailsLoadingService.REMOTE_ARG";
    public static final String HIDDEN_PATH = "ca.pkay.rcexplorer.ThumbnailsLoadingService.HIDDEN_PATH";
    public static final String SERVER_PORT = "ca.pkay.rcexplorer.ThumbnailsLoadingService.PORT";

    private Rclone rclone;
    private Process process;
    private volatile boolean running;

    @Override
    public void onCreate() {
        super.onCreate();
        rclone = new Rclone(this);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        // Promote to foreground immediately so the system doesn't kill us.
        startForegroundWithNotification();

        if (intent == null) {
            // Service was restarted by the system; nothing to serve, stop.
            stopSelf();
            return START_NOT_STICKY;
        }

        RemoteItem remote = intent.getParcelableExtra(REMOTE_ARG);
        if (remote == null) {
            FLog.w(TAG, "onStartCommand: missing REMOTE_ARG, stopping");
            stopSelf();
            return START_NOT_STICKY;
        }
        String authPath = intent.getStringExtra(HIDDEN_PATH);
        int serverPort = intent.getIntExtra(SERVER_PORT, 29179);
        String hiddenPath = "/" + (authPath == null ? "" : authPath) + '/' + remote.getName();

        FLog.d(TAG, "onStartCommand: hiddenPath=%s port=%d", hiddenPath, serverPort);
        running = true;
        new Thread(() -> runServe(remote, hiddenPath, serverPort), "rclone-serve-thumb").start();

        return START_STICKY;
    }

    private void runServe(RemoteItem remote, String hiddenPath, int serverPort) {
        try {
            process = rclone.serve(Rclone.SERVE_PROTOCOL_HTTP, serverPort, false, null, null, remote, "", hiddenPath);
        } catch (Throwable t) {
            FLog.e(TAG, "runServe: failed to start rclone serve", t);
            stopForegroundAndExit();
            return;
        }
        if (process == null) {
            FLog.w(TAG, "runServe: rclone.serve() returned null");
            stopForegroundAndExit();
            return;
        }
        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.pref_key_logs), false)) {
            new Thread(() -> rclone.logErrorOutput(process), "rclone-serve-err").start();
        }
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            FLog.e(TAG, "runServe: interrupted while waiting for rclone", e);
            Thread.currentThread().interrupt();
        } finally {
            stopForegroundAndExit();
        }
    }

    private void startForegroundWithNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && nm != null) {
            NotificationChannel channel = nm.getNotificationChannel(CHANNEL_ID);
            if (channel == null) {
                channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
                channel.setShowBadge(false);
                nm.createNotificationChannel(channel);
            }
        }
        Intent stopIntent = new Intent(this, ThumbnailsLoadingService.class);
        PendingIntent stopPi = PendingIntent.getService(this, 0, stopIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder b = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.thumbnail_service_notification_title))
                .setContentText(getString(R.string.thumbnail_service_notification_content))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setContentIntent(stopPi);
        ServiceCompat.startForeground(this, NOTIFICATION_ID, b.build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
    }

    private void stopForegroundAndExit() {
        if (!running) {
            return;
        }
        running = false;
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (process != null) {
            process.destroy();
            process = null;
        }
        running = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
