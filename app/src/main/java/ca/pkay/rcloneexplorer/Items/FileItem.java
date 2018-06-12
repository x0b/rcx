package ca.pkay.rcloneexplorer.Items;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileItem implements Parcelable {

    private RemoteItem remote;
    private String path;
    private String name;
    private long size;
    private String humanReadableSize;
    private long modTime;
    private String humanReadableModTime;
    private boolean isDir;

    public FileItem(RemoteItem remote, String path, String name, long size, String modTime, boolean isDir) {
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
        remote = in.readParcelable(RemoteItem.class.getClassLoader());
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

    public RemoteItem getRemote() {
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
        String yearMonthDay = dateTime[0];
        String hourMinuteSecond = dateTime[1].substring(0, dateTime[1].length() - 1);
        if (hourMinuteSecond.contains(".")) {
            int index = hourMinuteSecond.indexOf(".");
            hourMinuteSecond = hourMinuteSecond.substring(0, index);
        }

        String formattedDate = yearMonthDay + " " + hourMinuteSecond + " UTC";
        long dateInMillis;
        Date date;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
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
        long now = System.currentTimeMillis();
        long dateInMillis = modTimeToMilis(modTime);

        CharSequence humanReadable = DateUtils.getRelativeTimeSpanString(dateInMillis, now, DateUtils.MINUTE_IN_MILLIS);
        if (humanReadable.toString().startsWith("In") || humanReadable.toString().startsWith("0")) {
            humanReadable = "Now";
        }
        return humanReadable.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FileItem && ((FileItem) obj).getRemote().equals(this.remote) && ((FileItem) obj).getPath().equals(this.path) && ((FileItem) obj).getName().equals(this.name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(remote, 0);
        dest.writeString(path);
        dest.writeString(name);
        dest.writeLong(size);
        dest.writeString(humanReadableSize);
        dest.writeLong(modTime);
        dest.writeString(humanReadableModTime);
        dest.writeByte((byte) (isDir ? 1 : 0));
    }
}
