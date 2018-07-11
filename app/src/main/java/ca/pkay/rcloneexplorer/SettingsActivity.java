package ca.pkay.rcloneexplorer;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.pkay.rcloneexplorer.Dialogs.ColorPickerDialog;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import es.dmoral.toasty.Toasty;

public class SettingsActivity extends AppCompatActivity {

    public final static String THEME_CHANGED = "ca.pkay.rcexplorer.SettingsActivity.THEME_CHANGED";
    private final String OUTSTATE_THEME_CHANGE = "ca.pkay.rcexplorer.SettingsActivity.OUTSTATE_THEME_CHANGED";
    private Context context;
    private View primaryColorElement;
    private ImageView primaryColorPreview;
    private View accentColorElement;
    private ImageView accentColorPreview;
    private Switch darkThemeSwitch;
    private View darkThemeElement;
    private View notificationsElement;
    private View appUpdatesElement;
    private Switch useLogsSwitch;
    private View useLogsElement;
    private Switch appUpdatesSwitch;
    private View crashReportsElement;
    private Switch crashReportsSwitch;
    private View showThumbnailsElement;
    private Switch showThumbnailsSwitch;
    private View appShortcutsElement;
    private boolean isDarkTheme;
    private boolean themeHasChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();
        context = this;
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        themeHasChanged = savedInstanceState != null && savedInstanceState.getBoolean(OUTSTATE_THEME_CHANGE, false);
        Intent returnData = new Intent();
        returnData.putExtra(THEME_CHANGED, themeHasChanged);
        setResult(RESULT_OK, returnData);

        getViews();
        setDefaultStates();
        setClickListeners();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(OUTSTATE_THEME_CHANGE, themeHasChanged);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getFragmentManager() != null) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("primary color picker");
            if (fragment != null && fragment instanceof ColorPickerDialog) {
                ((ColorPickerDialog)fragment).setListener(primaryColorPickerListener);
                return;
            }

            fragment = getSupportFragmentManager().findFragmentByTag("accent color picker");
            if (fragment != null && fragment instanceof ColorPickerDialog) {
                ((ColorPickerDialog)fragment).setListener(accentColorPickerListener);
            }
        }
    }

    private void applyTheme() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int customPrimaryColor = sharedPreferences.getInt(getString(R.string.pref_key_color_primary), -1);
        int customAccentColor = sharedPreferences.getInt(getString(R.string.pref_key_color_accent), -1);
        isDarkTheme = sharedPreferences.getBoolean(getString(R.string.pref_key_dark_theme), false);
        getTheme().applyStyle(CustomColorHelper.getPrimaryColorTheme(this, customPrimaryColor), true);
        getTheme().applyStyle(CustomColorHelper.getAccentColorTheme(this, customAccentColor), true);
        if (isDarkTheme) {
            getTheme().applyStyle(R.style.DarkTheme, true);
        } else {
            getTheme().applyStyle(R.style.LightTheme, true);
        }

        // set recents app color to the primary color
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round);
        ActivityManager.TaskDescription taskDesc = new ActivityManager.TaskDescription(getString(R.string.app_name), bm, customPrimaryColor);
        setTaskDescription(taskDesc);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void getViews() {
        primaryColorElement = findViewById(R.id.primary_color);
        primaryColorPreview = findViewById(R.id.primary_color_preview);
        accentColorElement = findViewById(R.id.accent_color);
        accentColorPreview = findViewById(R.id.accent_color_preview);
        darkThemeSwitch = findViewById(R.id.dark_theme_switch);
        darkThemeElement = findViewById(R.id.dark_theme);
        notificationsElement = findViewById(R.id.notifications);
        appUpdatesElement = findViewById(R.id.app_updates);
        appUpdatesSwitch = findViewById(R.id.app_updates_switch);
        useLogsSwitch = findViewById(R.id.use_logs_switch);
        useLogsElement = findViewById(R.id.use_logs);
        crashReportsElement = findViewById(R.id.crash_reporting);
        crashReportsSwitch = findViewById(R.id.crash_reporting_switch);
        showThumbnailsElement = findViewById(R.id.show_thumbnails);
        showThumbnailsSwitch = findViewById(R.id.show_thumbnails_switch);
        appShortcutsElement = findViewById(R.id.app_shortcuts);
    }

    private void setDefaultStates() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isDarkTheme = sharedPreferences.getBoolean(getString(R.string.pref_key_dark_theme), false);
        boolean useLogs = sharedPreferences.getBoolean(getString(R.string.pref_key_logs), false);
        boolean appUpdates = sharedPreferences.getBoolean(getString(R.string.pref_key_app_updates), true);
        boolean crashReports = sharedPreferences.getBoolean(getString(R.string.pref_key_crash_reports), true);
        boolean showThumbnails = sharedPreferences.getBoolean(getString(R.string.pref_key_show_thumbnails), false);

        darkThemeSwitch.setChecked(isDarkTheme);
        useLogsSwitch.setChecked(useLogs);
        appUpdatesSwitch.setChecked(appUpdates);
        crashReportsSwitch.setChecked(crashReports);
        showThumbnailsSwitch.setChecked(showThumbnails);

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1) {
            appShortcutsElement.setVisibility(View.GONE);
            findViewById(R.id.general_label).setVisibility(View.GONE);
            findViewById(R.id.app_shortcuts_divider).setVisibility(View.GONE);
        }
    }

    private void setClickListeners() {
        primaryColorElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPrimaryColorPicker();
            }
        });
        accentColorElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAccentColorPicker();
            }
        });
        darkThemeElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (darkThemeSwitch.isChecked()) {
                    darkThemeSwitch.setChecked(false);
                } else {
                    darkThemeSwitch.setChecked(true);
                }
            }
        });
        darkThemeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onDarkThemeClicked(isChecked);
            }
        });
        notificationsElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNotificationsClicked();
            }
        });
        appUpdatesElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (appUpdatesSwitch.isChecked()) {
                    appUpdatesSwitch.setChecked(false);
                } else {
                    appUpdatesSwitch.setChecked(true);
                }
            }
        });
        appUpdatesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onAppUpdatesClicked(isChecked);
            }
        });
        useLogsElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (useLogsSwitch.isChecked()) {
                    useLogsSwitch.setChecked(false);
                } else {
                    useLogsSwitch.setChecked(true);
                }
            }
        });
        useLogsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onUseLogsClicked(isChecked);
            }
        });
        crashReportsElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (crashReportsSwitch.isChecked()) {
                    crashReportsSwitch.setChecked(false);
                } else {
                    crashReportsSwitch.setChecked(true);
                }
            }
        });
        crashReportsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                crashReportsClicked(isChecked);
            }
        });
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
    }

    private void showPrimaryColorPicker() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int defaultColor = sharedPreferences.getInt(getString(R.string.pref_key_color_primary), R.color.colorPrimary);

        ColorPickerDialog colorPickerDialog = new ColorPickerDialog()
                .setTitle(R.string.primary_color_picker_title)
                .setColorChoices(R.array.primary_color_choices)
                .setDefaultColor(defaultColor)
                .setDarkTheme(isDarkTheme)
                .setListener(primaryColorPickerListener);

        colorPickerDialog.show(getSupportFragmentManager(), "primary color picker");
    }

    private ColorPickerDialog.OnClickListener primaryColorPickerListener = new ColorPickerDialog.OnClickListener() {
        @Override
        public void onColorSelected(int color) {
            onPrimaryColorSelected(color);
        }
    };

    private void showAccentColorPicker() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int defaultColor = sharedPreferences.getInt(getString(R.string.pref_key_color_accent), R.color.colorAccent);

        ColorPickerDialog colorPickerDialog = new ColorPickerDialog()
                .setTitle(R.string.accent_color_picker_title)
                .setColorChoices(R.array.accent_color_choices)
                .setDefaultColor(defaultColor)
                .setDarkTheme(isDarkTheme)
                .setListener(accentColorPickerListener);

        colorPickerDialog.show(getSupportFragmentManager(), "accent color picker");
    }

    private ColorPickerDialog.OnClickListener accentColorPickerListener = new ColorPickerDialog.OnClickListener() {
        @Override
        public void onColorSelected(int color) {
            onAccentColorSelected(color);
        }
    };

    private void onPrimaryColorSelected(int color) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getString(R.string.pref_key_color_primary), color);
        editor.apply();

        primaryColorPreview.setColorFilter(color);

        themeHasChanged = true;
        recreate();
    }

    private void onAccentColorSelected(int color) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getString(R.string.pref_key_color_accent), color);
        editor.apply();

        accentColorPreview.setColorFilter(color);

        themeHasChanged = true;
        recreate();
    }

    private void onDarkThemeClicked(boolean isChecked) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_key_dark_theme), isChecked);
        editor.apply();

        themeHasChanged = true;
        recreate();
    }

    private void onNotificationsClicked() {
        Intent intent = new Intent();
        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");

        //for Android 5-7
        intent.putExtra("app_package", getPackageName());
        intent.putExtra("app_uid", getApplicationInfo().uid);

        // for Android O
        intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());

        startActivity(intent);
    }

    private void onAppUpdatesClicked(boolean isChecked) {
        if (isChecked) {
            FirebaseMessaging.getInstance().subscribeToTopic(getString(R.string.firebase_msg_app_updates_topic));
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(getString(R.string.firebase_msg_app_updates_topic));
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_key_app_updates), isChecked);
        editor.apply();
    }

    private void onUseLogsClicked(boolean isChecked) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_key_logs), isChecked);
        editor.apply();
    }

    private void crashReportsClicked(boolean isChecked) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_key_crash_reports), isChecked);
        editor.apply();

        Toasty.info(this, getString(R.string.restart_required), Toast.LENGTH_SHORT, true).show();
    }

    private void showThumbnails(boolean isChecked) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_key_show_thumbnails), isChecked);
        editor.apply();
    }

    private void showAppShortcutDialog() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1) {
            return;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> appShortcuts = sharedPreferences.getStringSet(getString(R.string.shared_preferences_app_shortcuts), new HashSet<String>());

        AlertDialog.Builder builder;
        if (isDarkTheme) {
            builder = new AlertDialog.Builder(this, R.style.DarkDialogTheme);
        } else {
            builder = new AlertDialog.Builder(this);
        }

        builder.setTitle(R.string.app_shortcuts_settings_dialog_title);

        Rclone rclone = new Rclone(this);
        final ArrayList<RemoteItem> remotes = new ArrayList<>(rclone.getRemotes());
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
                    ((AlertDialog)dialog).getListView().setItemChecked(which, false);
                    return;
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

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> savedAppShortcutIds = sharedPreferences.getStringSet(getString(R.string.shared_preferences_app_shortcuts), new HashSet<String>());
        Set<String> updatedAppShortcutIDds = new HashSet<>(savedAppShortcutIds);

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

            AppShortcutsHelper.addRemoteToAppShortcuts(this, remoteItem, id);
            updatedAppShortcutIDds.add(id);
        }

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

        editor.putStringSet(getString(R.string.shared_preferences_app_shortcuts), updatedAppShortcutIDds);
        editor.apply();
    }
}
