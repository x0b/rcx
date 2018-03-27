package ca.pkay.rcloneexplorer.Items;

import android.support.annotation.NonNull;
import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileItem {

    private String remote;
    private String path;
    private String name;
    private long size;
    private String humanReadableSize;
    private long modTime;
    private String humanReadableModTime;
    private boolean isDir;

    public FileItem(String remote, String path, String name, long size, String modTime, boolean isDir) {
        this.remote = remote;
        this.path = path;
        this.name = name;
        this.size = size;
        this.humanReadableSize = sizeToHumanReadable(size);
        this.modTime = modTimeToMilis(modTime);
        this.humanReadableModTime = modTimeToHumanReadable(modTime);
        this.isDir = isDir;
    }

    public String getRemote() {
        return remote;
    }
    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public String getHumanReadableSize() {
        return humanReadableSize;
    }

    public long getSize() {
        return size;
    }

    public String getHumanReadableModTime() {
        return humanReadableModTime;
    }

    public long getModTime() {
        return modTime;
    }

    public boolean isDir() {
        return isDir;
    }

    private String sizeToHumanReadable(long size) {
        int unit = 1000;
        if (size < unit) return size + " B";
        int exp = (int) (Math.log(size) / Math.log(unit));
        Character pre = "kMGTPE".charAt(exp - 1);
        return String.format(Locale.US, "%.1f %sB", size / Math.pow(unit, exp), pre);
    }

    private long modTimeToMilis(String modTime) {
        String[] dateTime = modTime.split("T");
        String formattedDate = dateTime[0] + " " + dateTime[1].substring(0, dateTime[1].length());
        long now = System.currentTimeMillis();
        long dateInMillis;
        Date date;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            date = simpleDateFormat.parse(formattedDate);
            dateInMillis = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            dateInMillis = 0;
        }

        return dateInMillis;
    }

    private String modTimeToHumanReadable(String modTime) {
        String[] dateTime = modTime.split("T");
        String formattedDate = dateTime[0] + " " + dateTime[1].substring(0, dateTime[1].length());
        long now = System.currentTimeMillis();
        long dateInMillis;
        Date date;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            date = simpleDateFormat.parse(formattedDate);
            dateInMillis = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            dateInMillis = 0;
        }

        CharSequence humanReadable = DateUtils.getRelativeTimeSpanString(dateInMillis, now, DateUtils.MINUTE_IN_MILLIS);
        if (humanReadable.toString().startsWith("In")) {
            humanReadable = "Now";
        }
        return humanReadable.toString();
    }
}
