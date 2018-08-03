package ca.pkay.rcloneexplorer.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ca.pkay.rcloneexplorer.Services.SyncService;

public class SyncCancelAction extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent syncIntent = new Intent(context, SyncService.class);
        context.stopService(syncIntent);
    }
}
