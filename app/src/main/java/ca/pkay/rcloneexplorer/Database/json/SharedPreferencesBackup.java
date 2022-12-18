package ca.pkay.rcloneexplorer.Database.json;

import static ca.pkay.rcloneexplorer.util.ActivityHelper.DARK;
import static ca.pkay.rcloneexplorer.util.ActivityHelper.FOLLOW_SYSTEM;
import static ca.pkay.rcloneexplorer.util.ActivityHelper.LIGHT;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import ca.pkay.rcloneexplorer.R;

public class SharedPreferencesBackup {

    public static String export(Context context) throws JSONException {

        //General Settings
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean showThumbnails = sharedPreferences.getBoolean(context.getString(R.string.pref_key_show_thumbnails), false);
        boolean isWifiOnly = sharedPreferences.getBoolean(context.getString(R.string.pref_key_wifi_only_transfers), false);
        boolean allowWhileIdle = sharedPreferences.getBoolean(context.getString(R.string.shared_preferences_allow_sync_trigger_while_idle), false);
        boolean useProxy = sharedPreferences.getBoolean(context.getString(R.string.pref_key_use_proxy), false);
        String proxyProtocol = sharedPreferences.getString(context.getString(R.string.pref_key_proxy_protocol), "http");
        String proxyHost = sharedPreferences.getString(context.getString(R.string.pref_key_proxy_host), "localhost");
        int proxyPort = sharedPreferences.getInt(context.getString(R.string.pref_key_proxy_port), 8080);

        // File Access
        boolean safEnabled = sharedPreferences.getBoolean(context.getString(R.string.pref_key_enable_saf), false);
        boolean refreshLaEnabled = sharedPreferences.getBoolean(context.getString(R.string.pref_key_refresh_local_aliases), true);
        boolean vcpEnabled = sharedPreferences.getBoolean(context.getString(R.string.pref_key_enable_vcp), false);
        boolean vcpDeclareLocal = sharedPreferences.getBoolean(context.getString(R.string.pref_key_vcp_declare_local), true);
        boolean vcpGrantAll = sharedPreferences.getBoolean(context.getString(R.string.pref_key_vcp_grant_all), false);

        // Look and Feel
        int darkTheme = sharedPreferences.getInt(context.getString(R.string.pref_key_dark_theme), FOLLOW_SYSTEM);
        boolean isWrapFilenames = sharedPreferences.getBoolean(context.getString(R.string.pref_key_wrap_filenames), true);

        // Notifications
        boolean appUpdates = sharedPreferences.getBoolean(context.getString(R.string.pref_key_app_updates), true);
        boolean betaUpdates = sharedPreferences.getBoolean(context.getString(R.string.pref_key_app_updates_beta), false);

        // Logging
        boolean useLogs = sharedPreferences.getBoolean(context.getString(R.string.pref_key_logs), false);
        boolean crashReports = sharedPreferences.getBoolean(context.getString(R.string.pref_key_crash_reports),
                context.getResources().getBoolean(R.bool.default_crash_log_enable));


        JSONObject main = new JSONObject();

        main.put("showThumbnails", showThumbnails);
        main.put("isWifiOnly", isWifiOnly);
        main.put("allowWhileIdle", allowWhileIdle);
        main.put("useProxy", useProxy);
        main.put("proxyProtocol", proxyProtocol);
        main.put("proxyHost", proxyHost);
        main.put("proxyPort", proxyPort);
        main.put("safEnabled", safEnabled);
        main.put("refreshLaEnabled", refreshLaEnabled);
        main.put("vcpEnabled", vcpEnabled);
        main.put("vcpDeclareLocal", vcpDeclareLocal);
        main.put("vcpGrantAll", vcpGrantAll);
        main.put("isDarkTheme", darkTheme);
        main.put("isWrapFilenames", isWrapFilenames);
        main.put("appUpdates", appUpdates);
        main.put("betaUpdates", betaUpdates);
        main.put("useLogs", useLogs);
        main.put("crashReports", crashReports);

        return main.toString();
    }

    public static void importJson(String json, Context context) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //General Settings
        editor.putBoolean(context.getString(R.string.pref_key_app_updates_beta), jsonObject.getBoolean("showThumbnails"));
        editor.putBoolean(context.getString(R.string.pref_key_wifi_only_transfers), jsonObject.getBoolean("isWifiOnly"));
        editor.putBoolean(context.getString(R.string.pref_key_use_proxy), jsonObject.getBoolean("useProxy"));
        editor.putBoolean(context.getString(R.string.shared_preferences_allow_sync_trigger_while_idle), jsonObject.optBoolean("allowWhileIdle", false));
        editor.putString(context.getString(R.string.pref_key_proxy_protocol), jsonObject.getString("proxyProtocol"));
        editor.putString(context.getString(R.string.pref_key_proxy_host), jsonObject.getString("proxyHost"));
        editor.putInt(context.getString(R.string.pref_key_proxy_port), jsonObject.getInt("proxyPort"));

        // File Access
        editor.putBoolean(context.getString(R.string.pref_key_enable_saf), jsonObject.getBoolean("safEnabled"));
        editor.putBoolean(context.getString(R.string.pref_key_refresh_local_aliases), jsonObject.getBoolean("refreshLaEnabled"));
        editor.putBoolean(context.getString(R.string.pref_key_enable_vcp), jsonObject.getBoolean("vcpEnabled"));
        editor.putBoolean(context.getString(R.string.pref_key_vcp_declare_local), jsonObject.getBoolean("vcpDeclareLocal"));
        editor.putBoolean(context.getString(R.string.pref_key_vcp_grant_all), jsonObject.getBoolean("vcpGrantAll"));

        // Look and Feel
        // The type changed. So we try to use boolean first, and if it fails we use the proper int
        Object darkTheme = jsonObject.get("isDarkTheme");
        int valueForTheme;
        if (darkTheme instanceof Integer) {
            valueForTheme = (Integer) darkTheme;
        } else {
           if((boolean) darkTheme) {
               valueForTheme = DARK;
           } else {
               valueForTheme = LIGHT;
           }
            editor.putInt(context.getString(R.string.pref_key_dark_theme), valueForTheme);
        }

        editor.putInt(context.getString(R.string.pref_key_dark_theme), valueForTheme);

        editor.putBoolean(context.getString(R.string.pref_key_wrap_filenames), jsonObject.getBoolean("isWrapFilenames"));

        // Notifications
        editor.putBoolean(context.getString(R.string.pref_key_app_updates), jsonObject.getBoolean("appUpdates"));
        editor.putBoolean(context.getString(R.string.pref_key_app_updates_beta), jsonObject.getBoolean("betaUpdates"));

        // Logging
        editor.putBoolean(context.getString(R.string.pref_key_logs), jsonObject.getBoolean("useLogs"));
        editor.putBoolean(context.getString(R.string.pref_key_crash_reports), jsonObject.getBoolean("crashReports"));

        editor.apply();
    }

}
