package ca.pkay.rcloneexplorer.Services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.Rclone;

public class ThumbnailsLoadingService extends IntentService {

    public static final String REMOTE_ARG = "ca.pkay.rcexplorer.ThumbnailsLoadingService.REMOTE_ARG";
    private Rclone rclone;
    private Process process;

    public ThumbnailsLoadingService() {
        super("ca.pkay.rcexplorer.ThumbnailLoadingService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        rclone = new Rclone(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        RemoteItem remote = intent.getParcelableExtra(REMOTE_ARG);
        process = rclone.serve(Rclone.SERVE_PROTOCOL_HTTP, 29170, true, remote, "");
        if (process != null) {
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (process != null && process.exitValue() != 0) {
            rclone.logErrorOutput(process);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (process != null) {
            process.destroy();
        }
    }
}
