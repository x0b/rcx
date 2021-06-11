package ca.pkay.rcloneexplorer.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import ca.pkay.rcloneexplorer.Items.Task;
import ca.pkay.rcloneexplorer.Items.Trigger;

public class DatabaseHandler extends SQLiteOpenHelper {


    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "rcloneExplorer.db";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DatabaseInfo.SQL_CREATE_TABLES_TASKS);
        sqLiteDatabase.execSQL(DatabaseInfo.SQL_CREATE_TABLE_TRIGGER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            sqLiteDatabase.execSQL(DatabaseInfo.SQL_CREATE_TABLE_TRIGGER);
        }
    }

    public List<Task> getAllTasks(){
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

        Cursor cursor = db.query(
                Task.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        List<Task> results = new ArrayList<>();
        while(cursor.moveToNext()) {

            Task task = new Task(cursor.getLong(0));
            task.setTitle(cursor.getString(1));
            task.setRemote_id(cursor.getString(2));
            task.setRemote_type(cursor.getInt(3));
            task.setRemote_path(cursor.getString(4));
            task.setLocal_path(cursor.getString(5));
            task.setDirection(cursor.getInt(6));

            results.add(task);
        }
        cursor.close();
        db.close();

        return results;

    }

    public Task getTask(Long id){
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

        String selection = Task.COLUMN_NAME_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(id)};

        String sortOrder = Task.COLUMN_NAME_ID + " ASC";

        Cursor cursor = db.query(
                Task.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        List<Task> results = new ArrayList<>();
        while(cursor.moveToNext()) {

            Task task = new Task(cursor.getLong(0));
            task.setTitle(cursor.getString(1));
            task.setRemote_id(cursor.getString(2));
            task.setRemote_type(cursor.getInt(3));
            task.setRemote_path(cursor.getString(4));
            task.setLocal_path(cursor.getString(5));
            task.setDirection(cursor.getInt(6));

            results.add(task);
        }
        cursor.close();
        db.close();

        return results.get(0);

    }

    public Task createEntry(Task taskToStore){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Task.COLUMN_NAME_TITLE, taskToStore.getTitle());
        values.put(Task.COLUMN_NAME_LOCAL_PATH, taskToStore.getLocal_path());
        values.put(Task.COLUMN_NAME_REMOTE_ID, taskToStore.getRemote_id());
        values.put(Task.COLUMN_NAME_REMOTE_PATH, taskToStore.getRemote_path());
        values.put(Task.COLUMN_NAME_REMOTE_TYPE, taskToStore.getRemote_type());
        values.put(Task.COLUMN_NAME_SYNC_DIRECTION, taskToStore.getDirection());

        long newRowId = db.insert(Task.TABLE_NAME, null, values);
        db.close();

        Task newObject = new Task(newRowId);
        newObject.setTitle(taskToStore.getTitle());
        newObject.setLocal_path(taskToStore.getLocal_path());
        newObject.setRemote_id(taskToStore.getRemote_id());
        newObject.setRemote_path(taskToStore.getRemote_path());
        newObject.setRemote_type(taskToStore.getRemote_type());
        newObject.setDirection(taskToStore.getDirection());

        return newObject;

    }

    public void updateEntry(Task taskToUpdate) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Task.COLUMN_NAME_TITLE, taskToUpdate.getTitle());
        values.put(Task.COLUMN_NAME_LOCAL_PATH, taskToUpdate.getLocal_path());
        values.put(Task.COLUMN_NAME_REMOTE_ID, taskToUpdate.getRemote_id());
        values.put(Task.COLUMN_NAME_REMOTE_PATH, taskToUpdate.getRemote_path());
        values.put(Task.COLUMN_NAME_REMOTE_TYPE, taskToUpdate.getRemote_type());
        values.put(Task.COLUMN_NAME_SYNC_DIRECTION, taskToUpdate.getDirection());

        db.update(Task.TABLE_NAME, values, Task.COLUMN_NAME_ID+" = ?", new String[]{String.valueOf(taskToUpdate.getId())});
        db.close();

    }

    public int deleteEntry(long id){
        SQLiteDatabase db = getWritableDatabase();
        String selection = Task.COLUMN_NAME_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(id)};
        int retcode = db.delete(Task.TABLE_NAME, selection, selectionArgs);
        db.close();
        return retcode;

    }


    public List<Trigger> getAllTrigger(){
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = getTriggerProjection();

        String selection = "";
        String[] selectionArgs = {};

        String sortOrder = Trigger.COLUMN_NAME_ID + " ASC";

        Cursor cursor = db.query(
                Trigger.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        List<Trigger> results = new ArrayList<>();
        while(cursor.moveToNext()) {
            results.add(triggerFromCursor(cursor));
        }
        cursor.close();
        db.close();
        return results;
    }

    public Trigger getTrigger(Long id){
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = getTriggerProjection();

        String selection = Trigger.COLUMN_NAME_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(id)};
        String sortOrder = Trigger.COLUMN_NAME_ID + " ASC";

        Cursor cursor = db.query(
                Trigger.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        List<Trigger> results = new ArrayList<>();
        while(cursor.moveToNext()) {
            results.add(triggerFromCursor(cursor));
        }
        cursor.close();
        db.close();
        return results.get(0);
    }

    public Trigger createTrigger(Trigger triggerToStore){
        SQLiteDatabase db = getWritableDatabase();
        long newRowId = db.insert(Trigger.TABLE_NAME, null, getTriggerContentValues(triggerToStore));
        db.close();
        triggerToStore.setId(newRowId);
        return triggerToStore;

    }

    public void updateTrigger(Trigger triggerToUpdate) {
        SQLiteDatabase db = getWritableDatabase();
        db.update(Trigger.TABLE_NAME, getTriggerContentValuesWithID(triggerToUpdate), Trigger.COLUMN_NAME_ID+" = ?", new String[]{String.valueOf(triggerToUpdate.getId())});
        db.close();
    }

    public int deleteTrigger(long id){
        SQLiteDatabase db = getWritableDatabase();
        String selection = Trigger.COLUMN_NAME_ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(id)};
        int retcode = db.delete(Trigger.TABLE_NAME, selection, selectionArgs);
        db.close();
        return retcode;

    }


    private ContentValues getTriggerContentValuesWithID(Trigger t){
        ContentValues values = getTriggerContentValues(t);
        values.put(Trigger.COLUMN_NAME_ID, t.getId());
        return values;
    }
    private ContentValues getTriggerContentValues(Trigger t){
        ContentValues values = new ContentValues();
        values.put(Trigger.COLUMN_NAME_TITLE, t.getTitle());
        values.put(Trigger.COLUMN_NAME_ENABLED, t.isEnabled());
        values.put(Trigger.COLUMN_NAME_TIME, t.getTime());
        values.put(Trigger.COLUMN_NAME_WEEKDAY, t.getWeekdays());
        values.put(Trigger.COLUMN_NAME_TARGET, t.getWhatToTrigger());
        return values;
    }
    private String[] getTriggerProjection(){
        String[] projection = {
                Trigger.COLUMN_NAME_ID,
                Trigger.COLUMN_NAME_TITLE,
                Trigger.COLUMN_NAME_ENABLED,
                Trigger.COLUMN_NAME_TIME,
                Trigger.COLUMN_NAME_WEEKDAY,
                Trigger.COLUMN_NAME_TARGET
        };
        return projection;
    }

    private Trigger triggerFromCursor(Cursor cursor){
        Trigger trigger = new Trigger(cursor.getLong(0));
        trigger.setTitle(cursor.getString(1));
        trigger.setEnabled(cursor.getInt(2) == 1);
        trigger.setTime(cursor.getInt(3));
        int weekdays = cursor.getInt(4);
        trigger.setWeekdays((byte)weekdays);
        trigger.setWhatToTrigger(cursor.getLong(5));
        return trigger;
    }
}
