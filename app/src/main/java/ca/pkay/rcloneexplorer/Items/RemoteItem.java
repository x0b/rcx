package ca.pkay.rcloneexplorer.Items;

import android.support.annotation.NonNull;

public class RemoteItem implements Comparable<RemoteItem> {

    private String name;
    private String type;

    public RemoteItem(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public static boolean hasTrashCan(String remoteType) {
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

    @Override
    public int compareTo(@NonNull RemoteItem remoteItem) {
        return name.compareTo(remoteItem.getName());
    }
}
