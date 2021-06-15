package ca.pkay.rcloneexplorer.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;

import ca.pkay.rcloneexplorer.BroadcastReceivers.UploadCancelAction;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.Log2File;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import ca.pkay.rcloneexplorer.util.FLog;


public class UploadService extends IntentService {

    private static final String TAG = "UploadService";
    public static final String UPLOAD_PATH_ARG = "ca.pkay.rcexplorer.upload_service.arg1";
    public static final String LOCAL_PATH_ARG = "ca.pkay.rcexplorer.upload_service.arg2";
    public static final String REMOTE_ARG = "ca.pkay.rcexplorer.upload_service.arg3";
    private final String CHANNEL_ID = "ca.pkay.rcexplorer.UPLOAD_CHANNEL";
    private final String UPLOAD_FINISHED_GROUP = "ca.pkay.rcexplorer.UPLOAD_FINISHED_GROUP";
    private final String UPLOAD_FAILED_GROUP = "ca.pkay.rcexplorer.UPLOAD_FAILED_GROUP";
    private final String CHANNEL_NAME = "Uploads";
    private final int PERSISTENT_NOTIFICATION_ID = 90;
    private final int UPLOAD_FINISHED_NOTIFICATION_ID = 41;
    private final int UPLOAD_FAILED_NOTIFICATION_ID = 14;
    private final int CONNECTIVITY_CHANGE_NOTIFICATION_ID = 94;
    private boolean connectivityChanged;
    private boolean transferOnWiFiOnly;
    private Rclone rclone;
    private Log2File log2File;
    private Process currentProcess;

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
            showConnectivityChangedNotification();
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

        Intent foregroundIntent = new Intent(this, UploadService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, foregroundIntent, 0);

        Intent cancelIntent = new Intent(this, UploadCancelAction.class);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, 0, cancelIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_upload)
                .setContentTitle(uploadFileName)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_cancel_download, getString(R.string.cancel), cancelPendingIntent);

        startForeground(PERSISTENT_NOTIFICATION_ID, builder.build());

        currentProcess = rclone.uploadFile(remote, uploadPath, uploadFilePath);

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

                    updateNotification(uploadFileName, notificationContent, notificationBigText, isFile);
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

        boolean result = currentProcess != null && currentProcess.exitValue() == 0;
        onUploadFinished(remote.getName(), uploadPath, uploadFilePath, result);

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

    private boolean checkWifiOnAndConnected() {
        WifiManager wifiMgr = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr == null) {
            return false;
        }

        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON

            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

            return wifiInfo.getNetworkId() != -1;
        }
        else {
            return false; // Wi-Fi adapter is OFF
        }
    }

    private void updateNotification(String uploadFileName, String content, String[] bigTextArray, boolean singleFile) {
        StringBuilder bigText = new StringBuilder();
        for (int i = 0; i < bigTextArray.length; i++) {
            String progressLine = bigTextArray[i];
            if (null != progressLine) {
                bigText.append(progressLine);
            }
            if (singleFile) {
                break;
            }
            if (i < 4) {
                bigText.append("\n");
            }
        }

        Intent foregroundIntent = new Intent(this, UploadService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, foregroundIntent, 0);

        Intent cancelIntent = new Intent(this, UploadCancelAction.class);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, 0, cancelIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_upload)
                .setContentTitle(uploadFileName)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText.toString()))
                .addAction(R.drawable.ic_cancel_download, getString(R.string.cancel), cancelPendingIntent);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(PERSISTENT_NOTIFICATION_ID, builder.build());
    }

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
            showUploadFinishedNotification(notificationId, fileName);
        } else if (transferOnWiFiOnly && connectivityChanged) {
                showConnectivityChangedNotification();
        } else {
            showUploadFailedNotification(notificationId, fileName);
        }
    }

    private void sendUploadFinishedBroadcast(String remote, String uploadPath) {
        Intent intent = new Intent();
        intent.setAction(getString(R.string.background_service_broadcast));
        intent.putExtra(getString(R.string.background_service_broadcast_data_remote), remote);
        intent.putExtra(getString(R.string.background_service_broadcast_data_path), uploadPath);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void showUploadFinishedNotification(int notificationID, String contentText) {
        createSummaryNotificationForFinished();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_upload_done)
                .setContentTitle(getString(R.string.upload_complete))
                .setContentText(contentText)
                .setGroup(UPLOAD_FINISHED_GROUP)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationID, builder.build());
    }

    private void createSummaryNotificationForFinished() {
        Notification summaryNotification =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle(getString(R.string.upload_complete))
                        //set content text to support devices running API level < 24
                        .setContentText(getString(R.string.upload_complete))
                        .setSmallIcon(android.R.drawable.stat_sys_upload_done)
                        .setGroup(UPLOAD_FINISHED_GROUP)
                        .setGroupSummary(true)
                        .setAutoCancel(true)
                        .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(UPLOAD_FINISHED_NOTIFICATION_ID, summaryNotification);
    }

    private void showUploadFailedNotification(int notificationID, String contentText) {
        createSummaryNotificationForFailed();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle(getString(R.string.upload_failed))
                .setContentText(contentText)
                .setGroup(UPLOAD_FAILED_GROUP)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationID, builder.build());
    }

    private void createSummaryNotificationForFailed() {
        Notification summaryNotification =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle(getString(R.string.upload_failed))
                        //set content text to support devices running API level < 24
                        .setContentText(getString(R.string.upload_failed))
                        .setSmallIcon(android.R.drawable.stat_sys_warning)
                        .setGroup(UPLOAD_FAILED_GROUP)
                        .setGroupSummary(true)
                        .setAutoCancel(true)
                        .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(UPLOAD_FAILED_NOTIFICATION_ID, summaryNotification);
    }

    private void showConnectivityChangedNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle(getString(R.string.upload_cancelled))
                .setContentText(getString(R.string.wifi_connections_isnt_available))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(CONNECTIVITY_CHANGE_NOTIFICATION_ID, builder.build());
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
