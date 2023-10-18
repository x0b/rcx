package ca.pkay.rcloneexplorer.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ca.pkay.rcloneexplorer.R

class AppErrorNotificationManager(var mContext: Context) {

    companion object {
        private const val APP_ERROR_CHANNEL_ID =
            "ca.pkay.rcloneexplorer.notifications.AppErrorNotificationManager"
        private const val APP_ERROR_ID =
            "ca.pkay.rcloneexplorer.notifications.AppErrorNotificationManager"
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

    fun showNotification() {
        val b = NotificationCompat.Builder(
            mContext,
            APP_ERROR_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_twotone_error_24)
            .setContentTitle("title")
            .setContentText("content")
            //.setContentIntent(pendingIntent)
            //.setStyle(NotificationCompat.BigTextStyle().bigText(bigText.toString()))
            //.addAction(
            //    R.drawable.ic_cancel_download,
            //    mContext.getString(R.string.cancel),
            //   cancelPendingIntent
            //)
            .setOnlyAlertOnce(true)
        //.setProgress(100, percent, false)

        val notificationManager = NotificationManagerCompat.from(mContext)
        notificationManager.notify(1134, b.build())
    }
}