package ca.pkay.rcloneexplorer.Services;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.ArrayList;

import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.Log2File;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import ca.pkay.rcloneexplorer.notifications.StatusObject;
import ca.pkay.rcloneexplorer.notifications.UploadNotifications;
import ca.pkay.rcloneexplorer.util.FLog;
import ca.pkay.rcloneexplorer.util.WifiConnectivitiyUtil;


public class UploadService extends IntentService {

    private static final String TAG = "UploadService";
    public static final String UPLOAD_PATH_ARG = "ca.pkay.rcexplorer.upload_service.arg1";
    public static final String LOCAL_PATH_ARG = "ca.pkay.rcexplorer.upload_service.arg2";
    public static final String REMOTE_ARG = "ca.pkay.rcexplorer.upload_service.arg3";

    private boolean connectivityChanged;
    private boolean transferOnWiFiOnly;
    private Rclone rclone;
    private Log2File log2File;
    private Process currentProcess;
    private UploadNotifications mNotifications;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.*
     */
    public UploadService() {
        super("ca.pkay.rcexplorer.uploadservice");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        rclone = new Rclone(this);
        log2File = new Log2File(this);
        mNotifications = new UploadNotifications(this);
        mNotifications.setNotificationChannel();

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

        final String uploadPath = intent.getStringExtra(UPLOAD_PATH_ARG);
        final String uploadFilePath = intent.getStringExtra(LOCAL_PATH_ARG);
        final RemoteItem remote = intent.getParcelableExtra(REMOTE_ARG);

        boolean isFile = new File(uploadFilePath).isFile();
        String uploadFileName;
        int slashIndex = uploadFilePath.lastIndexOf("/");
        if (slashIndex >= 0) {
            uploadFileName = uploadFilePath.substring(slashIndex + 1);
        } else {
            uploadFileName = uploadFilePath;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean isLoggingEnable = sharedPreferences.getBoolean(getString(R.string.pref_key_logs), false);


        ArrayList<String> notificationBigText = new ArrayList<>();
        startForeground(UploadNotifications.PERSISTENT_NOTIFICATION_ID, mNotifications.createUploadNotification(
                uploadFileName,
                notificationBigText
        ).build());

        currentProcess = rclone.uploadFile(remote, uploadPath, uploadFilePath);
        if (currentProcess != null) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(currentProcess.getErrorStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    StatusObject so = new StatusObject();
                    JSONObject logline = new JSONObject(line);
                    Log.e("TAG", line);
                    if(isLoggingEnable && logline.getString("level").equals("error")){
                        log2File.log(line);
                    } else if(logline.getString("level").equals("warning")){
                        so.readStuff(this, logline);
                    }

                    mNotifications.updateUploadNotification(
                                    uploadFileName,
                                    so.getNotificationContent(),
                                    so.getNotificationBigText(),
                                    so.getNotificationPercent()
                            );
                }
            } catch (InterruptedIOException e) {
                FLog.d(TAG, "onHandleIntent: I/O interrupted, stream closed");
            } catch (IOException e) {
                if (!"Stream closed".equals(e.getMessage())) {
                    FLog.e(TAG, "onHandleIntent: error reading stdout", e);
                }
            } catch (JSONException e) {
                FLog.e(TAG, "onHandleIntent: error reading json", e);
            }

            try {
                currentProcess.waitFor();
            } catch (InterruptedException e) {
                FLog.e(TAG, "onHandleIntent: error waiting for process", e);
            }
        }

        boolean result = currentProcess != null && currentProcess.exitValue() == 0;
        onUploadFinished(remote.getName(), uploadPath, uploadFilePath, result);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.cancel(UploadNotifications.PERSISTENT_NOTIFICATION_ID);
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


    private void onUploadFinished(String remote, String uploadPath, String file, boolean result) {
        int notificationId = (int)System.currentTimeMillis();
        int startIndex = file.lastIndexOf("/");
        String fileName;
        if (startIndex >= 0 && startIndex < file.length()) {
            fileName = file.substring(startIndex + 1);
        } else {
            fileName = file;
        }

        sendUploadFinishedBroadcast(remote, uploadPath);

        if (result) {
            mNotifications.showUploadFinishedNotification(notificationId, fileName);
        } else if (transferOnWiFiOnly && connectivityChanged) {
            mNotifications.showConnectivityChangedNotification();
        } else {
            mNotifications.showUploadFailedNotification(notificationId, fileName);
        }
    }

    private void sendUploadFinishedBroadcast(String remote, String uploadPath) {
        Intent intent = new Intent();
        intent.setAction(getString(R.string.background_service_broadcast));
        intent.putExtra(getString(R.string.background_service_broadcast_data_remote), remote);
        intent.putExtra(getString(R.string.background_service_broadcast_data_path), uploadPath);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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
