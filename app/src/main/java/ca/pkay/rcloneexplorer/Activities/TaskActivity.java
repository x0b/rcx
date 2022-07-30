package ca.pkay.rcloneexplorer.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import ca.pkay.rcloneexplorer.Database.DatabaseHandler;
import ca.pkay.rcloneexplorer.FilePicker;
import ca.pkay.rcloneexplorer.Fragments.FolderSelectorCallback;
import ca.pkay.rcloneexplorer.Fragments.RemoteFolderPickerFragment;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.Items.SyncDirectionObject;
import ca.pkay.rcloneexplorer.Items.Task;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import ca.pkay.rcloneexplorer.util.ActivityHelper;
import es.dmoral.toasty.Toasty;

public class TaskActivity extends AppCompatActivity implements FolderSelectorCallback {


    public static final String ID_EXTRA = "TASK_EDIT_ID";
    private final int REQUEST_CODE_FP_LOCAL = 500;
    private final int REQUEST_CODE_FP_REMOTE = 444;
    private Rclone rcloneInstance;
    private Task existingTask;
    private DatabaseHandler dbHandler;
    private String[] items;


    private TextView syncDescription;
    private EditText remotePath;
    private EditText localPath;
    private Spinner remoteDropdown;
    private Spinner syncDirection;

    private FloatingActionButton fab;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_FP_LOCAL:
                if(data != null) {
                    localPath.setText(data.getStringExtra(FilePicker.FILE_PICKER_RESULT));
                }
                localPath.clearFocus();
                break;
            case REQUEST_CODE_FP_REMOTE:
                if (data != null) {
                    String path = data.getData().toString();
                    try {
                        path = URLDecoder.decode(path, "UTF-8");
                    } catch (UnsupportedEncodingException e) {}

                    // Todo: check if this provider is still valid; search other occurences
                    Log.e("TaskActivity provider", "recieved path: "+path);
                    String provider = "content://io.github.x0b.rcx.vcp/tree/rclone/remotes/";
                   if(path.startsWith(provider)){
                       String[] parts = path.substring(provider.length()).split(":");
                       remotePath.setText(parts[1]);
                       int i=0;
                       for(String remote: items) {
                           if(remote.equals(parts[0])){
                               remoteDropdown.setSelection(i);
                           }
                           i++;
                       }
                   }else{
                       Toasty.error(this, "This Remote is not a RCX-Remote.").show();
                   }
                }
                break;
        }
        fab.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityHelper.applyTheme(this);
        setContentView(R.layout.activity_task);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        remotePath = findViewById(R.id.task_remote_path_textfield);
        localPath = findViewById(R.id.task_local_path_textfield);
        remoteDropdown = findViewById(R.id.task_remote_spinner);
        syncDirection = findViewById(R.id.task_direction_spinner);
        syncDescription = findViewById(R.id.descriptionSyncDirection);
        fab = findViewById(R.id.fab);

        rcloneInstance = new Rclone(this);
        dbHandler = new DatabaseHandler(this);

        Bundle extras = getIntent().getExtras();
        Long task_id;

        if (extras != null) {
            task_id = extras.getLong(ID_EXTRA);
            if(task_id!=0){
                existingTask = dbHandler.getTask(task_id);
                if(existingTask == null){
                    Toasty.error(this, this.getResources().getString(R.string.taskactivity_task_not_found)).show();
                    finish();
                }
            }
        }

        final Context c = this.getApplicationContext();
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            //Todo fix error when no remotes are available
            if(existingTask==null){
                saveTask();
            }else{
                persistTaskChanges();
            }
        });

        items = new String[rcloneInstance.getRemotes().size()];

        for (int i = 0; i< rcloneInstance.getRemotes().size(); i++) {
            items[i]= rcloneInstance.getRemotes().get(i).getName();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        remoteDropdown.setAdapter(adapter);

        String[] options = SyncDirectionObject.getOptionsArray(this);
        ArrayAdapter<String> directionAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, options);
        syncDirection.setAdapter(directionAdapter);
        populateFields(items);

        localPath.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus){
                Intent intent = new Intent(c, FilePicker.class);
                intent.putExtra(FilePicker.FILE_PICKER_PICK_DESTINATION_TYPE, true);
                startActivityForResult(intent, REQUEST_CODE_FP_LOCAL);
            }
        });

        // Todo: This will break if the remote changed, but the path did not.
        //       Catch this issue by forcing the path to be emtpy
        remotePath.setOnFocusChangeListener((v, hasFocus) -> {
            startRemotePicker(rcloneInstance.getRemoteItemFromName(remoteDropdown.getSelectedItem().toString()), "/");
            remotePath.clearFocus();
        });

        remoteDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                remotePath.setText("");
            }
            @Override public void onNothingSelected(AdapterView<?> parentView) {}
        });

        syncDirection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String text = getString(R.string.description_sync_direction);

                switch (position) {
                    case SyncDirectionObject.SYNC_LOCAL_TO_REMOTE:
                        text = getString(R.string.description_sync_direction_sync_tolocal);
                        break;
                    case SyncDirectionObject.SYNC_REMOTE_TO_LOCAL:
                        text = getString(R.string.description_sync_direction_sync_toremote);
                        break;
                    case SyncDirectionObject.COPY_LOCAL_TO_REMOTE:
                        text = getString(R.string.description_sync_direction_copy_toremote);
                        break;
                    case SyncDirectionObject.COPY_REMOTE_TO_LOCAL:
                        text = getString(R.string.description_sync_direction_copy_tolocal);
                        break;
                    case SyncDirectionObject.SYNC_BIDIRECTIONAL:
                        text = getString(R.string.description_sync_direction_sync_bidirectional);
                        break;
                }
                syncDescription.setText(text);
            }
            @Override public void onNothingSelected(AdapterView<?> adapterView) {}
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void populateFields(String[] remotes) {
        if(existingTask!=null){
            ((TextView)findViewById(R.id.task_title_textfield)).setText(existingTask.getTitle());

            int i=0;
            for(String remote: remotes) {
                if(remote.equals(existingTask.getRemoteId())){
                    remoteDropdown.setSelection(i);
                }
                i++;
            }

            remotePath.setText(existingTask.getRemotePath());
            localPath.setText(existingTask.getLocalPath());
            syncDirection.setSelection(existingTask.getDirection()-1);
        }
    }

    private void persistTaskChanges(){
        Task updatedTask = getTaskValues(existingTask.getId());
        if(updatedTask != null) {
            dbHandler.updateTask(updatedTask);
            finish();
        }
    }

    private void saveTask(){
        Task newTask = getTaskValues(0);
        if(newTask != null) {
            dbHandler.createTask(newTask);
            finish();
        }
    }

    private Task getTaskValues(long id ){
        Task taskToPopulate = new Task(id);
        taskToPopulate.setTitle(((EditText)findViewById(R.id.task_title_textfield)).getText().toString());

        String remotename=remoteDropdown.getSelectedItem().toString();
        taskToPopulate.setRemoteId(remotename);

        int direction = syncDirection.getSelectedItemPosition()+1;
        for (RemoteItem ri: rcloneInstance.getRemotes()) {
            if(ri.getName().equals(taskToPopulate.getRemoteId())){
                taskToPopulate.setRemoteType(ri.getType());
            }
        }

        taskToPopulate.setRemotePath(remotePath.getText().toString());
        taskToPopulate.setLocalPath(localPath.getText().toString());
        taskToPopulate.setDirection(direction);


        // Verify if data is completed
        if(localPath.getText().toString().equals("")) {
            Toasty.error(this.getApplicationContext(),
                    getString(R.string.task_data_validation_error_no_local_path),
                    Toast.LENGTH_SHORT,
                    true
            ).show();
            return null;
        }

        if(remotePath.getText().toString().equals("")) {
            Toasty.error(this.getApplicationContext(),
                    getString(R.string.task_data_validation_error_no_remote_path),
                    Toast.LENGTH_SHORT,
                    true
            ).show();
            return null;
        }

        return taskToPopulate;
    }

    private void startRemotePicker(RemoteItem remote, String initialPath) {
        Fragment fragment = RemoteFolderPickerFragment.newInstance(remote, this, initialPath);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.create_task_layout, fragment, "FILE_EXPLORER_FRAGMENT_TAG");
        transaction.addToBackStack("FILE_EXPLORER_FRAGMENT_TAG");
        transaction.commit();
        fab.setVisibility(View.GONE);
    }

    @Override
    public void selectFolder(String path) {
        remotePath.setText(path);
        fab.setVisibility(View.VISIBLE);
    }
}