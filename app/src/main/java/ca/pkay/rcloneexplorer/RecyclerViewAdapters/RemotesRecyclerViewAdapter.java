package ca.pkay.rcloneexplorer.RecyclerViewAdapters;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import ca.pkay.rcloneexplorer.Fragments.RemotesFragment;
import ca.pkay.rcloneexplorer.R;

public class RemotesRecyclerViewAdapter extends RecyclerView.Adapter<RemotesRecyclerViewAdapter.ViewHolder>{

    private final List<String> remotes;
    private final HashMap<String, String> remoteTypes;
    private final RemotesFragment.OnRemoteClickListener clickListener;

    public RemotesRecyclerViewAdapter(ArrayList<String> remotes, HashMap<String, String> remoteTypes, RemotesFragment.OnRemoteClickListener clickListener) {
        this.remotes = remotes;
        this.remoteTypes = remoteTypes;
        this.clickListener = clickListener;

        Collections.sort(this.remotes);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_remotes_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        String remoteName = remotes.get(position);
        String remoteType = remoteTypes.get(remoteName);
        holder.remoteName = remoteName;
        holder.tvName.setText(remoteName);

        switch (remoteType) {
            case "crypt":
                holder.ivIcon.setImageResource(R.drawable.ic_crypt);
                break;
            case "amazon cloud drive":
                holder.ivIcon.setImageResource(R.drawable.ic_amazon_cloud_drive);
                break;
            case "b2":
                holder.ivIcon.setImageResource(R.drawable.ic_b2);
                break;
            case "drive":
                holder.ivIcon.setImageResource(R.drawable.ic_drive);
                break;
            case "dropbox":
                holder.ivIcon.setImageResource(R.drawable.ic_dropbox);
                break;
            case "google cloud storage":
                holder.ivIcon.setImageResource(R.drawable.ic_google_cloud_storage);
                break;
            case "swift":
                holder.ivIcon.setImageResource(R.drawable.ic_swift);
                break;
            case "hubic":
                holder.ivIcon.setImageResource(R.drawable.ic_hubic);
                break;
            case "onedrive":
                holder.ivIcon.setImageResource(R.drawable.ic_onedrive);
                break;
            case "s3":
                holder.ivIcon.setImageResource(R.drawable.ic_s3);
                break;
            case "yandex":
                holder.ivIcon.setImageResource(R.drawable.ic_yandex);
                break;
            default:
                    holder.ivIcon.setImageResource(R.drawable.ic_unknown);
                    break;
        }

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != clickListener) {
                    clickListener.onRemoteClick(holder.remoteName);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return remotes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View view;
        public final ImageView ivIcon;
        public final TextView tvName;
        public String remoteName;

        public ViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            this.ivIcon = view.findViewById(R.id.remoteIcon);
            this.tvName = view.findViewById(R.id.remoteName);
        }
    }

}
