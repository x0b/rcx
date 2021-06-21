package ca.pkay.rcloneexplorer.util;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * Copyright (C) 2021  Felix Nüsse
 * Created on 20.06.21 - 15:30
 *
 * Edited by: Felix Nüsse felix.nuesse(at)t-online.de
 *
 */

public class SyncLog {

    private static int loglength = 4;

    public static JSONObject[] getLog(Context c){
        File log = new File(c.getFilesDir().getPath() + "/sync.log");
        StringBuilder file = new StringBuilder();
        try {
            char[] buffer = new char[4096];
            InputStream inputStream = new FileInputStream(log);
            Reader in = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            for (int numRead; (numRead = in.read(buffer, 0, buffer.length)) > 0; ) {
                file.append(buffer, 0, numRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String lines[] = file.toString().split("\\r?\\n");

        JSONObject[] jsons = new JSONObject[lines.length];
        for (int i = 0; i < lines.length; i++) {
            try {
                Log.e("app", lines[i].toString());
                jsons[i] = new JSONObject(lines[i]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return jsons;
    }

    private static void appendLog(Context c, String entry){

        JSONObject[] logs = getLog(c);
        int logsToStore = loglength;
        int availableLogs = logs.length;
        if(availableLogs<logsToStore){
            logsToStore=availableLogs;
        }
        JSONObject[] logList = new JSONObject[logsToStore];

        for (int i = logsToStore; i > 0; i--) {
            Log.e("app", "a: "+(i-logsToStore)*-1);
            Log.e("app", "b: "+(i));
            Log.e("app", "c: "+logs[i].toString());
            logList[(i-logsToStore)*-1]=logs[i];
        }

        File log = new File(c.getFilesDir().getPath() + "/sync.log");
        try {
            FileWriter writer = new FileWriter(log);
            for (JSONObject logEntry:logList) {
                writer.append(logEntry.toString());
                writer.append(System.lineSeparator());
            }
            writer.append(entry);
            writer.flush();
            writer.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void error(Context c, String entry){
        JSONObject json = new JSONObject();
        try {
            json.put("timestamp", System.currentTimeMillis());
            json.put("content", entry);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        appendLog(c, json.toString());
    }

    public static void delete(Context c){
        File log = new File(c.getFilesDir().getPath() + "/sync.log");
        if (log.exists()) {
            if (log.delete()) {
                System.out.println("file Deleted");
            } else {
                System.out.println("file not Deleted");
            }
        }
    }

}
