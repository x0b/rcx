package ca.pkay.rcloneexplorer.RecyclerViewAdapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.LruCache;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import ca.pkay.rcloneexplorer.Items.FileItem;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import ca.pkay.rcloneexplorer.util.FLog;
import io.github.x0b.safdav.SafAccessProvider;
import io.github.x0b.safdav.file.FileAccessError;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileExplorerRecyclerViewAdapter extends RecyclerView.Adapter<FileExplorerRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "FileExplorerRVA";
    private static final int THUMBNAIL_CACHE_ENTRIES = 64;
    private static final long SIZE_LIMIT_UNLIMITED = Long.MAX_VALUE;

    private List<FileItem> files;
    private final int selectionColor;
    private final View emptyView;
    private final View noSearchResultsView;
    private final OnClickListener listener;
    private boolean isInSelectMode;
    private List<FileItem> selectedItems;
    private boolean isInMoveMode;
    private boolean isInSearchMode;
    private boolean canSelect;
    private boolean showThumbnails;
    private int cardColor;
    private boolean optionsDisabled;
    private boolean wrapFileNames;
    private final Context context;
    private long sizeLimit;

    /** Small in-memory LRU cache for video frame bitmaps (per remote URL). */
    private final LruCache<String, Bitmap> videoFrameCache =
            new LruCache<String, Bitmap>(THUMBNAIL_CACHE_ENTRIES) {
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return value.getByteCount() / 1024 == 0 ? 1 : value.getByteCount() / 1024;
                }
            };
    private final ExecutorService videoFrameExecutor = Executors.newFixedThreadPool(2);
    private final Handler mainThread = new Handler(Looper.getMainLooper());

    public interface OnClickListener {
        void onFileClicked(FileItem fileItem);
        void onDirectoryClicked(FileItem fileItem, int position);
        void onFilesSelected();
        void onFileDeselected();
        void onFileOptionsClicked(View view, FileItem fileItem);
        String[] getThumbnailServerParams();
    }

    public FileExplorerRecyclerViewAdapter(Context context, View emptyView, View noSearchResultsView, OnClickListener listener) {
        files = new ArrayList<>();
        this.context = context;
        this.emptyView = emptyView;
        this.noSearchResultsView = noSearchResultsView;
        this.listener = listener;
        isInSelectMode = false;
        selectedItems = new ArrayList<>();
        isInMoveMode = false;
        isInSearchMode = false;
        canSelect = true;
        wrapFileNames = true;

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.cardColor, typedValue, true);
        cardColor = typedValue.data;

        theme.resolveAttribute(R.attr.colorPrimaryLight, typedValue, true);
        selectionColor = typedValue.data;
        optionsDisabled = false;
        sizeLimit = readSizeLimit(context);
    }

    private static long readSizeLimit(@NonNull Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getLong(ctx.getString(R.string.pref_key_thumbnail_size_limit),
                        ctx.getResources().getInteger(R.integer.default_thumbnail_size_limit));
    }

    /** Re-reads the cache size pref from disk. Call after the user changes the setting. */
    public void refreshSizeLimit() {
        sizeLimit = readSizeLimit(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_file_explorer_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        // When the ViewHolder scrolls off-screen and is recycled, cancel any
        // in-flight Glide request so we don't keep fetching files the user
        // can no longer see. This is the cornerstone of lazy loading.
        Glide.with(holder.itemView).clear(holder.fileIcon);
        holder.thumbnailProgress.setVisibility(View.GONE);
        super.onViewRecycled(holder);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final FileItem item = files.get(position);

        holder.fileItem = item;
        // Cancel any in-flight thumbnail request from a previous bind of this
        // recycled ViewHolder before we touch the ImageView. This prevents
        // stale callbacks from clobbering the new bind.
        Glide.with(holder.itemView).clear(holder.fileIcon);

        if (item.isDir()) {
            holder.dirIcon.setVisibility(View.VISIBLE);
            holder.fileIcon.setVisibility(View.GONE);
            holder.fileSize.setVisibility(View.GONE);
            holder.interpunct.setVisibility(View.GONE);
            holder.thumbnailProgress.setVisibility(View.GONE);
        } else {
            holder.fileIcon.setVisibility(View.VISIBLE);
            holder.dirIcon.setVisibility(View.GONE);
            holder.fileSize.setText(item.getHumanReadableSize());
            holder.fileSize.setVisibility(View.VISIBLE);
            holder.interpunct.setVisibility(View.VISIBLE);
        }

        if (showThumbnails && !item.isDir()) {
            bindThumbnail(holder, item);
        } else {
            holder.thumbnailProgress.setVisibility(View.GONE);
            if (!item.isDir()) {
                holder.fileIcon.setImageResource(R.drawable.ic_file);
            }
        }

        RemoteItem itemRemote = item.getRemote();
        if (!itemRemote.isDirectoryModifiedTimeSupported() && item.isDir()) {
            holder.fileModTime.setVisibility(View.GONE);
        } else {
            holder.fileModTime.setVisibility(View.VISIBLE);
            holder.fileModTime.setText(item.getHumanReadableModTime());
        }

        holder.fileName.setText(item.getName());

        if (isInSelectMode) {
            if (selectedItems.contains(item)) {
                holder.view.setBackgroundColor(selectionColor);
            } else {
                holder.view.setBackgroundColor(cardColor);
            }
        } else {
            holder.view.setBackgroundColor(cardColor);
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

        if ((isInSelectMode || isInMoveMode) && !optionsDisabled) {
            holder.fileOptions.setVisibility(View.INVISIBLE);
        } else if (optionsDisabled) {
            holder.fileOptions.setVisibility(View.GONE);
        } else {
            holder.fileOptions.setVisibility(View.VISIBLE);
            holder.fileOptions.setOnClickListener(v -> listener.onFileOptionsClicked(v, item));
        }

        if (wrapFileNames) {
            holder.fileName.setEllipsize(TextUtils.TruncateAt.MIDDLE);
            holder.fileName.setSingleLine(true);
        } else {
            holder.fileName.setEllipsize(null);
            holder.fileName.setSingleLine(false);
        }

        holder.view.setOnClickListener(view -> {
            if (isInSelectMode) {
                onLongClickAction(item, holder);
            } else {
                onClickAction(item, holder.getAdapterPosition());
            }
        });

        holder.view.setOnLongClickListener(view -> {
            if (!isInMoveMode && canSelect) {
                onLongClickAction(item, holder);
            }
            return true;
        });

        holder.icons.setOnClickListener(v -> {
            if (!isInMoveMode && canSelect) {
                onLongClickAction(item, holder);
            }
        });
    }

    /**
     * Loads a thumbnail for an image or video file. Images go through Glide
     * (HTTP via the local rclone serve). Videos use
     * {@link MediaMetadataRetriever} on a background thread, since Glide
     * cannot extract frames from remote HTTP sources.
     */
    private void bindThumbnail(@NonNull ViewHolder holder, FileItem item) {
        String mimeType = item.getMimeType();
        boolean isImage = mimeType != null && mimeType.startsWith("image/");
        boolean isVideo = mimeType != null && mimeType.startsWith("video/");
        if (!(isImage || isVideo) || item.getSize() > sizeLimit || sizeLimit == 0) {
            holder.thumbnailProgress.setVisibility(View.GONE);
            holder.fileIcon.setImageResource(R.drawable.ic_file);
            return;
        }
        boolean localLoad = item.getRemote().getType() == RemoteItem.SAFW;
        RequestOptions glideOption = new RequestOptions()
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_file);

        if (isImage) {
            showLoading(holder, true);
            if (localLoad) {
                bindSafImage(holder, item, glideOption);
            } else {
                bindRemoteImage(holder, item, glideOption);
            }
        } else { // video
            // Check cache first
            String cacheKey = videoCacheKey(item, localLoad);
            Bitmap cached = videoFrameCache.get(cacheKey);
            if (cached != null) {
                showLoading(holder, false);
                holder.fileIcon.setImageBitmap(cached);
                return;
            }
            showLoading(holder, true);
            holder.fileIcon.setImageResource(R.drawable.ic_file);
            if (localLoad) {
                bindSafVideo(holder, item, cacheKey);
            } else {
                bindRemoteVideo(holder, item, cacheKey);
            }
        }
    }

    private void bindRemoteImage(@NonNull ViewHolder holder, FileItem item, RequestOptions glideOption) {
        String[] serverParams = listener.getThumbnailServerParams();
        String hiddenPath = serverParams[0];
        int serverPort = Integer.parseInt(serverParams[1]);
        String url = "http://127.0.0.1:" + serverPort + "/" + hiddenPath + '/' + item.getPath();
        FLog.v(TAG, "bindRemoteImage: url=%s", url);
        // Glide.with(holder.itemView) ties the request to this ViewHolder's
        // lifecycle: when the ViewHolder is recycled or detached, Glide
        // automatically cancels the in-flight request - which means we never
        // keep fetching files that have scrolled off-screen. Combined with
        // Glide's memory + disk cache (DiskCacheStrategy.ALL), this gives
        // proper lazy loading: only visible items are fetched, and once
        // fetched they're cached for subsequent rebinds.
        Glide.with(holder.itemView)
                .load(new PersistentGlideUrl(url))
                .apply(glideOption)
                .listener(new SimpleRequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public void onResourceReady() {
                        FLog.v(TAG, "bindRemoteImage: loaded %s", url);
                        showLoading(holder, false);
                    }
                    @Override
                    public void onLoadFailed() {
                        FLog.w(TAG, "bindRemoteImage: FAILED %s", url);
                        showLoading(holder, false);
                    }
                })
                .into(holder.fileIcon);
    }

    private void bindSafImage(@NonNull ViewHolder holder, FileItem item, RequestOptions glideOption) {
        try {
            Uri contentUri = SafAccessProvider.getDirectServer(context).getDocumentUri('/' + item.getPath());
            FLog.v(TAG, "bindSafImage: uri=%s", contentUri);
            Glide.with(holder.itemView)
                    .load(contentUri)
                    .apply(glideOption)
                    .listener(new SimpleRequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public void onResourceReady() {
                            showLoading(holder, false);
                        }
                        @Override
                        public void onLoadFailed() {
                            FLog.w(TAG, "bindSafImage: FAILED %s", contentUri);
                            showLoading(holder, false);
                        }
                    })
                    .into(holder.fileIcon);
        } catch (FileAccessError e) {
            FLog.e(TAG, "onBindViewHolder: SAF image error", e);
            showLoading(holder, false);
            holder.fileIcon.setImageResource(R.drawable.ic_file);
        }
    }

    private void bindSafVideo(@NonNull ViewHolder holder, FileItem item, String cacheKey) {
        try {
            Uri contentUri = SafAccessProvider.getDirectServer(context).getDocumentUri('/' + item.getPath());
            extractVideoFrame(contentUri.toString(), true, cacheKey, bitmap ->
                    applyVideoBitmap(holder, bitmap));
        } catch (FileAccessError e) {
            FLog.e(TAG, "bindSafVideo: SAF error", e);
            showLoading(holder, false);
            holder.fileIcon.setImageResource(R.drawable.ic_file);
        }
    }

    private void bindRemoteVideo(@NonNull ViewHolder holder, FileItem item, String cacheKey) {
        String[] serverParams = listener.getThumbnailServerParams();
        String hiddenPath = serverParams[0];
        int serverPort = Integer.parseInt(serverParams[1]);
        String url = "http://127.0.0.1:" + serverPort + "/" + hiddenPath + '/' + item.getPath();
        extractVideoFrame(url, false, cacheKey, bitmap ->
                applyVideoBitmap(holder, bitmap));
    }

    private void applyVideoBitmap(@NonNull ViewHolder holder, @Nullable Bitmap bitmap) {
        mainThread.post(() -> {
            showLoading(holder, false);
            if (bitmap != null) {
                holder.fileIcon.setImageBitmap(bitmap);
            } else {
                holder.fileIcon.setImageResource(R.drawable.ic_file);
            }
        });
    }

    /**
     * Runs {@link MediaMetadataRetriever} on a background thread to extract a
     * representative frame for the given URL or content URI. Caches the
     * result so subsequent binds for the same item are instant.
     */
    private void extractVideoFrame(String source, boolean isContent, String cacheKey,
                                   VideoFrameCallback callback) {
        videoFrameExecutor.execute(() -> {
            Bitmap cached = videoFrameCache.get(cacheKey);
            if (cached != null) {
                callback.onFrame(cached);
                return;
            }
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            Bitmap frame = null;
            try {
                if (isContent) {
                    retriever.setDataSource(context, Uri.parse(source));
                } else {
                    // HTTP path; works against the local rclone serve
                    retriever.setDataSource(source, new java.util.HashMap<>());
                }
                frame = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            } catch (RuntimeException e) {
                FLog.w(TAG, "extractVideoFrame: failed for %s: %s", source, e.toString());
            } finally {
                try {
                    retriever.release();
                } catch (Exception ignored) {
                    // release() may throw IOException on some API levels; ignore.
                }
            }
            if (frame != null) {
                videoFrameCache.put(cacheKey, frame);
            }
            callback.onFrame(frame);
        });
    }

    private String videoCacheKey(FileItem item, boolean localLoad) {
        return (localLoad ? "saf:" : "http:") + item.getRemote().getName() + ':' + item.getPath();
    }

    private void showLoading(@NonNull ViewHolder holder, boolean loading) {
        holder.thumbnailProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void bindSafFile(@NonNull ViewHolder holder, FileItem item, RequestOptions glideOption) {
        // Legacy entry point kept for API compatibility - not used after the
        // image/video split above. Image path is the same as bindSafImage.
        bindSafImage(holder, item, glideOption);
    }

    private static class PersistentGlideUrl extends GlideUrl {

        public PersistentGlideUrl(String url) {
            super(url);
        }

        @Override
        public String getCacheKey() {
            try {
                URL url = super.toURL();
                String path = url.getPath();
                return path.substring(path.indexOf('/', 1));
            } catch (MalformedURLException e) {
                return super.getCacheKey();
            }
        }
    }

    private interface VideoFrameCallback {
        void onFrame(@Nullable Bitmap bitmap);
    }

    /**
     * Minimal Glide RequestListener that only forwards success/failure to
     * drive the loading-spinner visibility. Subclasses override only what
     * they need; the rest stays as a no-op default.
     */
    private abstract static class SimpleRequestListener<R>
            implements com.bumptech.glide.request.RequestListener<R> {
        @Override
        public boolean onLoadFailed(@Nullable com.bumptech.glide.load.engine.GlideException e,
                                    Object model, Target<R> target,
                                    boolean isFirstResource) {
            onLoadFailed();
            return false;
        }
        @Override
        public boolean onResourceReady(R resource, Object model, Target<R> target,
                                       com.bumptech.glide.load.DataSource dataSource,
                                       boolean isFirstResource) {
            onResourceReady();
            return false;
        }
        public abstract void onResourceReady();
        public abstract void onLoadFailed();
    }

    @Override
    public int getItemCount() {
        if (files == null) {
            return 0;
        } else {
            return files.size();
        }
    }

    public void disableFileOptions() {
        optionsDisabled = true;
    }

    public void showThumbnails(boolean showThumbnails) {
        this.showThumbnails = showThumbnails;
    }

    public List<FileItem> getCurrentContent() {
        return new ArrayList<>(files);
    }

    public void clear() {
        if (files == null) {
            return;
        }
        int count = files.size();
        files.clear();
        isInSelectMode = false;
        if (!selectedItems.isEmpty()) {
            selectedItems.clear();
            listener.onFileDeselected();
        }
        notifyItemRangeRemoved(0, count);
    }

    public void newData(List<FileItem> data) {
        this.clear();
        files = new ArrayList<>(data);
        isInSelectMode = false;
        if (files.isEmpty()) {
            showEmptyState(true);
        } else {
            showEmptyState(false);
        }
        if (isInMoveMode) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeInserted(0, files.size());
        }
    }

    public void updateData(List<FileItem> data) {
        if (data.isEmpty()) {
            int count = files.size();
            files.clear();
            notifyItemRangeRemoved(0, count);
            showEmptyState(true);
            return;
        }
        showEmptyState(false);
        List<FileItem> newData = new ArrayList<>(data);
        List<FileItem> diff = new ArrayList<>(files);

        diff.removeAll(newData);
        for (FileItem fileItem : diff) {
            int index = files.indexOf(fileItem);
            files.remove(index);
            if (selectedItems.contains(fileItem)) {
                selectedItems.remove(fileItem);
                isInSelectMode = !selectedItems.isEmpty();
                listener.onFileDeselected();
            }
            notifyItemRemoved(index);
        }

        diff = new ArrayList<>(data);
        diff.removeAll(files);
        for (FileItem fileItem : diff) {
            int index = newData.indexOf(fileItem);
            files.add(index, fileItem);
            notifyItemInserted(index);
        }
    }

    public void updateSortedData(List<FileItem> data) {
        if (files != null) {
            int count = files.size();
            files.clear();
            notifyItemRangeRemoved(0, count);
        }
        files = new ArrayList<>(data);
        if (files.isEmpty()) {
            showEmptyState(true);
        } else {
            showEmptyState(false);
        }
        if (isInMoveMode) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeInserted(0, files.size());
        }
    }

    public void refreshData() {
        notifyDataSetChanged();
    }

    public void setMoveMode(Boolean mode) {
        isInMoveMode = mode;
    }

    public void setSearchMode(Boolean mode) {
        isInSearchMode = mode;
    }

    public void setSelectedItems(List<FileItem> selectedItems) {
        this.selectedItems = new ArrayList<>(selectedItems);
        this.isInSelectMode = true;
        notifyDataSetChanged();
    }

    public Boolean isInSelectMode() {
        return isInSelectMode;
    }

    public List<FileItem> getSelectedItems() {
        return new ArrayList<>(selectedItems);
    }

    public int getNumberOfSelectedItems() {
        return selectedItems.size();
    }

    public Boolean isInMoveMode() {
        return isInMoveMode;
    }

    public void setWrapFileNames(boolean wrapFileNames) {
        this.wrapFileNames = wrapFileNames;
        refreshData();
    }

    private void showEmptyState(Boolean show) {
        if (isInSearchMode) {
            if (show) {
                noSearchResultsView.setVisibility(View.VISIBLE);
            } else {
                noSearchResultsView.setVisibility(View.INVISIBLE);
            }
        } else {
            if (show) {
                emptyView.setVisibility(View.VISIBLE);
            } else {
                emptyView.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void onClickAction(FileItem item, int position) {
        if (item.isDir() && null != listener) {
            listener.onDirectoryClicked(item, position);
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
            listener.onFileDeselected();
        } else {
            isInSelectMode = true;
            selectedItems.clear();
            selectedItems.addAll(files);
            listener.onFilesSelected();
        }
        notifyDataSetChanged();
    }

    public void cancelSelection() {
        isInSelectMode = false;
        selectedItems.clear();
        listener.onFileDeselected();
        notifyDataSetChanged();
    }

    public void setCanSelect(Boolean canSelect) {
        this.canSelect = canSelect;
    }

    private void onLongClickAction(FileItem item, ViewHolder holder) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item);
            holder.view.setBackgroundColor(cardColor);
            if (selectedItems.size() == 0) {
                isInSelectMode = false;
                listener.onFileDeselected();
            }
            listener.onFileDeselected();
        } else {
            selectedItems.add(item);
            isInSelectMode = true;
            holder.view.setBackgroundColor(selectionColor);
            listener.onFilesSelected();
        }
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View view;
        public final View icons;
        public final ImageView fileIcon;
        public final ImageView dirIcon;
        public final TextView fileName;
        public final TextView fileModTime;
        public final TextView fileSize;
        public final TextView interpunct;
        public final ImageButton fileOptions;
        public final ProgressBar thumbnailProgress;
        public FileItem fileItem;

        ViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            this.icons = view.findViewById(R.id.icons);
            this.fileIcon = view.findViewById(R.id.file_icon);
            this.dirIcon = view.findViewById(R.id.dir_icon);
            this.fileName = view.findViewById(R.id.file_name);
            this.fileModTime = view.findViewById(R.id.file_modtime);
            this.fileSize = view.findViewById(R.id.file_size);
            this.fileOptions = view.findViewById(R.id.file_options);
            this.interpunct = view.findViewById(R.id.interpunct);
            this.thumbnailProgress = view.findViewById(R.id.thumbnail_progress);
        }
    }
}
