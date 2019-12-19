package ca.pkay.rcloneexplorer;

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
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import es.dmoral.toasty.Toasty;

public class ActivityHelper {

    @SuppressLint("CheckResult")
    public static void tryStartActivity(Context context, Intent intent) {
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            showErrorToast(context);
        }
    }

    @SuppressLint("CheckResult")
    public static void tryStartActivityForResult(Fragment fragment, Intent intent, int requestCode) {
        try {
            fragment.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            showErrorToast(fragment.getContext());
        }
    }

    public static void tryStartActivityForResult(Activity activity, Intent intent, int requestCode) {
        try {
            activity.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            showErrorToast(activity);
        }
    }

    private static void showErrorToast(final Context context) {
        Looper main = Looper.getMainLooper();
        if(main.equals(Looper.myLooper())){
            Toasty.error(context, "No app found for this link", Toast.LENGTH_LONG, true).show();
        } else {
            new Handler(main).post(new Runnable() {
                @Override
                public void run() {
                    Toasty.error(context, "No app found for this link", Toast.LENGTH_LONG, true).show();
                }
            });
        }
    }

    public static void applyTheme(Activity context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int customPrimaryColor = sharedPreferences.getInt(context.getString(R.string.pref_key_color_primary), -1);
        int customAccentColor = sharedPreferences.getInt(context.getString(R.string.pref_key_color_accent), -1);
        Boolean isDarkTheme = sharedPreferences.getBoolean(context.getString(R.string.pref_key_dark_theme), false);
        context.getTheme().applyStyle(CustomColorHelper.getPrimaryColorTheme(context, customPrimaryColor), true);
        context.getTheme().applyStyle(CustomColorHelper.getAccentColorTheme(context, customAccentColor), true);
        if (isDarkTheme) {
            context.getTheme().applyStyle(R.style.DarkTheme, true);
        } else {
            context.getTheme().applyStyle(R.style.LightTheme, true);
        }

        // set recents app color to the primary color
        ActivityManager.TaskDescription taskDesc;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            taskDesc = new ActivityManager.TaskDescription(
                    context.getString(R.string.app_name), R.mipmap.ic_launcher_round, customPrimaryColor);
        } else {
            Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round);
            //noinspection deprecation
            taskDesc = new ActivityManager.TaskDescription(context.getString(R.string.app_name), bm, customPrimaryColor);
        }
        context.setTaskDescription(taskDesc);
    }
}
