package ca.pkay.rcloneexplorer.Database;

class DatabaseInfo {


    public static final String SQL_CREATE_TABLES = "CREATE TABLE " + Task.TABLE_NAME + " (" +
            Task.COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
            Task.COLUMN_NAME_TITLE + " TEXT," +
            Task.COLUMN_NAME_REMOTE_ID + " TEXT," +
            Task.COLUMN_NAME_REMOTE_TYPE + " INTEGER," +
            Task.COLUMN_NAME_REMOTE_PATH+ " TEXT," +
            Task.COLUMN_NAME_LOCAL_PATH + " TEXT," +
            Task.COLUMN_NAME_SYNC_DIRECTION + " INTEGER)";
}
