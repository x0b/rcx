package ca.pkay.rcloneexplorer.RecyclerViewAdapters;


import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.octicons_typeface_library.Octicons;

import java.util.Collections;
import java.util.List;

import ca.pkay.rcloneexplorer.Fragments.RemotesFragment;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.R;

public class RemotesRecyclerViewAdapter extends RecyclerView.Adapter<RemotesRecyclerViewAdapter.ViewHolder>{

    private final List<RemoteItem> remotes;
    private final RemotesFragment.OnRemoteClickListener clickListener;
    private View view;

    public RemotesRecyclerViewAdapter(List<RemoteItem> remotes, RemotesFragment.OnRemoteClickListener clickListener) {
        this.remotes = remotes;
        Collections.sort(remotes);
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
                holder.ivIcon.setImageDrawable(new IconicsDrawable(view.getContext()).icon(CommunityMaterial.Icon.cmd_lock).color(Color.BLACK).sizeDp(24));
                break;
            case "amazon cloud drive":
                holder.ivIcon.setImageDrawable(new IconicsDrawable(view.getContext()).icon(CommunityMaterial.Icon.cmd_amazon).color(Color.BLACK).sizeDp(24));
                break;
            case "b2":
                holder.ivIcon.setImageDrawable(new IconicsDrawable(view.getContext()).icon(FontAwesome.Icon.faw_gripfire).color(Color.BLACK).sizeDp(24));
                break;
            case "drive":
                holder.ivIcon.setImageDrawable(new IconicsDrawable(view.getContext()).icon(CommunityMaterial.Icon.cmd_google_drive).color(Color.BLACK).sizeDp(24));
                break;
            case "dropbox":
                holder.ivIcon.setImageDrawable(new IconicsDrawable(view.getContext()).icon(CommunityMaterial.Icon.cmd_dropbox).color(Color.BLACK).sizeDp(24));
                break;
            case "google cloud storage":
                holder.ivIcon.setImageDrawable(new IconicsDrawable(view.getContext()).icon(CommunityMaterial.Icon.cmd_google).color(Color.BLACK).sizeDp(24));
                break;
            case "onedrive":
                holder.ivIcon.setImageDrawable(new IconicsDrawable(view.getContext()).icon(CommunityMaterial.Icon.cmd_onedrive).color(Color.BLACK).sizeDp(24));
                break;
            case "s3":
                holder.ivIcon.setImageDrawable(new IconicsDrawable(view.getContext()).icon(CommunityMaterial.Icon.cmd_amazon).color(Color.BLACK).sizeDp(24));
                break;
            case "yandex":
                holder.ivIcon.setImageDrawable(new IconicsDrawable(view.getContext()).icon(FontAwesome.Icon.faw_yandex).color(Color.BLACK).sizeDp(24));
                break;
            case "box":
                holder.ivIcon.setImageDrawable(new IconicsDrawable(view.getContext()).icon(CommunityMaterial.Icon.cmd_box).color(Color.BLACK).sizeDp(24));
                break;
            case "sftp":
                holder.ivIcon.setImageDrawable(new IconicsDrawable(view.getContext()).icon(Octicons.Icon.oct_terminal).color(Color.BLACK).sizeDp(24));
                break;
            default:
                holder.ivIcon.setImageDrawable(new IconicsDrawable(view.getContext()).icon(CommunityMaterial.Icon.cmd_cloud).color(Color.BLACK).sizeDp(24));
                    break;
        }

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != clickListener) {
                    clickListener.onRemoteClick(remotes.get(holder.getAdapterPosition()));
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

        ViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            this.ivIcon = view.findViewById(R.id.remoteIcon);
            this.tvName = view.findViewById(R.id.remoteName);
        }
    }

}
