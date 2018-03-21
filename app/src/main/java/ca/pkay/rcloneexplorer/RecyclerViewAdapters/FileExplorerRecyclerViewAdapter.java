package ca.pkay.rcloneexplorer.RecyclerViewAdapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ca.pkay.rcloneexplorer.Items.FileItem;
import ca.pkay.rcloneexplorer.R;

public class FileExplorerRecyclerViewAdapter extends RecyclerView.Adapter<FileExplorerRecyclerViewAdapter.ViewHolder> {

    private List<FileItem> files;

    public FileExplorerRecyclerViewAdapter(List<FileItem> files) {
        this.files = files;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_file_explorer_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FileItem item = files.get(position);

        holder.fileItem = item;
        holder.fileName.setText(item.getName());
    }

    @Override
    public int getItemCount() {
        if (files == null) {
            return 0;
        } else {
            return files.size();
        }
    }

    public void newData(List<FileItem> data) {
        files = data;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View view;
        public final ImageView fileIcon;
        public final TextView fileName;
        public FileItem fileItem;

        public ViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            this.fileIcon = view.findViewById(R.id.file_icon);
            this.fileName = view.findViewById(R.id.file_name);
        }
    }
}
