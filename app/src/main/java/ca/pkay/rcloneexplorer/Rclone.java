package ca.pkay.rcloneexplorer;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ca.pkay.rcloneexplorer.Items.FileItem;
import ca.pkay.rcloneexplorer.Items.RemoteItem;

public class Rclone {

    private AppCompatActivity activity;
    private String rclone;
    private String rcloneConf;

    public Rclone(AppCompatActivity activity) {
        this.activity = activity;

        if (!isRcloneBinaryCreated()) {
            try {
                createRcloneBinary();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.rclone = activity.getFilesDir().getPath() + "/rclone";
        this.rcloneConf = activity.getFilesDir().getPath() + "/rclone.conf";
    }

    private ArrayList<String> runCommand(String command) {
        Process process;
        try {
            process = Runtime.getRuntime().exec(command);
            process.waitFor();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            ArrayList<String> output = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                output.add(line);
            }
            return output;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    private JSONArray runCommandForJSON(String command) {
        Process process;
        try {
            process = Runtime.getRuntime().exec(command);
            process.waitFor();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
            return new JSONArray(output.toString());

        } catch (IOException | InterruptedException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String createCommand(String arg) {
        return rclone + " --config " + rcloneConf + " " + arg;
    }

    public List<FileItem> getDirectoryContent(String remote) {
        String command = createCommand("lsjson " + remote + ":");
        JSONArray results = runCommandForJSON(command);

        assert results != null;

        List<FileItem> fileItemList = new ArrayList<>();
        for (int i = 0; i < results.length(); i++) {
            try {
                JSONObject jsonObject = results.getJSONObject(i);
                String path = jsonObject.getString("Path");
                String name = jsonObject.getString("Name");
                long size = jsonObject.getLong("Size");
                String modTime = jsonObject.getString("ModTime");
                boolean isDir = jsonObject.getBoolean("IsDir");

                FileItem fileItem = new FileItem(path, name, size, modTime, isDir);
                fileItemList.add(fileItem);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return fileItemList;
    }

    public List<RemoteItem> getRemotes() {
        String command = createCommand("listremotes -l");
        ArrayList<String> result = runCommand(command);

        assert result != null;

        List<RemoteItem> remoteItemList = new ArrayList<>();
        for (String line : result) {
            String[] split = line.split(":");
            RemoteItem remoteItem = new RemoteItem(split[0], split[1].trim());
            remoteItemList.add(remoteItem);
        }

        return remoteItemList;
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
        String rcloneArchitecture = null;
        String[] supportedAbis = Build.SUPPORTED_ABIS;
        if (supportedAbis[0].toUpperCase().contains("ARM")) {
            if (supportedAbis[0].contains("64")) {
                rcloneArchitecture = "rclone-arm64";
            } else {
                rcloneArchitecture = "rclone-arm32";
            }
        } else if (supportedAbis[0].toUpperCase().contains("X86")) {
            if (supportedAbis[0].contains("64")) {
                rcloneArchitecture = "rclone-x86_32";
            } else {
                rcloneArchitecture = "rclone-x86_32";
            }
        } else {
            Log.e("Rclone", "Unsupported architecture '" + supportedAbis[0] + "'");
            System.exit(1);
        }
        Log.i("Rclone", "Architecture: " + supportedAbis[0]);

        String exeFilePath = appsFileDir + "/rclone";
        InputStream inputStream = activity.getAssets().open(rcloneArchitecture);
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
