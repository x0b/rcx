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
import java.util.List;

import ca.pkay.rcloneexplorer.Dialogs.NumberPickerDialog;
import ca.pkay.rcloneexplorer.Dialogs.RemoteDestinationDialog;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import es.dmoral.toasty.Toasty;

public class CacheConfig extends Fragment implements    NumberPickerDialog.OnValueSelected,
                                                        RemoteDestinationDialog.OnDestinationSelectedListener {

    private final String SAVED_REMOTE_PATH = "ca.pkay.rcexplorer.CacheConfig.REMOTE_PATH";
    private final String SAVED_CHUNK_SIZE = "ca.pkay.rcexplorer.CacheConfig.CHUNK_SIZE";
    private final String SAVED_CACHE_EXPIRY = "ca.pkay.rcexplorer.CacheConfig.CACHE_EXPIRY";
    private final String SAVED_CACHE_SIZE = "ca.pkay.rcexplorer.CacheConfig.CACHE_SIZE";
    private final String SAVED_SELECTED_REMOTE = "ca.pkay.rcexplorer.CacheConfig.SELECTED_REMOTE";
    private final String CHUNK_SIZE_TAG = "chunk size";
    private final String CACHE_EXPIRY_TAG = "cache expiry";
    private final String CACHE_SIZE_TAG = "cache size";
    private Context context;
    private Rclone rclone;
    private TextInputLayout remoteNameInputLayout;
    private EditText remoteName;
    private TextView remote;
    private TextView chunkSize;
    private TextView infoAge;
    private TextView chunkTotalSize;
    private View remoteSelectorLine;
    private RemoteItem selectedRemote;
    private String remotePath;
    private String chunkSizeString;
    private String infoAgeString;
    private String chunkTotalSizeString;
    private int defaultValueChunkSize;
    private int defaultValueCacheAge;
    private int defaultValueCacheSize;
    private boolean isDarkTheme;

    public CacheConfig() {
        defaultValueChunkSize = 1;
        defaultValueCacheAge = 1;
        defaultValueCacheSize = 1;
    }

    public static CacheConfig newInstance() { return new CacheConfig(); }

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
        if (chunkSizeString != null) {
            outState.putString(SAVED_CHUNK_SIZE, chunkSizeString);
        }
        if (infoAgeString != null) {
            outState.putString(SAVED_CACHE_EXPIRY, infoAgeString);
        }
        if (chunkTotalSizeString != null) {
            outState.putString(SAVED_CACHE_SIZE, chunkTotalSizeString);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) {
            return;
        }

        selectedRemote = savedInstanceState.getParcelable(SAVED_SELECTED_REMOTE);

        String savedRemotePath = savedInstanceState.getString(SAVED_REMOTE_PATH);
        if (savedRemotePath != null) {
            remotePath = savedRemotePath;
            remote.setText(remotePath);
        }

        String savedChunkSize = savedInstanceState.getString(SAVED_CHUNK_SIZE);
        if (savedChunkSize != null) {
            chunkSizeString = savedChunkSize;
            chunkSize.setText(getString(R.string.selected_chunk_size, chunkSizeString));
        }

        String savedInfoAge = savedInstanceState.getString(SAVED_CACHE_EXPIRY);
        if (savedInfoAge != null) {
            infoAgeString = savedInfoAge;
            infoAge.setText(getString(R.string.selected_cache_expiry, infoAgeString));
        }

        String savedChunkTotalSize = savedInstanceState.getString(SAVED_CACHE_SIZE);
        if (savedChunkTotalSize != null) {
            chunkTotalSizeString = savedChunkTotalSize;
            chunkTotalSize.setText(getString(R.string.selected_cache_size, chunkTotalSizeString));
        }
    }

    private void setUpForm(View view) {
        View content = view.findViewById(R.id.form_content);
        int padding = getResources().getDimensionPixelOffset(R.dimen.config_form_template);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, padding);

        remoteNameInputLayout = view.findViewById(R.id.remote_name_layout);
        remoteNameInputLayout.setVisibility(View.VISIBLE);
        remoteName = view.findViewById(R.id.remote_name);

        View remoteSelectorTemplate = View.inflate(context, R.layout.config_form_template_text_field, null);
        remoteSelectorTemplate.setLayoutParams(params);
        ((ViewGroup) content).addView(remoteSelectorTemplate);

        remoteSelectorLine = view.findViewById(R.id.text_view_line);
        remote = remoteSelectorTemplate.findViewById(R.id.text_view);
        remote.setText(R.string.alias_remote_hint);
        remoteSelectorTemplate.setOnClickListener(v -> setRemote());

        View chunkSizeTemplate = View.inflate(context, R.layout.config_form_template_text_field, null);
        chunkSizeTemplate.setLayoutParams(params);
        ((ViewGroup) content).addView(chunkSizeTemplate);

        chunkSize = chunkSizeTemplate.findViewById(R.id.text_view);
        chunkSize.setText(R.string.chunk_size_hint);
        chunkSizeTemplate.setOnClickListener(v -> showChunkSizeDialog());

        View infoAgeTemplate = View.inflate(context, R.layout.config_form_template_text_field, null);
        infoAgeTemplate.setLayoutParams(params);
        ((ViewGroup) content).addView(infoAgeTemplate);

        infoAge = infoAgeTemplate.findViewById(R.id.text_view);
        infoAge.setText(R.string.info_age_hint);
        infoAgeTemplate.setOnClickListener(v -> showCacheExpiryDialog());

        View chunkTotalSizeTemplate = View.inflate(context, R.layout.config_form_template_text_field, null);
        chunkTotalSizeTemplate.setLayoutParams(params);
        ((ViewGroup) content).addView(chunkTotalSizeTemplate);

        chunkTotalSize = chunkTotalSizeTemplate.findViewById(R.id.text_view);
        chunkTotalSize.setText(R.string.chunk_total_size_hint);
        chunkTotalSizeTemplate.setOnClickListener(v -> showCacheSizeDialog());

        view.findViewById(R.id.next).setOnClickListener(v -> setUpRemote());

        view.findViewById(R.id.cancel).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }

    private void showChunkSizeDialog() {
        NumberPickerDialog numberPickerDialog = new NumberPickerDialog()
                .setDarkTheme(isDarkTheme)
                .setNumberUnits(NumberPickerDialog.UNITS_STORAGE)
                .setTitle(R.string.chunk_size)
                .setDefaultValue(defaultValueChunkSize);
        if (getFragmentManager() != null) {
            numberPickerDialog.show(getChildFragmentManager(), CHUNK_SIZE_TAG);
        }
    }

    private void setChunkSize(int size, int units) {
        defaultValueChunkSize = size;
        chunkSizeString = String.valueOf(size);

        switch (units) {
            case NumberPickerDialog.UNITS_MB:
                chunkSizeString += "M";
                break;
            case NumberPickerDialog.UNITS_GB:
                chunkSizeString += "G";
                break;
        }

        chunkSize.setText(getString(R.string.selected_chunk_size, chunkSizeString));
    }

    private void showCacheExpiryDialog() {
        NumberPickerDialog numberPickerDialog = new NumberPickerDialog()
                .setDarkTheme(isDarkTheme)
                .setNumberUnits(NumberPickerDialog.UNITS_TIME)
                .setTitle(R.string.cache_expiry)
                .setDefaultValue(defaultValueCacheAge);
        if (getFragmentManager() != null) {
            numberPickerDialog.show(getChildFragmentManager(), CACHE_EXPIRY_TAG);
        }
    }

    private void setCacheExpiry(int time, int units) {
        defaultValueCacheAge = time;
        infoAgeString = String.valueOf(time);

        switch (units) {
            case NumberPickerDialog.UNITS_S:
                infoAgeString += "s";
                break;
            case NumberPickerDialog.UNITS_M:
                infoAgeString += "m";
                break;
            case NumberPickerDialog.UNITS_H:
                infoAgeString += "h";
                break;
        }

        infoAge.setText(getString(R.string.selected_cache_expiry, infoAgeString));
    }

    private void showCacheSizeDialog() {
        NumberPickerDialog numberPickerDialog = new NumberPickerDialog()
                .setDarkTheme(isDarkTheme)
                .setNumberUnits(NumberPickerDialog.UNITS_STORAGE)
                .setTitle(R.string.cache_size)
                .setDefaultValue(defaultValueCacheSize);
        if (getFragmentManager() != null) {
            numberPickerDialog.show(getChildFragmentManager(), CACHE_SIZE_TAG);
        }
    }

    private void setCacheSize(int size, int units) {
        defaultValueCacheSize = size;
        chunkTotalSizeString = String.valueOf(size);

        switch (units) {
            case NumberPickerDialog.UNITS_MB:
                chunkTotalSizeString += "M";
                break;
            case NumberPickerDialog.UNITS_GB:
                chunkTotalSizeString += "G";
                break;
        }

        chunkTotalSize.setText(getString(R.string.selected_cache_size, chunkTotalSizeString));
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
        options.add("cache");
        options.add("remote");
        options.add(remotePath);
        if (chunkSizeString != null && !chunkSizeString.trim().isEmpty()) {
            options.add("chunk_size");
            options.add(chunkSizeString);
        }
        if (infoAgeString != null && !infoAgeString.trim().isEmpty()) {
            options.add("info_age");
            options.add(infoAgeString);
        }
        if (chunkTotalSizeString != null && !chunkTotalSizeString.trim().isEmpty()) {
            options.add("chunk_total_size");
            options.add(chunkTotalSizeString);
        }

        RemoteConfigHelper.setupAndWait(context, options);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onValueSelected(String tag, int number, int units) {
        switch (tag) {
            case CHUNK_SIZE_TAG:
                setChunkSize(number, units);
                break;
            case CACHE_EXPIRY_TAG:
                setCacheExpiry(number, units);
                break;
            case CACHE_SIZE_TAG:
                setCacheSize(number, units);
                break;
        }
    }
}
