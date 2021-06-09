package ca.pkay.rcloneexplorer.Services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ca.pkay.rcloneexplorer.BuildConfig;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import ca.pkay.rcloneexplorer.util.FLog;

import static ca.pkay.rcloneexplorer.ActivityHelper.tryStartActivity;

/**
 * A service for collecting bug reports.
 */
public class ReportService extends Service {

    private static final String TAG = "ReportCollectionService";

    private static final String REPORT_HEADER = "RCX_BUG_REPORT_v0";

    private static final String NOTIFICATION_CHANNEL = "ca.pkay.rcloneexplorer.reportcollector";

    private static final int NOTIFICATION_ID = 201;

    /**
     * Start collecting report data
     */
    private static final String ACTION_START_COLLECTION = "ca.pkay.rcloneexplorer.Services.action.START_COLLECTION";
    /**
     * Stop collecting report data
     */
    private static final String ACTION_STOP_COLLECTION = "ca.pkay.rcloneexplorer.Services.action.STOP_COLLECTION";

    /**
     * Collect rclone log files
     */
    private static final String EXTRA_RCLONE_LOGS = "ca.pkay.rcloneexplorer.Services.extra.RCLONE_LOGS";

    /**
     * Collect logcat data
     */
    private static final String EXTRA_LOGCAT = "ca.pkay.rcloneexplorer.Services.extra.LOGCAT";

    /**
     * Collect configuration data
     */
    private static final String EXTRA_CONFIG = "ca.pkay.rcloneexplorer.Services.extra.CONFIG";

    private static final String EXTRA_COLL_TARGETS = "ca.pkay.rcloneexplorer.Services.extra.COLL_TARGETS";

    private static final String EXTRA_LOGCAT_LEVEL = "ca.pkay.rcloneexplorer.Services.extra.LOGCAT_LVL";

    public static final int RCLONE_LOGS = 2 << 0;

    public static final int LOGCAT = 2 << 1;

    public static final int CONFIG = 2 << 2;

    private volatile boolean reportRunning = false;
    private int flags = 0;
    private Thread logcatThread;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = {RCLONE_LOGS, LOGCAT, CONFIG}, flag = true)
    public @interface CollectionFlags {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void startCollection(@NonNull Context context, @CollectionFlags int flags) {
        Intent intent = new Intent(context, ReportService.class);
        intent.setAction(ACTION_START_COLLECTION);
        intent.putExtra(EXTRA_COLL_TARGETS, flags);
        context.startService(intent);
    }

    public static void stopCollection(@NonNull Context context) {
        Intent intent = new Intent(context, ReportService.class);
        intent.setAction(ACTION_STOP_COLLECTION);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null == intent) {
            return START_STICKY;
        }

        if (ACTION_START_COLLECTION.equals(intent.getAction()) && intent.hasExtra(EXTRA_COLL_TARGETS) && !reportRunning) {
            int collectionTargets = intent.getIntExtra(EXTRA_COLL_TARGETS, RCLONE_LOGS | LOGCAT);
            int logcatLevel = intent.getIntExtra(EXTRA_LOGCAT_LEVEL, Log.INFO);
            handleStartCollection(getApplicationContext(), collectionTargets, logcatLevel);
        } else if (ACTION_STOP_COLLECTION.equals(intent.getAction()) && reportRunning) {
            handleStopCollection(getApplicationContext());
        }
        return START_STICKY;
    }

    private void handleStartCollection(Context context, @CollectionFlags int flags, int logLevel) {
        FLog.d(TAG, "Starting collection (%d) (%d)", flags, logLevel);
        this.reportRunning = true;
        this.flags = flags;

        File baseDirectory = new File(context.getCacheDir(), "report");
        if (!baseDirectory.exists()) {
            baseDirectory.mkdir();
        }

        if ((flags & LOGCAT) == LOGCAT) {
            System.setProperty("log.tag.APP_MIN", Integer.toString(logLevel));
            String logcatFile = new File(baseDirectory, "logcat.log").getPath();
            logcatThread = new LogcatWatcherThread(logcatFile);
            logcatThread.start();
        }

        Intent foregroundIntent = new Intent(this, ReportService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, foregroundIntent, 0);

        Intent stopIntent = new Intent(this, StopCollectionReceiver.class);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_bug_report)
                .setContentTitle(getString(R.string.report_collection_title))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentText(getString(R.string.report_collection_collecting))
                .addAction(R.drawable.ic_baseline_stop_24, getString(R.string.report_collection_stop_collection), cancelPendingIntent);

        setNotificationChannel();
        startForeground(NOTIFICATION_ID, builder.build());

    }

    private void setNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL, "Bug Reporter", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Helps you to collect information about bugs.");
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void handleStopCollection(Context context) {
        FLog.d(TAG, "Stopping collection (%d)", flags);
        if ((flags & LOGCAT) == LOGCAT) {
            logcatThread.interrupt();
        }
        packageReport(context);
        reportRunning = false;
        stopForeground(true);
        stopSelf();
    }

    private void packageReport(Context context) {
        File baseDirectory = new File(context.getCacheDir(), "report");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss", Locale.US);
        String fileName = "report-" + sdf.format(new Date()) + ".rcx-report";
        File reportZipFile = new File(baseDirectory, fileName);
        boolean logcat = (flags & LOGCAT) == LOGCAT;
        boolean rcloneLogs = (flags & RCLONE_LOGS) == RCLONE_LOGS;
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(reportZipFile))) {

            zos.setComment(REPORT_HEADER);

            MetaData meta = new MetaData(
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE,
                    context.getPackageManager().getInstallerPackageName(context.getPackageName()),
                    new Rclone(context).getRcloneVersion(),
                    Build.VERSION.SDK_INT,
                    android.os.Build.MODEL,
                    android.os.Build.PRODUCT,
                    android.os.Build.MANUFACTURER,
                    android.os.Build.SUPPORTED_ABIS,
                    logcat,
                    rcloneLogs,
                    false
            );
            File metaFile = new File(baseDirectory, meta.getFileName());
            ObjectMapper mapper = new ObjectMapper();
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NON_PRIVATE);
            mapper.writeValue(metaFile, meta);
            addToZip(zos, metaFile);

            if (logcat) {
                File logcatLog = new File(baseDirectory, "logcat.log");
                addToZip(zos, logcatLog);
            }

            if (rcloneLogs) {
                File logsTxt = new File(context.getExternalFilesDir("logs"), "log.txt");
                addToZip(zos, logsTxt);

                File rcdLogs = new File(context.getExternalFilesDir("logs"), "rcd.log");
                addToZip(zos, rcdLogs);
            }

            zos.flush();
            zos.finish();
        } catch (FileNotFoundException e) {
            FLog.e(TAG, "Report collection stopped: could not find file", e);
            return;
        } catch (IOException e) {
            FLog.e(TAG, "Report collection stopped: I/O error", e);
            return;
        }

        Uri reportZipUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", reportZipFile);
        Intent intent = new Intent(Intent.ACTION_SEND, reportZipUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        tryStartActivity(context, intent);
    }

    private void addToZip(ZipOutputStream base, File srcFile) throws IOException {
        if (srcFile.exists() && srcFile.canRead()) {
            ZipEntry zipEntry = new ZipEntry(srcFile.getName());
            base.putNextEntry(zipEntry);
            try (FileInputStream fis = new FileInputStream(srcFile)) {
                byte[] buf = new byte[4096];
                int len;
                while ((len = fis.read(buf)) >= 0) {
                    base.write(buf, 0, len);
                }
            }
            base.closeEntry();
            base.flush();
        }
    }

    private void addToZip(ZipOutputStream base, ReportData model) throws IOException {
        ZipEntry zipEntry = new ZipEntry(model.getFileName());
        base.putNextEntry(zipEntry);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(base, model);
        base.closeEntry();
    }

    interface ReportData {
        String getFileName();
    }

    static final class MetaData implements ReportData {
        String appVersionName;
        int appVersionCode;
        String appInstaller;
        String rcloneVersion;
        int androidSdk;
        String deviceModel;
        String deviceProduct;
        String deviceManufacturer;
        String[] supportedABIs;
        boolean logcat;
        boolean rcloneLogs;
        boolean config;

        public MetaData(String appVersionName, int appVersionCode, String appInstaller, String rcloneVersion, int androidSdk, String deviceModel, String deviceProduct, String deviceManufacturer, String[] supportedABIs, boolean logcat, boolean rcloneLogs, boolean config) {
            this.appVersionName = appVersionName;
            this.appVersionCode = appVersionCode;
            this.appInstaller = appInstaller;
            this.rcloneVersion = rcloneVersion;
            this.androidSdk = androidSdk;
            this.deviceModel = deviceModel;
            this.deviceProduct = deviceProduct;
            this.deviceManufacturer = deviceManufacturer;
            this.supportedABIs = supportedABIs;
            this.logcat = logcat;
            this.rcloneLogs = rcloneLogs;
            this.config = config;
        }

        @Override
        public String getFileName() {
            return "metadata.json";
        }
    }

    public static class StopCollectionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ReportService.stopCollection(context);
        }
    }

    static final class LogcatWatcherThread extends Thread {

        private final static int MAX_BUFFER = 165537;
        private String filePath;

        public LogcatWatcherThread(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public void run() {
            try {
                Process process = Runtime.getRuntime().exec("logcat");
                try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                     BufferedWriter bw = new BufferedWriter(new FileWriter(filePath), MAX_BUFFER)) {
                    FLog.d(TAG, "Retrieving logcat data");
                    String line;
                    while (!interrupted() && (line = br.readLine()) != null) {
                        bw.write(line);
                        bw.newLine();
                    }
                    bw.flush();
                    process.destroy();
                }
            } catch (IOException e) {
                FLog.e(TAG, "Error starting logcat", e);
            }
        }
    }
}
