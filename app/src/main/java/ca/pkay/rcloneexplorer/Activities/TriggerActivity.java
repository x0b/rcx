package ca.pkay.rcloneexplorer.Activities;

import static ca.pkay.rcloneexplorer.Items.Trigger.TRIGGER_DAY_FRI;
import static ca.pkay.rcloneexplorer.Items.Trigger.TRIGGER_DAY_MON;
import static ca.pkay.rcloneexplorer.Items.Trigger.TRIGGER_DAY_SAT;
import static ca.pkay.rcloneexplorer.Items.Trigger.TRIGGER_DAY_SUN;
import static ca.pkay.rcloneexplorer.Items.Trigger.TRIGGER_DAY_THU;
import static ca.pkay.rcloneexplorer.Items.Trigger.TRIGGER_DAY_TUE;
import static ca.pkay.rcloneexplorer.Items.Trigger.TRIGGER_DAY_WED;
import static ca.pkay.rcloneexplorer.Items.Trigger.TRIGGER_ID_DOESNTEXIST;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import ca.pkay.rcloneexplorer.Database.DatabaseHandler;
import ca.pkay.rcloneexplorer.Items.Task;
import ca.pkay.rcloneexplorer.Items.Trigger;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Services.TriggerService;
import ca.pkay.rcloneexplorer.util.ActivityHelper;
import es.dmoral.toasty.Toasty;

public class TriggerActivity extends AppCompatActivity {


    public static final String ID_EXTRA = "TRIGGER_EDIT_ID";
    private Trigger mTrigger;
    private DatabaseHandler dbHandler;
    private List<Task> mTaskList;

    private CardView mCardInterval;
    private CardView mCardWeekday;
    private CardView mCardTime;

    private EditText mTitle;
    private CheckBox mEnabled;
    private Spinner mType;
    private Spinner mInterval;

    private CheckBox mWeekdayMon;
    private CheckBox mWeekdayTue;
    private CheckBox mWeekdayWed;
    private CheckBox mWeekdayThu;
    private CheckBox mWeekdayFri;
    private CheckBox mWeekdaySat;
    private CheckBox mWeekdaySun;

    private Spinner mTargetDropdown;
    private TimePicker mTimepicker;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityHelper.applyTheme(this);
        setContentView(R.layout.activity_trigger);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mCardInterval = findViewById(R.id.intervalCard);
        mCardWeekday = findViewById(R.id.weekdaysCard);
        mCardTime = findViewById(R.id.timeCard);

        mTitle = findViewById(R.id.trigger_name_edit);
        mEnabled = findViewById(R.id.cb_is_enabled);
        mType = findViewById(R.id.triggerType);
        mInterval = findViewById(R.id.spinnerIntervals);

        mWeekdayMon = findViewById(R.id.trigger_cb_monday);
        mWeekdayTue = findViewById(R.id.trigger_cb_tuesday);
        mWeekdayWed = findViewById(R.id.trigger_cb_wednesday);
        mWeekdayThu = findViewById(R.id.trigger_cb_thursday);
        mWeekdayFri = findViewById(R.id.trigger_cb_friday);
        mWeekdaySat = findViewById(R.id.trigger_cb_saturday);
        mWeekdaySun = findViewById(R.id.trigger_cb_sunday);
        mTimepicker = findViewById(R.id.trigger_time);

        mTargetDropdown = findViewById(R.id.trigger_targets);



        dbHandler = new DatabaseHandler(this);
        mTaskList = dbHandler.getAllTasks();

        Bundle extras = getIntent().getExtras();
        long trigger_id;

        if (extras != null) {
            trigger_id = extras.getLong(ID_EXTRA);
            if(trigger_id != 0){
                mTrigger = dbHandler.getTrigger(trigger_id);
                if(mTrigger == null){
                    Toasty.error(this, this.getResources().getString(R.string.triggeractivity_trigger_not_found)).show();
                    finish();
                }
            }
        } else {
            mTrigger = new Trigger(TRIGGER_ID_DOESNTEXIST);
        }

        FloatingActionButton saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> saveTrigger());


        setUpTargetsDropdown();

        mEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> mTrigger.setEnabled(isChecked));

        mType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Object item = parent.getItemAtPosition(pos);
                System.out.println(item.toString());
                if(pos == Trigger.TRIGGER_TYPE_SCHEDULE) {
                    mTrigger.setType(Trigger.TRIGGER_TYPE_SCHEDULE);
                } else {
                    mTrigger.setType(Trigger.TRIGGER_TYPE_INTERVAL);
                }

                //update ui here, because typechanges also changes displayed items.
                updateUiFromTrigger();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        mWeekdayMon.setOnCheckedChangeListener((buttonView, isChecked) -> mTrigger.setEnabledAtDay(TRIGGER_DAY_MON, isChecked));
        mWeekdayThu.setOnCheckedChangeListener((buttonView, isChecked) -> mTrigger.setEnabledAtDay(TRIGGER_DAY_TUE, isChecked));
        mWeekdayWed.setOnCheckedChangeListener((buttonView, isChecked) -> mTrigger.setEnabledAtDay(TRIGGER_DAY_WED, isChecked));
        mWeekdayThu.setOnCheckedChangeListener((buttonView, isChecked) -> mTrigger.setEnabledAtDay(TRIGGER_DAY_THU, isChecked));
        mWeekdayFri.setOnCheckedChangeListener((buttonView, isChecked) -> mTrigger.setEnabledAtDay(TRIGGER_DAY_FRI, isChecked));
        mWeekdaySat.setOnCheckedChangeListener((buttonView, isChecked) -> mTrigger.setEnabledAtDay(TRIGGER_DAY_SAT, isChecked));
        mWeekdaySun.setOnCheckedChangeListener((buttonView, isChecked) -> mTrigger.setEnabledAtDay(TRIGGER_DAY_SUN, isChecked));

        mTimepicker.setOnTimeChangedListener((view, hourOfDay, minute) -> mTrigger.setTime(hourOfDay * 60 + minute));

        mTargetDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                mTrigger.setWhatToTrigger(mTaskList.get(pos).getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        updateUiFromTrigger();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }


    /**
     * Set up Task-Target Dropdown
     */
    private void setUpTargetsDropdown() {
        String[] items = new String[mTaskList.size()];
        for (int i = 0; i< mTaskList.size(); i++) {
            items[i]= mTaskList.get(i).getTitle();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        mTargetDropdown.setAdapter(adapter);
    }



    private void updateUiFromTrigger() {
        if (DateFormat.is24HourFormat(this)){
            mTimepicker.setIs24HourView(true);
        }

        if(mTrigger != null){
            mTitle.setText(mTrigger.getTitle());
            mEnabled.setChecked(mTrigger.isEnabled());

            // seconds if time is schedule, otherwise minutes (as in interval).
            int timeValue = mTrigger.getTime();

            Log.e("Prepare", "type: "+ mTrigger.getType());


            if(mTrigger.getType() == Trigger.TRIGGER_TYPE_SCHEDULE) {
                mCardInterval.setVisibility(View.GONE);
                mCardWeekday.setVisibility(View.VISIBLE);
                mCardTime.setVisibility(View.VISIBLE);

                mWeekdayMon.setChecked(mTrigger.isEnabledAtDay(TRIGGER_DAY_MON));
                mWeekdayTue.setChecked(mTrigger.isEnabledAtDay(TRIGGER_DAY_TUE));
                mWeekdayWed.setChecked(mTrigger.isEnabledAtDay(TRIGGER_DAY_WED));
                mWeekdayThu.setChecked(mTrigger.isEnabledAtDay(TRIGGER_DAY_THU));
                mWeekdayFri.setChecked(mTrigger.isEnabledAtDay(TRIGGER_DAY_FRI));
                mWeekdaySat.setChecked(mTrigger.isEnabledAtDay(TRIGGER_DAY_SAT));
                mWeekdaySun.setChecked(mTrigger.isEnabledAtDay(TRIGGER_DAY_SUN));

                mTimepicker.setHour(timeValue/60);
                mTimepicker.setMinute(timeValue%60);
            } else {
                mCardInterval.setVisibility(View.VISIBLE);
                mCardWeekday.setVisibility(View.GONE);
                mCardTime.setVisibility(View.GONE);

                switch (timeValue) {
                    case 15:
                        mInterval.setSelection(0);
                        break;
                    case 30:
                        mInterval.setSelection(1);
                        break;
                    case 60:
                        mInterval.setSelection(2);
                        break;
                    case 120:
                        mInterval.setSelection(3);
                        break;
                }
            }

            //Todo properly populate the fields
            for(Task task : mTaskList){
                if(task.getId() == mTrigger.getWhatToTrigger()){
                    mTargetDropdown.setSelection(mTaskList.indexOf(task));
                }
            }
        }
    }

    private boolean checkTaskExistence(){
        if(mTaskList.size()==0){
            Toasty.error(this, this.getResources().getString(R.string.trigger_save_notasks)).show();
            return false;
        }
        return true;
    }

    private void saveTrigger(){
        if(checkTaskExistence()) {
            if(mTrigger.getId() == TRIGGER_ID_DOESNTEXIST){
                mTrigger = dbHandler.createTrigger(mTrigger);
            } else {
                dbHandler.updateTrigger(mTrigger);

            }
            new TriggerService(this).queueSingleTrigger(mTrigger);
            finish();
        }

    }
}