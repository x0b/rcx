package ca.pkay.rcloneexplorer.Database

import ca.pkay.rcloneexplorer.Items.Task
import ca.pkay.rcloneexplorer.Items.Trigger

class DatabaseInfo {

    companion object {

        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 3
        const val DATABASE_NAME = "rcloneExplorer.db"


        val SQL_CREATE_TABLES_TASKS = "CREATE TABLE " + Task.TABLE_NAME + " (" +
                Task.COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                Task.COLUMN_NAME_TITLE + " TEXT," +
                Task.COLUMN_NAME_REMOTE_ID + " TEXT," +
                Task.COLUMN_NAME_REMOTE_TYPE + " INTEGER," +
                Task.COLUMN_NAME_REMOTE_PATH + " TEXT," +
                Task.COLUMN_NAME_LOCAL_PATH + " TEXT," +
                Task.COLUMN_NAME_SYNC_DIRECTION + " INTEGER)"

        val SQL_CREATE_TABLE_TRIGGER = "CREATE TABLE " + Trigger.TABLE_NAME + " (" +
                Trigger.COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                Trigger.COLUMN_NAME_TITLE + " TEXT," +
                Trigger.COLUMN_NAME_ENABLED + " INTEGER," +
                Trigger.COLUMN_NAME_TIME + " INTEGER," +
                Trigger.COLUMN_NAME_WEEKDAY + " INTEGER," +
                Trigger.COLUMN_NAME_TARGET + " INTEGER)"


        val SQL_UPDATE_TASK_ADD_MD5 = "ALTER TABLE ${Task.TABLE_NAME} ADD COLUMN ${Task.COLUMN_NAME_MD5SUM} INTEGER"
        val SQL_UPDATE_TASK_ADD_WIFI = "ALTER TABLE ${Task.TABLE_NAME} ADD COLUMN ${Task.COLUMN_NAME_WIFI_ONLY} INTEGER"

    }
}