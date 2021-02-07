package ca.pkay.rcloneexplorer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.view.ContextThemeWrapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;
import androidx.preference.PreferenceManager;

import java.util.Locale;

import ca.pkay.rcloneexplorer.util.FLog;

/**
 * Design goal: Manage runtime resource configuration (locale, theme)
 */
public class RuntimeConfiguration {

    private static final String TAG = "RuntimeConfiguration";

    @NonNull
    public static Context attach(@Nullable Activity host, @NonNull Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        Locale locale = Locale.getDefault();
        if (preferences.contains(context.getString(R.string.pref_key_locale))) {
            String localeTag = preferences.getString(context.getString(R.string.pref_key_locale), "en-US");
            locale = Locale.forLanguageTag(localeTag);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
           context = applyLocale(context, locale, host);
        } else {
            applyLocalePre24(context, locale);
        }

        boolean darkTheme = preferences.getBoolean(context.getString(R.string.pref_key_dark_theme), false);
        int themeRes = darkTheme ? R.style.LightTheme : R.style.DarkTheme;
        return new ContextThemeWrapper(context, themeRes);
    }

    @NonNull
    public static Context attach(@NonNull Context context) {
        return attach(null, context);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @NonNull
    public static Context applyLocale(@NonNull Context context, @NonNull Locale locale, @Nullable Activity host) {
        Configuration conf = context.getResources().getConfiguration();
        Locale.setDefault(locale);
        conf.setLocale(locale);
        if (null != host && shouldOverride()) {
            host.applyOverrideConfiguration(conf);
        }
        return context.createConfigurationContext(conf);
    }

    private static boolean shouldOverride() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1;
    }

    @SuppressWarnings("deprecation")
    @NonNull
    public static Context applyLocalePre24(@NonNull Context context, @NonNull Locale locale) {
        Resources res = context.getResources();
        Configuration conf = res.getConfiguration();
        Locale.setDefault(locale);
        conf.locale = locale;
        res.updateConfiguration(conf, res.getDisplayMetrics());
        return context;
    }

    public static Context applyTheme(@NonNull Context context, boolean darkTheme) {
        int themeRes = R.style.LightTheme;
        if (darkTheme) {
            themeRes = R.style.DarkTheme;
        }
        return new ContextThemeWrapper(context, themeRes);
    }

    @VisibleForTesting
    static class ContextConfigurationWrapper extends ContextThemeWrapper {

        public static ContextThemeWrapper wrap (Context context, int themeResId, Locale locale) {
            Configuration conf = context.getResources().getConfiguration();
            conf.setLocale(locale);
            // API 24+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                LocaleList localeList = new LocaleList(locale);
                LocaleList.setDefault(localeList);
                conf.setLocales(localeList);
            }
            context = context.createConfigurationContext(conf);
            return new ContextThemeWrapper(context, themeResId);
        }
    }
}
