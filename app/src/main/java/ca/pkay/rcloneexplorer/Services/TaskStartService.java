package ca.pkay.rcloneexplorer.Services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
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

    public TaskStartService() {
        super("TaskStartService");
        Log.e("Service", "Start service intent!");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
       // Log.e("Service", "Start Intent: "+intent.);

        if (intent != null) {
            final String action = intent.getAction();
            if (action.equals("START_TASK")) {
                DatabaseHandler db = new DatabaseHandler(this);
                for (Task t: db.getAllTasks()){
                    if(t.getId()==intent.getIntExtra("task", -1)){
                        Log.e("Service", "Start Task: "+t.getTitle());

                        String path = t.getLocal_path();

                        RemoteItem ri = new RemoteItem(t.getRemote_id(), t.getRemote_type(), "");
                        Intent i = new Intent();
                        i.setClass(this.getApplicationContext(), ca.pkay.rcloneexplorer.Services.SyncService.class);

                        i.putExtra(SyncService.REMOTE_ARG, ri);
                        i.putExtra(SyncService.LOCAL_PATH_ARG, path);
                        i.putExtra(SyncService.SYNC_DIRECTION_ARG, t.getDirection());
                        i.putExtra(SyncService.REMOTE_PATH_ARG, t.getRemote_path());
                        startService(i);
                    }
                }
            }
        }
    }

}
