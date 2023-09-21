package ca.pkay.rcloneexplorer.Services;

import static ca.pkay.rcloneexplorer.notifications.UploadNotifications.CHANNEL_NAME;

import android.content.Intent;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;

import ca.pkay.rcloneexplorer.Items.FileItem;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Services.support.QueueItem;
import ca.pkay.rcloneexplorer.Services.support.QueueService;
import ca.pkay.rcloneexplorer.notifications.DownloadNotifications;
import ca.pkay.rcloneexplorer.notifications.support.StatusObject;
import ca.pkay.rcloneexplorer.util.FLog;
import ca.pkay.rcloneexplorer.util.SyncLog;


public class DownloadService extends QueueService {

    private static final String TAG = "DownloadService";
    public static final String DOWNLOAD_ITEM_ARG = "ca.pkay.rcexplorer.download_service.arg1";
    public static final String DOWNLOAD_PATH_ARG = "ca.pkay.rcexplorer.download_service.arg2";
    public static final String REMOTE_ARG = "ca.pkay.rcexplorer.download_service.arg3";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.*
     */
    public DownloadService() {
        super("ca.pkay.rcexplorer.downloadservice");
    }

    @Override
    public void prepare() {
        mNotifications = new DownloadNotifications(this.getApplicationContext());
        setUpNotificationChannels(
                DownloadNotifications.CHANNEL_ID,
                CHANNEL_NAME,
                getString(R.string.download_service_notification_description)
        );
    }

    @Override
    public QueueItem unpackIntent(Intent intent) {

        FileItem downloadItem = intent.getParcelableExtra(DOWNLOAD_ITEM_ARG);
        String downloadPath = intent.getStringExtra(DOWNLOAD_PATH_ARG);
        RemoteItem remote = intent.getParcelableExtra(REMOTE_ARG);

        QueueItem item = new QueueItem(downloadItem.getName());
        item.set(DOWNLOAD_ITEM_ARG, downloadItem);
        item.set(DOWNLOAD_PATH_ARG, downloadPath);
        item.set(REMOTE_ARG, remote);

        return item;
    }

    @Override
    public void handleAction(QueueItem item) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean isLoggingEnable = sharedPreferences.getBoolean(getString(R.string.pref_key_logs), false);

        currentProcess = rclone.downloadFile(
                (RemoteItem) item.get(REMOTE_ARG),
                (FileItem) item.get(DOWNLOAD_ITEM_ARG),
                (String) item.get(DOWNLOAD_PATH_ARG)
        );


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

                        mNotifications.updateNotification(
                                item.getTitle(),
                                so.getNotificationContent(),
                                so.getNotificationBigText(),
                                so.getNotificationPercent()
                        );
                    } catch (JSONException e) {
                        //FLog.e(TAG, "onHandleIntent: error reading json", e);
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

            SyncLog.info(this, getString(R.string.download_complete), item.getTitle());
            mNotifications.showFinishedNotification(notificationId, item.getTitle());
        } else {
            SyncLog.error(this, getString(R.string.download_failed), item.getTitle());
            mNotifications.showFailedNotification(notificationId, item.getTitle());
        }
    }
}
