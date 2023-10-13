package ca.pkay.rcloneexplorer.BroadcastReceivers;

import static ca.pkay.rcloneexplorer.workmanager.SyncWorker.EXTRA_TASK_ID;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ca.pkay.rcloneexplorer.workmanager.SyncManager;

public class SyncRestartAction extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SyncManager sm = new SyncManager(context);
        sm.queue(intent.getLongExtra(EXTRA_TASK_ID, -1));
    }
}
