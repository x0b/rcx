package ca.pkay.rcloneexplorer.Services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Calendar;

import ca.pkay.rcloneexplorer.Database.DatabaseHandler;
import ca.pkay.rcloneexplorer.Items.Trigger;

public class TriggerService extends IntentService {

    private DatabaseHandler dbHandler;
    private Context context;

    private static String TRIGGER_RECIEVE = "TRIGGER_RECIEVE";
    private static String TRIGGER_ID = "TRIGGER_ID";

    public TriggerService() {
        super("TriggerService");
        dbHandler = new DatabaseHandler(this);
        context = this;
    }

    public TriggerService(Context c) {
        super("TriggerService");
        this.dbHandler = new DatabaseHandler(c);
        this.context = c;
        queueTrigger();
    }

    private void queueTrigger(){
        for(Trigger t : dbHandler.getAllTrigger()){
            if(!t.isEnabled()){
                AlarmManager am =(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                Intent i = new Intent(context, TriggerService.class);
                i.setAction(TRIGGER_RECIEVE);
                i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                i.putExtra(TRIGGER_ID, t.getWhatToTrigger().intValue());

                PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());

                int seconds = t.getTime();
                calendar.set(Calendar.HOUR_OF_DAY, seconds/60);
                calendar.set(Calendar.MINUTE, seconds%60);

                long difference = calendar.getTimeInMillis()-System.currentTimeMillis();
                //Properly schedule past events
                if(difference<0){
                    difference = (24*60*60*1000) + difference;
                }
                am.cancel(pi);
                am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+difference, AlarmManager.INTERVAL_DAY, pi);
            }
        }
    }

    private void startTask(int id){
        Trigger t = dbHandler.getTrigger((long) id);

        boolean skipBecauseOfWeekday;
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK)-2; //account for monday beeing 1 and sunday beeing 0

        if(day==-1){//check for sundays. Calendar starts with sunday.
            skipBecauseOfWeekday = !t.isEnabledAtDay(6);
        }else{
            skipBecauseOfWeekday = !t.isEnabledAtDay(day);
        }

        if(skipBecauseOfWeekday){
            return;
        }

        Intent i = new Intent(context, TaskStartService.class);
        i.setAction(TaskStartService.TASK_ACTION);
        i.putExtra(TaskStartService.EXTRA_TASK_ID, t.getWhatToTrigger().intValue());
        //i.putExtra(TaskStartService.EXTRA_TASK_SILENT, true);
        context.startService(i);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        assert intent != null;
        if(intent.getAction().equals(TRIGGER_RECIEVE)){
            int i = intent.getIntExtra(TRIGGER_ID, -1);
            if(i==-1)
                return;
            startTask(i);
        }
    }
}
