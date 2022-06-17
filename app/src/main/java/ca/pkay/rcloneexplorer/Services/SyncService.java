package ca.pkay.rcloneexplorer.Services;


import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

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
import java.util.PriorityQueue;
import java.util.Queue;

import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.Items.Task;
import ca.pkay.rcloneexplorer.Log2File;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import ca.pkay.rcloneexplorer.util.FLog;
import ca.pkay.rcloneexplorer.util.SyncServiceNotifications;

import static android.text.format.Formatter.formatFileSize;

public class SyncService extends IntentService {


    enum FAILURE_REASON {
        NONE,
        NO_WIFI,
        RCLONE_ERROR
    }
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
        Log.e(TAG, "onHandleIntent "+intent.getStringExtra(TASK_NAME));

        startForeground(SyncServiceNotifications.PERSISTENT_NOTIFICATION_ID_FOR_SYNC, notificationManager.getPersistentNotification("SyncService").build());

        InternalTaskItem t = InternalTaskItem.newInstance(intent);
        handleTask(t);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.cancel(SyncServiceNotifications.PERSISTENT_NOTIFICATION_ID_FOR_SYNC);
        stopForeground(true);

    }

    private void handleTask(InternalTaskItem t) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean isLoggingEnable = sharedPreferences.getBoolean(getString(R.string.pref_key_logs), false);

        String title = "";
        FAILURE_REASON failureReason = FAILURE_REASON.NONE;
        if(t.title.equals("")){
            title = t.remotePath;
        }

        if (transferOnWiFiOnly && !checkWifiOnAndConnected()) {
            failureReason = FAILURE_REASON.NO_WIFI;
        } else {
            currentProcess = rclone.sync(t.remoteItem, t.remotePath, t.localPath, t.syncDirection);
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

                        notificationManager.updateNotification(title, notificationContent, notificationBigText, notificationPercent);

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
            sendUploadFinishedBroadcast(t.remoteItem.getName(), t.remotePath);
        }

        int notificationId = (int)System.currentTimeMillis();

        if(t.silentRun){
            if (transferOnWiFiOnly && connectivityChanged || (currentProcess == null || currentProcess.exitValue() != 0)) {
                String errorTitle = getString(R.string.notification_sync_failed);
                String content = title;

                switch (failureReason) {
                    case NONE:
                        if(connectivityChanged){
                            content = title+" failed because the device lost wifi connection and mobile data was not allowed.";
                        }
                        break;
                    case NO_WIFI:
                        content = title+" failed because wifi was not available";
                        break;
                }
                notificationManager.showFailedNotification(errorTitle, content, notificationId, t.id);
            }else{
                notificationManager.showSuccessNotification(title, notificationId);
            }
        }
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


    private static class InternalTaskItem {
        public long id;
        public RemoteItem remoteItem;
        public String remotePath;
        public String localPath;
        public String title;
        public int syncDirection = 1;
        public boolean silentRun = true;


        public static InternalTaskItem newInstance(Intent intent)
        {
            InternalTaskItem itt = new InternalTaskItem();
            itt.id = intent.getLongExtra(TASK_ID, -1);
            itt.remoteItem = intent.getParcelableExtra(REMOTE_ARG);
            itt.remotePath = intent.getStringExtra(REMOTE_PATH_ARG);
            itt.localPath = intent.getStringExtra(LOCAL_PATH_ARG);
            itt.title = intent.getStringExtra(TASK_NAME);
            itt.syncDirection = intent.getIntExtra(SYNC_DIRECTION_ARG, 1);
            itt.silentRun = intent.getBooleanExtra(SHOW_RESULT_NOTIFICATION, true);
            return itt;
        }

    }

}
