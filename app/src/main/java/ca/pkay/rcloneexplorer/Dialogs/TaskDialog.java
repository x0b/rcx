package ca.pkay.rcloneexplorer.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import ca.pkay.rcloneexplorer.Database.DatabaseHandler;
import ca.pkay.rcloneexplorer.Database.Task;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import ca.pkay.rcloneexplorer.RecyclerViewAdapters.TasksRecyclerViewAdapter;

public class TaskDialog extends Dialog {

    private Button task_back;
    private Button task_next;
    private Button task_save;
    private Button task_cancel;
    private TasksRecyclerViewAdapter recyclerViewAdapter;
    private Rclone r = new Rclone(getContext());

    private int state = 0;

    public TaskDialog(@NonNull Context context, TasksRecyclerViewAdapter recyclerViewAdapter) {
        super(context);
        this.recyclerViewAdapter=recyclerViewAdapter;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.task_dialog);


        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        setCanceledOnTouchOutside(true);

        task_back = findViewById(R.id.task_back);
        task_next = findViewById(R.id.task_next);
        task_save = findViewById(R.id.task_save);
        task_cancel = findViewById(R.id.task_cancel);

        task_back.setVisibility(View.INVISIBLE);



        Spinner remoteDropdown = findViewById(R.id.task_remote_spinner);

        String[] items = new String[r.getRemotes().size()];

        for (int i = 0; i< r.getRemotes().size();i++) {
            items[i]=r.getRemotes().get(i).getName();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, items);
        remoteDropdown.setAdapter(adapter);


        Spinner directionDropdown = findViewById(R.id.task_direction_spinner);
        String[] options = new String[] {getContext().getResources().getString(R.string.sync_direction_local_remote),getContext().getString(R.string.sync_direction_remote_local)};
        ArrayAdapter<String> directionAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, options);
        directionDropdown.setAdapter(directionAdapter);

        hideAll();
        decideState();


        task_next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.e("APP!", "TaskDialog: next!");
                hideAll();
                state++;
                decideState();
            }
        });
        task_back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.e("APP!", "TaskDialog: back!");
                hideAll();
                state--;
                decideState();
            }
        });
        task_cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.e("APP!", "TaskDialog: cancel!");
                cancel();
            }
        });
        task_save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.e("APP!", "TaskDialog: Save!");
                save();
            }
        });
    }

    private void save(){

        DatabaseHandler dbHandler = new DatabaseHandler(getContext());
        Task t = new Task(0L);

        t.setTitle(((EditText)findViewById(R.id.task_title_textfield)).getText().toString());

        String remotename=((Spinner)findViewById(R.id.task_remote_spinner)).getSelectedItem().toString();
        t.setRemote_id(remotename);

        int direction = ((Spinner)findViewById(R.id.task_direction_spinner)).getSelectedItemPosition()+1;



        for (RemoteItem ri: r.getRemotes()) {
            if(ri.getName().equals(t.getRemote_id())){
                t.setRemote_type(ri.getType());
            }
        }

        t.setRemote_path(((EditText)findViewById(R.id.task_remote_path_textfield)).getText().toString());
        t.setLocal_path(((EditText)findViewById(R.id.task_local_path_textfield)).getText().toString());
        t.setDirection(direction);

        Task newTask = dbHandler.createEntry(t);
        recyclerViewAdapter.addTask(newTask);

        Log.e("app!", "Task Dialog: "+newTask.toString());

        cancel();
    }


    private void hideAll(){

        findViewById(R.id.task_name_layout).setVisibility(View.GONE);
        findViewById(R.id.task_remote_layout).setVisibility(View.GONE);
        findViewById(R.id.task_remote_path_layout).setVisibility(View.GONE);
        findViewById(R.id.task_local_path_layout).setVisibility(View.GONE);
        findViewById(R.id.task_direction_layout).setVisibility(View.GONE);

    }

    private void decideState(){

        if(state==0){
            task_back.setVisibility(View.INVISIBLE);
            task_save.setVisibility(View.GONE);
            task_next.setVisibility(View.VISIBLE);
        }else if (state == 4){
            task_save.setVisibility(View.VISIBLE);
            task_back.setVisibility(View.VISIBLE);
            task_next.setVisibility(View.GONE);
        }else {
            task_back.setVisibility(View.VISIBLE);
            task_next.setVisibility(View.VISIBLE);
            task_save.setVisibility(View.GONE);
        }

        switch(state) {
            case 0:
                ((TextView)findViewById(R.id.task_dialog_title)).setText("Name");
                findViewById(R.id.task_name_layout).setVisibility(View.VISIBLE);
                break;
            case 1:
                ((TextView)findViewById(R.id.task_dialog_title)).setText("remote");
                findViewById(R.id.task_remote_layout).setVisibility(View.VISIBLE);
                break;
            case 2:
                ((TextView)findViewById(R.id.task_dialog_title)).setText("remote path");
                findViewById(R.id.task_remote_path_layout).setVisibility(View.VISIBLE);
                break;
            case 3:
                ((TextView)findViewById(R.id.task_dialog_title)).setText("local path");
                findViewById(R.id.task_local_path_layout).setVisibility(View.VISIBLE);
                break;
            case 4:
                ((TextView)findViewById(R.id.task_dialog_title)).setText("direction");
                findViewById(R.id.task_direction_layout).setVisibility(View.VISIBLE);
                break;
        }
    }

}
