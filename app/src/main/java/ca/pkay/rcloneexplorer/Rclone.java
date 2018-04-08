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

    private final String TAG = "Rclone";
    private Context activity;
    private String rclone;
    private String rcloneConf;

    public Rclone(Context activity) {
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

    private ArrayList<String> runCommand(String[] command) {
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

            reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder error = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                error.append(line);
            }
            if (error.length() != 0) {
                Log.e(TAG, error.toString());
            }

            return output;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    private JSONArray runCommandForJSON(String[] command) {
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

            reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder error = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                error.append(line);
            }
            if (error.length() != 0) {
                Log.e(TAG, error.toString());
            }

            return new JSONArray(output.toString());

        } catch (IOException | InterruptedException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String[] createCommand(String ...args) {
        int arraySize = args.length + 3;
        String[] command = new String[arraySize];

        command[0] = rclone;
        command[1] = "--config";
        command[2] = rcloneConf;

        int i = 3;
        for (String arg : args) {
            command[i++] = arg;
        }
        return command;
    }

    public List<FileItem> getDirectoryContent(String remote, String path) {
        String remoteAndPath = remote + ":";
        if (path.compareTo("//" + remote) != 0) {
            remoteAndPath += path;
        }

        String[] command = createCommand("lsjson", remoteAndPath);

        JSONArray results = runCommandForJSON(command);

        assert results != null;

        List<FileItem> fileItemList = new ArrayList<>();
        for (int i = 0; i < results.length(); i++) {
            try {
                JSONObject jsonObject = results.getJSONObject(i);
                String filePath = (path.compareTo("//" + remote) == 0) ? "" : path + "/";
                filePath += jsonObject.getString("Path");
                String fileName = jsonObject.getString("Name");
                long fileSize = jsonObject.getLong("Size");
                String fileModTime = jsonObject.getString("ModTime");
                boolean fileIsDir = jsonObject.getBoolean("IsDir");

                FileItem fileItem = new FileItem(remote, filePath, fileName, fileSize, fileModTime, fileIsDir);
                fileItemList.add(fileItem);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return fileItemList;
    }

    public List<RemoteItem> getRemotes() {
        String[] command = createCommand("listremotes", "-l");
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

    public Process serveHttp(String remote, String servePath) {
        Process process;
        String path = (servePath.compareTo("//" + remote) == 0) ? remote + ":" : remote + ":" + servePath;
        String[] command = createCommand("serve", "http", path);

        try {
            process = Runtime.getRuntime().exec(command);
            return process;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Process> downloadItems(String remote, List<FileItem> downloadList, String downloadPath) {
        List<Process> runningProcesses = new ArrayList<>();
        Process process;
        String[] command;
        String remoteFilePath;

        for (FileItem item : downloadList) {
            remoteFilePath = remote + ":" + item.getPath();
            command = createCommand("copy", remoteFilePath, downloadPath);

            try {
                process = Runtime.getRuntime().exec(command);
                runningProcesses.add(process);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return  runningProcesses;
    }

    public List<Process> uploadFiles(String remote, String uploadPath, ArrayList<String> uploadList) {
        List<Process> runningProcesses = new ArrayList<>();
        Process process;
        String path;
        String[] command;

        for (String localPath : uploadList) {
            File file = new File(localPath);
            if (file.isDirectory()) {
                int index = localPath.lastIndexOf('/');
                String dirName = localPath.substring(index + 1);
                path = (uploadPath.compareTo("//" + remote) == 0) ? remote + ":" + dirName: remote + ":" + uploadPath + "/" + dirName;
            } else {
                path = (uploadPath.compareTo("//" + remote) == 0) ? remote + ":" : remote + ":" + uploadPath;
            }

            command = createCommand("copy", localPath, path);

            try {
                process = Runtime.getRuntime().exec(command);
                runningProcesses.add(process);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return runningProcesses;
    }

    public void deleteItems(String remote, List<FileItem> deleteList) {
        String[] command;
        String filePath;

        for (FileItem item : deleteList) {
            filePath = remote + ":" + item.getPath();
            if (item.isDir()) {
                command = createCommand("purge", filePath);
            } else {
                command = createCommand("delete", filePath);
            }
            runCommand(command);
        }
    }

    public void makeDirectory(String remote, String path) {
        String newDir = remote + ":" + path;
        String[] command = createCommand("mkdir", newDir);
        runCommand(command);
    }

    public void moveTo(String remote, List<FileItem> moveList, String newLocation) {
        String[] command;
        String oldFilePath;
        String newFilePath;

        for (FileItem fileItem : moveList) {
            oldFilePath = remote + ":" + fileItem.getPath();
            newFilePath = (newLocation.compareTo("//" + remote) == 0) ? remote + ":" + fileItem.getName() : remote + ":" + newLocation + "/" + fileItem.getName();
            command = createCommand("moveto", oldFilePath, newFilePath);
            runCommand(command);
        }
    }

    public void moveTo(String remote, String oldFile, String newFile) {
        String oldFilePath = remote + ":" + oldFile;
        String newFilePath = remote + ":" + newFile;
        String[] command = createCommand("moveto", oldFilePath, newFilePath);
        runCommand(command);
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
