package ca.pkay.rcloneexplorer.Services;


import static ca.pkay.rcloneexplorer.Items.SyncDirectionObject.SYNC_LOCAL_TO_REMOTE;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
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
import java.util.PriorityQueue;
import java.util.Queue;

import ca.pkay.rcloneexplorer.Database.DatabaseHandler;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.Items.Task;
import ca.pkay.rcloneexplorer.Log2File;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import ca.pkay.rcloneexplorer.notifications.GenericSyncNotification;
import ca.pkay.rcloneexplorer.notifications.StatusObject;
import ca.pkay.rcloneexplorer.notifications.SyncServiceNotifications;
import ca.pkay.rcloneexplorer.util.FLog;
import ca.pkay.rcloneexplorer.util.SyncLog;
import ca.pkay.rcloneexplorer.util.WifiConnectivitiyUtil;
import ca.pkay.rcloneexplorer.util.WifiConnectivitiyUtil.Connection;


public class SyncService extends IntentService {


    //those Extras do not follow the above schema, because they are exposed to external applications
    //That means shorter values make it easier to use. There is no other technical reason
    public static final String TASK_ACTION= "START_TASK";
    public static final String EXTRA_TASK_ID= "task";
    public static final String EXTRA_TASK_SILENT= "notification";

    enum FAILURE_REASON {
        NONE,
        NO_UNMETERED,
        NO_CONNECTION,
        RCLONE_ERROR
    }
    private static final String TAG = "SyncService";

    public static final String REMOTE_ARG = "ca.pkay.rcexplorer.SYNC_SERVICE_REMOTE_ARG";
    public static final String REMOTE_PATH_ARG = "ca.pkay.rcexplorer.SYNC_SERVICE_REMOTE_PATH_ARG";
    public static final String LOCAL_PATH_ARG = "ca.pkay.rcexplorer.SYNC_LOCAL_PATH_ARG";
    public static final String SYNC_DIRECTION_ARG = "ca.pkay.rcexplorer.SYNC_DIRECTION_ARG";
    public static final String SHOW_RESULT_NOTIFICATION = "ca.pkay.rcexplorer.SHOW_RESULT_NOTIFICATION";
    public static final String TASK_NAME = "ca.pkay.rcexplorer.TASK_NAME";
    public static final String TASK_WIFI_ONLY = "ca.pkay.rcexplorer.TASK_WIFI_ONLY";
    public static final String TASK_MD5SUM = "ca.pkay.rcexplorer.TASK_MD5SUM";

    private Rclone rclone;
    private Log2File log2File;
    private boolean connectivityChanged;
    Process currentProcess;
    SyncServiceNotifications notificationManager = new SyncServiceNotifications(this);

    private Queue<InternalTaskItem> mTaskQueue = new PriorityQueue();
    private boolean mTaskRecieved = false;

    public SyncService() {
        super("ca.pkay.rcexplorer.SYNC_SERCVICE");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        (new GenericSyncNotification(this)).setNotificationChannel(
                SyncServiceNotifications.CHANNEL_ID,
                getString(R.string.sync_service_notification_channel_title),
                R.string.sync_service_notification_channel_description
        );
        (new GenericSyncNotification(this)).setNotificationChannel(
                SyncServiceNotifications.CHANNEL_SUCCESS_ID,
                getString(R.string.sync_service_notification_channel_success_title),
                R.string.sync_service_notification_channel_success_description
        );
        (new GenericSyncNotification(this)).setNotificationChannel(
                SyncServiceNotifications.CHANNEL_FAIL_ID,
                getString(R.string.sync_service_notification_channel_fail_title),
                R.string.sync_service_notification_channel_fail_description
        );
        rclone = new Rclone(this);
        log2File = new Log2File(this);
        registerBroadcastReceivers();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.e(TAG, "onHandleIntent");
        if (intent == null) {
            return;
        }

        Log.e(TAG, "onHandleIntent "+intent.getLongExtra(EXTRA_TASK_ID, -1));

        startForeground(SyncServiceNotifications.PERSISTENT_NOTIFICATION_ID_FOR_SYNC, notificationManager.getPersistentNotification("SyncService").build());

        handleTask(handleTaskStartIntent(intent));

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.cancel(SyncServiceNotifications.PERSISTENT_NOTIFICATION_ID_FOR_SYNC);
        stopForeground(true);

    }

    private void handleTask(InternalTaskItem internalTask) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isLoggingEnable = sharedPreferences.getBoolean(getString(R.string.pref_key_logs), false);

        String title = internalTask.title;
        FAILURE_REASON failureReason = FAILURE_REASON.NONE;
        if(internalTask.title.equals("")){
            title = internalTask.remotePath;
        }

        StatusObject statusObject = new StatusObject(this);
        Connection connection = WifiConnectivitiyUtil.Companion.dataConnection(this.getApplicationContext());


        if (internalTask.transferOnWiFiOnly && connection == Connection.METERED) {
            failureReason = FAILURE_REASON.NO_UNMETERED;
        } else if (connection == Connection.DISCONNECTED || connection == Connection.NOT_AVAILABLE) {
            failureReason = FAILURE_REASON.NO_CONNECTION;
        } else {
            currentProcess = rclone.sync(internalTask.remoteItem, internalTask.remotePath, internalTask.localPath, internalTask.syncDirection, internalTask.md5sum);
            if (currentProcess != null) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(currentProcess.getErrorStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        boolean isJson = false;
                        try {
                            JSONObject logline = new JSONObject(line);
                            isJson = true;
                        } catch (JSONException e) {}

                        if(isJson) {
                            try {
                                JSONObject logline = new JSONObject(line);

                                //todo: migrate this to StatusObject, so that we can handle everything properly.
                                if(logline.getString("level").equals("error")){
                                    if(isLoggingEnable) {
                                        log2File.log(line);
                                    }
                                    statusObject.parseLoglineToStatusObject(logline);
                                } else if(logline.getString("level").equals("warning")){
                                    statusObject.parseLoglineToStatusObject(logline);
                                }

                                //Log.e("TAG", logline.toString());
                                notificationManager.updateSyncNotification(
                                        title,
                                        statusObject.getNotificationContent(),
                                        statusObject.getNotificationBigText(),
                                        statusObject.getNotificationPercent()
                                );

                            } catch (JSONException e) {
                                FLog.e(TAG, "onHandleIntent: the offending line:", line);
                                FLog.e(TAG, "onHandleIntent: error reading json", e);
                            }
                        }
                    }
                } catch (InterruptedIOException e) {
                    FLog.d(TAG, "onHandleIntent: I/O interrupted, stream closed");
                } catch (IOException e) {
                    if (!"Stream closed".equals(e.getMessage())) {
                        FLog.e(TAG, "onHandleIntent: error reading stdout", e);
                    }
                    FLog.e(TAG, "onHandleIntent: error reading stdout", e);
                }

                try {
                    currentProcess.waitFor();
                } catch (InterruptedException e) {
                    FLog.e(TAG, "onHandleIntent: error waiting for process", e);
                }
            }
            sendUploadFinishedBroadcast(internalTask.remoteItem.getName(), internalTask.remotePath);
        }

        int notificationId = (int)System.currentTimeMillis();

        if(internalTask.silentRun){
            if (internalTask.transferOnWiFiOnly && connectivityChanged || (currentProcess == null || currentProcess.exitValue() != 0)) {
                String content = getString(R.string.operation_failed_unknown, title);

                switch (failureReason) {
                    case NONE:
                        if(connectivityChanged){
                            content = getString(R.string.operation_failed_data_change, title);
                        }
                        break;
                    case NO_UNMETERED:
                        content = getString(R.string.operation_failed_no_unmetered, title);
                        break;
                    case NO_CONNECTION:
                        content = getString(R.string.operation_failed_no_connection, title);
                        break;
                }
                //Todo: check if we should also add errors on success
                statusObject.printErrors();
                String errors = statusObject.getAllErrorMessages();
                if(!errors.isEmpty()) {
                    content += "\n\n\n"+statusObject.getAllErrorMessages();
                }
                SyncLog.error(this, getString(R.string.operation_failed), content);
                notificationManager.showFailedNotification(content, notificationId, internalTask.id);
            }else{
                String message = getResources().getQuantityString(R.plurals.operation_success_description,
                        statusObject.getTotalTransfers(),
                        title,
                        statusObject.getTotalSize(),
                        statusObject.getTotalTransfers()
                );
                if(statusObject.getTotalTransfers() == 0) {
                    message = getResources().getString(R.string.operation_success_description_zero);
                }
                if(statusObject.getDeletions()>0){
                    message += "\n" + getString(R.string.operation_success_description_deletions_prefix, statusObject.getDeletions());
                }
                SyncLog.info(this, getString(R.string.operation_success, title), message);
                notificationManager.showSuccessNotification(title, message, notificationId);
            }
        }
    }

    private InternalTaskItem handleTaskStartIntent(Intent intent) {
        final String action = intent.getAction();
        if (action.equals(TASK_ACTION)) {
            DatabaseHandler db = new DatabaseHandler(this);
            for (Task task: db.getAllTasks()){
                if(task.getId() == intent.getLongExtra(EXTRA_TASK_ID, -1)){
                    String path = task.getLocalPath();

                    boolean silentRun = intent.getBooleanExtra(EXTRA_TASK_SILENT, true);

                    RemoteItem remoteItem = new RemoteItem(task.getRemoteId(), task.getRemoteType(), "");
                    Intent taskIntent = new Intent();
                    taskIntent.setClass(this.getApplicationContext(), ca.pkay.rcloneexplorer.Services.SyncService.class);

                    taskIntent.putExtra(SyncService.REMOTE_ARG, remoteItem);
                    taskIntent.putExtra(SyncService.LOCAL_PATH_ARG, path);
                    taskIntent.putExtra(SyncService.SYNC_DIRECTION_ARG, task.getDirection());
                    taskIntent.putExtra(SyncService.REMOTE_PATH_ARG, task.getRemotePath());
                    taskIntent.putExtra(SyncService.TASK_NAME, task.getTitle());
                    taskIntent.putExtra(EXTRA_TASK_ID, task.getId());
                    taskIntent.putExtra(SyncService.SHOW_RESULT_NOTIFICATION, silentRun);
                    taskIntent.putExtra(SyncService.TASK_WIFI_ONLY, task.getWifionly());
                    taskIntent.putExtra(SyncService.TASK_MD5SUM, task.getMd5sum());

                    return InternalTaskItem.newInstance(taskIntent, this);
                }
            }
            return null;
        } else {
            return InternalTaskItem.newInstance(intent, this);
        }
    }


    public static Intent createInternalStartIntent(Context context, long id) {
        Intent i = new Intent(context, SyncService.class);
        i.setAction(TASK_ACTION);
        i.putExtra(EXTRA_TASK_ID, id);
        return i;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (currentProcess != null) {
            currentProcess.destroy();
        }

        unregisterReceiver(connectivityChangeBroadcastReceiver);
    }

    private void sendUploadFinishedBroadcast(String remote, String path) {
        Intent intent = new Intent();
        intent.setAction(getString(R.string.background_service_broadcast));
        intent.putExtra(getString(R.string.background_service_broadcast_data_remote), remote);
        intent.putExtra(getString(R.string.background_service_broadcast_data_path), path);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    /**
     * Okay this is a bit embarrasing. I did not write down WHY i needed this internal class.
     * I assume it was because we have multiple vectors on how this service can be started,
     * and because they use different methods to do stuff, we need to unify it here.
     */
    private static class InternalTaskItem {
        public long id;
        public RemoteItem remoteItem;
        public String remotePath;
        public String localPath;
        public String title;
        public int syncDirection = SYNC_LOCAL_TO_REMOTE;
        public boolean silentRun = true;
        public boolean md5sum = Task.TASK_MD5SUM_DEFAULT;

        // this should not use the task default. It should fallback to the settings.
        // reason: tasks should not use the settings-setting, but everything else should.
        public boolean transferOnWiFiOnly = Task.TASK_WIFIONLY_DEFAULT;

        public static InternalTaskItem newInstance(Intent intent, Context context)
        {

            // todo: define this default value somewhere central. Dont hardcode it.
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            boolean transferOnWiFiOnly = sharedPreferences.getBoolean(context.getString(R.string.pref_key_wifi_only_transfers), false);

            InternalTaskItem itt = new InternalTaskItem();
            itt.id = intent.getLongExtra(EXTRA_TASK_ID, -1);
            itt.remoteItem = intent.getParcelableExtra(REMOTE_ARG);
            itt.remotePath = intent.getStringExtra(REMOTE_PATH_ARG);
            itt.localPath = intent.getStringExtra(LOCAL_PATH_ARG);
            itt.title = intent.getStringExtra(TASK_NAME);
            itt.syncDirection = intent.getIntExtra(SYNC_DIRECTION_ARG, 1);
            itt.silentRun = intent.getBooleanExtra(SHOW_RESULT_NOTIFICATION, true);
            itt.md5sum = intent.getBooleanExtra(TASK_MD5SUM, Task.TASK_MD5SUM_DEFAULT);
            itt.transferOnWiFiOnly = intent.getBooleanExtra(TASK_WIFI_ONLY, transferOnWiFiOnly);
            return itt;
        }

    }

}
