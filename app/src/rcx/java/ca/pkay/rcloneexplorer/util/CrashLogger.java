package ca.pkay.rcloneexplorer.util;

import android.content.Context;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class CrashLogger {

    public static void initCrashLogging(Context context) {
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        crashlytics.setCrashlyticsCollectionEnabled(true);
    }
}
