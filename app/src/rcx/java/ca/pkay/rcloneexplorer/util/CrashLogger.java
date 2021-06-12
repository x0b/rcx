package ca.pkay.rcloneexplorer.util;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import ca.pkay.rcloneexplorer.BuildConfig;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import com.microsoft.appcenter.crashes.ingestion.models.ErrorAttachmentLog;
import com.microsoft.appcenter.crashes.ingestion.models.json.ErrorAttachmentLogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import static java.lang.String.valueOf;

public class CrashLogger {

    private static final String TAG = "CrashLogger";
    private static final int PROP_PACK_LENGTH = 64;
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
        ArrayList<ErrorAttachmentLog> attachments = new ArrayList<>();
        properties.put("tag", tag);
        if (message.length() >= PROP_PACK_LENGTH) {
            properties.put("message", "see attachment message.txt");
            ErrorAttachmentLog attachmentLog = ErrorAttachmentLog.attachmentWithText(message, "message.txt");
            attachments.add(attachmentLog);
        } else {
            properties.put("message", message);
        }
        Crashes.trackError(e, properties, attachments);
    }

    private static String generateReportId(String sourceId) {
        return valueOf(new UUID(m(sourceId) ^ (r = new Random(k)).nextLong(),
                r.nextLong() ^ l(sourceId)));
    }
}
