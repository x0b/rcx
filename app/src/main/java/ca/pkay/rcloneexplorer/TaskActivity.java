package ca.pkay.rcloneexplorer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import ca.pkay.rcloneexplorer.Database.DatabaseHandler;
import ca.pkay.rcloneexplorer.Items.Task;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.Items.SyncDirectionObject;

public class TaskActivity extends AppCompatActivity {


    public static final String ID_EXTRA = "TASK_EDIT_ID";
    private Rclone rcloneInstance;
    private Task existingTask;
    private DatabaseHandler dbHandler;
    final private int REQUEST_CODE_FP_LOCAL = 500;
    private Uri sdCardUri;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_FP_LOCAL:
                EditText tv_local = findViewById(R.id.task_local_path_textfield);
                tv_local.setText(data.getStringExtra(FilePicker.FILE_PICKER_RESULT));
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHandler = new DatabaseHandler(this);

        Bundle extras = getIntent().getExtras();
        Long task_id;

        if (extras != null) {
            task_id = extras.getLong(ID_EXTRA);
            if(task_id!=0){
                existingTask = dbHandler.getTask(task_id);
            }
        }

        final Context c = this.getApplicationContext();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Todo fix error when no remotes are available
                if(existingTask==null){
                    saveTask();
                }else{
                    persistTaskChanges();
                }
            }
        });

        EditText tv_local = findViewById(R.id.task_local_path_textfield);
        tv_local.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    Intent intent = new Intent(c, FilePicker.class);
                    intent.putExtra(FilePicker.FILE_PICKER_PICK_DESTINATION_TYPE, true);
                    startActivityForResult(intent, REQUEST_CODE_FP_LOCAL);
                }
            }
        });



        rcloneInstance = new Rclone(this);

        Spinner remoteDropdown = findViewById(R.id.task_remote_spinner);

        String[] items = new String[rcloneInstance.getRemotes().size()];

        for (int i = 0; i< rcloneInstance.getRemotes().size(); i++) {
            items[i]= rcloneInstance.getRemotes().get(i).getName();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        remoteDropdown.setAdapter(adapter);


        Spinner directionDropdown = findViewById(R.id.task_direction_spinner);
        String[] options = SyncDirectionObject.getOptionsArray(this);
        ArrayAdapter<String> directionAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, options);
        directionDropdown.setAdapter(directionAdapter);
        populateFields(items);
    }

    private void populateFields(String[] remotes) {
        Log.e("app!", "Populate Task");
        if(existingTask!=null){
            Log.e("app!", "Populate Task"+existingTask.getTitle());
            ((TextView)findViewById(R.id.task_title_textfield)).setText(existingTask.getTitle());
            Spinner s = findViewById(R.id.task_remote_spinner);

            int i=0;
            for(String remote: remotes) {
                if(remote.equals(existingTask.getRemoteId())){
                    s.setSelection(i);
                }
                i++;
            }

            ((TextView)findViewById(R.id.task_remote_path_textfield)).setText(existingTask.getRemotePath());
            ((TextView)findViewById(R.id.task_local_path_textfield)).setText(existingTask.getLocalPath());
            ((Spinner)findViewById(R.id.task_direction_spinner)).setSelection(existingTask.getDirection()-1);
        }
    }

    private void persistTaskChanges(){
        dbHandler.updateTask(getTaskValues(existingTask.getId()));
        Log.e("app!", "Update Task: ");
        finish();
    }

    private void saveTask(){
        Task newTask = dbHandler.createTask(getTaskValues(0L));
        Log.e("app!", "Task Dialog: "+newTask.toString());
        finish();
    }

    private Task getTaskValues(Long id ){
        Task taskToPopulate = new Task(id);
        taskToPopulate.setTitle(((EditText)findViewById(R.id.task_title_textfield)).getText().toString());

        String remotename=((Spinner)findViewById(R.id.task_remote_spinner)).getSelectedItem().toString();
        taskToPopulate.setRemoteId(remotename);

        int direction = ((Spinner)findViewById(R.id.task_direction_spinner)).getSelectedItemPosition()+1;



        for (RemoteItem ri: rcloneInstance.getRemotes()) {
            if(ri.getName().equals(taskToPopulate.getRemoteId())){
                taskToPopulate.setRemoteType(ri.getType());
            }
        }

        taskToPopulate.setRemotePath(((EditText)findViewById(R.id.task_remote_path_textfield)).getText().toString());
        taskToPopulate.setLocalPath(((EditText)findViewById(R.id.task_local_path_textfield)).getText().toString());
        taskToPopulate.setDirection(direction);
        return taskToPopulate;
    }

}