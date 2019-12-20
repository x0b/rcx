package ca.pkay.rcloneexplorer.RecyclerViewAdapters;


import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import ca.pkay.rcloneexplorer.Database.DatabaseHandler;
import ca.pkay.rcloneexplorer.Database.Task;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import ca.pkay.rcloneexplorer.Services.SyncService;

public class TasksRecyclerViewAdapter extends RecyclerView.Adapter<TasksRecyclerViewAdapter.ViewHolder>{

    private List<Task> tasks;
    private View view;
    private Context context;


    public TasksRecyclerViewAdapter(List<Task> tasks, Context context) {
        this.tasks = tasks;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_tasks_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Task t = tasks.get(position);
        String remoteName = t.getTitle();

        holder.taskName.setText(remoteName);

        RemoteItem ri = new RemoteItem(t.getRemote_id(), String.valueOf(t.getRemote_type()));

        holder.taskIcon.setImageDrawable(view.getResources().getDrawable(ri.getRemoteIcon()));

        if(t.getDirection()== Rclone.SYNC_DIRECTION_LOCAL_TO_REMOTE){
            holder.fromID.setVisibility(View.GONE);
            holder.fromPath.setText(String.format("%s:", t.getLocal_path()));
            holder.toID.setText(t.getRemote_id());
            holder.toPath.setText(t.getRemote_path());
        }

        if(t.getDirection()== Rclone.SYNC_DIRECTION_REMOTE_TO_LOCAL){
            holder.fromID.setText(String.format("%s:", t.getRemote_id()));
            holder.fromPath.setText(t.getRemote_path());
            holder.toID.setVisibility(View.GONE);
            holder.toPath.setText(t.getLocal_path());
        }


        holder.button_layout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String path = t.getLocal_path();
                RemoteItem ri = new RemoteItem(t.getRemote_id(), t.getRemote_type(), "");
                Intent intent = new Intent(context, SyncService.class);
                intent.putExtra(SyncService.REMOTE_ARG, ri);
                intent.putExtra(SyncService.LOCAL_PATH_ARG, path);
                intent.putExtra(SyncService.SYNC_DIRECTION_ARG, t.getDirection());
                intent.putExtra(SyncService.REMOTE_PATH_ARG, t.getRemote_path());
                context.startService(intent);
            }
        });

        holder.task_delete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new DatabaseHandler(context).deleteEntry(t.getId());
                notifyDataSetChanged();
                removeItem(t);
            }
        });


    }

    public void addTask(Task data) {
        tasks.add(data);
        notifyDataSetChanged();
    }

    public void removeItem(Task task) {
        int index = tasks.indexOf(task);
        if (index >= 0) {
            tasks.remove(index);
            notifyItemRemoved(index);
        }
    }

    @Override
    public int getItemCount() {
        if (tasks == null) {
            return 0;
        }
        return tasks.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        final View view;
        final ImageView taskIcon;
        final ImageView task_delete;
        final TextView taskName;
        final TextView toID;
        final TextView fromID;
        final TextView toPath;
        final TextView fromPath;
        final LinearLayout button_layout;

        ViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            this.taskIcon = view.findViewById(R.id.taskIcon);
            this.task_delete = view.findViewById(R.id.task_delete);
            this.taskName = view.findViewById(R.id.taskName);
            this.toID = view.findViewById(R.id.toID);
            this.fromID = view.findViewById(R.id.fromID);
            this.toPath = view.findViewById(R.id.toPath);
            this.fromPath = view.findViewById(R.id.fromPath);
            this.button_layout = view.findViewById(R.id.button_layout);
        }
    }

}
