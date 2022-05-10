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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.ArrayList;

import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.Log2File;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import ca.pkay.rcloneexplorer.util.FLog;
import ca.pkay.rcloneexplorer.util.SyncServiceNotifications;

import static android.text.format.Formatter.formatFileSize;

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
        JSONObject stats;
        String notificationContent = "";
        ArrayList<String> notificationBigText = new ArrayList<>();
        int notificationPercent = 0;
        if (currentProcess != null) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(currentProcess.getErrorStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    JSONObject logline = new JSONObject(line);
                    if(isLoggingEnable && logline.getString("level").equals("error")){
                        log2File.log(line);
                    } else if(logline.getString("level").equals("warning")){
                        //available stats:
                        //bytes,checks,deletedDirs,deletes,elapsedTime,errors,eta,fatalError,renames,retryError
                        //speed,totalBytes,totalChecks,totalTransfers,transferTime,transfers
                        stats = logline.getJSONObject("stats");

                        String speed = formatFileSize(this, stats.getLong("speed"))+"/s";
                        String size = formatFileSize(this, stats.getLong("bytes"));
                        String allsize = formatFileSize(this, stats.getLong("totalBytes"));
                        double percent = ((double)  stats.getLong("bytes")/stats.getLong("totalBytes"))*100;

                        notificationContent = String.format(getString(ca.pkay.rcloneexplorer.R.string.sync_notification_short), size, allsize, stats.get("eta"));
                        notificationBigText.clear();
                        notificationBigText.add(String.format(getString(ca.pkay.rcloneexplorer.R.string.sync_notification_transferred), size, allsize));
                        notificationBigText.add(String.format(getString(ca.pkay.rcloneexplorer.R.string.sync_notification_speed), speed));
                        notificationBigText.add(String.format(getString(ca.pkay.rcloneexplorer.R.string.sync_notification_remaining), stats.get("eta")));
                        if(stats.getInt("errors")>0){
                            notificationBigText.add(String.format(getString(ca.pkay.rcloneexplorer.R.string.sync_notification_errors), stats.getInt("errors")));
                        }
                        //notificationBigText.add(String.format("Checks:      %d / %d", stats.getInt("checks"),  stats.getInt("totalChecks")));
                        //notificationBigText.add(String.format("Transferred: %s / %s", size, allsize));
                        notificationBigText.add(String.format(getString(ca.pkay.rcloneexplorer.R.string.sync_notification_elapsed), stats.getInt("elapsedTime")));
                        notificationPercent = (int) percent;
                    }

                    notificationManager.updateNotification(title, notificationContent, notificationBigText);
                    updateNotification(title, notificationContent, notificationBigText, notificationPercent);

                }
            } catch (InterruptedIOException e) {
                FLog.d(TAG, "onHandleIntent: I/O interrupted, stream closed");
            } catch (IOException e) {
                if (!"Stream closed".equals(e.getMessage())) {
                    FLog.e(TAG, "onHandleIntent: error reading stdout", e);
                }
                FLog.e(TAG, "onHandleIntent: error reading stdout", e);
            } catch (JSONException e) {
                FLog.e(TAG, "onHandleIntent: error reading json", e);
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

    private void updateNotification(String title, String content, ArrayList<String> bigTextArray, int percent) {
        StringBuilder bigText = new StringBuilder();
        for (int i = 0; i < bigTextArray.size(); i++) {
            bigText.append(bigTextArray.get(i));
            if (i < 4) {
                bigText.append("\n");
            }
        }

        Intent foregroundIntent = new Intent(this, SyncService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, foregroundIntent, 0);

        Intent cancelIntent = new Intent(this, SyncCancelAction.class);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, 0, cancelIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.syncing_service, title))
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setProgress(100, percent, false)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText.toString()))
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
