package ca.pkay.rcloneexplorer.Items;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileItem implements Parcelable {

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

    protected FileItem(Parcel in) {
        remote = in.readString();
        path = in.readString();
        name = in.readString();
        size = in.readLong();
        humanReadableSize = in.readString();
        modTime = in.readLong();
        humanReadableModTime = in.readString();
        isDir = in.readByte() != 0;
    }

    public static final Creator<FileItem> CREATOR = new Creator<FileItem>() {
        @Override
        public FileItem createFromParcel(Parcel in) {
            return new FileItem(in);
        }

        @Override
        public FileItem[] newArray(int size) {
            return new FileItem[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(remote);
        dest.writeString(path);
        dest.writeString(name);
        dest.writeLong(size);
        dest.writeString(humanReadableSize);
        dest.writeLong(modTime);
        dest.writeString(humanReadableModTime);
        dest.writeByte((byte) (isDir ? 1 : 0));
    }
}
