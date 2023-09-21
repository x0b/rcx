package ca.pkay.rcloneexplorer.Services.support;

import static ca.pkay.rcloneexplorer.notifications.DownloadNotifications.PERSISTENT_NOTIFICATION_ID;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import ca.pkay.rcloneexplorer.Log2File;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import ca.pkay.rcloneexplorer.notifications.support.GenericNotification;
import ca.pkay.rcloneexplorer.notifications.GenericSyncNotification;
import ca.pkay.rcloneexplorer.util.WifiConnectivitiyUtil;


public abstract class QueueService extends IntentService {
    protected boolean connectivityChanged;
    protected boolean transferOnWiFiOnly;

    protected Rclone rclone;
    protected Log2File log2File;
    protected Process currentProcess;
    protected GenericNotification mNotifications;
    private String mTAG;
    private static HashMap<String, LinkedList<QueueItem>> mQueue = new HashMap();
    private static HashMap<String, Thread> mWorker = new HashMap();

    /**
     * @param name
     * @deprecated
     */
    public QueueService(String name) {
        super(name);
        mTAG = name;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        rclone = new Rclone(this);
        log2File = new Log2File(this);

        prepare();

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


        /*

        final FileItem downloadItem = intent.getParcelableExtra(DOWNLOAD_ITEM_ARG);
        final String downloadPath = intent.getStringExtra(DOWNLOAD_PATH_ARG);
        final RemoteItem remote = intent.getParcelableExtra(REMOTE_ARG);

        final String uploadPath = intent.getStringExtra(UPLOAD_PATH_ARG);
        final String uploadFilePath = intent.getStringExtra(LOCAL_PATH_ARG);
        final RemoteItem remote = intent.getParcelableExtra(REMOTE_ARG);*/

        LinkedList<QueueItem> queue = getCurrentQueue();
        QueueItem qi = unpackIntent(intent);
        queue.push(qi);

        Thread worker = mWorker.get(mTAG);
        if(worker == null) {
            worker = new Thread(() -> {
                while(queue.size()>0) {

                    QueueItem current = queue.poll();

                    ArrayList<String> notificationBigText = new ArrayList<>();
                    startForeground(PERSISTENT_NOTIFICATION_ID, mNotifications.createNotification(
                            current.getTitle(),
                            notificationBigText
                    ).build());

                    handleAction(current);
                }
                mNotifications.cancelPersistent();
                stopForeground(true);
            });
            mWorker.put(mTAG, worker);
            worker.start();
        }
    }

    abstract public void handleAction(QueueItem item);

    abstract public QueueItem unpackIntent(Intent intent);

    private LinkedList<QueueItem> getCurrentQueue() {
        LinkedList<QueueItem> queue = mQueue.get(mTAG);
        if(queue != null) {
            return queue;
        }
        mQueue.put(mTAG, new LinkedList<>());
        return mQueue.get(mTAG);
    }

    abstract public void prepare();
    public void setUpNotificationChannels(String channelid, String channelname, String channelDescription) {
        (new GenericSyncNotification(this)).setNotificationChannel(
                channelid,
                channelname,
                channelDescription
        );
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

        if (transferOnWiFiOnly) {
            unregisterReceiver(connectivityChangeBroadcastReceiver);
        }
    }
}
