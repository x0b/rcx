package ca.pkay.rcloneexplorer.util

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.app.ActivityCompat

class PermissionManager(var mContext: Context) {

    companion object {
        const val PERM_NOTIFICATIONS = Manifest.permission.POST_NOTIFICATIONS
        const val PERM_ALARMS = Manifest.permission.SCHEDULE_EXACT_ALARM
        const val PERM_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE

    }

    public fun hasAllRequiredPermissions(): Boolean {
        //if(!checkGenericPermission(context, PERM_NOTIFICATIONS)) {
        //    return false
        //}
        //if(!checkGenericPermission(context, PERM_ALARMS)) {
        //    return false
        //}
        if(!checkGenericPermission(PERM_STORAGE)) {
            return false
        }
        return true
    }

    fun grantedAlarms(): Boolean {
        return checkGenericPermission(PERM_ALARMS)
    }

    fun grantedStorage(): Boolean {
        return checkGenericPermission(PERM_STORAGE)
    }
    fun grantedNotifications(): Boolean {
        return checkGenericPermission(PERM_NOTIFICATIONS)
    }

    fun checkGenericPermission(permission: String): Boolean {
        if(permission == PERM_ALARMS) {
            val alarmManager: AlarmManager = mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                false
            }
        } else if(permission == PERM_STORAGE) {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            }
        } else {
            return mContext.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        }
    }

}