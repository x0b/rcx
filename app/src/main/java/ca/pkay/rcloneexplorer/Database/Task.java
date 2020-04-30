package ca.pkay.rcloneexplorer.Database;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Task implements Parcelable {

    public static final String TABLE_NAME = "task_table";

    public static final String COLUMN_NAME_ID = "id";
    public static final String COLUMN_NAME_TITLE = "title";
    public static final String COLUMN_NAME_REMOTE_ID = "remote_id";
    public static final String COLUMN_NAME_REMOTE_TYPE = "remote_type";
    public static final String COLUMN_NAME_REMOTE_PATH = "remote_path";
    public static final String COLUMN_NAME_LOCAL_PATH = "local_path";
    public static final String COLUMN_NAME_SYNC_DIRECTION = "direction";

    private Long id;
    private String title = "";
    private String remoteId = "";
    private String remoteType = "";
    private String remotePath = "";
    private String localPath = "";
    private int direction = 0;

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

    public String getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }

    public String getRemoteType() {
        return remoteType;
    }

    public void setRemoteType(String remoteType) {
        this.remoteType = remoteType;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public String toString() {
        return title + ": " + remoteId + ": " + remoteType + ": " + remotePath + ": " + localPath + ": " + direction;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.title);
        dest.writeString(this.remoteId);
        dest.writeString(this.remoteType);
        dest.writeString(this.remotePath);
        dest.writeString(this.localPath);
        dest.writeInt(this.direction);
    }

    protected Task(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.title = in.readString();
        this.remoteId = in.readString();
        this.remoteType = in.readString();
        this.remotePath = in.readString();
        this.localPath = in.readString();
        this.direction = in.readInt();
    }

    public static final Parcelable.Creator<Task> CREATOR = new Parcelable.Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel source) {
            return new Task(source);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };
}
