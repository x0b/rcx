package ca.pkay.rcloneexplorer.util;

import android.content.Context;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class CrashLogger {

    public static void initCrashLogging(Context context) {
        Fabric.with(context, new Crashlytics());
    }
}
