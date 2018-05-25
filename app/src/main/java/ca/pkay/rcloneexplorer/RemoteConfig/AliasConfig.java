package ca.pkay.rcloneexplorer.RemoteConfig;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ca.pkay.rcloneexplorer.Dialogs.RemoteDestinationDialog;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import es.dmoral.toasty.Toasty;

public class AliasConfig extends Fragment {

    private final String OUTSTATE_REMOTE_PATH = "ca.pkay.rcexplorer.AliasConfig.REMOTE_PATH";
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
        if (remotePath != null) {
            outState.putString(OUTSTATE_REMOTE_PATH, remotePath);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) {
            return;
        }

        String savedRemotePath = savedInstanceState.getString(OUTSTATE_REMOTE_PATH);
        if (savedRemotePath != null) {
            remotePath = savedRemotePath;
            remote.setText(remotePath);
        }
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
        remoteSelectorTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRemote();
            }
        });

        view.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpRemote();
            }
        });

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });
    }

    private void setRemote() {
        final List<RemoteItem> remotes = rclone.getRemotes();
        if (remotes.isEmpty()) {
            Toasty.info(context, getString(R.string.no_remotes), Toast.LENGTH_SHORT, true).show();
            return;
        }

        String[] options = new String[remotes.size()];
        int i = 0;
        for (RemoteItem remote : remotes) {
            options[i++] = remote.getName();
        }

        AlertDialog.Builder builder;
        if (isDarkTheme) {
            builder = new AlertDialog.Builder(context, R.style.DarkDialogTheme);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        builder.setTitle(R.string.select_remote)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedRemote = null;
                    }
                })
                .setPositiveButton(R.string.select, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedRemote == null) {
                            Toasty.info(context, getString(R.string.nothing_selected), Toast.LENGTH_SHORT, true).show();
                            setRemote();
                        } else {
                            setPath();
                        }
                    }
                })
                .setSingleChoiceItems(options, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedRemote = remotes.get(which);
                    }
                })
                .show();
    }

    private void setPath() {
        RemoteDestinationDialog remoteDestinationDialog = new RemoteDestinationDialog()
                .withContext(context)
                .setDarkTheme(isDarkTheme)
                .setRemote(selectedRemote.getName())
                .setTitle(R.string.select_path_to_alias)
                .setPositiveButtonListener(new RemoteDestinationDialog.OnDestinationSelectedListener() {
                    @Override
                    public void onDestinationSelected(String path) {
                        if (path.equals("//" + selectedRemote.getName())) {
                            remotePath = selectedRemote.getName() + ":";
                        } else {
                            remotePath = selectedRemote.getName() + ":" + path;
                        }
                        remote.setText(remotePath);
                    }
                });
        remoteDestinationDialog.setTargetFragment(this, 0);
        if (getFragmentManager() != null) {
            remoteDestinationDialog.show(getFragmentManager(), "remote destination dialog");
        }
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

        Process process = rclone.configCreate(options);
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (process.exitValue() != 0) {
            Toasty.error(context, getString(R.string.error_creating_remote), Toast.LENGTH_SHORT, true).show();
        } else {
            Toasty.success(context, getString(R.string.remote_creation_success), Toast.LENGTH_SHORT, true).show();
        }
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
