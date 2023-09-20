package ca.pkay.rcloneexplorer.BroadcastReceivers;

import static ca.pkay.rcloneexplorer.Services.SyncService.EXTRA_TASK_ID;
import static ca.pkay.rcloneexplorer.Services.SyncService.TASK_CANCEL_ACTION;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ca.pkay.rcloneexplorer.Services.SyncService;

public class SyncCancelAction extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent syncIntent = new Intent(context, SyncService.class);
        syncIntent.setAction(TASK_CANCEL_ACTION);
        syncIntent.putExtra(EXTRA_TASK_ID, intent.getLongExtra(EXTRA_TASK_ID, -1));
        context.startService(syncIntent);
    }
}
