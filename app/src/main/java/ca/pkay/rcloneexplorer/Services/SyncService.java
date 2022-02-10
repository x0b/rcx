package ca.pkay.rcloneexplorer.Services;


import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.Log2File;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import ca.pkay.rcloneexplorer.util.FLog;
import ca.pkay.rcloneexplorer.util.SyncServiceNotifications;

public class SyncService extends IntentService {

    private static final String TAG = "SyncService";

    public static final String REMOTE_ARG = "ca.pkay.rcexplorer.SYNC_SERVICE_REMOTE_ARG";
    public static final String REMOTE_PATH_ARG = "ca.pkay.rcexplorer.SYNC_SERVICE_REMOTE_PATH_ARG";
    public static final String LOCAL_PATH_ARG = "ca.pkay.rcexplorer.SYNC_LOCAL_PATH_ARG";
    public static final String SYNC_DIRECTION_ARG = "ca.pkay.rcexplorer.SYNC_DIRECTION_ARG";
    public static final String SHOW_RESULT_NOTIFICATION = "ca.pkay.rcexplorer.SHOW_RESULT_NOTIFICATION";
    public static final String TASK_NAME = "ca.pkay.rcexplorer.TASK_NAME";
    public static final String TASK_ID = "ca.pkay.rcexplorer.TASK_ID";

    private Rclone rclone;
    private Log2File log2File;
    private boolean connectivityChanged;
    private boolean transferOnWiFiOnly;
    Process currentProcess;
    SyncServiceNotifications notificationManager = new SyncServiceNotifications(this);

    public SyncService() {
        super("ca.pkay.rcexplorer.SYNC_SERCVICE");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager.setNotificationChannel();
        rclone = new Rclone(this);
        log2File = new Log2File(this);

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

        if (transferOnWiFiOnly && !checkWifiOnAndConnected()) {
            notificationManager.showConnectivityChangedNotification();
            stopSelf();
            return;
        }

        final long taskID = intent.getLongExtra(TASK_ID, -1);
        final RemoteItem remoteItem = intent.getParcelableExtra(REMOTE_ARG);
        final String remotePath = intent.getStringExtra(REMOTE_PATH_ARG);
        final String localPath = intent.getStringExtra(LOCAL_PATH_ARG);
        String title = intent.getStringExtra(TASK_NAME);
        final int syncDirection = intent.getIntExtra(SYNC_DIRECTION_ARG, 1);

        final boolean silentRun = intent.getBooleanExtra(SHOW_RESULT_NOTIFICATION, true);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean isLoggingEnable = sharedPreferences.getBoolean(getString(R.string.pref_key_logs), false);

        if(title.equals("")){
            title = remotePath;
        }

        startForeground(SyncServiceNotifications.PERSISTENT_NOTIFICATION_ID_FOR_SYNC, notificationManager.getPersistentNotification(title).build());

        currentProcess = rclone.sync(remoteItem, remotePath, localPath, syncDirection);
        if (currentProcess != null) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(currentProcess.getErrorStream()));
                String line;
                String notificationContent = "";
                String[] notificationBigText = new String[5];
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Transferred:") && !line.matches("Transferred:\\s+\\d+\\s+/\\s+\\d+,\\s+\\d+%$")) {
                        String s = line.substring(12).trim();
                        notificationBigText[0] = s;
                        notificationContent = s;
                    } else if (line.startsWith(" *")) {
                        String s = line.substring(2).trim();
                        notificationBigText[1] = s;
                    } else if (line.startsWith("Errors:")) {
                        notificationBigText[2] = line;
                    } else if (line.startsWith("Checks:")) {
                        notificationBigText[3] = line;
                    } else if (line.matches("Transferred:\\s+\\d+\\s+/\\s+\\d+,\\s+\\d+%$")) {
                        notificationBigText[4] = line;
                    } else if (isLoggingEnable && line.startsWith("ERROR :")){
                        log2File.log(line);
                    }

                    notificationManager.updateNotification(title, notificationContent, notificationBigText);
                }
            } catch (IOException e) {
                FLog.e(TAG, "onHandleIntent: error reading stdout", e);
            }

            try {
                currentProcess.waitFor();
            } catch (InterruptedException e) {
                FLog.e(TAG, "onHandleIntent: error waiting for process", e);
            }
        }

        sendUploadFinishedBroadcast(remoteItem.getName(), remotePath);

        int notificationId = (int)System.currentTimeMillis();

        if(silentRun){
            if (transferOnWiFiOnly && connectivityChanged) {
                notificationManager.showConnectivityChangedNotification();
            } else if (currentProcess == null || currentProcess.exitValue() != 0) {
                String errorTitle = getString(R.string.notification_sync_failed);
                notificationManager.showFailedNotification(errorTitle, title, notificationId, taskID);
            }else{
                notificationManager.showSuccessNotification(title, notificationId);
            }
        }

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.cancel(SyncServiceNotifications.PERSISTENT_NOTIFICATION_ID_FOR_SYNC);
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

    private boolean checkWifiOnAndConnected() {
        WifiManager wifiMgr = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr == null) {
            FLog.e(TAG, "No Wifi found.");
            return false;
        }

        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON
            // WifiManager requires location access. This is not available, so we query the metered instead.
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            return !cm.isActiveNetworkMetered();
        }
        else {
            FLog.e(TAG, "Wifi not turned on.");
            return false; // Wi-Fi adapter is OFF
        }
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

    private void sendUploadFinishedBroadcast(String remote, String path) {
        Intent intent = new Intent();
        intent.setAction(getString(R.string.background_service_broadcast));
        intent.putExtra(getString(R.string.background_service_broadcast_data_remote), remote);
        intent.putExtra(getString(R.string.background_service_broadcast_data_path), path);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


}
