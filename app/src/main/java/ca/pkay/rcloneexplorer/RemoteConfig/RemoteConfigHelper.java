package ca.pkay.rcloneexplorer.RemoteConfig;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import es.dmoral.toasty.Toasty;

import java.util.ArrayList;

public class RemoteConfigHelper {

    public static String getRemotePath(String path, RemoteItem selectedRemote) {
        String remotePath;
        if (selectedRemote.isRemoteType(RemoteItem.LOCAL)) {
            if (path.equals("//" + selectedRemote.getName())) {
                remotePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
            } else {
                remotePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + path;
            }
        } else {
            if (path.equals("//" + selectedRemote.getName())) {
                remotePath = selectedRemote.getName() + ":";
            } else {
                remotePath = selectedRemote.getName() + ":" + path;
            }
        }
        return remotePath;
    }

    public static void setupAndWait(Context context, ArrayList<String> options) {
        Rclone rclone = new Rclone(context);
        Process process = rclone.configCreate(options);
        int exitCode;
        while (true) {
            try {
                exitCode = process.waitFor();
                break;
            } catch (InterruptedException e) {
                try {
                    exitCode = process.exitValue();
                    break;
                } catch (IllegalStateException ignored) {}
            }
        }
        if (0 != exitCode) {
            Toasty.error(context, context.getString(R.string.error_creating_remote), Toast.LENGTH_SHORT, true).show();
        } else {
            Toasty.success(context, context.getString(R.string.remote_creation_success), Toast.LENGTH_SHORT, true).show();
        }
    }
}
