package ca.pkay.rcloneexplorer;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.messaging.FirebaseMessaging;

import ca.pkay.rcloneexplorer.Dialogs.ColorPickerDialog;

public class SettingsActivity extends AppCompatActivity {

    public final static String THEME_CHANGED = "ca.pkay.rcexplorer.SettingsActivity.THEME_CHANGED";
    private final String OUTSTATE_THEME_CHANGE = "ca.pkay.rcexplorer.SettingsActivity.OUTSTATE_THEME_CHANGED";
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
    private boolean isDarkTheme;
    private boolean themeHasChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();
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
    }

    private void setDefaultStates() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isDarkTheme = sharedPreferences.getBoolean(getString(R.string.pref_key_dark_theme), false);
        boolean useLogs = sharedPreferences.getBoolean(getString(R.string.pref_key_logs), false);
        boolean appUpdates = sharedPreferences.getBoolean(getString(R.string.pref_key_app_updates), true);

        darkThemeSwitch.setChecked(isDarkTheme);
        useLogsSwitch.setChecked(useLogs);
        appUpdatesSwitch.setChecked(appUpdates);
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
        useLogsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onUseLogsClicked(isChecked);
            }
        });
    }

    private void showPrimaryColorPicker() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int defaultColor = sharedPreferences.getInt(getString(R.string.pref_key_color_primary), R.color.colorPrimary);

        ColorPickerDialog colorPickerDialog = new ColorPickerDialog()
                .withContext(this)
                .setTitle(R.string.primary_color_picker_title)
                .setColorChoices(R.array.primary_color_choices)
                .setDefaultColor(defaultColor)
                .setDarkTheme(isDarkTheme)
                .setListener(new ColorPickerDialog.OnClickListener() {
                    @Override
                    public void onColorSelected(int color) {
                        onPrimaryColorSelected(color);
                    }
                });

        colorPickerDialog.show(getSupportFragmentManager(), "Primary color picker");
    }

    private void showAccentColorPicker() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int defaultColor = sharedPreferences.getInt(getString(R.string.pref_key_color_accent), R.color.colorAccent);

        ColorPickerDialog colorPickerDialog = new ColorPickerDialog()
                .withContext(this)
                .setTitle(R.string.accent_color_picker_title)
                .setColorChoices(R.array.accent_color_choices)
                .setDefaultColor(defaultColor)
                .setDarkTheme(isDarkTheme)
                .setListener(new ColorPickerDialog.OnClickListener() {
                    @Override
                    public void onColorSelected(int color) {
                        onAccentColorSelected(color);
                    }
                });

        colorPickerDialog.show(getSupportFragmentManager(), "Accent color picker");
    }

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
}
