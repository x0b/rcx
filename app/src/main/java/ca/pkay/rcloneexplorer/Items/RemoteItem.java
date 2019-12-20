package ca.pkay.rcloneexplorer.Items;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import ca.pkay.rcloneexplorer.R;

public class RemoteItem implements Comparable<RemoteItem>, Parcelable {

    public final static int AMAZON_DRIVE = 10;
    public final static int S3 = 11;
    public final static int B2 = 12;
    public final static int BOX = 13;
    public final static int CACHE = 14;
    public final static int DROPBOX = 15;
    public final static int FTP = 16;
    public final static int GOOGLE_CLOUD_STORAGE = 17;
    public final static int GOOGLE_DRIVE = 18;
    public final static int HUBIC = 19;
    public final static int LOCAL = 20;
    public final static int MEGA = 21;
    public final static int AZUREBLOB = 22;
    public final static int ONEDRIVE = 23;
    public final static int SWIFT = 24;
    public final static int PCLOUD = 25;
    public final static int QINGSTOR = 26;
    public final static int SFTP = 27;
    public final static int WEBDAV = 28;
    public final static int YANDEX = 29;
    public final static int HTTP = 30;
    private String name;
    private int type;
    private String typeReadable;
    private boolean isCrypt;
    private boolean isAlias;
    private boolean isCache;
    private boolean isPinned;
    private boolean isDrawerPinned;

    public RemoteItem(String name, String type) {
        this.name = name;
        this.typeReadable = type;
        this.type = getTypeFromString(type);
    }

    public RemoteItem(String name, int type, String typeReadable) {
        this.name = name;
        this.typeReadable = typeReadable;
        this.type = type;
    }

    private RemoteItem(Parcel in) {
        name = in.readString();
        type = in.readInt();
        typeReadable = in.readString();
        isCrypt = in.readByte() != 0;
        isAlias = in.readByte() != 0;
        isCache = in.readByte() != 0;
        isPinned = in.readByte() != 0;
        isDrawerPinned = in.readByte() != 0;
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
        switch (type) {
            case GOOGLE_DRIVE:
            case PCLOUD:
            case YANDEX:
                return true;
            default:
                return false;
        }
    }

    public boolean isDirectoryModifiedTimeSupported() {
        switch (type) {
            case DROPBOX:
            case B2:
            case HUBIC:
                return false;
            default:
                return true;
        }
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public void setType(String type) {
        this.typeReadable = type;
        this.type = getTypeFromString(type);
    }

    public String getTypeReadable() {
        return this.typeReadable;
    }

    public boolean isCrypt() {
        return this.isCrypt;
    }

    public RemoteItem setIsCrypt(boolean isCrypt) {
        this.isCrypt = isCrypt;
        return this;
    }

    public boolean isAlias() {
        return this.isAlias;
    }

    public RemoteItem setIsAlias(boolean isAlias) {
        this.isAlias = isAlias;
        return this;
    }

    public boolean isCache() {
        return this.isCache;
    }

    public RemoteItem setIsCache(boolean isCache) {
        this.isCache = isCache;
        return this;
    }

    public RemoteItem pin(boolean isPinned) {
        this.isPinned = isPinned;
        return this;
    }

    public boolean isPinned() {
        return this.isPinned;
    }

    public RemoteItem setDrawerPinned(boolean isPinned) {
        this.isDrawerPinned = isPinned;
        return this;
    }

    public boolean isDrawerPinned() {
        return this.isDrawerPinned;
    }

    public boolean isRemoteType(int ...remotes) {
        boolean isSameType = false;

        for (int remote : remotes) {
            if (this.type == remote) {
                isSameType = true;
            }
        }
        return isSameType;
    }

    private int getTypeFromString(String type) {
        switch (type) {
            case "amazon cloud drive":
                return AMAZON_DRIVE;
            case "s3":
                return S3;
            case "b2":
                return B2;
            case "box":
                return BOX;
            case "cache":
                return CACHE;
            case "dropbox":
                return DROPBOX;
            case "ftp":
                return FTP;
            case "google cloud storage":
                return GOOGLE_CLOUD_STORAGE;
            case "drive":
                return GOOGLE_DRIVE;
            case "hubic":
                return HUBIC;
            case "local":
                return LOCAL;
            case "mega":
                return MEGA;
            case "azureblob":
                return AZUREBLOB;
            case "onedrive":
                return ONEDRIVE;
            case "swift":
                return SWIFT;
            case "pcloud":
                return PCLOUD;
            case "qingstor":
                return QINGSTOR;
            case "sftp":
                return SFTP;
            case "webdav":
                return WEBDAV;
            case "yandex":
                return YANDEX;
            case "http":
                return HTTP;
            default:
                return -1;
        }
    }

    public int getRemoteIcon() {
        if (isCrypt()) {
            return R.drawable.ic_lock_black;
        } else {
            switch (type) {
                case RemoteItem.AMAZON_DRIVE:
                    return R.drawable.ic_amazon;
                case RemoteItem.GOOGLE_DRIVE:
                    return R.drawable.ic_google_drive;
                case RemoteItem.DROPBOX:
                    return R.drawable.ic_dropbox;
                case RemoteItem.GOOGLE_CLOUD_STORAGE:
                    return R.drawable.ic_google;
                case RemoteItem.ONEDRIVE:
                    return R.drawable.ic_onedrive;
                case RemoteItem.S3:
                    return R.drawable.ic_amazon;
                case RemoteItem.BOX:
                    return R.drawable.ic_box;
                case RemoteItem.SFTP:
                    return R.drawable.ic_terminal;
                case RemoteItem.LOCAL:
                    return R.drawable.ic_tablet_cellphone;
                default:
                    return R.drawable.ic_cloud;
            }
        }
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
    public boolean equals(Object obj) {
        return obj instanceof RemoteItem && ((RemoteItem) obj).getName().equals(this.name) && ((RemoteItem) obj).getType() == this.type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(type);
        dest.writeString(typeReadable);
        dest.writeByte((byte) (isCrypt ? 1 : 0));
        dest.writeByte((byte) (isAlias ? 1 : 0));
        dest.writeByte((byte) (isCache ? 1 : 0));
        dest.writeByte((byte) (isPinned ? 1 : 0));
        dest.writeByte((byte) (isDrawerPinned ? 1 : 0));
    }
}
