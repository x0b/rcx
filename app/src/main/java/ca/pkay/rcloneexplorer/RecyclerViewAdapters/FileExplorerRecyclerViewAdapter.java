package ca.pkay.rcloneexplorer.RecyclerViewAdapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ca.pkay.rcloneexplorer.Fragments.FileExplorerFragment;
import ca.pkay.rcloneexplorer.Items.FileItem;
import ca.pkay.rcloneexplorer.R;

public class FileExplorerRecyclerViewAdapter extends RecyclerView.Adapter<FileExplorerRecyclerViewAdapter.ViewHolder> {

    private List<FileItem> files;
    private OnClickListener listener;

    public interface OnClickListener {
        void onFileClicked(FileItem fileItem);
        void onDirectoryClicked(FileItem fileItem);
    }

    public FileExplorerRecyclerViewAdapter(List<FileItem> files, OnClickListener listener) {
        this.files = files;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_file_explorer_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final FileItem item = files.get(position);

        holder.fileItem = item;
        if (item.isDir()) {
            holder.fileIcon.setImageResource(R.drawable.ic_folder);
        } else {
            holder.fileIcon.setImageResource(R.drawable.ic_file);
        }
        holder.fileName.setText(item.getName());
        holder.fileModTime.setText(item.getHumanReadableModTime());
        if (!item.isDir()) {
            holder.fileSize.setText(item.getHumanReadableSize());
            holder.fileSize.setVisibility(View.VISIBLE);
            holder.interpunct.setVisibility(View.VISIBLE);
        } else {
            holder.fileSize.setVisibility(View.GONE);
            holder.interpunct.setVisibility(View.GONE);
        }

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (item.isDir() && null != listener) {
                    listener.onDirectoryClicked(item);
                } else if (!item.isDir() && null != listener) {
                    listener.onFileClicked(item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (files == null) {
            return 0;
        } else {
            return files.size();
        }
    }

    public void clear() {
        files.clear();
        notifyDataSetChanged();
    }

    public void newData(List<FileItem> data) {
        files = data;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View view;
        public final ImageView fileIcon;
        public final TextView fileName;
        public final TextView fileModTime;
        public final TextView fileSize;
        public final TextView interpunct;
        public FileItem fileItem;

        public ViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            this.fileIcon = view.findViewById(R.id.file_icon);
            this.fileName = view.findViewById(R.id.file_name);
            this.fileModTime = view.findViewById(R.id.file_modtime);
            this.fileSize = view.findViewById(R.id.file_size);
            this.interpunct = view.findViewById(R.id.interpunct);
        }
    }
}
