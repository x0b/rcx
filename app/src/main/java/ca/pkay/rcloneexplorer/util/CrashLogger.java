package ca.pkay.rcloneexplorer.util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * No-op crash logger for the privacy-focused fork.
 * <p>
 * All error and crash data is written to the local Android log only
 * ({@link android.util.Log}). No telemetry, network upload, or third-party
 * crash reporter is initialised. The original AppCenter integration has been
 * removed; this class is kept as a no-op sink so existing call sites in
 * {@link FLog} continue to compile without further changes.
 */
public class CrashLogger {

    private static final String TAG = "CrashLogger";

    private CrashLogger() {
    }

    /**
     * Previously initialised the AppCenter crash reporter. Now a no-op; the
     * privacy-focused fork does not upload any crash data.
     */
    public static void initCrashLogging(@NonNull Context context) {
        Log.i(TAG, "Crash logging is disabled in this build (local logs only)");
    }

    /**
     * Previously forwarded the error to AppCenter. Now only writes a local
     * breadcrumb to logcat so the error trail remains visible via
     * {@code adb logcat} or the in-app log collector.
     */
    public static void logNonFatal(@NonNull String tag, @NonNull String message, @NonNull Throwable e) {
        Log.w(tag, message, e);
    }
}
