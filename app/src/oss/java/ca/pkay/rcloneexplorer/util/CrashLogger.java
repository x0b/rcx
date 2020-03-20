package ca.pkay.rcloneexplorer.util;

import android.content.Context;

public class CrashLogger {

    private static final String TAG = "CrashLogger";

    public static void initCrashLogging(Context context) {
        FLog.e(TAG, "No crash Logging in OSS build!");
    }
}
