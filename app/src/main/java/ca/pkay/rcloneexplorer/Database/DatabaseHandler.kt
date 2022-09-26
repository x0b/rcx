package ca.pkay.rcloneexplorer.Database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import ca.pkay.rcloneexplorer.Database.DatabaseInfo.Companion.DATABASE_NAME
import ca.pkay.rcloneexplorer.Database.DatabaseInfo.Companion.DATABASE_VERSION
import ca.pkay.rcloneexplorer.Database.DatabaseInfo.Companion.SQL_CREATE_TABLES_TASKS
import ca.pkay.rcloneexplorer.Database.DatabaseInfo.Companion.SQL_CREATE_TABLE_TRIGGER
import ca.pkay.rcloneexplorer.Database.DatabaseInfo.Companion.SQL_UPDATE_TASK_ADD_MD5
import ca.pkay.rcloneexplorer.Database.DatabaseInfo.Companion.SQL_UPDATE_TASK_ADD_WIFI
import ca.pkay.rcloneexplorer.Items.Task
import ca.pkay.rcloneexplorer.Items.Trigger
import java.util.ArrayList

class DatabaseHandler(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_TABLES_TASKS)
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_TRIGGER)
        sqLiteDatabase.execSQL(SQL_UPDATE_TASK_ADD_MD5)
        sqLiteDatabase.execSQL(SQL_UPDATE_TASK_ADD_WIFI)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            sqLiteDatabase.execSQL(SQL_CREATE_TABLE_TRIGGER)
        }
        if (oldVersion < 3) {
            sqLiteDatabase.execSQL(SQL_UPDATE_TASK_ADD_MD5)
            sqLiteDatabase.execSQL(SQL_UPDATE_TASK_ADD_WIFI)
        }
    }

    val allTasks: List<Task>
        get() {
            val db = readableDatabase
            val selection = ""
            val selectionArgs = arrayOf<String>()
            val sortOrder = Task.COLUMN_NAME_ID + " ASC"
            val cursor = db.query(
                Task.TABLE_NAME,
                taskProjection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
            )
            val results: MutableList<Task> = ArrayList()
            while (cursor.moveToNext()) {
                results.add(taskFromCursor(cursor))
            }
            cursor.close()
            db.close()
            return results
        }

    fun getTask(id: Long): Task? {
        val db = readableDatabase
        val selection = Task.COLUMN_NAME_ID + " LIKE ?"
        val selectionArgs = arrayOf(id.toString())
        val sortOrder = Task.COLUMN_NAME_ID + " ASC"
        val cursor = db.query(
            Task.TABLE_NAME,
            taskProjection,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder
        )
        val results: MutableList<Task> = ArrayList()
        while (cursor.moveToNext()) {
            results.add(taskFromCursor(cursor))
        }
        cursor.close()
        db.close()
        return if (results.size == 0) {
            null
        } else results[0]
    }

    fun createTask(taskToStore: Task): Task {
        val db = writableDatabase
        val newRowId = db.insert(Task.TABLE_NAME, null, getTaskContentValues(taskToStore))
        db.close()
        taskToStore.id = newRowId
        return taskToStore
    }

    fun updateTask(taskToUpdate: Task) {
        val db = writableDatabase
        db.update(
            Task.TABLE_NAME,
            getTaskContentValues(taskToUpdate),
            Task.COLUMN_NAME_ID + " = ?",
            arrayOf(taskToUpdate.id.toString())
        )
        db.close()
    }

    private val taskProjection: Array<String>
        private get() = arrayOf(
            Task.COLUMN_NAME_ID,
            Task.COLUMN_NAME_TITLE,
            Task.COLUMN_NAME_REMOTE_ID,
            Task.COLUMN_NAME_REMOTE_TYPE,
            Task.COLUMN_NAME_REMOTE_PATH,
            Task.COLUMN_NAME_LOCAL_PATH,
            Task.COLUMN_NAME_SYNC_DIRECTION,
            Task.COLUMN_NAME_MD5SUM,
            Task.COLUMN_NAME_WIFI_ONLY
        )

    private fun taskFromCursor(cursor: Cursor): Task {
        val task = Task(cursor.getLong(0))
        task.title = cursor.getString(1)
        task.remoteId = cursor.getString(2)
        task.remoteType = cursor.getInt(3)
        task.remotePath = cursor.getString(4)
        task.localPath = cursor.getString(5)
        task.direction = cursor.getInt(6)
        task.md5sum = getBoolean(cursor, 7)
        task.wifionly = getBoolean(cursor, 8)
        return task
    }

    private fun getTaskContentValuesWithID(task: Task): ContentValues {
        val values = getTaskContentValues(task)
        values.put(Task.COLUMN_NAME_ID, task.id)
        return values
    }

    fun deleteTask(id: Long): Int {
        val db = writableDatabase
        val selection = Task.COLUMN_NAME_ID + " LIKE ?"
        val selectionArgs = arrayOf(id.toString())
        val retcode = db.delete(Task.TABLE_NAME, selection, selectionArgs)
        db.close()
        return retcode
    }

    private fun getTaskContentValues(task: Task): ContentValues {
        val values = ContentValues()
        values.put(Task.COLUMN_NAME_TITLE, task.title)
        values.put(Task.COLUMN_NAME_LOCAL_PATH, task.localPath)
        values.put(Task.COLUMN_NAME_REMOTE_ID, task.remoteId)
        values.put(Task.COLUMN_NAME_REMOTE_PATH, task.remotePath)
        values.put(Task.COLUMN_NAME_REMOTE_TYPE, task.remoteType)
        values.put(Task.COLUMN_NAME_SYNC_DIRECTION, task.direction)
        values.put(Task.COLUMN_NAME_MD5SUM, task.md5sum)
        values.put(Task.COLUMN_NAME_WIFI_ONLY, task.wifionly)
        return values
    }

    val allTrigger: List<Trigger>
        get() {
            val db = readableDatabase
            val projection = triggerProjection
            val selection = ""
            val selectionArgs = arrayOf<String>()
            val sortOrder = Trigger.COLUMN_NAME_ID + " ASC"
            val cursor = db.query(
                Trigger.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
            )
            val results: MutableList<Trigger> = ArrayList()
            while (cursor.moveToNext()) {
                results.add(triggerFromCursor(cursor))
            }
            cursor.close()
            db.close()
            return results
        }

    fun getTrigger(id: Long): Trigger? {
        val db = readableDatabase
        val projection = triggerProjection
        val selection = Trigger.COLUMN_NAME_ID + " LIKE ?"
        val selectionArgs = arrayOf(id.toString())
        val sortOrder = Trigger.COLUMN_NAME_ID + " ASC"
        val cursor = db.query(
            Trigger.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder
        )
        val results: MutableList<Trigger> = ArrayList()
        while (cursor.moveToNext()) {
            results.add(triggerFromCursor(cursor))
        }
        cursor.close()
        db.close()
        return if (results.size == 0) {
            null
        } else results[0]
    }

    fun createTrigger(triggerToStore: Trigger): Trigger {
        val db = writableDatabase
        val newRowId = db.insert(Trigger.TABLE_NAME, null, getTriggerContentValues(triggerToStore))
        db.close()
        triggerToStore.id = newRowId
        return triggerToStore
    }

    fun updateTrigger(triggerToUpdate: Trigger) {
        val db = writableDatabase
        db.update(
            Trigger.TABLE_NAME,
            getTriggerContentValuesWithID(triggerToUpdate),
            Trigger.COLUMN_NAME_ID + " = ?",
            arrayOf(triggerToUpdate.id.toString())
        )
        db.close()
    }

    fun deleteTrigger(id: Long): Int {
        val db = writableDatabase
        val selection = Trigger.COLUMN_NAME_ID + " LIKE ?"
        val selectionArgs = arrayOf(id.toString())
        val retcode = db.delete(Trigger.TABLE_NAME, selection, selectionArgs)
        db.close()
        return retcode
    }

    private fun getTriggerContentValuesWithID(t: Trigger): ContentValues {
        val values = getTriggerContentValues(t)
        values.put(Trigger.COLUMN_NAME_ID, t.id)
        return values
    }

    private fun getTriggerContentValues(t: Trigger): ContentValues {
        val values = ContentValues()
        values.put(Trigger.COLUMN_NAME_TITLE, t.title)
        values.put(Trigger.COLUMN_NAME_ENABLED, t.isEnabled)
        values.put(Trigger.COLUMN_NAME_TIME, t.time)
        values.put(Trigger.COLUMN_NAME_WEEKDAY, t.weekdays)
        values.put(Trigger.COLUMN_NAME_TARGET, t.whatToTrigger)
        return values
    }

    private val triggerProjection: Array<String>
        private get() = arrayOf(
            Trigger.COLUMN_NAME_ID,
            Trigger.COLUMN_NAME_TITLE,
            Trigger.COLUMN_NAME_ENABLED,
            Trigger.COLUMN_NAME_TIME,
            Trigger.COLUMN_NAME_WEEKDAY,
            Trigger.COLUMN_NAME_TARGET
        )

    private fun triggerFromCursor(cursor: Cursor): Trigger {
        val trigger = Trigger(cursor.getLong(0))
        trigger.title = cursor.getString(1)
        trigger.isEnabled = cursor.getInt(2) == 1
        trigger.time = cursor.getInt(3)
        val weekdays = cursor.getInt(4)
        trigger.setWeekdays(weekdays.toByte())
        trigger.whatToTrigger = cursor.getLong(5)
        return trigger
    }

    fun deleteEveryting() {
        for (trigger in allTrigger) {
            deleteTrigger(trigger.id)
        }
        for (task in allTasks) {
            deleteTask(task.id)
        }
    }

    private fun getBoolean(cursor: Cursor, cursorid: Int): Boolean {
        return when(cursor.getInt(cursorid)){
            0 -> false
            1 -> true
            else -> true
        }
    }
}