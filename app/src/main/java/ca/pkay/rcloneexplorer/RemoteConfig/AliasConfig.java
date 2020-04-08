package ca.pkay.rcloneexplorer.RemoteConfig;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.pkay.rcloneexplorer.Dialogs.RemoteDestinationDialog;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import es.dmoral.toasty.Toasty;

public class AliasConfig extends Fragment implements RemoteDestinationDialog.OnDestinationSelectedListener {

    private final String SAVED_REMOTE_PATH = "ca.pkay.rcexplorer.AliasConfig.REMOTE_PATH";
    private final String SAVED_SELECTED_REMOTE = "ca.pkay.rcexplorer.AliasConfig.SELECTED_REMOTE";
    private Context context;
    private Rclone rclone;
    private TextInputLayout remoteNameInputLayout;
    private EditText remoteName;
    private TextView remote;
    private View remoteSelectorLine;
    private RemoteItem selectedRemote;
    private String remotePath;
    private boolean isDarkTheme;

    public AliasConfig() {}

    public static AliasConfig newInstance() { return new AliasConfig(); }

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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.remote_config_form, container, false);
        setUpForm(view);
        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (selectedRemote != null) {
            outState.putParcelable(SAVED_SELECTED_REMOTE, selectedRemote);
        }
        if (remotePath != null) {
            outState.putString(SAVED_REMOTE_PATH, remotePath);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) {
            return;
        }

        String savedRemotePath = savedInstanceState.getString(SAVED_REMOTE_PATH);
        if (savedRemotePath != null) {
            remotePath = savedRemotePath;
            remote.setText(remotePath);
        }
        selectedRemote = savedInstanceState.getParcelable(SAVED_SELECTED_REMOTE);
    }

    private void setUpForm(View view) {
        View content = view.findViewById(R.id.form_content);
        int padding = getResources().getDimensionPixelOffset(R.dimen.config_form_template);
        remoteNameInputLayout = view.findViewById(R.id.remote_name_layout);
        remoteNameInputLayout.setVisibility(View.VISIBLE);
        remoteName = view.findViewById(R.id.remote_name);

        View remoteSelectorTemplate = View.inflate(context, R.layout.config_form_template_text_field, null);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, padding);
        remoteSelectorTemplate.setLayoutParams(params);
        ((ViewGroup) content).addView(remoteSelectorTemplate);

        remoteSelectorLine = view.findViewById(R.id.text_view_line);
        remote = view.findViewById(R.id.text_view);
        remote.setText(R.string.alias_remote_hint);
        remoteSelectorTemplate.setOnClickListener(v -> setRemote());

        view.findViewById(R.id.next).setOnClickListener(v -> setUpRemote());

        view.findViewById(R.id.cancel).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }

    private void setRemote() {
        final List<RemoteItem> remotes = rclone.getRemotes();
        if (remotes.isEmpty()) {
            Toasty.info(context, getString(R.string.no_remotes), Toast.LENGTH_SHORT, true).show();
            return;
        }

        RemoteItem.prepareDisplay(context, remotes);
        Collections.sort(remotes, (a, b) -> a.getDisplayName().compareTo(b.getDisplayName()));
        String[] options = new String[remotes.size()];
        int i = 0;
        for (RemoteItem remote : remotes) {
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
                        setRemote();
                    } else {
                        setPath();
                    }
                })
                .setSingleChoiceItems(options, -1, (dialog, which) -> selectedRemote = remotes.get(which))
                .show();
    }

    private void setPath() {
        RemoteDestinationDialog remoteDestinationDialog = new RemoteDestinationDialog()
                .setDarkTheme(isDarkTheme)
                .setRemote(selectedRemote)
                .setTitle(R.string.select_path_to_alias);
        remoteDestinationDialog.show(getChildFragmentManager(), "remote destination dialog");
    }

    /*
     * RemoteDestinationDialog callback
     */
    @Override
    public void onDestinationSelected(String path) {
        remotePath = RemoteConfigHelper.getRemotePath(path, selectedRemote);
        remote.setText(remotePath);
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
        if (remotePath == null) {
            remoteSelectorLine.setBackgroundColor(Color.parseColor("#B14525"));
            error = true;
        }
        if (error) {
            return;
        }

        ArrayList<String> options = new ArrayList<>();
        options.add(name);
        options.add("alias");
        options.add("remote");
        options.add(remotePath);

        RemoteConfigHelper.setupAndWait(context, options);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
