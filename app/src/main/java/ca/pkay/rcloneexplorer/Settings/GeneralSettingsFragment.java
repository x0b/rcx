package ca.pkay.rcloneexplorer.Settings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.pkay.rcloneexplorer.AppShortcutsHelper;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import es.dmoral.toasty.Toasty;

public class GeneralSettingsFragment extends Fragment {

    private Context context;
    private View appShortcutsElement;
    private View showThumbnailsElement;
    private Switch showThumbnailsSwitch;
    private View wifiOnlyElement;
    private Switch wifiOnlySwitch;
    private boolean isDarkTheme;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public GeneralSettingsFragment() {
    }

    public static GeneralSettingsFragment newInstance() {
        return new GeneralSettingsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.general_settings_fragment, container, false);
        getViews(view);
        setDefaultStates();
        setClickListeners();

        if (getActivity() != null) {
            getActivity().setTitle(getString(R.string.pref_header_general));
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.context = null;
    }

    private void getViews(View view) {
        appShortcutsElement = view.findViewById(R.id.app_shortcuts);
        showThumbnailsElement = view.findViewById(R.id.show_thumbnails);
        showThumbnailsSwitch = view.findViewById(R.id.show_thumbnails_switch);
        wifiOnlyElement = view.findViewById(R.id.wifi_only);
        wifiOnlySwitch = view.findViewById(R.id.wifi_only_switch);
    }
    
    private void setDefaultStates() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean showThumbnails = sharedPreferences.getBoolean(getString(R.string.pref_key_show_thumbnails), false);
        boolean isWifiOnly = sharedPreferences.getBoolean(getString(R.string.pref_key_wifi_only_transfers), false);
        isDarkTheme = sharedPreferences.getBoolean(getString(R.string.pref_key_dark_theme), false);

        showThumbnailsSwitch.setChecked(showThumbnails);
        wifiOnlySwitch.setChecked(isWifiOnly);

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1) {
            appShortcutsElement.setVisibility(View.GONE);
        }
    }
    
    private void setClickListeners() {
        showThumbnailsElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showThumbnailsSwitch.isChecked()) {
                    showThumbnailsSwitch.setChecked(false);
                } else {
                    showThumbnailsSwitch.setChecked(true);
                }
            }
        });
        showThumbnailsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showThumbnails(isChecked);
            }
        });
        appShortcutsElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAppShortcutDialog();
            }
        });
        wifiOnlyElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wifiOnlySwitch.isChecked()) {
                    wifiOnlySwitch.setChecked(false);
                } else {
                    wifiOnlySwitch.setChecked(true);
                }
            }
        });
        wifiOnlySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setWifiOnlyTransfers(isChecked);
            }
        });
    }

    private void showThumbnails(boolean isChecked) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_key_show_thumbnails), isChecked);
        editor.apply();
    }

    private void showAppShortcutDialog() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1) {
            return;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> appShortcuts = sharedPreferences.getStringSet(getString(R.string.shared_preferences_app_shortcuts), new HashSet<String>());

        AlertDialog.Builder builder;
        if (isDarkTheme) {
            builder = new AlertDialog.Builder(context, R.style.DarkDialogTheme);
        } else {
            builder = new AlertDialog.Builder(context);
        }

        builder.setTitle(R.string.app_shortcuts_settings_dialog_title);

        Rclone rclone = new Rclone(context);
        final ArrayList<RemoteItem> remotes = new ArrayList<>(rclone.getRemotes());
        Collections.sort(remotes);
        final CharSequence[] options = new CharSequence[remotes.size()];
        int i = 0;
        for (RemoteItem remoteItem : remotes) {
            options[i++] = remoteItem.getName();
        }

        final ArrayList<String> userSelected = new ArrayList<>();
        boolean[] checkedItems = new boolean[options.length];
        i = 0;
        for (CharSequence cs : options) {
            String s = cs.toString();
            String hash = AppShortcutsHelper.getUniqueIdFromString(s);
            if (appShortcuts.contains(hash)) {
                userSelected.add(cs.toString());
                checkedItems[i] = true;
            }
            i++;
        }

        builder.setMultiChoiceItems(options, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (userSelected.size() >= 4 && isChecked) {
                    Toasty.info(context, getString(R.string.app_shortcuts_max_toast), Toast.LENGTH_SHORT, true).show();
                    //((AlertDialog)dialog).getListView().setItemChecked(which, false); This doesn't work
                }
                if (isChecked) {
                    userSelected.add(options[which].toString());
                } else {
                    userSelected.remove(options[which].toString());
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.select, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setAppShortcuts(remotes, userSelected);
            }
        });

        builder.show();
    }

    private void setAppShortcuts(ArrayList<RemoteItem> remoteItems, ArrayList<String> appShortcuts) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1) {
            return;
        }

        if (appShortcuts.size() > 4) {
            appShortcuts = new ArrayList<>(appShortcuts.subList(0, 4));
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> savedAppShortcutIds = sharedPreferences.getStringSet(getString(R.string.shared_preferences_app_shortcuts), new HashSet<String>());
        Set<String> updatedAppShortcutIDds = new HashSet<>(savedAppShortcutIds);

        // Remove app shortcuts first
        ArrayList<String> appShortcutIds= new ArrayList<>();
        for (String s : appShortcuts) {
            appShortcutIds.add(AppShortcutsHelper.getUniqueIdFromString(s));
        }
        List<String> removedIds = new ArrayList<>(savedAppShortcutIds);
        removedIds.removeAll(appShortcutIds);
        if (!removedIds.isEmpty()) {
            AppShortcutsHelper.removeAppShortcutIds(context, removedIds);
        }

        updatedAppShortcutIDds.removeAll(removedIds);

        // add new app shortcuts
        for (String appShortcut : appShortcuts) {
            String id = AppShortcutsHelper.getUniqueIdFromString(appShortcut);
            if (updatedAppShortcutIDds.contains(id)) {
                continue;
            }

            RemoteItem remoteItem = null;
            for (RemoteItem item : remoteItems) {
                if (item.getName().equals(appShortcut)) {
                    remoteItem = item;
                    break;
                }
            }
            if (remoteItem == null) {
                continue;
            }

            AppShortcutsHelper.addRemoteToAppShortcuts(context, remoteItem, id);
            updatedAppShortcutIDds.add(id);
        }

        editor.putStringSet(getString(R.string.shared_preferences_app_shortcuts), updatedAppShortcutIDds);
        editor.apply();
    }

    private void setWifiOnlyTransfers(boolean isChecked) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_key_wifi_only_transfers), isChecked);
        editor.apply();
    }
}
