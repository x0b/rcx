package ca.pkay.rcloneexplorer.Services;

import static ca.pkay.rcloneexplorer.notifications.UploadNotifications.CHANNEL_ID;
import static ca.pkay.rcloneexplorer.notifications.UploadNotifications.CHANNEL_NAME;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;

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

import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Services.support.QueueItem;
import ca.pkay.rcloneexplorer.Services.support.QueueService;
import ca.pkay.rcloneexplorer.notifications.DownloadNotifications;
import ca.pkay.rcloneexplorer.notifications.support.StatusObject;
import ca.pkay.rcloneexplorer.util.FLog;
import ca.pkay.rcloneexplorer.util.SyncLog;


public class UploadService extends QueueService {

    private static final String TAG = "UploadService";
    public static final String UPLOAD_PATH_ARG = "ca.pkay.rcexplorer.upload_service.arg1";
    public static final String LOCAL_PATH_ARG = "ca.pkay.rcexplorer.upload_service.arg2";
    public static final String REMOTE_ARG = "ca.pkay.rcexplorer.upload_service.arg3";


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.*
     */
    public UploadService() {
        super("ca.pkay.rcexplorer.uploadservice");
    }

    @Override
    public void prepare() {
        mNotifications = new DownloadNotifications(this.getApplicationContext());
        setUpNotificationChannels(
                CHANNEL_ID,
                CHANNEL_NAME,
                getString(R.string.upload_service_notification_channel_description)
        );
    }

    @Override
    public QueueItem unpackIntent(Intent intent) {

        String uploadPath = intent.getStringExtra(UPLOAD_PATH_ARG);
        String uploadFilePath = intent.getStringExtra(LOCAL_PATH_ARG);
        RemoteItem remote = intent.getParcelableExtra(REMOTE_ARG);

        // todo: update title
        QueueItem item = new QueueItem(getTitle(uploadFilePath));
        item.set(UPLOAD_PATH_ARG, uploadPath);
        item.set(LOCAL_PATH_ARG, uploadFilePath);
        item.set(REMOTE_ARG, remote);

        return item;
    }

    @Override
    public void handleAction(QueueItem item) {


        final String uploadPath = (String) item.get(UPLOAD_PATH_ARG);
        final String uploadFilePath = (String) item.get(LOCAL_PATH_ARG);
        final RemoteItem remote = (RemoteItem) item.get(REMOTE_ARG);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isLoggingEnable = sharedPreferences.getBoolean(getString(R.string.pref_key_logs), false);

        //Todo: Check if this can be moved together with UploadService;SyncService etc.
        currentProcess = rclone.uploadFile(remote, uploadPath, uploadFilePath);
        if (currentProcess != null) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(currentProcess.getErrorStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        StatusObject so = new StatusObject(this);
                        JSONObject logline = new JSONObject(line);
                        if(isLoggingEnable && logline.getString("level").equals("error")){
                            log2File.log(line);
                        } else if(logline.getString("level").equals("warning")){
                            so.parseLoglineToStatusObject(logline);
                        }

                        mNotifications.updateNotification(
                                item.getTitle(),
                                so.getNotificationContent(),
                                so.getNotificationBigText(),
                                so.getNotificationPercent()
                        );

                    } catch (JSONException e) {
                        FLog.e(TAG, "onHandleIntent: error reading json", e);
                        FLog.e(TAG, "onHandleIntent: the offending line:", line);
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

        boolean result = currentProcess != null && currentProcess.exitValue() == 0;
        onUploadFinished(remote.getName(), uploadPath, uploadFilePath, result);

    }

    private String getTitle(String path) {

        String uploadFileName;
        int slashIndex = path.lastIndexOf("/");
        if (slashIndex >= 0) {
            uploadFileName = path.substring(slashIndex + 1);
        } else {
            uploadFileName = path;
        }
        return uploadFileName;
    }

    //Todo: Check if this can be moved together with UploadService;SyncService etc.
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
            mNotifications.showFinishedNotification(notificationId, fileName);
            SyncLog.error(this, getString(R.string.upload_complete), fileName);
        } else if (transferOnWiFiOnly && connectivityChanged) {
            mNotifications.showConnectivityChangedNotification();
            SyncLog.error(this, getString(R.string.upload_cancelled), fileName);
        } else {
            mNotifications.showFailedNotification(notificationId, fileName);
            SyncLog.error(this, getString(R.string.upload_failed), fileName);
        }
    }

    private void sendUploadFinishedBroadcast(String remote, String uploadPath) {
        Intent intent = new Intent();
        intent.setAction(getString(R.string.background_service_broadcast));
        intent.putExtra(getString(R.string.background_service_broadcast_data_remote), remote);
        intent.putExtra(getString(R.string.background_service_broadcast_data_path), uploadPath);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
