package ca.pkay.rcloneexplorer.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ca.pkay.rcloneexplorer.Services.UploadService;

public class UploadCancelAction extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent uploadIntent = new Intent(context, UploadService.class);
        context.stopService(uploadIntent);
    }
}
