package ca.pkay.rcloneexplorer.Services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.RcloneRcd;
import ca.pkay.rcloneexplorer.util.FLog;


public class RcdService extends Service implements RcloneRcd.JobsUpdateHandler {

    private static final String TAG = "RcdService";
    private static final String CHANNEL_ID = "ca.pkay.rcexplorer.rcd_channel";
    private static final String CHANNEL_NAME = "Rclone";
    private static final int PERSISTENT_NOTIFICATION_ID = 200;
    public static final String ACTION_START_FOREGROUND = "ca.pkay.rcloneexplorer.RcdService.StartForeground";
    public static final String ACTION_STOP_FOREGROUND = "ca.pkay.rcloneexplorer.RcdService.StopForeground";

    private RcloneRcd rcloneRcd;
    private boolean shutdown;
    private Boolean available;

    private final IBinder binder = new RcdBinder();
    private NotificationManagerCompat notificationManager;

    public RcdService() {
        super();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        FLog.d(TAG, "onBind: client binds to service");
        return binder;
    }

    @Override
    public void onRcdJobsUpdate(@NonNull SparseArray<RcloneRcd.JobStatusResponse> status) {
        if(shutdown) {
            FLog.w(TAG, "Unexpected jobs update after service shutdown");
            return;
        }

        Intent foregroundIntent = new Intent(this, RcdService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, foregroundIntent, 0);

        // build job statistics
        long running = 0;
        long finished = 0;
        long failed = 0;
        for (int i = 0; i < status.size(); i++) {
            int key = status.keyAt(i);
            RcloneRcd.JobStatusResponse response = status.get(key);
            if(response.finished) {
                if (response.success) {
                    finished++;
                } else {
                    failed++;
                }
            } else {
                running++;
            }
        }

        String statusLine = getString(R.string.rcd_service_notification_stats_template, running, finished, failed);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_rclone_logo)
                .setContentTitle(getString(R.string.rcd_service_notification_running_in_background))
                .setContentText(statusLine)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_cancel_download, getString(R.string.rcd_service_notification_btn_stop_all), stopServiceIntent());

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(PERSISTENT_NOTIFICATION_ID, builder.build());
    }

    public class RcdBinder extends Binder {
        public RcdService getService() {
            FLog.d(TAG, "getService: returning RcdService");
            return RcdService.this;
        }
    }

    @Override
    public void onRebind(Intent intent) {
        FLog.v(TAG, "onRebind: client binds again");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        FLog.v(TAG, "onUnbind: unbinding from client");
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FLog.d(TAG, "onCreate: service is being created");
        notificationManager = NotificationManagerCompat.from(getApplicationContext());
        setNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        FLog.d(TAG, "onStartCommand: service onStart()");
        shutdown = false;
        if (intent != null && ACTION_STOP_FOREGROUND.equals(intent.getAction())) {
            FLog.d(TAG, "Removing foreground service");
            shutdown();
            stopForeground(true);
            stopSelf();
        } else {
            showNotification();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void showNotification() {
        Intent foregroundIntent = new Intent(this, RcdService.class);
        foregroundIntent.setAction(ACTION_START_FOREGROUND);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, foregroundIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_rclone_logo)
                .setContentTitle(getString(R.string.rcd_service_notification_running_in_background))
                .setContentText(getString(R.string.rcd_service_notification_no_active_jobs))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_cancel_download, getString(R.string.rcd_service_notification_btn_stop_all), stopServiceIntent());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(PERSISTENT_NOTIFICATION_ID, builder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(PERSISTENT_NOTIFICATION_ID, builder.build());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        shutdown();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        FLog.v(TAG, "onTaskRemoved(%s)", rootIntent.toString());
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        FLog.v(TAG, "onConfigurationChanged(%s)", newConfig.toString());
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onTrimMemory(int level) {
        if (shutdown) {
            // Service is already in cached state, will be reaped completely if
            // the system really needs memory.
            return;
        }
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL) {
            FLog.v(TAG, "Memory running critical (15)");
            shutdownIfPossible();
        } else if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
            FLog.v(TAG, "Memory running low (10)");
            shutdownIfPossible();
        } else if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE) {
            FLog.v(TAG, "Memory running low (5)");
        }
    }

    private void shutdownIfPossible() {
        if (null != rcloneRcd) {
            if (!rcloneRcd.hasPendingJobs()) {
                FLog.d(TAG, "No running jobs, killing service");
                shutdown();
                stopForeground(true);
                stopSelf();
            }
        }
    }

    private void shutdown() {
        FLog.d(TAG, "Service shutting down");
        if (null != rcloneRcd) {
            rcloneRcd.stopRcd();
            rcloneRcd = null;
        }
        shutdown = true;
    }

    /**
     * If the service has been told to shutdown and waiting to be killed by OOM.
     * @return
     */
    public boolean isShutdown() {
        return shutdown;
    }

    private PendingIntent stopServiceIntent() {
        Intent cancelIntent = new Intent(this, RcdServiceAction.class);
        cancelIntent.setAction(RcdServiceAction.ACTION_STOP_ALL);
        return PendingIntent.getBroadcast(this, 0, cancelIntent, 0);
    }

    public boolean waitOnline(long timeout) {
        long retries = timeout / 150;
        while (retries > 0) {
            synchronized (available) {
                try {
                    rcloneRcd.isOnline();
                } catch (NullPointerException | RcloneRcd.RcdIOException e) {
                    FLog.v(TAG, "rcd not yet online");
                    try {
                        available.wait(150);
                    } catch (InterruptedException ignored) {
                    }
                    retries--;
                    continue;
                }
                available = true;
                break;
            }
        }
        return available;
    }

    public RcloneRcd getLocalRcd() {
        if (null == rcloneRcd || !rcloneRcd.isAlive()) {
            FLog.d(TAG, "Creating rcd process");
            rcloneRcd = new RcloneRcd(getApplicationContext(), this);
            rcloneRcd.startRcd();
        } else if (rcloneRcd.hasCrashed()) {
            FLog.d(TAG, "Reviving rclone");
            rcloneRcd = new RcloneRcd(getApplicationContext(), this);
            rcloneRcd.startRcd();
        }
        return rcloneRcd;
    }

    private void setNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(getString(R.string.rcd_service_notification_channel_description));
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static class RcdServiceAction extends BroadcastReceiver {

        public static final String ACTION_STOP_ALL = "ca.pkay.rcloneexplorer.RcdService.Stop";
        public static final String ACTION_PAUSE = "ca.pkay.rcloneexplorer.RcdService.Pause";

        @Override
        public void onReceive(Context context, Intent intent) {
            FLog.d(TAG, "Broadcast received");
            if (null == intent.getAction()) {
                FLog.w(TAG, "Empty action received, something has gone wrong");
                return;
            }
            switch (intent.getAction()) {
                case ACTION_STOP_ALL:
                    FLog.d(TAG, "Requesting shutdown of RcdService");
                    Intent stopIntent = new Intent(context, RcdService.class);
                    stopIntent.setAction(ACTION_STOP_FOREGROUND);
                    context.startService(stopIntent);
                    break;
                case ACTION_PAUSE:
                    FLog.w(TAG, "Pause not implemented");
                    break;
                default:
                    FLog.e(TAG, "Unknown action '%s' received. Check your code.", intent.getAction());
                    break;
            }
        }
    }
}
