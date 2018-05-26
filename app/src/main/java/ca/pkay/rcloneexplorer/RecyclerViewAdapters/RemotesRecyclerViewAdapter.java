package ca.pkay.rcloneexplorer.RecyclerViewAdapters;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.pkay.rcloneexplorer.Fragments.RemotesFragment;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.R;

public class RemotesRecyclerViewAdapter extends RecyclerView.Adapter<RemotesRecyclerViewAdapter.ViewHolder>{

    private List<RemoteItem> remotes;
    private final RemotesFragment.OnRemoteClickListener clickListener;
    private OnRemoteOptionsClick optionsListener;
    private View view;

    public interface OnRemoteOptionsClick {
        void onRemoteOptionsClicked(View view, RemoteItem remoteItem);
    }

    public RemotesRecyclerViewAdapter(List<RemoteItem> remotes, RemotesFragment.OnRemoteClickListener clickListener, OnRemoteOptionsClick optionsListener) {
        this.remotes = remotes;
        Collections.sort(remotes);
        this.optionsListener = optionsListener;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_remotes_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        String remoteName = remotes.get(position).getName();
        String remoteType = remotes.get(position).getType();
        holder.remoteName = remoteName;
        holder.tvName.setText(remoteName);

        switch (remoteType) {
            case "crypt":
                holder.ivIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.ic_lock_black));
                break;
            case "amazon cloud drive":
                holder.ivIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.ic_amazon));
                break;
            case "drive":
                holder.ivIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.ic_google_drive));
                break;
            case "dropbox":
                holder.ivIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.ic_dropbox));
                break;
            case "google cloud storage":
                holder.ivIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.ic_google));
                break;
            case "onedrive":
                holder.ivIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.ic_onedrive));
                break;
            case "s3":
                holder.ivIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.ic_amazon));
                break;
            case "box":
                holder.ivIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.ic_box));
                break;
            case "sftp":
                holder.ivIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.ic_terminal));
                break;
            default:
                holder.ivIcon.setImageDrawable(view.getResources().getDrawable(R.drawable.ic_cloud));
                    break;
        }

        holder.options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionsListener.onRemoteOptionsClicked(holder.options, remotes.get(holder.getAdapterPosition()));
            }
        });

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != clickListener) {
                    clickListener.onRemoteClick(remotes.get(holder.getAdapterPosition()));
                }
            }
        });
    }

    public void newData(List<RemoteItem> data) {
        remotes = new ArrayList<>(data);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (remotes == null) {
            return 0;
        }
        return remotes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View view;
        public final ImageView ivIcon;
        public final TextView tvName;
        public final ImageButton options;
        public String remoteName;

        ViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            this.ivIcon = view.findViewById(R.id.remoteIcon);
            this.tvName = view.findViewById(R.id.remoteName);
            this.options = view.findViewById(R.id.remote_options);
        }
    }

}
