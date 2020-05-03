package ca.pkay.rcloneexplorer.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "rcx.db";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DatabaseInfo.SQL_CREATE_TABLES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }

    public List<Task> getAllTasks() {
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                Task.COLUMN_NAME_ID,
                Task.COLUMN_NAME_TITLE,
                Task.COLUMN_NAME_REMOTE_ID,
                Task.COLUMN_NAME_REMOTE_TYPE,
                Task.COLUMN_NAME_REMOTE_PATH,
                Task.COLUMN_NAME_LOCAL_PATH,
                Task.COLUMN_NAME_SYNC_DIRECTION
        };

        String selection = "";
        String[] selectionArgs = {};

        String sortOrder = Task.COLUMN_NAME_ID + " ASC";

        List<Task> results = new ArrayList<>();
        try (Cursor cursor = db.query(
                Task.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        )) {
            while (cursor.moveToNext()) {
                Task task = new Task(cursor.getLong(0));
                task.setTitle(cursor.getString(1));
                task.setRemoteId(cursor.getString(2));
                task.setRemoteType(cursor.getString(3));
                task.setRemotePath(cursor.getString(4));
                task.setLocalPath(cursor.getString(5));
                task.setDirection(cursor.getInt(6));

                results.add(task);
            }
        }
        db.close();
        return results;
    }

    public Task createTask(Task taskToStore) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Task.COLUMN_NAME_TITLE, taskToStore.getTitle());
        values.put(Task.COLUMN_NAME_LOCAL_PATH, taskToStore.getLocalPath());
        values.put(Task.COLUMN_NAME_REMOTE_ID, taskToStore.getRemoteId());
        values.put(Task.COLUMN_NAME_REMOTE_PATH, taskToStore.getRemotePath());
        values.put(Task.COLUMN_NAME_REMOTE_TYPE, taskToStore.getRemoteType());
        values.put(Task.COLUMN_NAME_SYNC_DIRECTION, taskToStore.getDirection());

        long newRowId = db.insert(Task.TABLE_NAME, null, values);

        Task task = new Task(newRowId);
        task.setTitle(taskToStore.getTitle());
        task.setLocalPath(taskToStore.getLocalPath());
        task.setRemoteId(taskToStore.getRemoteId());
        task.setRemotePath(taskToStore.getRemotePath());
        task.setRemoteType(taskToStore.getRemoteType());
        task.setDirection(taskToStore.getDirection());

        return task;
    }

    public void updateTask(Task taskToUpdate) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Task.COLUMN_NAME_TITLE, taskToUpdate.getTitle());
        values.put(Task.COLUMN_NAME_LOCAL_PATH, taskToUpdate.getLocalPath());
        values.put(Task.COLUMN_NAME_REMOTE_ID, taskToUpdate.getRemoteId());
        values.put(Task.COLUMN_NAME_REMOTE_PATH, taskToUpdate.getRemotePath());
        values.put(Task.COLUMN_NAME_REMOTE_TYPE, taskToUpdate.getRemoteType());
        values.put(Task.COLUMN_NAME_SYNC_DIRECTION, taskToUpdate.getDirection());

        db.update(Task.TABLE_NAME, values, Task.COLUMN_NAME_ID + " = ?", new String[]{String.valueOf(taskToUpdate.getId())});
    }

    public int deleteEntry(long id) {
        SQLiteDatabase db = getWritableDatabase();
        String selection = Task.COLUMN_NAME_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(id)};
        return db.delete(Task.TABLE_NAME, selection, selectionArgs);
    }

    public void deleteAll () {
        SQLiteDatabase database = getReadableDatabase();
        database.delete(Task.TABLE_NAME, null, null);
    }

}
