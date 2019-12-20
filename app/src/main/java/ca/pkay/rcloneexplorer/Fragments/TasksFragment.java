package ca.pkay.rcloneexplorer.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.pkay.rcloneexplorer.Database.DatabaseHandler;
import ca.pkay.rcloneexplorer.Dialogs.TaskDialog;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.RecyclerViewAdapters.TasksRecyclerViewAdapter;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

public class TasksFragment extends Fragment {

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
        ((FragmentActivity) getContext()).setTitle("Tasks");
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
}
