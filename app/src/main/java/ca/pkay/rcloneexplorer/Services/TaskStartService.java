package ca.pkay.rcloneexplorer.Services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import ca.pkay.rcloneexplorer.Database.DatabaseHandler;
import ca.pkay.rcloneexplorer.Database.Task;
import ca.pkay.rcloneexplorer.Items.RemoteItem;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class TaskStartService extends IntentService {

    public static String TASK_ACTION= "START_TASK";
    private static String EXTRA_TASK_ID= "task";
    private static String EXTRA_TASK_SILENT= "notification";

    public TaskStartService() {
        super("TaskStartService");
        Log.e("Service", "Start service intent!");
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
                    if(task.getId()==intent.getIntExtra(EXTRA_TASK_ID, -1)){
                        String path = task.getLocal_path();

                        boolean silentRun =intent.getBooleanExtra(EXTRA_TASK_SILENT, true);

                        RemoteItem remoteItem = new RemoteItem(task.getRemote_id(), task.getRemote_type(), "");
                        Intent taskIntent = new Intent();
                        taskIntent.setClass(this.getApplicationContext(), ca.pkay.rcloneexplorer.Services.SyncService.class);

                        taskIntent.putExtra(SyncService.REMOTE_ARG, remoteItem);
                        taskIntent.putExtra(SyncService.LOCAL_PATH_ARG, path);
                        taskIntent.putExtra(SyncService.SYNC_DIRECTION_ARG, task.getDirection());
                        taskIntent.putExtra(SyncService.REMOTE_PATH_ARG, task.getRemote_path());
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

    /**
     * This can be called when an intent is recieved. If no notification is created when a service is started via startForegroundService(), the service is beeing killed by
     * android after 5 seconds. In this case, we need to create a persistent notification because otherwise we cant start the sync task.
     */
    private void createPersistentNotification() {
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "task_intent_notification";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Channel for intent notifications", NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("").setContentText("").build();

            startForeground(1, notification);
        }
    }

}
