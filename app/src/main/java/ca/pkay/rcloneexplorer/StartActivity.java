package ca.pkay.rcloneexplorer;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import es.dmoral.toasty.Toasty;

public class StartActivity {

    @SuppressLint("CheckResult")
    public static void tryStartActivity(Context context, Intent intent) {
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            showErrorToast(context);
        }
    }

    @SuppressLint("CheckResult")
    public static void tryStartActivityForResult(Fragment activity, Intent intent, int requestCode) {
        try {
            activity.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            showErrorToast(activity.getContext());
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
}
