package ca.pkay.rcloneexplorer.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;

import ca.pkay.rcloneexplorer.Database.DatabaseHandler;
import ca.pkay.rcloneexplorer.Database.Task;
import ca.pkay.rcloneexplorer.Database.xml.Exporter;
import ca.pkay.rcloneexplorer.Database.xml.Importer;
import ca.pkay.rcloneexplorer.Dialogs.TaskDialog;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.RecyclerViewAdapters.TasksRecyclerViewAdapter;
import es.dmoral.toasty.Toasty;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

public class TasksFragment extends Fragment {

    private TasksRecyclerViewAdapter recyclerViewAdapter;
    private Activity filePickerActivity;


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
                if(Importer.getFilePermission(getActivity())){
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("text/xml");
                    startActivityForResult(intent, Importer.READ_REQUEST_CODE);
                }
                break;
            case R.id.action_export:
                Exporter.export(getActivity());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        Activity activity = getActivity();
        Context context = getContext();

        if(activity==null){
            Toasty.error(context, context.getResources().getString(R.string.importer_unknown_error), Toast.LENGTH_SHORT, true).show();
            return;
        }

        switch (requestCode){
            case Importer.PERM_REQUEST_CODE:
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/xml");
                startActivityForResult(intent, Importer.READ_REQUEST_CODE);
                break;
            case Importer.READ_REQUEST_CODE:
                try {
                    String importedData="";
                    if (resultCode == Activity.RESULT_OK) {

                        Uri uri = null;
                        if (resultData != null) {
                            uri = resultData.getData();

                            if(uri==null){
                                Toasty.error(context, context.getResources().getString(R.string.importer_no_file_selected), Toast.LENGTH_SHORT, true).show();
                                return;
                            }

                            InputStream in =  activity.getContentResolver().openInputStream(uri);
                            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                            StringBuilder out = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                out.append(line);
                            }

                            reader.close();
                            if (in != null) {
                                in.close();
                            }
                            importedData = out.toString();
                        }
                    }

                    ArrayList<Task> importedList = Importer.createTasklist(importedData);
                    DatabaseHandler dbHandler = new DatabaseHandler(context);
                    for (Task t : dbHandler.getAllTasks()){
                        dbHandler.deleteEntry(t.getId());
                    }

                    for(Task t: importedList){
                        dbHandler.createEntry(t);
                    }

                    recyclerViewAdapter.setList((ArrayList<Task>) dbHandler.getAllTasks());

                } catch (IOException | SAXException | ParserConfigurationException e) {
                    e.printStackTrace();
                }

                break;
        }
    }
}
