package ca.pkay.rcloneexplorer.notifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ca.pkay.rcloneexplorer.R
import ca.pkay.rcloneexplorer.util.PermissionManager
import ca.pkay.rcloneexplorer.util.SyncLog


class AppErrorNotificationManager(var mContext: Context) {

    companion object {
        private const val APP_ERROR_CHANNEL_ID =
            "ca.pkay.rcloneexplorer.notifications.AppErrorNotificationManager"
        private const val APP_ERROR_ID = 51913
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            val channel = NotificationChannel(
                APP_ERROR_CHANNEL_ID,
                mContext.getString(R.string.app_error_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description =
                mContext.getString(R.string.app_error_notification_channel_description)
            // Register the channel with the system
            val notificationManager =
                mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    fun showNotification() {


        /*val contentIntent = PendingIntent.getActivity(
            mContext,
            APP_ERROR_ID,
            PermissionManager.getNotificationSettingsIntent(mContext), FLAG_IMMUTABLE
        )*/

        val b = NotificationCompat.Builder(mContext, APP_ERROR_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_twotone_error_24)
            .setContentTitle(mContext.getString(R.string.app_error_notification_alarmpermission_missing))
            .setContentText(mContext.getString(R.string.app_error_notification_alarmpermission_missing_description))
            /*.addAction(
                R.drawable.ic_cancel_download,
                mContext.getString(R.string.cancel),
                contentIntent
            )*/
            .setOnlyAlertOnce(true)

        val notificationManager = NotificationManagerCompat.from(mContext)

        if(PermissionManager(mContext).grantedNotifications()) {
            notificationManager.notify(APP_ERROR_ID, b.build())
        } else {
            Log.e("AppErrorNotificationManager", "We dont have Notification Permission!")
        }
    }
}