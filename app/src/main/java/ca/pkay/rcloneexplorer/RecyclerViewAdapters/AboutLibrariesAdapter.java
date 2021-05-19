package ca.pkay.rcloneexplorer.RecyclerViewAdapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

import ca.pkay.rcloneexplorer.databinding.AboutIconItemBinding;
import ca.pkay.rcloneexplorer.databinding.AboutLibrariesItemBinding;

public class AboutLibrariesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int CONTENT_TYPE = 0;
    private final int FOOTER_TYPE = 1;
    private final List<String> libraryNames;
    private final Map<String, String> libraryUrls;
    private final Map<String, String> libraryLicences;
    private final Map<String, String> libraryLicenceUrls;
    private final OnClickListener listener;

    public interface OnClickListener {
        void onLibraryClick(String url);
    }

    public AboutLibrariesAdapter(List<String> libNames, Map<String, String> libUrls, Map<String, String> libLicences, Map<String, String> libLicenceUrls, OnClickListener l) {
        this.libraryNames = libNames;
        this.libraryUrls = libUrls;
        this.libraryLicences = libLicences;
        this.libraryLicenceUrls = libLicenceUrls;
        this.listener = l;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == CONTENT_TYPE) {
            AboutLibrariesItemBinding libBinding = AboutLibrariesItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ContentViewHolder(libBinding);
        }
        AboutIconItemBinding iconBinding = AboutIconItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FooterViewHolder(iconBinding);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == libraryNames.size()) {
            return FOOTER_TYPE;
        }
        return CONTENT_TYPE;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ContentViewHolder) {
            setContent(holder, position);
        } else if (holder instanceof FooterViewHolder) {
            setFooter(holder);
        }
    }

    private void setContent(RecyclerView.ViewHolder viewHolder, int position) {
        ContentViewHolder holder = (ContentViewHolder) viewHolder;
        String libraryName = libraryNames.get(position);
        final String libraryUrl = libraryUrls.get(libraryName);
        String libraryLicence = libraryLicences.get(libraryName);
        final String libraryLicenceUrl = libraryLicenceUrls.get(libraryName);

        holder.libraryName.setText(libraryName);
        if (libraryUrl != null) {
            holder.libraryLicence.setText(libraryLicence);
        }
        if (libraryUrl != null) {
            holder.libraryName.setOnClickListener(v -> listener.onLibraryClick(libraryUrl));
        }
        if (libraryLicenceUrl != null) {
            holder.libraryLicence.setOnClickListener(v -> listener.onLibraryClick(libraryLicenceUrl));
        }
    }

    private void setFooter(RecyclerView.ViewHolder viewHolder) {
        FooterViewHolder holder = (FooterViewHolder) viewHolder;
        holder.smashIcons.setOnClickListener(v -> listener.onLibraryClick("https://www.flaticon.com/authors/smashicons"));
        holder.flatIcon.setOnClickListener(v -> listener.onLibraryClick("https://www.flaticon.com/"));
    }

    @Override
    public int getItemCount() {
        if (libraryNames == null) {
            return 1;
        }
        return libraryNames.size() + 1;
    }

    class ContentViewHolder extends RecyclerView.ViewHolder {

        final TextView libraryName;
        final TextView libraryLicence;

        ContentViewHolder(AboutLibrariesItemBinding binding) {
            super(binding.getRoot());
            this.libraryName = binding.libraryName;
            this.libraryLicence = binding.libraryLicence;
        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder {

        final TextView smashIcons;
        final TextView flatIcon;

        FooterViewHolder(AboutIconItemBinding binding) {
            super(binding.getRoot());
            this.smashIcons = binding.smashicons;
            this.flatIcon = binding.flaticon;
        }
    }
}
