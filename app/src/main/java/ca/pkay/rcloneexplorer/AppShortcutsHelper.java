package ca.pkay.rcloneexplorer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.pkay.rcloneexplorer.Items.RemoteItem;

public class AppShortcutsHelper {

    public static final String APP_SHORTCUT_REMOTE_NAME = "arg_remote_name";

    public static void populateAppShortcuts(Context context, List<RemoteItem> remotes) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1) {
            return;
        }

        ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
        if (shortcutManager == null) {
            return;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Set<String> shortcutSet = new HashSet<>();
        List<ShortcutInfo> shortcutInfoList = new ArrayList<>();

        RemoteItem.prepareDisplay(context, remotes);
        for (RemoteItem remoteItem : remotes) {
            String id = getUniqueIdFromString(remoteItem.getName());

            Intent intent = new Intent(Intent.ACTION_MAIN, Uri.EMPTY, context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(APP_SHORTCUT_REMOTE_NAME, remoteItem.getName());

            ShortcutInfo shortcut = new ShortcutInfo.Builder(context, id)
                    .setShortLabel(remoteItem.getDisplayName())
                    .setIcon(Icon.createWithResource(context, AppShortcutsHelper.getRemoteIcon(remoteItem.getType(), remoteItem.isCrypt())))
                    .setIntent(intent)
                    .build();

            shortcutInfoList.add(shortcut);
            shortcutSet.add(id);

            if (shortcutInfoList.size() >= 4) {
                break;
            }
        }

        shortcutManager.setDynamicShortcuts(shortcutInfoList);
        editor.putStringSet(context.getString(R.string.shared_preferences_app_shortcuts), shortcutSet);
        editor.apply();
    }

    public static void removeAllAppShortcuts(Context context) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1) {
            return;
        }

        ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
        if (shortcutManager == null) {
            return;
        }

        shortcutManager.removeAllDynamicShortcuts();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(context.getString(R.string.shared_preferences_app_shortcuts));
        editor.apply();
    }

    public static void removeAppShortcut(Context context, String remoteName) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1) {
            return;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> appShortcutIds = sharedPreferences.getStringSet(context.getString(R.string.shared_preferences_app_shortcuts), new HashSet<>());
        String id = getUniqueIdFromString(remoteName);

        if (!appShortcutIds.contains(id)) {
            return;
        }

        ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
        if (shortcutManager == null) {
            return;
        }

        List<ShortcutInfo> shortcutInfoList = shortcutManager.getDynamicShortcuts();
        for (ShortcutInfo shortcutInfo : shortcutInfoList) {
            if (shortcutInfo.getId().equals(id)) {
                shortcutManager.removeDynamicShortcuts(Collections.singletonList(shortcutInfo.getId()));
                return;
            }
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        appShortcutIds.remove(id);
        Set<String> updateAppShortcutIds = new HashSet<>(appShortcutIds);
        editor.putStringSet(context.getString(R.string.shared_preferences_app_shortcuts), updateAppShortcutIds);
        editor.apply();
    }

    public static void removeAppShortcutIds(Context context, List<String> ids) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1) {
            return;
        }

        ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
        if (shortcutManager == null) {
            return;
        }

        shortcutManager.removeDynamicShortcuts(ids);
    }

    public static void reportAppShortcutUsage(Context context, String remoteName) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1) {
            return;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> appShortcutIds = sharedPreferences.getStringSet(context.getString(R.string.shared_preferences_app_shortcuts), new HashSet<>());

        String id = getUniqueIdFromString(remoteName);

        if (appShortcutIds.contains(id)) {
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
            if (shortcutManager == null) {
                return;
            }
            shortcutManager.reportShortcutUsed(id);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    public static void addRemoteToAppShortcuts(Context context, RemoteItem remoteItem, String id) {
        ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
        if (shortcutManager == null) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_MAIN, Uri.EMPTY, context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(APP_SHORTCUT_REMOTE_NAME, remoteItem.getName());

        ShortcutInfo shortcut = new ShortcutInfo.Builder(context, id)
                .setShortLabel(remoteItem.getDisplayName())
                .setIcon(Icon.createWithResource(context, AppShortcutsHelper.getRemoteIcon(remoteItem.getType(), remoteItem.isCrypt())))
                .setIntent(intent)
                .build();

        shortcutManager.addDynamicShortcuts(Collections.singletonList(shortcut));
    }

    public static boolean isRequestPinShortcutSupported(Context context) {
        return ShortcutManagerCompat.isRequestPinShortcutSupported(context);
    }

    public static void addRemoteToHomeScreen(Context context, RemoteItem remoteItem) {
        String id = getUniqueIdFromString(remoteItem.getName());

        Intent intent = new Intent(Intent.ACTION_MAIN, Uri.EMPTY, context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(APP_SHORTCUT_REMOTE_NAME, remoteItem.getName());

        ShortcutInfoCompat shortcutInfo = new ShortcutInfoCompat.Builder(context, id)
                .setShortLabel(remoteItem.getDisplayName())
                .setIcon(IconCompat.createWithResource(context, AppShortcutsHelper.getRemoteIcon(remoteItem.getType(), remoteItem.isCrypt())))
                .setIntent(intent)
                .build();

        ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null);
    }

    public static String getUniqueIdFromString(String s) {
        StringBuilder hash = new StringBuilder();
        char[] chars = s.toCharArray();

        for (char c : chars) {
            int ascii = (int) c;
            hash.append(ascii);
            hash.append(".");
        }

        return hash.toString();
    }

    private static int getRemoteIcon(int remoteType, boolean isCrypt) {
        if (isCrypt) {
            return R.mipmap.ic_shortcut_lock;
        }
        switch (remoteType) {
            case RemoteItem.AMAZON_DRIVE:
                return R.mipmap.ic_shortcut_amazon;
            case RemoteItem.GOOGLE_DRIVE:
                return R.mipmap.ic_shortcut_drive;
            case RemoteItem.DROPBOX:
                return R.mipmap.ic_shortcut_dropbox;
            case RemoteItem.GOOGLE_CLOUD_STORAGE:
                return R.mipmap.ic_shortcut_google;
            case RemoteItem.ONEDRIVE:
                return R.mipmap.ic_shortcut_onedrive;
            case RemoteItem.S3:
                return R.mipmap.ic_shortcut_amazon;
            case RemoteItem.BOX:
                return R.mipmap.ic_shortcut_box;
            case RemoteItem.SFTP:
                return R.mipmap.ic_shortcut_terminal;
            case RemoteItem.LOCAL:
                return R.mipmap.ic_shortcut_local;
            default:
                return R.mipmap.ic_shortcut_cloud;
        }
    }
}
