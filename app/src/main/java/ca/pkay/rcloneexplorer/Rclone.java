package ca.pkay.rcloneexplorer;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
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
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ca.pkay.rcloneexplorer.Items.FileItem;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import es.dmoral.toasty.Toasty;

public class Rclone {

    private Context context;
    private String rclone;
    private String rcloneConf;
    private Log2File log2File;

    public Rclone(Context context) {
        this.context = context;
        this.rclone = context.getFilesDir().getPath() + "/rclone";
        this.rcloneConf = context.getFilesDir().getPath() + "/rclone.conf";
        log2File = new Log2File(context);
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

    private void logErrorOutput(Process process) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean isLoggingEnable = sharedPreferences.getBoolean(context.getString(R.string.pref_key_logs), false);
        if (!isLoggingEnable) {
            return;
        }

        StringBuilder stringBuilder = new StringBuilder(100);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        log2File.log(stringBuilder.toString());
    }

    public List<FileItem> getDirectoryContent(String remote, String path) {
        String remoteAndPath = remote + ":";
        if (path.compareTo("//" + remote) != 0) {
            remoteAndPath += path;
        }

        String[] command = createCommand("lsjson", remoteAndPath);

        JSONArray results;
        Process process;
        try {
            process = Runtime.getRuntime().exec(command);
            process.waitFor();
            if (process.exitValue() != 0) {
                logErrorOutput(process);
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            results = new JSONArray(output.toString());

        } catch (IOException | InterruptedException | JSONException e) {
            e.printStackTrace();
            return null;
        }

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
        ArrayList<String> result = new ArrayList<>();
        Process process;
        try {
            process = Runtime.getRuntime().exec(command);
            process.waitFor();
            if (process.exitValue() != 0) {
                Toasty.error(context, context.getString(R.string.error_getting_remotes), Toast.LENGTH_SHORT, true).show();
                logErrorOutput(process);
                return new ArrayList<>();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
        } catch (IOException | InterruptedException e) {
            Toasty.error(context, context.getString(R.string.error_getting_remotes), Toast.LENGTH_SHORT, true).show();
            e.printStackTrace();
            return new ArrayList<>();
        }

        List<RemoteItem> remoteItemList = new ArrayList<>();
        for (String line : result) {
            String[] split = line.split(":");
            RemoteItem remoteItem = new RemoteItem(split[0], split[1].trim());
            remoteItemList.add(remoteItem);
        }

        return remoteItemList;
    }

    public Process configCreate(List<String> options) {
        String[] command = createCommand("config", "create");
        String[] opt = options.toArray(new String[0]);
        String[] commandWithOptions = new String[command.length + options.size()];

        System.arraycopy(command, 0, commandWithOptions, 0, command.length);

        System.arraycopy(opt, 0, commandWithOptions, command.length, opt.length);


        try {
            return Runtime.getRuntime().exec(commandWithOptions);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String obscure(String pass) {
        String[] command = createCommand("obscure", pass);

        Process process;
        try {
            process = Runtime.getRuntime().exec(command);
            process.waitFor();
            if (process.exitValue() != 0) {
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return  reader.readLine();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Process serveHttp(String remote, String servePath) {
        String path = (servePath.compareTo("//" + remote) == 0) ? remote + ":" : remote + ":" + servePath;
        String[] command = createCommand("serve", "http", path);

        try {
            return Runtime.getRuntime().exec(command);
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
        String localFilePath;

        for (FileItem item : downloadList) {
            remoteFilePath = remote + ":" + item.getPath();
            if (item.isDir()) {
                localFilePath = downloadPath + "/" + item.getName();
            } else {
                localFilePath = downloadPath;
            }
            command = createCommand("copy", remoteFilePath, localFilePath);

            try {
                process = Runtime.getRuntime().exec(command);
                runningProcesses.add(process);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return  runningProcesses;
    }

    public Process downloadItems(String remote, FileItem downloadItem, String downloadPath) {
        String[] command;
        String remoteFilePath;
        String localFilePath;

        remoteFilePath = remote + ":" + downloadItem.getPath();
        if (downloadItem.isDir()) {
            localFilePath = downloadPath + "/" + downloadItem.getName();
        } else {
            localFilePath = downloadPath;
        }
        command = createCommand("copy", remoteFilePath, localFilePath);

        try {
            return Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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

    public Boolean deleteItems(String remote, List<FileItem> deleteList) {
        String[] command;
        String filePath;
        Boolean result = true;

        for (FileItem item : deleteList) {
            filePath = remote + ":" + item.getPath();
            if (item.isDir()) {
                command = createCommand("purge", filePath);
            } else {
                command = createCommand("delete", filePath);
            }

            try {
                Process process = Runtime.getRuntime().exec(command);
                process.waitFor();
                if (process.exitValue() != 0) {
                    result = false;
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                result = false;
            }
        }
        return result;
    }

    public Boolean makeDirectory(String remote, String path) {
        String newDir = remote + ":" + path;
        String[] command = createCommand("mkdir", newDir);
        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            if (process.exitValue() != 0) {
                logErrorOutput(process);
                return false;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Boolean moveTo(String remote, List<FileItem> moveList, String newLocation) {
        String[] command;
        String oldFilePath;
        String newFilePath;
        Boolean result = true;

        for (FileItem fileItem : moveList) {
            oldFilePath = remote + ":" + fileItem.getPath();
            newFilePath = (newLocation.compareTo("//" + remote) == 0) ? remote + ":" + fileItem.getName() : remote + ":" + newLocation + "/" + fileItem.getName();
            command = createCommand("moveto", oldFilePath, newFilePath);
            try {
                Process process = Runtime.getRuntime().exec(command);
                process.waitFor();
                if (process.exitValue() != 0) {
                    logErrorOutput(process);
                    result = false;
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                result = false;
            }
        }
        return result;
    }

    public Boolean moveTo(String remote, String oldFile, String newFile) {
        String oldFilePath = remote + ":" + oldFile;
        String newFilePath = remote + ":" + newFile;
        String[] command = createCommand("moveto", oldFilePath, newFilePath);
        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            if (process.exitValue() != 0) {
                logErrorOutput(process);
                return false;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String calculateMD5(String remote, FileItem fileItem) {
        String remoteAndPath = remote + ":" + fileItem.getName();
        String[] command = createCommand("md5sum", remoteAndPath);
        Process process;
        try {
            process = Runtime.getRuntime().exec(command);
            process.waitFor();
            if (process.exitValue() != 0) {
                return context.getString(R.string.hash_error);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            String[] split = line.split("\\s+");
            if (split[0].trim().isEmpty()) {
                return context.getString(R.string.hash_unsupported);
            } else {
                return split[0];
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return context.getString(R.string.hash_error);
        }
    }

    public String calculateSHA1(String remote, FileItem fileItem) {
        String remoteAndPath = remote + ":" + fileItem.getName();
        String[] command = createCommand("sha1sum", remoteAndPath);
        Process process;
        try {
            process = Runtime.getRuntime().exec(command);
            process.waitFor();
            if (process.exitValue() != 0) {
                return context.getString(R.string.hash_error);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            String[] split = line.split("\\s+");
            if (split[0].trim().isEmpty()) {
                return context.getString(R.string.hash_unsupported);
            } else {
                return split[0];
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return context.getString(R.string.hash_error);
        }
    }

    public String getRcloneVersion() {
        String[] command = createCommand("--version");
        ArrayList<String> result = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            if (process.exitValue() != 0) {
                logErrorOutput(process);
                return "-1";
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "-1";
        }

        String[] version = result.get(0).split("\\s+");
        return version[1];
    }

    public Boolean isConfigEncrypted() {
        if (!isConfigFileCreated()) {
            return false;
        }
        String[] command = createCommand( "--ask-password=false", "listremotes");
        Process process;
        try {
            process = Runtime.getRuntime().exec(command);
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return process.exitValue() != 0;
    }

    public Boolean decryptConfig(String password) {
        String[] command = createCommand("--ask-password=false", "config", "show");
        String[] environmentalVars = {"RCLONE_CONFIG_PASS=" + password};
        Process process;

        try {
            process = Runtime.getRuntime().exec(command, environmentalVars);
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        if (process.exitValue() != 0) {
            return false;
        }

        ArrayList<String> result = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        String appsFileDir = context.getFilesDir().getPath();
        File file = new File(appsFileDir, "rclone.conf");

        try {
            file.delete();
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            for (String line2 : result) {
                outputStreamWriter.append(line2);
                outputStreamWriter.append("\n");
            }
            outputStreamWriter.close();
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean isConfigFileCreated() {
        String appsFileDir = context.getFilesDir().getPath();
        String configFile = appsFileDir + "/rclone.conf";
        File file = new File(configFile);
        return file.exists();
    }

    public void copyConfigFile(Uri uri) throws IOException {
        String appsFileDir = context.getFilesDir().getPath();
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        File outFile = new File(appsFileDir, "rclone.conf");
        FileOutputStream fileOutputStream = new FileOutputStream(outFile);

        byte[] buffer = new byte[4096];
        int offset;
        while ((offset = inputStream.read(buffer)) > 0) {
            fileOutputStream.write(buffer, 0, offset);
        }
        inputStream.close();
        fileOutputStream.flush();
        fileOutputStream.close();
    }

    public boolean isRcloneBinaryCreated() {
        String appsFileDir = context.getFilesDir().getPath();
        String exeFilePath = appsFileDir + "/rclone";
        File file = new File(exeFilePath);
        return file.exists() && file.canExecute();
    }

    public void createRcloneBinary() throws IOException {
        String appsFileDir = context.getFilesDir().getPath();
        String rcloneArchitecture = null;
        String[] supportedABIS = Build.SUPPORTED_ABIS;
        if (supportedABIS[0].toUpperCase().contains("ARM")) {
            if (supportedABIS[0].contains("64")) {
                rcloneArchitecture = "rclone-arm64";
            } else {
                rcloneArchitecture = "rclone-arm32";
            }
        } else if (supportedABIS[0].toUpperCase().contains("X86")) {
            if (supportedABIS[0].contains("64")) {
                rcloneArchitecture = "rclone-x86_32";
            } else {
                rcloneArchitecture = "rclone-x86_32";
            }
        } else {
            System.exit(1);
        }
        String exeFilePath = appsFileDir + "/rclone";
        InputStream inputStream = context.getAssets().open(rcloneArchitecture);
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
