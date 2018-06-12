package ca.pkay.rcloneexplorer.Items;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class RemoteItem implements Comparable<RemoteItem>, Parcelable {

    private String name;
    private String type;
    private String remote;
    private boolean isCrypt;
    private boolean isPinned;

    public RemoteItem(String name, String type) {
        this.name = name;
        this.type = type;
        this.isPinned = false;
        this.isCrypt = type.equals("crypt");
    }

    public RemoteItem(String name, String type, String remote) {
        this(name, type);
        this.remote = remote;
        this.isCrypt = remote.equals("crypt") || type.equals("crypt");
    }

    private RemoteItem(Parcel in) {
        name = in.readString();
        type = in.readString();
        remote = in.readString();
        isCrypt = in.readByte() != 0;
    }

    public static final Creator<RemoteItem> CREATOR = new Creator<RemoteItem>() {
        @Override
        public RemoteItem createFromParcel(Parcel in) {
            return new RemoteItem(in);
        }

        @Override
        public RemoteItem[] newArray(int size) {
            return new RemoteItem[size];
        }
    };

    public boolean hasTrashCan() {
        String remoteType = type;
        if (remote != null && !remote.trim().isEmpty()) {
            remoteType = remote;
        }
        switch (remoteType) {
            case "drive":
            case "pcloud":
            case "yandex":
                return true;
            default:
                return false;
        }
    }

    public boolean isDirectoryModifiedTimeSupported() {
        String remoteType = type;
        if (remote != null && !remote.trim().isEmpty()) {
            remoteType = remote;
        }
        switch (remoteType) {
            case "dropbox":
            case "b2":
            case "hubic":
                return false;
            default:
                return true;
        }
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean hasRemote() {
        return remote != null && !remote.trim().isEmpty();
    }

    public String getRemote() {
        return remote;
    }

    public boolean isCrypt() {
        return this.isCrypt;
    }

    public void setIsCrypt(boolean isCrypt) {
        this.isCrypt = isCrypt;
    }

    public void pin(boolean isPinned) {
        this.isPinned = isPinned;
    }

    public boolean isPinned() {
        return this.isPinned;
    }

    @Override
    public int compareTo(@NonNull RemoteItem remoteItem) {
        if (this.isPinned && !remoteItem.isPinned()) {
            return -1;
        } else if (!this.isPinned && remoteItem.isPinned) {
            return 1;
        }
        return name.toLowerCase().compareTo(remoteItem.getName().toLowerCase());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(type);
        dest.writeString(remote);
        dest.writeByte((byte) (isCrypt ? 1 : 0));
    }
}
