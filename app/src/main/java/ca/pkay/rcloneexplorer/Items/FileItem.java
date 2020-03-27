package ca.pkay.rcloneexplorer.Items;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.webkit.MimeTypeMap;
import io.github.x0b.rfc3339parser.Rfc3339Parser;
import io.github.x0b.rfc3339parser.Rfc3339Strict;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Locale;

public class FileItem implements Parcelable {

    private RemoteItem remote;
    private String path;
    private String name;
    private String mimeType;
    private long size;
    private String humanReadableSize;
    private long modTime;
    private String humanReadableModTime;
    private String formattedModTime;
    private boolean isDir;
    private static final Rfc3339Parser rfc3339Parser = new Rfc3339Strict();

    public FileItem(RemoteItem remote, String path, String name, long size, String modTime, String mimeType, boolean isDir) {
        this.remote = remote;
        this.path = path;
        this.name = name;
        this.size = size;
        this.humanReadableSize = sizeToHumanReadable(size);
        try {
            this.modTime = rfc3339Parser.parseCalendar(modTime).getTimeInMillis();
        } catch (ParseException e) {
            this.modTime = -1;
        }
        this.humanReadableModTime = modTimeToHumanReadable(this.modTime);
        this.formattedModTime = modTimeToFormattedTime(this.modTime);
        this.mimeType = getMimeType(mimeType, path);
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
        formattedModTime = in.readString();
        mimeType = in.readString();
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

    public String getFormattedModTime() {
        return formattedModTime;
    }

    public long getModTime() {
        return modTime;
    }

    public String getMimeType() { return mimeType; }

    public boolean isDir() {
        return isDir;
    }

    public static String getMimeType(String mimeType, String path) {
        if ("application/octet-stream".equals(mimeType)) {
            String extension = MimeTypeMap.getFileExtensionFromUrl(path);
            if ((extension == null || "".equals(extension)) && path.lastIndexOf('.') < path.length() + 1) {
                extension = path.substring(path.lastIndexOf('.') + 1);
            }
            if (extension != null) {
                String mimeQueryResult = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                if (null != mimeQueryResult) {
                    mimeType = mimeQueryResult;
                }
            }
        }
        return mimeType;
    }

    private String sizeToHumanReadable(long size) {
        int unit = 1000;
        if (size < unit) return size + " B";
        int exp = (int) (Math.log(size) / Math.log(unit));
        Character pre = "kMGTPE".charAt(exp - 1);
        return String.format(Locale.US, "%.1f %sB", size / Math.pow(unit, exp), pre);
    }

    private String modTimeToHumanReadable(long modTime) {
        long now = System.currentTimeMillis();
        if(1 > modTime){
            // mod time is invalid, don't show it
            return "";
        }

        CharSequence humanReadable = DateUtils.getRelativeTimeSpanString(modTime, now, DateUtils.MINUTE_IN_MILLIS);
        if (humanReadable.toString().startsWith("In") || humanReadable.toString().startsWith("0")) {
            humanReadable = "Now";
        }
        return humanReadable.toString();
    }

    private String modTimeToFormattedTime(long modTime) {
        formattedModTime = DateFormat.getDateTimeInstance().format(modTime);
        return formattedModTime;
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
        dest.writeString(formattedModTime);
        dest.writeString(mimeType);
        dest.writeByte((byte) (isDir ? 1 : 0));
    }
}
