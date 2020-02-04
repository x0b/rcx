package ca.pkay.rcloneexplorer.Items;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import ca.pkay.rcloneexplorer.R;
import io.github.x0b.safdav.file.SafConstants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RemoteItem implements Comparable<RemoteItem>, Parcelable {

    /**
     * A remote of type SAFW, a virtual WebDAV remote made backed by Storage
     * Access Framework (SAF).
     */
    public final static int SAFW = -10;

    public final static int ALIAS = 10;
    public final static int AMAZON_DRIVE = 11;
    public final static int AZUREBLOB = 12;
    public final static int B2 = 13;
    public final static int BOX = 14;
    public final static int CRYPT = 15;
    public final static int CACHE = 16;
    public final static int GOOGLE_DRIVE = 17;
    public final static int DROPBOX = 18;
    public final static int FICHIER = 19;
    public final static int FTP = 20;
    public final static int GOOGLE_CLOUD_STORAGE = 21;
    public final static int GOOGLE_PHOTOS = 22;
    public final static int HTTP = 23;
    public final static int SWIFT = 24;
    public final static int HUBIC = 25;
    public final static int JOTTACLOUD = 26;
    public final static int KOOFR = 27;
    public final static int LOCAL = 28;
    public final static int MEGA = 29;
    public final static int ONEDRIVE = 30;
    public final static int OPENDRIVE = 31;
    public final static int PCLOUD = 32;
    public final static int QINGSTOR = 33;
    public final static int S3 = 34;
    public final static int SFTP = 35;
    public final static int UNION = 36;
    public final static int WEBDAV = 37;
    public final static int YANDEX = 38;
    public final static int SHAREFILE = 39;
    public final static int MAILRU = 40;
    public final static int PUTIO = 41;
    public final static int PREMIUMIZEME = 42;

    private String name;
    private int type;
    private String typeReadable;
    private boolean isCrypt;
    private boolean isAlias;
    private boolean isCache;
    private boolean isPinned;
    private boolean isDrawerPinned;
    private String displayName;

    public RemoteItem(String name, String type) {
        this.name = name;
        this.typeReadable = type;
        this.type = getTypeFromString(type);
    }

    private RemoteItem(Parcel in) {
        name = in.readString();
        displayName = in.readString();
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
            case GOOGLE_PHOTOS:
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

    public String getDisplayName() {
        return displayName != null ? displayName : name;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public static List<RemoteItem> prepareDisplay(Context context, List<RemoteItem> items) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> renamedRemotes = pref.getStringSet(context.getString(R.string.pref_key_renamed_remotes), new HashSet<>());
        for(RemoteItem item : items) {
            if(renamedRemotes.contains(item.name)) {
                String displayName = pref.getString(
                        context.getString(R.string.pref_key_renamed_remote_prefix, item.name), item.name);
                item.displayName = displayName;
            }
        }
        return items;
    }

    private int getTypeFromString(String type) {
        switch (type) {
            case SafConstants.SAF_REMOTE_NAME:
                return SAFW;
            case "alias":
                return ALIAS;
            case "amazon cloud drive":
                return AMAZON_DRIVE;
            case "azureblob":
                return AZUREBLOB;
            case "b2":
                return B2;
            case "box":
                return BOX;
            case "cache":
                return CACHE;
            case "crypt":
                return CRYPT;
            case "dropbox":
                return DROPBOX;
            case "drive":
                return GOOGLE_DRIVE;
            case "fichier":
                return FICHIER;
            case "ftp":
                return FTP;
            case "google cloud storage":
                return GOOGLE_CLOUD_STORAGE;
            case "google photos":
                return GOOGLE_PHOTOS;
            case "http":
                return HTTP;
            case "swift":
                return SWIFT;
            case "hubic":
                return HUBIC;
            case "jottacloud":
                return JOTTACLOUD;
            case "koofr":
                return KOOFR;
            case "local":
                return LOCAL;
            case "mega":
                return MEGA;
            case "onedrive":
                return ONEDRIVE;
            case "opendrive":
                return OPENDRIVE;
            case "pcloud":
                return PCLOUD;
            case "qingstor":
                return QINGSTOR;
            case "s3":
                return S3;
            case "sftp":
                return SFTP;
            case "union":
                return UNION;
            case "webdav":
                return WEBDAV;
            case "yandex":
                return YANDEX;
            case "sharefile":
                return SHAREFILE;
            case "mailru":
                return MAILRU;
            case "putio":
                return PUTIO;
            case "premiumizeme":
                return PREMIUMIZEME;
            default:
                return -1;
        }
    }

    public int getRemoteIcon() {
        if (isCrypt()) {
            return R.drawable.ic_lock_black;
        } else {
            switch (type) {
                case RemoteItem.SAFW:
                    return R.drawable.ic_tablet_cellphone;
                case RemoteItem.AMAZON_DRIVE:
                    return R.drawable.ic_amazon;
                case RemoteItem.GOOGLE_DRIVE:
                    return R.drawable.ic_google_drive;
                case RemoteItem.DROPBOX:
                    return R.drawable.ic_dropbox;
                case RemoteItem.GOOGLE_CLOUD_STORAGE:
                    return R.drawable.ic_google;
                case RemoteItem.GOOGLE_PHOTOS:
                    return R.drawable.ic_google_photos;
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
                case RemoteItem.UNION:
                    return R.drawable.ic_union_24dp;
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
        dest.writeString(displayName);
        dest.writeInt(type);
        dest.writeString(typeReadable);
        dest.writeByte((byte) (isCrypt ? 1 : 0));
        dest.writeByte((byte) (isAlias ? 1 : 0));
        dest.writeByte((byte) (isCache ? 1 : 0));
        dest.writeByte((byte) (isPinned ? 1 : 0));
        dest.writeByte((byte) (isDrawerPinned ? 1 : 0));
    }
}
