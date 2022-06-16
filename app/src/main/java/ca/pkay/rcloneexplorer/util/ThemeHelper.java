package ca.pkay.rcloneexplorer.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.util.TypedValue;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import ca.pkay.rcloneexplorer.ActivityHelper;
import ca.pkay.rcloneexplorer.R;

/**
 * Copyright (C) 2021  Felix Nüsse
 * Created on 13.06.21 - 22:43
 *
 * Edited by: Felix Nüsse felix.nuesse(at)t-online.de
 */

public class ThemeHelper {


    public static final int FOLLOW_SYSTEM = 0;
    public static final int DARK = 1;
    public static final int LIGHT = 2;


    public static void applyTheme(Activity activity) {
        //ActivityHelper.applyTheme(activity);
        TypedValue typedValue = new TypedValue();
        activity.getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        activity.getWindow().setStatusBarColor(typedValue.data);
    }

    public static boolean isDarkTheme(Context context) {
        int mode = PreferenceManager.getDefaultSharedPreferences(context).getInt(context.getString(R.string.pref_key_dark_theme), FOLLOW_SYSTEM);
        switch(mode) {
            case LIGHT:
                return false;
            case DARK:
                return true;
            case FOLLOW_SYSTEM:
            default:
                Configuration config = context.getApplicationContext().getResources().getConfiguration();
                int currentNightMode = config.uiMode & Configuration.UI_MODE_NIGHT_MASK;
                switch (currentNightMode) {
                    case Configuration.UI_MODE_NIGHT_NO:
                        return false;
                    case Configuration.UI_MODE_NIGHT_YES:
                        return true;
                }
        }
        return true;
    }

    public static void applyDarkMode(Activity activity) {
        int mode = PreferenceManager.getDefaultSharedPreferences(activity).getInt(activity.getString(R.string.pref_key_dark_theme), FOLLOW_SYSTEM);
        switch(mode) {
            case LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            case DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            case FOLLOW_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }
}
