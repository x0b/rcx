package ca.pkay.rcloneexplorer.Items;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class RemoteItem implements Comparable<RemoteItem>, Parcelable {

    private String name;
    private String type;
    private String remote;

    public RemoteItem(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public RemoteItem(String name, String type, String remote) {
        this(name, type);
        this.remote = remote;
    }

    private RemoteItem(Parcel in) {
        name = in.readString();
        type = in.readString();
        remote = in.readString();
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

    @Override
    public int compareTo(@NonNull RemoteItem remoteItem) {
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
    }
}
