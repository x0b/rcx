package ca.pkay.rcloneexplorer;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ca.pkay.rcloneexplorer.Database.DatabaseHandler;
import ca.pkay.rcloneexplorer.Database.Task;
import ca.pkay.rcloneexplorer.Database.Trigger;

public class TriggerActivity extends AppCompatActivity {


    public static final String ID_EXTRA = "TRIGGER_EDIT_ID";
    private Trigger existingTrigger;
    private DatabaseHandler dbHandler;
    private List<Task> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trigger);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHandler = new DatabaseHandler(this);

        Bundle extras = getIntent().getExtras();
        Long trigger_id;

        if (extras != null) {
            trigger_id = extras.getLong(ID_EXTRA);
            if(trigger_id!=0){
                existingTrigger = dbHandler.getTrigger(trigger_id);
            }
        }

        final Context c = this.getApplicationContext();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(existingTrigger==null){
                    saveTrigger();
                }else{
                    persistTaskChanges();
                }
            }
        });


        Spinner taskDropdown = findViewById(R.id.trigger_targets);

        taskList = dbHandler.getAllTasks();
        String[] items = new String[taskList.size()];

        for (int i = 0; i< taskList.size(); i++) {
            items[i]= taskList.get(i).getTitle();
            Log.e("app", "app: "+items[i].toString());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        taskDropdown.setAdapter(adapter);

        populateFields();
    }

    private void populateFields() {
        Log.e("app!", "Populate Task");
        if(existingTrigger!=null){
            Log.e("app!", "Populate Task"+existingTrigger.getTitle());
            ((TextView)findViewById(R.id.trigger_name_edit)).setText(existingTrigger.getTitle());

            ((CheckBox)findViewById(R.id.cb_is_enabled)).setChecked(existingTrigger.isEnabled());

            ((CheckBox)findViewById(R.id.trigger_cb_monday)).setChecked(existingTrigger.isEnabledAtDay(0));
            ((CheckBox)findViewById(R.id.trigger_cb_tuesday)).setChecked(existingTrigger.isEnabledAtDay(1));
            ((CheckBox)findViewById(R.id.trigger_cb_wednesday)).setChecked(existingTrigger.isEnabledAtDay(2));
            ((CheckBox)findViewById(R.id.trigger_cb_thursday)).setChecked(existingTrigger.isEnabledAtDay(3));
            ((CheckBox)findViewById(R.id.trigger_cb_friday)).setChecked(existingTrigger.isEnabledAtDay(4));
            ((CheckBox)findViewById(R.id.trigger_cb_saturday)).setChecked(existingTrigger.isEnabledAtDay(5));
            ((CheckBox)findViewById(R.id.trigger_cb_sunday)).setChecked(existingTrigger.isEnabledAtDay(6));
            //Todo properly populate the fields
            int pos = 0;

            for(Task t : taskList){
                if(t.getId()==existingTrigger.getWhatToTrigger()){
                    pos = taskList.indexOf(t);
                }
            }
            ((Spinner)findViewById(R.id.trigger_targets)).setSelection(pos);

            TimePicker tp = (TimePicker)findViewById(R.id.trigger_time);
            int seconds = existingTrigger.getTime();
            tp.setHour(seconds/60);
            tp.setMinute(seconds%60);
        }
    }

    private void persistTaskChanges(){
        dbHandler.updateTrigger(getTriggerValues(existingTrigger.getId()));
        Log.e("app!", "Update Task: ");
        finish();
    }

    private void saveTrigger(){
        Trigger newTrigger = dbHandler.createTrigger(getTriggerValues(0L));
        Log.e("app!", "Trigger Dialog: "+newTrigger.toString());
        finish();
    }

    private Trigger getTriggerValues(Long id ){
        Trigger triggerToPopulate = new Trigger(id);
        triggerToPopulate.setTitle(((EditText)findViewById(R.id.trigger_name_edit)).getText().toString());

        triggerToPopulate.setEnabled(((CheckBox)findViewById(R.id.cb_is_enabled)).isChecked());

        triggerToPopulate.setEnabledAtDay(0, ((CheckBox)findViewById(R.id.trigger_cb_monday)).isChecked());
        triggerToPopulate.setEnabledAtDay(1, ((CheckBox)findViewById(R.id.trigger_cb_tuesday)).isChecked());
        triggerToPopulate.setEnabledAtDay(2, ((CheckBox)findViewById(R.id.trigger_cb_wednesday)).isChecked());
        triggerToPopulate.setEnabledAtDay(3, ((CheckBox)findViewById(R.id.trigger_cb_thursday)).isChecked());
        triggerToPopulate.setEnabledAtDay(4, ((CheckBox)findViewById(R.id.trigger_cb_friday)).isChecked());
        triggerToPopulate.setEnabledAtDay(5, ((CheckBox)findViewById(R.id.trigger_cb_saturday)).isChecked());
        triggerToPopulate.setEnabledAtDay(6, ((CheckBox)findViewById(R.id.trigger_cb_sunday)).isChecked());


        //Todo implement proper valuegetting
        int item = ((Spinner)findViewById(R.id.trigger_targets)).getSelectedItemPosition();
        triggerToPopulate.setWhatToTrigger(taskList.get(item).getId());

        TimePicker tp = (TimePicker)findViewById(R.id.trigger_time);

        int sinceMidnight=tp.getHour()*60+tp.getMinute();
        triggerToPopulate.setTime(sinceMidnight);

        return triggerToPopulate;
    }

}