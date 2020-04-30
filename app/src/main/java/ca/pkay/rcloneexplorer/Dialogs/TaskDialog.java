package ca.pkay.rcloneexplorer.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ca.pkay.rcloneexplorer.Database.DatabaseHandler;
import ca.pkay.rcloneexplorer.Database.Task;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.Items.SyncDirectionObject;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import ca.pkay.rcloneexplorer.RecyclerViewAdapters.TasksRecyclerViewAdapter;
import ca.pkay.rcloneexplorer.util.FLog;

public class TaskDialog extends Dialog {

    private static final String TAG = "TaskDialog";
    private Button taskBackBtn;
    private Button taskNextBtn;
    private Button taskSaveBtn;
    private Button taskCancelBtn;
    private TasksRecyclerViewAdapter recyclerViewAdapter;
    private Rclone rclone = new Rclone(getContext());

    private Task existingTask;

    private int uiButtonState = 0;
    private String[] remoteIds;

    public TaskDialog(@NonNull Context context, @NonNull TasksRecyclerViewAdapter tasksRecyclerViewAdapter) {
        super(context);
        this.recyclerViewAdapter = tasksRecyclerViewAdapter;
    }

    public TaskDialog(@NonNull Context context, @NonNull TasksRecyclerViewAdapter tasksRecyclerViewAdapter, @Nullable Task task) {
        super(context);
        this.recyclerViewAdapter = tasksRecyclerViewAdapter;
        this.existingTask = task;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.task_dialog);

        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        setCanceledOnTouchOutside(true);

        taskBackBtn = findViewById(R.id.task_back);
        taskNextBtn = findViewById(R.id.task_next);
        taskSaveBtn = findViewById(R.id.task_save);
        taskCancelBtn = findViewById(R.id.task_cancel);
        taskBackBtn.setVisibility(View.INVISIBLE);
        Spinner remoteDropdown = findViewById(R.id.task_remote_spinner);

        List<RemoteItem> remotes = rclone.getRemotes();
        RemoteItem.prepareDisplay(getContext(), remotes);
        int existingPosition = -1;
        String[] remoteNames = new String[remotes.size()];
        remoteIds = new String[remotes.size()];
        for (int i = 0; i < remotes.size(); i++) {
            remoteNames[i] = remotes.get(i).getDisplayName();
            remoteIds[i] = remotes.get(i).getName();
            if(existingTask != null && existingTask.getRemoteId().equals(remotes.get(i).getName())) {
                existingPosition = i;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, remoteNames);
        remoteDropdown.setAdapter(adapter);

        Spinner directionDropdown = findViewById(R.id.task_direction_spinner);
        String[] options = SyncDirectionObject.getOptionsArray(getContext());
        ArrayAdapter<String> directionAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, options);
        directionDropdown.setAdapter(directionAdapter);

        populateFields(existingPosition);
        hideAllSettingsInUI();
        decideUIButtonState();

        taskNextBtn.setOnClickListener(v -> {
            FLog.v(TAG, "TaskDialog: next!");
            hideAllSettingsInUI();
            uiButtonState++;
            decideUIButtonState();
        });

        taskBackBtn.setOnClickListener(v -> {
            FLog.v(TAG, "TaskDialog: back!");
            hideAllSettingsInUI();
            uiButtonState--;
            decideUIButtonState();
        });

        taskCancelBtn.setOnClickListener(v -> {
            FLog.v(TAG, "TaskDialog: cancel!");
            cancel();
        });

        taskSaveBtn.setOnClickListener(v -> {
            FLog.v(TAG, "TaskDialog: Save!");
            if (existingTask == null) {
                saveTask();
            } else {
                persistTaskChanges();
            }
        });
    }

    private void populateFields(int spinnerPosition) {
        FLog.v(TAG, "Populate Task");
        if (null != existingTask) {
            FLog.v(TAG, "Populate Task %s", existingTask.getTitle());
            ((TextView) findViewById(R.id.task_title_textfield)).setText(existingTask.getTitle());
            Spinner spinner = findViewById(R.id.task_remote_spinner);
            spinner.setSelection(spinnerPosition);

            ((TextView) findViewById(R.id.task_remote_path_textfield)).setText(existingTask.getRemotePath());
            ((TextView) findViewById(R.id.task_local_path_textfield)).setText(existingTask.getLocalPath());
            ((Spinner) findViewById(R.id.task_direction_spinner)).setSelection(existingTask.getDirection() - 1);
        }
    }

    // TODO: method with no parameter but with side effects
    private void persistTaskChanges() {
        DatabaseHandler dbHandler = new DatabaseHandler(getContext());
        dbHandler.updateTask(getTaskValues(existingTask.getId()));
        recyclerViewAdapter.setList((ArrayList<Task>) dbHandler.getAllTasks());
        FLog.v(TAG, "Update Task: ");
        cancel();
    }

    // TODO: method with no parameter but with side effects
    private void saveTask() {
        DatabaseHandler dbHandler = new DatabaseHandler(getContext());
        Task newTask = dbHandler.createTask(getTaskValues(0L));
        recyclerViewAdapter.addTask(newTask);

        FLog.v(TAG, "Task Dialog: %s", newTask.toString());
        cancel();
    }

    private Task getTaskValues(Long id) {
        Task taskToPopulate = new Task(id);
        taskToPopulate.setTitle(((EditText) findViewById(R.id.task_title_textfield)).getText().toString());
        String remoteId = remoteIds[((Spinner) findViewById(R.id.task_remote_spinner)).getSelectedItemPosition()];
        taskToPopulate.setRemoteId(remoteId);
        int direction = ((Spinner) findViewById(R.id.task_direction_spinner)).getSelectedItemPosition() + 1;

        for (RemoteItem remoteItem : rclone.getRemotes()) {
            if (remoteItem.getName().equals(taskToPopulate.getRemoteId())) {
                taskToPopulate.setRemoteType(remoteItem.getTypeReadable());
            }
        }

        taskToPopulate.setRemotePath(((EditText) findViewById(R.id.task_remote_path_textfield)).getText().toString());
        taskToPopulate.setLocalPath(((EditText) findViewById(R.id.task_local_path_textfield)).getText().toString());
        taskToPopulate.setDirection(direction);
        return taskToPopulate;
    }


    private void hideAllSettingsInUI() {
        findViewById(R.id.task_name_layout).setVisibility(View.GONE);
        findViewById(R.id.task_remote_layout).setVisibility(View.GONE);
        findViewById(R.id.task_remote_path_layout).setVisibility(View.GONE);
        findViewById(R.id.task_local_path_layout).setVisibility(View.GONE);
        findViewById(R.id.task_direction_layout).setVisibility(View.GONE);
    }

    private void decideUIButtonState() {
        if (uiButtonState == 0) {
            taskBackBtn.setVisibility(View.INVISIBLE);
            taskSaveBtn.setVisibility(View.GONE);
            taskNextBtn.setVisibility(View.VISIBLE);
        } else if (uiButtonState == 4) {
            taskSaveBtn.setVisibility(View.VISIBLE);
            taskBackBtn.setVisibility(View.VISIBLE);
            taskNextBtn.setVisibility(View.GONE);
        } else {
            taskBackBtn.setVisibility(View.VISIBLE);
            taskNextBtn.setVisibility(View.VISIBLE);
            taskSaveBtn.setVisibility(View.GONE);
        }

        switch (uiButtonState) {
            case 0:
                ((TextView) findViewById(R.id.task_dialog_title)).setText(getContext().getString(R.string.task_dialog_title_name));
                findViewById(R.id.task_name_layout).setVisibility(View.VISIBLE);
                break;
            case 1:
                ((TextView) findViewById(R.id.task_dialog_title)).setText(getContext().getString(R.string.task_dialog_title_remote));
                findViewById(R.id.task_remote_layout).setVisibility(View.VISIBLE);
                break;
            case 2:
                ((TextView) findViewById(R.id.task_dialog_title)).setText(getContext().getString(R.string.task_dialog_title_remote_path));
                findViewById(R.id.task_remote_path_layout).setVisibility(View.VISIBLE);
                break;
            case 3:
                ((TextView) findViewById(R.id.task_dialog_title)).setText(getContext().getString(R.string.task_dialog_title_local_path));
                findViewById(R.id.task_local_path_layout).setVisibility(View.VISIBLE);
                break;
            case 4:
                ((TextView) findViewById(R.id.task_dialog_title)).setText(getContext().getString(R.string.task_dialog_title_sync_direction));
                findViewById(R.id.task_direction_layout).setVisibility(View.VISIBLE);
                break;
        }
    }

}
