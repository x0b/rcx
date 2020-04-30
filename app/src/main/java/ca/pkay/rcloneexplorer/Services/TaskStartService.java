package ca.pkay.rcloneexplorer.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
  import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import ca.pkay.rcloneexplorer.Database.Task;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.util.FLog;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class TaskStartService extends IntentService {

    private static final String TAG = "TaskStartService";
    public static final String TASK_ACTION = "START_TASK";
    private static final String EXTRA_TASK = "task";
    private static final String EXTRA_TASK_SILENT = "notification";

    public TaskStartService() {
        super(TAG);
        FLog.v(TAG, "Start service intent!");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createPersistentNotification();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (null == intent) {
            return;
        }
        if (TASK_ACTION.equals(intent.getAction()) && null != intent.getExtras()) {
            Task task = intent.getParcelableExtra(EXTRA_TASK);
            if (null == task) {
                return;
            }
            String path = task.getLocalPath();

            boolean silentRun = intent.getBooleanExtra(EXTRA_TASK_SILENT, true);

            RemoteItem remoteItem = new RemoteItem(task.getRemoteId(), task.getRemoteType());
            Intent taskIntent = new Intent();
            taskIntent.setClass(getApplicationContext(), SyncService.class);
            taskIntent.putExtra(SyncService.REMOTE_ARG, remoteItem);
            taskIntent.putExtra(SyncService.LOCAL_PATH_ARG, path);
            taskIntent.putExtra(SyncService.SYNC_DIRECTION_ARG, task.getDirection());
            taskIntent.putExtra(SyncService.REMOTE_PATH_ARG, task.getRemotePath());
            taskIntent.putExtra(SyncService.SHOW_RESULT_NOTIFICATION, silentRun);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(taskIntent);
            } else {
                startService(taskIntent);
            }
        }
    }

    /**
     * This can be called when an intent is recieved. If no notification is created when a service is started via startForegroundService(), the service is beeing killed by
     * android after 5 seconds. In this case, we need to create a persistent notification because otherwise we cant start the sync task.
     */
    private void createPersistentNotification() {
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "task_intent_notification";
            // TODO i10n
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Channel for intent notifications", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManagerCompat.from(this).createNotificationChannel(channel);
            // TODO: create meaningful notification
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("").setContentText("").build();

            startForeground(1, notification);
        }
    }

}
