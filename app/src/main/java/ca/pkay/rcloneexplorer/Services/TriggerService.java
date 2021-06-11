package ca.pkay.rcloneexplorer.Services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.Calendar;

import ca.pkay.rcloneexplorer.BroadcastReceivers.TriggerReciever;
import ca.pkay.rcloneexplorer.Database.DatabaseHandler;
import ca.pkay.rcloneexplorer.Items.Trigger;
import ca.pkay.rcloneexplorer.R;

public class TriggerService extends Service {

    private DatabaseHandler dbHandler;
    private Context context;

    public static String TRIGGER_RECIEVE = "TRIGGER_RECIEVE";
    public static String TRIGGER_ID = "TRIGGER_ID";

    public static String CHANNEL_ID = "CHANNEL_ID";
    public static int SERVICE_NOTIFICATION_ID = 42;

    //Required for Servicecall
    public TriggerService() {}

    public TriggerService(Context c) {
        Log.e("app", "StartTs2");
        this.dbHandler = new DatabaseHandler(c);
        this.context = c;
    }

    public void queueTrigger(){
        Log.e("app", "queue trigger");
        for(Trigger t : dbHandler.getAllTrigger()){
            queueSingleTrigger(t);
        }
    }

    public void queueSingleTrigger(Trigger trigger){
        if(trigger.isEnabled()){
            AlarmManager am =(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent i = new Intent(context, TriggerReciever.class);
            i.setAction(TRIGGER_RECIEVE);
            i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            long wtt = trigger.getId();
            i.putExtra(TRIGGER_ID, wtt);


            // Todo: Beacause of the long to int cast, this may fail when the user has more than Integer.MAX tasks.
            PendingIntent pi = PendingIntent.getBroadcast(context, (int) wtt, i, PendingIntent.FLAG_UPDATE_CURRENT);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            int seconds = trigger.getTime();
            calendar.set(Calendar.HOUR_OF_DAY, seconds/60);
            calendar.set(Calendar.MINUTE, seconds%60);

            long difference = calendar.getTimeInMillis()-System.currentTimeMillis();
            //Properly schedule past events
            if(difference<0){
                difference = (24*60*60*1000) + difference;
            }

            // If a triggered event schedules the next occurence, we need to make sure that it does not create an endless loop for 60 seconds.
            // Todo: Think about moving the scheduling into a handler that waits 60 seconds and triggers then.
            Log.e("app", ""+Calendar.getInstance().get(Calendar.MINUTE));
            Log.e("app", ""+seconds%60);
            if(Calendar.getInstance().get(Calendar.MINUTE) == seconds%60){
                int move = 3*60*1000;
                //difference = difference + move;
                Log.e("app", "Move Back "+difference+"<-"+(difference-move));
            }

            long timeToTrigger = System.currentTimeMillis() + difference;

            Log.e("app", "Queue: "+trigger.getId());
            am.cancel(pi);
            am.setExact(
                    AlarmManager.RTC_WAKEUP,
                    timeToTrigger,
                    pi
            );
            return;
        }
        Log.e("app", "Not enabled: "+trigger.getId());
    }

    private void startTask(Trigger trigger){
        Log.e("app", "start: "+trigger.getTitle());
        boolean skipBecauseOfWeekday;
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK)-2; //account for monday beeing 1 and sunday beeing 0

        if(day==-1){//check for sundays. Calendar starts with sunday.
            skipBecauseOfWeekday = !trigger.isEnabledAtDay(6);
        }else{
            skipBecauseOfWeekday = !trigger.isEnabledAtDay(day);
        }

        if(skipBecauseOfWeekday){
            return;
        }

        Intent i = new Intent(context, TaskStartService.class);
        i.setAction(TaskStartService.TASK_ACTION);
        i.putExtra(TaskStartService.EXTRA_TASK_ID, trigger.getWhatToTrigger().intValue());
        //i.putExtra(TaskStartService.EXTRA_TASK_SILENT, true);
        context.startService(i);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotification();
        long id = intent.getLongExtra(TRIGGER_ID, -1);
        this.dbHandler = new DatabaseHandler(getBaseContext());
        this.context = getBaseContext();
        Trigger t = dbHandler.getTrigger(id);
        startTask(t);
        queueSingleTrigger(t);
        stopForeground(true);
        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotification(){
        createNotificationChannel();
        Notification notification;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle(getText(R.string.notification_triggerservice_title))
                    .setContentText(getText(R.string.notification_triggerservice_description))
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .build();
        } else {

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setContentTitle(getText(R.string.notification_triggerservice_title))
                    .setContentText(getText(R.string.notification_triggerservice_description))
                    .setSmallIcon(R.drawable.ic_launcher_foreground);
            notification = notificationBuilder.build();
        }
        startForeground(SERVICE_NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, getString(R.string.notification_triggerservice_title), importance);
            channel.setDescription(getString(R.string.notification_triggerservice_description));
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
