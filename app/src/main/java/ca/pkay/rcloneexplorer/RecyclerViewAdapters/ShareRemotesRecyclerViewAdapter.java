package ca.pkay.rcloneexplorer.RecyclerViewAdapters;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import ca.pkay.rcloneexplorer.Fragments.ShareRemotesFragment;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.databinding.FragmentShareRemotesItemBinding;

public class ShareRemotesRecyclerViewAdapter extends RecyclerView.Adapter<ShareRemotesRecyclerViewAdapter.ViewHolder>{

    private List<RemoteItem> remotes;
    private final ShareRemotesFragment.OnRemoteClickListener clickListener;
    private FragmentShareRemotesItemBinding binding;

    public ShareRemotesRecyclerViewAdapter(List<RemoteItem> remotes, ShareRemotesFragment.OnRemoteClickListener clickListener) {
        this.remotes = remotes;
        Collections.sort(remotes);
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = FragmentShareRemotesItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        String remoteName = remotes.get(position).getName();
        boolean isPinned = remotes.get(position).isPinned();
        holder.remoteName = remoteName;
        holder.tvName.setText(remotes.get(position).getDisplayName());

        int icon = remotes.get(position).getRemoteIcon();
        holder.ivIcon.setImageDrawable(binding.getRoot().getResources().getDrawable(icon));

        if (isPinned) {
            holder.pinIcon.setVisibility(View.VISIBLE);
        } else {
            holder.pinIcon.setVisibility(View.INVISIBLE);
        }

        holder.view.setOnClickListener(view -> {
            if (null != clickListener) {
                clickListener.onRemoteClick(remotes.get(holder.getAdapterPosition()));
            }
        });
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
        final ImageView pinIcon;
        public String remoteName;

        ViewHolder(FragmentShareRemotesItemBinding binding) {
            super(binding.getRoot());
            this.view = binding.getRoot();
            this.ivIcon = binding.remoteIcon;
            this.tvName = binding.remoteName;
            this.pinIcon = binding.pinIcon;
        }
    }

}
