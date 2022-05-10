package ca.pkay.rcloneexplorer.util;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import ca.pkay.rcloneexplorer.BuildConfig;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import com.microsoft.appcenter.crashes.ingestion.models.ErrorAttachmentLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static java.lang.String.valueOf;

public class CrashLogger {

    private static final String TAG = "CrashLogger";
    private static final String PROP_BOARD = "board";
    private static final String PROP_ABI = "abi";
    private static final String PROP_DEVICE = "device";
    private static final String PROP_NLD = "native_dir";
    private static final int PROP_PACK_LENGTH = 64;
    private static String nativeLibraryDir = "";
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
            nativeLibraryDir = appCtx.getApplicationInfo().nativeLibraryDir;
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
        // only attach HW ABI stats for ABI errors
        if (e instanceof IOException && e.getMessage() != null && e.getMessage().contains("librclone.so")) {
            attachHwStats(properties);
        }
        Crashes.trackError(e, properties, attachments);
    }

    // Occasionally, bug reports from devices with really weird device
    // properties arrive. Often, these devices fail to execute due to a
    // mismatch between app ABI and device ABI.
    private static void attachHwStats(@NonNull Map<String, String> properties) {
        properties.put(PROP_ABI, TextUtils.join("; ", Build.SUPPORTED_ABIS));
        properties.put(PROP_NLD, nativeLibraryDir);
        properties.put(PROP_BOARD, Build.BOARD);
        properties.put(PROP_DEVICE, Build.DEVICE);
    }

    private static String generateReportId(String sourceId) {
        return valueOf(new UUID(m(sourceId) ^ (r = new Random(k)).nextLong(),
                r.nextLong() ^ l(sourceId)));
    }
}
