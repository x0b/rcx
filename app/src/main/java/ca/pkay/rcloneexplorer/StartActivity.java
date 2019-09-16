package ca.pkay.rcloneexplorer;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import es.dmoral.toasty.Toasty;

public class StartActivity {

    @SuppressLint("CheckResult")
    public static void tryStartActivity(Context context, Intent intent) {
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toasty.error(context, "No app found for this link", Toast.LENGTH_LONG, true).show();
        }
    }
}
