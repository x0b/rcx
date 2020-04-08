package ca.pkay.rcloneexplorer.RemoteConfig;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import ca.pkay.rcloneexplorer.Dialogs.RemoteDestinationDialog;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import es.dmoral.toasty.Toasty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UnionConfig extends Fragment implements RemoteDestinationDialog.OnDestinationSelectedListener {

    private final String SAVED_REMOTES_PATHS = "ca.pkay.rcexplorer.AliasConfig.REMOTE_PATH";
    private final String SAVED_SELECTED_REMOTE = "ca.pkay.rcexplorer.AliasConfig.SELECTED_REMOTE";
    private Context context;
    private Rclone rclone;
    private TextInputLayout remoteNameInputLayout;
    private EditText remoteName;
    private RemoteItem selectedRemote;
    private ArrayList<String> unionRemotes;
    private boolean isDarkTheme;
    private RecyclerView remotesList;
    private RemotesListAdapter remotesListAdapter;

    public UnionConfig() {}

    public static UnionConfig newInstance() { return new UnionConfig(); }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext() == null) {
            return;
        }
        context = getContext();
        rclone = new Rclone(context);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        isDarkTheme = sharedPreferences.getBoolean(getString(R.string.pref_key_dark_theme), false);
        unionRemotes = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.config_form_union, container, false);
        setUpForm(view);
        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (selectedRemote != null) {
            outState.putParcelable(SAVED_SELECTED_REMOTE, selectedRemote);
        }
        if (unionRemotes != null) {
            outState.putStringArrayList(SAVED_REMOTES_PATHS, unionRemotes);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) {
            return;
        }

        ArrayList<String> savedRemotePath = savedInstanceState.getStringArrayList(SAVED_REMOTES_PATHS);
        if (savedRemotePath != null) {
            unionRemotes = savedRemotePath;
            for (String remotePath: unionRemotes) {
                remotesListAdapter.add(remotePath);
            }
        }
        selectedRemote = savedInstanceState.getParcelable(SAVED_SELECTED_REMOTE);
    }

    private void setUpForm(View view) {
        remoteNameInputLayout = view.findViewById(R.id.remote_name_layout);
        remoteNameInputLayout.setVisibility(View.VISIBLE);
        remoteName = view.findViewById(R.id.remote_name);

        remotesList = view.findViewById(R.id.union_list_remotes);
        remotesListAdapter = new RemotesListAdapter(unionRemotes);
        remotesList.setAdapter(remotesListAdapter);


        view.findViewById(R.id.union_btn_add_remote).setOnClickListener(v -> addRemote());

        view.findViewById(R.id.create).setOnClickListener(v -> setUpRemote());

        view.findViewById(R.id.cancel).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }

    private void addRemote() {
        final List<RemoteItem> configuredRemotes = rclone.getRemotes();
        if (configuredRemotes.isEmpty()) {
            Toasty.info(context, getString(R.string.no_remotes), Toast.LENGTH_SHORT, true).show();
            return;
        }

        RemoteItem.prepareDisplay(context, configuredRemotes);
        Collections.sort(configuredRemotes, (a, b) -> a.getDisplayName().compareTo(b.getDisplayName()));
        String[] options = new String[configuredRemotes.size()];
        int i = 0;
        for (RemoteItem remote : configuredRemotes) {
            options[i++] = remote.getDisplayName();
        }

        AlertDialog.Builder builder;
        if (isDarkTheme) {
            builder = new AlertDialog.Builder(context, R.style.DarkDialogTheme);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        builder.setTitle(R.string.select_remote)
                .setNegativeButton(R.string.cancel, (dialog, which) -> selectedRemote = null)
                .setPositiveButton(R.string.select, (dialog, which) -> {
                    if (selectedRemote == null) {
                        Toasty.info(context, getString(R.string.nothing_selected), Toast.LENGTH_SHORT, true).show();
                    } else {
                        setPath();
                    }
                })
                .setSingleChoiceItems(options, -1, (dialog, which) -> selectedRemote = configuredRemotes.get(which))
                .show();
    }

    private void setPath() {
        RemoteDestinationDialog remoteDestinationDialog = new RemoteDestinationDialog()
                .setDarkTheme(isDarkTheme)
                .setRemote(selectedRemote)
                .setTitle(R.string.union_select_remote);
        remoteDestinationDialog.show(getChildFragmentManager(), "remote destination dialog");
    }

    /*
     * RemoteDestinationDialog callback
     */
    @Override
    public void onDestinationSelected(String path) {
        String remotePath;
        if (selectedRemote.isRemoteType(RemoteItem.LOCAL)) {
            if (path.equals("//" + selectedRemote.getName())) {
                remotePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
            } else {
                remotePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + path;
            }
        } else {
            if (path.equals("//" + selectedRemote.getName())) {
                remotePath = selectedRemote.getName() + ":";
            } else {
                remotePath = selectedRemote.getName() + ":" + path;
            }
        }
        remotesListAdapter.add(remotePath);
    }

    private void setUpRemote() {
        String name = remoteName.getText().toString();
        boolean error = false;

        if (name.trim().isEmpty()) {
            remoteNameInputLayout.setErrorEnabled(true);
            remoteNameInputLayout.setError(getString(R.string.remote_name_cannot_be_empty));
            error = true;
        } else {
            remoteNameInputLayout.setErrorEnabled(false);
        }
        if (unionRemotes == null) {
            error = true;
        }
        if (error) {
            return;
        }

        ArrayList<String> options = new ArrayList<>();
        options.add(name);
        options.add("union");
        options.add("remotes");

        StringBuilder pathString = new StringBuilder();
        for (int i = unionRemotes.size()-1; i >= 0; i--) {
            pathString.append(unionRemotes.get(i)).append(' ');
        }
        options.add(pathString.toString());

        RemoteConfigHelper.setupAndWait(context, options);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private static class RemotePathsViewHolder extends RecyclerView.ViewHolder {

        TextView text;
        ImageView icon;
        String path;

        public RemotePathsViewHolder(@NonNull View itemView, final RemotesListAdapter adapter){
            super(itemView);
            text = itemView.findViewById(R.id.remote_path);
            icon = itemView.findViewById(R.id.remote_icon);
            itemView.findViewById(R.id.remote_remove).setOnClickListener(v -> adapter.remove(getAdapterPosition()));
        }
    }

    private class RemotesListAdapter extends RecyclerView.Adapter<RemotePathsViewHolder> {

        List<String> remotePaths;

        public RemotesListAdapter(List<String> remotePaths) {
            this.remotePaths = remotePaths;
        }

        @NonNull
        @Override
        public RemotePathsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            View v = LayoutInflater.from(context).inflate(R.layout.fragment_union_item, parent, false);
            RemotePathsViewHolder holder = new RemotePathsViewHolder(v, this);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RemotePathsViewHolder holder, int position) {
            holder.path = remotePaths.get(position);
            holder.text.setText(holder.path);
        }

        @Override
        public int getItemCount() {
            return remotePaths.size();
        }

        public void add(String path){
            remotePaths.add(path);
            notifyDataSetChanged();
        }

        public void remove(int index){
            remotePaths.remove(index);
            notifyDataSetChanged();
        }
    }
}
