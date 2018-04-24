package ca.pkay.rcloneexplorer.Services;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import ca.pkay.rcloneexplorer.BroadcastReceivers.DownloadCancelAction;
import ca.pkay.rcloneexplorer.BroadcastReceivers.UploadCancelAction;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;


public class UploadService extends IntentService {

    public static final String UPLOAD_PATH_ARG = "ca.pkay.rcexplorer.upload_service.arg1";
    public static final String LOCAL_PATH_ARG = "ca.pkay.rcexplorer.upload_service.arg2";
    public static final String REMOTE_ARG = "ca.pkay.rcexplorer.upload_service.arg3";
    private final String CHANNEL_ID = "ca.pkay.rcexplorer.upload_channel";
    private final String CHANNEL_NAME = "Uploads";
    private final int PERSISTENT_NOTIFICATION_ID = 90;
    private final int UPLOAD_FINISHED_NOTIFICATION_ID = 41;
    private final int UPLOAD_FAILED_NOTIFICATION_ID = 14;
    private Rclone rclone;
    private int numOfProcessesRunning;
    private int numOfFinishedUploads;
    private int numOfFailedUploads;
    private List<Process> runningProcesses;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.*
     */
    public UploadService() {
        super("ca.pkay.rcexplorer.uploadservice");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setNotificationChannel();
        rclone = new Rclone(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Intent foregroundIntent = new Intent(this, UploadService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, foregroundIntent, 0);

        Intent cancelIntent = new Intent(this, UploadCancelAction.class);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, 0, cancelIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_upload)
                .setContentTitle(getString(R.string.upload_service_notification_title))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_cancel_download, getString(R.string.cancel), cancelPendingIntent);

        startForeground(PERSISTENT_NOTIFICATION_ID, builder.build());

        if (intent == null) {
            return;
        }
        final String uploadPath = intent.getStringExtra(UPLOAD_PATH_ARG);
        final ArrayList<String> uploadList = intent.getStringArrayListExtra(LOCAL_PATH_ARG);
        final String remote = intent.getStringExtra(REMOTE_ARG);

        runningProcesses = rclone.uploadFiles(remote, uploadPath, uploadList);
        numOfProcessesRunning = runningProcesses.size();
        numOfFinishedUploads = 0;
        numOfFailedUploads = 0;
        AsyncTask[] asyncTasks = new AsyncTask[numOfProcessesRunning];
        int i = 0;
        for (Process process : runningProcesses) {
            asyncTasks[i++] = new MonitorUpload().execute(process);
        }
        
        for (AsyncTask asyncTask : asyncTasks) {
            try {
                asyncTask.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        stopForeground(true);
    }

    private void showUploadFinishedNotification(int numOfFinished, int numOfTotal) {
        String notificationText = numOfFinished + " " + getString(R.string.out_of) + " " + numOfTotal + " " + getString(R.string.files_uploaded);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_upload_done)
                .setContentTitle(getString(R.string.upload_complete))
                .setContentText(notificationText)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(UPLOAD_FINISHED_NOTIFICATION_ID, builder.build());
        }
    }

    private void showUploadFailedNotification(int numOfFailed, int numOfTotal) {
        String notificationText = numOfFailed + " " + getString(R.string.out_of) + " " + numOfTotal + " " + getString(R.string.failed_to_upload);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle(getString(R.string.upload_failed))
                .setContentText(notificationText)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(UPLOAD_FAILED_NOTIFICATION_ID, builder.build());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (Process process : runningProcesses) {
            process.destroy();
        }
    }

    private void setNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(getString(R.string.upload_service_notification_channel_description));
            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class MonitorUpload extends AsyncTask<Process, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Process... processes) {
            Process process = processes[0];
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return process.exitValue() == 0;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                showUploadFinishedNotification(++numOfFinishedUploads, numOfProcessesRunning);
            } else {
                showUploadFailedNotification(++numOfFailedUploads, numOfProcessesRunning);
            }
        }
    }
}
