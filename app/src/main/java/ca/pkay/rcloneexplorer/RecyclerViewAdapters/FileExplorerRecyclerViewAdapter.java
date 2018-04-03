package ca.pkay.rcloneexplorer.RecyclerViewAdapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ca.pkay.rcloneexplorer.Items.FileItem;
import ca.pkay.rcloneexplorer.R;

public class FileExplorerRecyclerViewAdapter extends RecyclerView.Adapter<FileExplorerRecyclerViewAdapter.ViewHolder> {

    private List<FileItem> files;
    private OnClickListener listener;
    private Boolean isInSelectMode;
    private List<FileItem> selectedItems;
    private Boolean isInMoveMode;

    public interface OnClickListener {
        void onFileClicked(FileItem fileItem);
        void onDirectoryClicked(FileItem fileItem);
        void onFilesSelected(boolean selection);
    }

    public FileExplorerRecyclerViewAdapter(List<FileItem> files, OnClickListener listener) {
        this.files = files;
        this.listener = listener;
        isInSelectMode = false;
        selectedItems = new ArrayList<>();
        isInMoveMode = false;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_file_explorer_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
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
        if (isInSelectMode) {
            if (selectedItems.contains(item)) {
                holder.view.setBackgroundColor(holder.view.getResources().getColor(R.color.colorPrimaryLight));
            } else {
                holder.view.setBackgroundColor(0x00000000);
            }
        } else {
            holder.view.setBackgroundColor(0x00000000);
        }
        if (isInMoveMode) {
            if (item.isDir()) {
                holder.view.setAlpha(1f);
            } else {
                holder.view.setAlpha(.5f);
            }
        } else if (holder.view.getAlpha() == .5f) {
            holder.view.setAlpha(1f);
        }

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isInSelectMode) {
                    onLongClickAction(item, holder);
                } else {
                    onClickAction(item);
                }
            }
        });

        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!isInMoveMode) {
                    onLongClickAction(item, holder);
                }
                return true;
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
        isInSelectMode = false;
        selectedItems.clear();
        listener.onFilesSelected(false);
        notifyDataSetChanged();
    }

    public void newData(List<FileItem> data) {
        files = data;
        isInSelectMode = false;
        selectedItems.clear();
        listener.onFilesSelected(false);
        notifyDataSetChanged();
    }

    public void updateData(List<FileItem> data) {
        files = data;
        notifyDataSetChanged();
    }

    public void refreshData() {
        notifyDataSetChanged();
    }

    public void setMoveMode(Boolean mode) {
        isInMoveMode = mode;
    }

    public Boolean isInSelectMode() {
        return isInSelectMode;
    }

    public List<FileItem> getSelectedItems() {
        return selectedItems;
    }

    public int getNumberOfSelectedItems() {
        return selectedItems.size();
    }

    private void onClickAction(FileItem item) {
        if (item.isDir() && null != listener) {
            listener.onDirectoryClicked(item);
        } else if (!item.isDir() && !isInMoveMode && null != listener) {
            listener.onFileClicked(item);
        }
    }

    public void toggleSelectAll() {
        if (null == files) {
            return;
        }
        if (selectedItems.size() == files.size()) {
            isInSelectMode = false;
            selectedItems.clear();
            listener.onFilesSelected(false);
        } else {
            isInSelectMode = true;
            selectedItems.clear();
            selectedItems.addAll(files);
            listener.onFilesSelected(true);
        }
        notifyDataSetChanged();
    }

    public void cancelSelection() {
        isInSelectMode = false;
        selectedItems.clear();
        listener.onFilesSelected(false);
        notifyDataSetChanged();
    }

    private void onLongClickAction(FileItem item, ViewHolder holder) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item);
            holder.view.setBackgroundColor(0x00000000);
            if (selectedItems.size() == 0) {
                isInSelectMode = false;
                listener.onFilesSelected(false);
            }
            listener.onFilesSelected(true);
        } else {
            selectedItems.add(item);
            isInSelectMode = true;
            holder.view.setBackgroundColor(holder.view.getResources().getColor(R.color.colorPrimaryLight));
            listener.onFilesSelected(true);
        }
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
