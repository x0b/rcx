package ca.pkay.rcloneexplorer.util;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import ca.pkay.rcloneexplorer.BuildConfig;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import static java.lang.String.valueOf;

public class CrashLogger {

    private static final String TAG = "CrashLogger";
    private static String rcloneLog;
    private static Random r;
    private static final int k = BuildConfig.VERSION_NAME.hashCode();
    private static final String s = BuildConfig.CLI;

    private static long m (String s) {
        return UUID.fromString(s).getMostSignificantBits();
    }

    private static long l (String s) {
        return UUID.fromString(s).getLeastSignificantBits();
    }

    public static void initCrashLogging(@NonNull Context context) {
        Context appCtx = context.getApplicationContext();
        if (appCtx instanceof Application) {
            AppCenter.start((Application) appCtx, generateReportId(s),
                    Analytics.class, Crashes.class);
        }
    }

    public static void logNonFatal(@NonNull String tag, @NonNull String message, @NonNull Throwable e) {
        HashMap<String, String> properties = new HashMap<>();
        properties.put("tag", tag);
        properties.put("message", message);
        Crashes.trackError(e, properties, new ArrayList<>());
    }

    private static String generateReportId(String sourceId) {
        return valueOf(new UUID(m(sourceId) ^ (r = new Random(k)).nextLong(),
                r.nextLong() ^ l(sourceId)));
    }
}
