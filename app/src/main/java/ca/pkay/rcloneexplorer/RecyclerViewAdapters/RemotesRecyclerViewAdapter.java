package ca.pkay.rcloneexplorer.RecyclerViewAdapters;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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
        RemoteItem item = remotes.get(position);
        boolean isPinned = item.isPinned();
        holder.remoteName = item.getName();
        holder.tvName.setText(item.getDisplayName());

        int icon = item.getRemoteIcon();
        holder.ivIcon.setImageDrawable(view.getResources().getDrawable(icon));

        if (isPinned) {
            holder.pinIcon.setVisibility(View.VISIBLE);
        } else {
            holder.pinIcon.setVisibility(View.INVISIBLE);
        }

        holder.options.setOnClickListener(v -> optionsListener.onRemoteOptionsClicked(holder.options, remotes.get(holder.getAdapterPosition())));

        holder.view.setOnClickListener(view -> {
            if (null != clickListener) {
                clickListener.onRemoteClick(remotes.get(holder.getAdapterPosition()));
            }
        });
    }

    public void newData(List<RemoteItem> data) {
        remotes = new ArrayList<>(data);
        notifyDataSetChanged();
    }

    public void removeItem(RemoteItem remoteItem) {
        int index = remotes.indexOf(remoteItem);
        if (index >= 0) {
            remotes.remove(index);
            notifyItemRemoved(index);
        }
    }

    public void moveDataItem(List<RemoteItem> data, int from, int to) {
        remotes = new ArrayList<>(data);
        notifyItemRemoved(from);
        notifyItemInserted(to);
    }

    @Override
    public int getItemCount() {
        if (remotes == null) {
            return 0;
        }
        return remotes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        final View view;
        final ImageView ivIcon;
        final TextView tvName;
        final ImageButton options;
        final ImageView pinIcon;
        public String remoteName;

        ViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            this.ivIcon = view.findViewById(R.id.remoteIcon);
            this.tvName = view.findViewById(R.id.remoteName);
            this.options = view.findViewById(R.id.remote_options);
            this.pinIcon = view.findViewById(R.id.pin_icon);
        }
    }

}
