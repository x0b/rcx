package ca.pkay.rcloneexplorer.Services;

import static ca.pkay.rcloneexplorer.notifications.DownloadNotifications.CHANNEL_ID;
import static ca.pkay.rcloneexplorer.notifications.DownloadNotifications.PERSISTENT_NOTIFICATION_ID;
import static ca.pkay.rcloneexplorer.notifications.UploadNotifications.CHANNEL_NAME;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.ArrayList;

import ca.pkay.rcloneexplorer.BroadcastReceivers.DownloadCancelAction;
import ca.pkay.rcloneexplorer.Items.FileItem;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.Log2File;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import ca.pkay.rcloneexplorer.notifications.DownloadNotifications;
import ca.pkay.rcloneexplorer.notifications.GenericSyncNotification;
import ca.pkay.rcloneexplorer.notifications.StatusObject;
import ca.pkay.rcloneexplorer.util.FLog;
import ca.pkay.rcloneexplorer.util.SyncLog;
import ca.pkay.rcloneexplorer.util.WifiConnectivitiyUtil;


public class DownloadService extends IntentService {

    private static final String TAG = "DownloadService";
    public static final String DOWNLOAD_ITEM_ARG = "ca.pkay.rcexplorer.download_service.arg1";
    public static final String DOWNLOAD_PATH_ARG = "ca.pkay.rcexplorer.download_service.arg2";
    public static final String REMOTE_ARG = "ca.pkay.rcexplorer.download_service.arg3";

    private boolean connectivityChanged;
    private boolean transferOnWiFiOnly;
    private Rclone rclone;
    private Log2File log2File;
    private Process currentProcess;
    private DownloadNotifications mNotifications;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.*
     */
    public DownloadService() {
        super("ca.pkay.rcexplorer.downloadservice");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        rclone = new Rclone(this);
        log2File = new Log2File(this);

        mNotifications = new DownloadNotifications(this);
        (new GenericSyncNotification(this)).setNotificationChannel(
                DownloadNotifications.CHANNEL_ID,
                CHANNEL_NAME,
                R.string.download_service_notification_description
        );

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        transferOnWiFiOnly = sharedPreferences.getBoolean(getString(R.string.pref_key_wifi_only_transfers), false);

        if (transferOnWiFiOnly) {
            registerBroadcastReceivers();
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }


        if (transferOnWiFiOnly && !WifiConnectivitiyUtil.Companion.checkWifiOnAndConnected(this.getApplicationContext())) {
            mNotifications.showConnectivityChangedNotification();
            stopSelf();
            return;
        }

        final FileItem downloadItem = intent.getParcelableExtra(DOWNLOAD_ITEM_ARG);
        final String downloadPath = intent.getStringExtra(DOWNLOAD_PATH_ARG);
        final RemoteItem remote = intent.getParcelableExtra(REMOTE_ARG);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean isLoggingEnable = sharedPreferences.getBoolean(getString(R.string.pref_key_logs), false);

        ArrayList<String> notificationBigText = new ArrayList<>();
        startForeground(PERSISTENT_NOTIFICATION_ID, mNotifications.createDownloadNotification(
                downloadItem.getName(),
                notificationBigText
        ).build());

        currentProcess = rclone.downloadFile(remote, downloadItem, downloadPath);


        //Todo: Check if this can be moved together with UploadService;SyncService etc.
        if (currentProcess != null) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(currentProcess.getErrorStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        StatusObject so = new StatusObject(this);
                        JSONObject logline = new JSONObject(line);
                        if (isLoggingEnable && logline.getString("level").equals("error")) {
                            log2File.log(line);
                        } else if (logline.getString("level").equals("warning")) {
                            so.parseLoglineToStatusObject(logline);
                        }

                        mNotifications.updateDownloadNotification(
                                downloadItem.getName(),
                                so.getNotificationContent(),
                                so.getNotificationBigText(),
                                so.getNotificationPercent()
                        );
                    } catch (JSONException e) {
                        FLog.e(TAG, "onHandleIntent: error reading json", e);
                    }
                }
            } catch (InterruptedIOException e) {
                FLog.d(TAG, "onHandleIntent: I/O interrupted, stream closed");
            } catch (IOException e) {
                if (!"Stream closed".equals(e.getMessage())) {
                    FLog.e(TAG, "onHandleIntent: error reading stdout", e);
                }
            }

            try {
                currentProcess.waitFor();
            } catch (InterruptedException e) {
                FLog.e(TAG, "onHandleIntent: error waiting for process", e);
            }
        }

        int notificationId = (int)System.currentTimeMillis();

        if (transferOnWiFiOnly && connectivityChanged) {
            mNotifications.showConnectivityChangedNotification();
        } else if (currentProcess != null && currentProcess.exitValue() == 0) {

            SyncLog.info(this, getString(R.string.download_complete), downloadItem.getName());
            mNotifications.showDownloadFinishedNotification(notificationId, downloadItem.getName());
        } else {
            SyncLog.error(this, getString(R.string.download_failed), downloadItem.getName());
            mNotifications.showDownloadFailedNotification(notificationId, downloadItem.getName());
        }

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.cancel(PERSISTENT_NOTIFICATION_ID);
        stopForeground(true);
    }

    private void registerBroadcastReceivers() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        registerReceiver(connectivityChangeBroadcastReceiver, intentFilter);
    }

    private BroadcastReceiver connectivityChangeBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            connectivityChanged = true;
            stopSelf();
        }
    };

    private void updateNotification(FileItem downloadItem, String content, String[] bigTextArray) {
        StringBuilder bigText = new StringBuilder();
        for (int i = 0; i < bigTextArray.length; i++) {
            String progressLine = bigTextArray[i];
            if (null != progressLine) {
                bigText.append(progressLine);
            }
            if (!"inode/directory".equals(downloadItem.getMimeType())) {
                break;
            }
            if (i < 4) {
                bigText.append("\n");
            }
        }

        Intent foregroundIntent = new Intent(this, DownloadService.class);
        int flags = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags = PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, foregroundIntent, flags);

        Intent cancelIntent = new Intent(this, DownloadCancelAction.class);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, 0, cancelIntent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_twotone_cloud_download_24)
                .setContentTitle(downloadItem.getName())
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText.toString()))
                .addAction(R.drawable.ic_cancel_download, getString(R.string.cancel), cancelPendingIntent);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(PERSISTENT_NOTIFICATION_ID, builder.build());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (currentProcess != null) {
            currentProcess.destroy();
        }

        if (transferOnWiFiOnly) {
            unregisterReceiver(connectivityChangeBroadcastReceiver);
        }
    }
}
