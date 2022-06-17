package ca.pkay.rcloneexplorer.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import ca.pkay.rcloneexplorer.CustomColorHelper;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.util.FLog;
import ca.pkay.rcloneexplorer.util.ThemeHelper;
import es.dmoral.toasty.Toasty;

public class ActivityHelper {

    private static final String TAG = "ActivityHelper";

    @SuppressLint("CheckResult")
    public static void tryStartActivity(Context context, Intent intent) {
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            showErrorToast(context, intent);
        }
    }

    @SuppressLint("CheckResult")
    public static void tryStartActivityForResult(Fragment fragment, Intent intent, int requestCode) {
        try {
            if (!fragment.isAdded()) {
                FLog.w(TAG, "tryStartActivityForResult: fragment is not attached, can't start");
                return;
            }
            fragment.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            showErrorToast(fragment.getContext(), intent);
        }
    }

    public static void tryStartActivityForResult(Activity activity, Intent intent, int requestCode) {
        try {
            activity.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            showErrorToast(activity, intent);
        }
    }

    private static boolean requiresDocumentsUI(Intent intent) {
        if (null == intent || null == intent.getAction()) {
            return false;
        }
        switch (intent.getAction()) {
            case Intent.ACTION_GET_CONTENT:
            case Intent.ACTION_CREATE_DOCUMENT:
            case Intent.ACTION_OPEN_DOCUMENT:
            case Intent.ACTION_OPEN_DOCUMENT_TREE:
                return true;
            default:
                return false;
        }
    }

    private static void showErrorToast(final Context context, Intent intent) {
        Looper main = Looper.getMainLooper();
        String errorMessage = requiresDocumentsUI(intent) ?
                context.getString(R.string.documentsui_missing) :
                context.getString(R.string.no_app_found_for_this_link);
        Toast errorToast = Toasty.error(context, errorMessage, Toast.LENGTH_LONG, true);
        if (main.equals(Looper.myLooper())) {
            errorToast.show();
        } else {
            new Handler(main).post(errorToast::show);
        }
    }

    public static void tryStartService(@NonNull Context context, @NonNull Intent intent) {
        try {
            context.startService(intent);
        } catch (IllegalStateException e) {
            FLog.e(TAG, "Host context state is invalid, not starting service", e);
        }
    }

    public static void applyTheme(Activity activity) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        int customPrimaryColor = sharedPreferences.getInt(activity.getString(R.string.pref_key_color_primary), R.color.colorPrimary);
        int customAccentColor = sharedPreferences.getInt(activity.getString(R.string.pref_key_color_accent), R.color.colorAccent);
        activity.getTheme().applyStyle(CustomColorHelper.getPrimaryColorTheme(activity, customPrimaryColor), true);
        activity.getTheme().applyStyle(CustomColorHelper.getAccentColorTheme(activity, customAccentColor), true);
        ThemeHelper.applyDarkMode(activity);
    }
}
