package ca.pkay.rcloneexplorer.Items;

public class RemoteItem {

    private String name;
    private String type;

    public RemoteItem(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
