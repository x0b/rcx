package ca.pkay.rcloneexplorer.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import ca.pkay.rcloneexplorer.Database.DatabaseHandler;
import ca.pkay.rcloneexplorer.Items.Task;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.R;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * Instead of moving this class to the SyncService, we keep it here so that we dont need to expose
 * the more complicated SyncService to other apps.
 *
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class TaskStartService extends IntentService {


    //those Extras do not follow the above schema, because they are exposed to external applications
    //That means shorter values make it easier to use. There is no other technical reason
    public static final String TASK_ACTION= "START_TASK";
    public static final String EXTRA_TASK_ID= "task";
    public static final String EXTRA_TASK_SILENT= "notification";

    public TaskStartService() {
        super("TaskStartService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createPersistentNotification();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action.equals(TASK_ACTION)) {
                DatabaseHandler db = new DatabaseHandler(this);
                for (Task task: db.getAllTasks()){
                    if(task.getId()==intent.getLongExtra(EXTRA_TASK_ID, -1)){
                        String path = task.getLocalPath();

                        boolean silentRun =intent.getBooleanExtra(EXTRA_TASK_SILENT, true);

                        RemoteItem remoteItem = new RemoteItem(task.getRemoteId(), task.getRemoteType(), "");
                        Intent taskIntent = new Intent();
                        taskIntent.setClass(this.getApplicationContext(), ca.pkay.rcloneexplorer.Services.SyncService.class);

                        taskIntent.putExtra(SyncService.REMOTE_ARG, remoteItem);
                        taskIntent.putExtra(SyncService.LOCAL_PATH_ARG, path);
                        taskIntent.putExtra(SyncService.SYNC_DIRECTION_ARG, task.getDirection());
                        taskIntent.putExtra(SyncService.REMOTE_PATH_ARG, task.getRemotePath());
                        taskIntent.putExtra(SyncService.TASK_NAME, task.getTitle());
                        taskIntent.putExtra(SyncService.TASK_ID, task.getId());
                        taskIntent.putExtra(SyncService.SHOW_RESULT_NOTIFICATION, silentRun);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(taskIntent);
                        }else {
                            startService(taskIntent);
                        }
                    }
                }
            }
        }
    }

    public static Intent createInternalStartIntent(Context context, long id) {
        Intent i = new Intent(context, TaskStartService.class);
        i.setAction(TASK_ACTION);
        i.putExtra(EXTRA_TASK_ID, id);
        return i;
    }

    /**
     * This can be called when an intent is recieved. If no notification is created when a service is started via startForegroundService(), the service is beeing killed by
     * android after 5 seconds. In this case, we need to create a persistent notification because otherwise we cant start the sync task.
     */
    private void createPersistentNotification() {
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "task_intent_notification";
            String notification_description = this.getResources().getString(R.string.intent_channel_notification_description);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, notification_description, NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManagerCompat.from(this).createNotificationChannel(channel);
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("").setContentText("").build();
            startForeground(1, notification);
        }
    }

}
