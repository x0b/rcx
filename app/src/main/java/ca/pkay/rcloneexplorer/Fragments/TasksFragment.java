package ca.pkay.rcloneexplorer.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import ca.pkay.rcloneexplorer.Database.DatabaseHandler;
import ca.pkay.rcloneexplorer.Database.Task;
import ca.pkay.rcloneexplorer.Dialogs.TaskDialog;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.RecyclerViewAdapters.TasksRecyclerViewAdapter;
import ca.pkay.rcloneexplorer.util.FLog;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

import static ca.pkay.rcloneexplorer.ActivityHelper.tryStartActivityForResult;

public class TasksFragment extends Fragment {

    private static final String TAG = "TasksFragment";
    private static final int EXPORT_TASKS_REQUEST_CODE = 211;
    private static final int IMPORT_TASKS_REQUEST_CODE = 221;
    private TasksRecyclerViewAdapter recyclerViewAdapter;

    public TasksFragment() {
        // Required empty public constructor
    }

    public static TasksFragment newInstance() {
        return new TasksFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((FragmentActivity) getContext()).setTitle(R.string.tasks);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        final Context context = view.getContext();

        RecyclerView recyclerView =  view.findViewById(R.id.task_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setItemAnimator(new LandingAnimator());

        final DatabaseHandler dbHandler = new DatabaseHandler(context);

        recyclerViewAdapter = new TasksRecyclerViewAdapter(dbHandler.getAllTasks(), context);
        recyclerView.setAdapter(recyclerViewAdapter);


        view.findViewById(R.id.newTask).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new TaskDialog(context, recyclerViewAdapter).show();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.tasks_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_import:
                startImport();
                break;
            case R.id.action_export:
                startExport();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startImport() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        tryStartActivityForResult(this, intent, IMPORT_TASKS_REQUEST_CODE);
    }

    public void startExport() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
        intent.putExtra(Intent.EXTRA_TITLE, "tasks.json");
        tryStartActivityForResult(this, intent, EXPORT_TASKS_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @NonNull Intent resultData) {
        Context context = getContext();
        if (resultCode != Activity.RESULT_OK || null == resultData.getData() || null == context) {
            return;
        }

        switch (requestCode){
            case IMPORT_TASKS_REQUEST_CODE:
                importTasks(context, resultData.getData());
                break;
            case EXPORT_TASKS_REQUEST_CODE:
                exportTasks(context, resultData.getData());
                break;
        }
    }

    private void importTasks(@NonNull Context context, @NonNull Uri uri) {
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (null == in) {
                return;
            }
            ObjectMapper mapper = new ObjectMapper();
            ArrayList<Task> tasks = mapper.readValue(in, new TypeReference<ArrayList<Task>>(){});
            DatabaseHandler handler = new DatabaseHandler(context);
            handler.deleteAll();
            for (Task task : tasks) {
                handler.createTask(task);
            }
            recyclerViewAdapter.setList((ArrayList<Task>) handler.getAllTasks());
        } catch (IOException e) {
            FLog.e(TAG, "importTasks: Couldn't import tasks", e);
        }
    }

    private void exportTasks (@NonNull Context context, @NonNull Uri uri) {
        try(OutputStream out = context.getContentResolver().openOutputStream(uri)) {
            if (null == out) {
                return;
            }
            ObjectMapper mapper = new ObjectMapper();
            DatabaseHandler handler = new DatabaseHandler(context);
            mapper.writeValue(out, handler.getAllTasks());
        } catch (IOException e) {
            FLog.e(TAG, "Error exporting tasks ", e);
        }
    }
}
