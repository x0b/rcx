package ca.pkay.rcloneexplorer.Database;

public class Task {

    public static String TABLE_NAME = "task_table";

    public static String COLUMN_NAME_ID= "task_id";
    public static String COLUMN_NAME_TITLE = "task_title";
    public static String COLUMN_NAME_REMOTE_ID = "task_remote_id";
    public static String COLUMN_NAME_REMOTE_TYPE = "task_remote_type";
    public static String COLUMN_NAME_REMOTE_PATH = "task_remote_path";
    public static String COLUMN_NAME_LOCAL_PATH = "task_local_path";
    public static String COLUMN_NAME_SYNC_DIRECTION = "task_direction";

    private Long id;

    private String title="";
    private String remote_id="";
    private int remote_type=0;
    private String remote_path="";
    private String local_path = "";
    private int direction=0;


    public Task(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRemote_id() {
        return remote_id;
    }

    public void setRemote_id(String remote_id) {
        this.remote_id = remote_id;
    }

    public int getRemote_type() {
        return remote_type;
    }

    public void setRemote_type(int remote_type) {
        this.remote_type = remote_type;
    }

    public String getRemote_path() {
        return remote_path;
    }

    public void setRemote_path(String remote_path) {
        this.remote_path = remote_path;
    }

    public String getLocal_path() {
        return local_path;
    }

    public void setLocal_path(String local_path) {
        this.local_path = local_path;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public String toString(){
        return title + ": " + remote_id + ": " + remote_type + ": " + remote_path + ": " + local_path + ": " + direction;
    }
}
