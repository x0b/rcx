package ca.pkay.rcloneexplorer;

import android.content.Context;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Rclone {

    private AppCompatActivity activity;

    public Rclone(AppCompatActivity activity) {
        this.activity = activity;

        if (!isRcloneBinaryCreated()) {
            try {
                createRcloneBinary();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isConfigFileCreated() {
        String appsFileDir = activity.getFilesDir().getPath();
        String configFile = appsFileDir + "/rclone.conf";
        File file = new File(configFile);
        return file.exists();
    }

    public void copyConfigFile(Uri uri) throws IOException {
        String appsFileDir = activity.getFilesDir().getPath();
        InputStream inputStream = activity.getContentResolver().openInputStream(uri);
        File outFile = new File(appsFileDir, "rclone.conf");
        FileOutputStream fileOutputStream = new FileOutputStream(outFile);

        byte[] buffer = new byte[4096];
        int offset;
        while ((offset = inputStream.read(buffer)) > 0) {
            fileOutputStream.write(buffer, 0, offset);
        }
        inputStream.close();
        fileOutputStream.close();

        Context context = activity.getApplicationContext();
        Toast toast = Toast.makeText(context, "Config file imported", Toast.LENGTH_LONG);
        toast.show();
    }

    private boolean isRcloneBinaryCreated() {
        String appsFileDir = activity.getFilesDir().getPath();
        String exeFilePath = appsFileDir + "/rclone";
        File file = new File(exeFilePath);
        return file.exists() && file.canExecute();
    }

    private void createRcloneBinary() throws IOException {
        String appsFileDir = activity.getFilesDir().getPath();
        String exeFilePath = appsFileDir + "/rclone";
        InputStream inputStream = activity.getAssets().open("rclone-arm32");
        File outFile = new File(appsFileDir, "rclone");
        FileOutputStream fileOutputStream = new FileOutputStream(outFile);

        byte[] buffer = new byte[4096];
        int offset;
        while ((offset = inputStream.read(buffer)) > 0) {
            fileOutputStream.write(buffer, 0, offset);
        }
        inputStream.close();
        fileOutputStream.close();

        Runtime.getRuntime().exec("chmod 0777 " + exeFilePath);
    }
}
