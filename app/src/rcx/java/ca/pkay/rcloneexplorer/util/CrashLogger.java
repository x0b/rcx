package ca.pkay.rcloneexplorer.util;

import android.content.Context;
import androidx.annotation.NonNull;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class CrashLogger {

    public static void initCrashLogging(Context context) {
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        crashlytics.setCrashlyticsCollectionEnabled(true);
    }

    public static void logNonFatal(@NonNull String tag, @NonNull String message, @NonNull Throwable e) {
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        crashlytics.setCustomKey("tag", tag);
        crashlytics.setCustomKey("message", message);
        crashlytics.recordException(e);
    }

}
