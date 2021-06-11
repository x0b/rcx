package ca.pkay.rcloneexplorer.BroadcastReceivers;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Calendar;

import ca.pkay.rcloneexplorer.Database.DatabaseHandler;
import ca.pkay.rcloneexplorer.Items.Trigger;
import ca.pkay.rcloneexplorer.Services.TriggerService;

public class TriggerReciever extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("app", "onRec");

        assert intent != null;
        if(intent.getAction().equals(TriggerService.TRIGGER_RECIEVE)){
            long i = intent.getLongExtra(TriggerService.TRIGGER_ID, -1);
            Log.e("app", "rec: "+i);
            if(i==-1)
                return;

            Intent service = new Intent(context, TriggerService.class);
            service.setAction(TriggerService.TRIGGER_RECIEVE);
            service.putExtra(TriggerService.TRIGGER_ID, i);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(service);
            }else{
                context.startService(service);
            }
        }
    }

}
