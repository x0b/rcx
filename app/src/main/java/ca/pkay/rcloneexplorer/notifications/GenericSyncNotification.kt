package ca.pkay.rcloneexplorer.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import ca.pkay.rcloneexplorer.R

class GenericSyncNotification(var mContext: Context) {

    fun updateGenericNotification(
        title: String?,
        content: String?,
        icon: Int,
        bigTextArray: ArrayList<String?>,
        percent: Int,
        cls: Class<*>?,
        cancelClass: Class<*>?,
        channelID: String?
    ): NotificationCompat.Builder? {
        val bigText = StringBuilder()
        for (i in bigTextArray.indices) {
            bigText.append(bigTextArray[i])
            if (i < 4) {
                bigText.append("\n")
            }
        }
        val foregroundIntent = Intent(mContext, cls)
        val pendingIntent = PendingIntent.getActivity(mContext, 0, foregroundIntent, FLAG_IMMUTABLE)
        val cancelIntent = Intent(mContext, cancelClass)
        val cancelPendingIntent = PendingIntent.getBroadcast(mContext, 0, cancelIntent, FLAG_IMMUTABLE)

        return NotificationCompat.Builder(
            mContext,
            channelID!!
        )
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText.toString()))
            .addAction(
                R.drawable.ic_cancel_download,
                mContext.getString(R.string.cancel),
                cancelPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setProgress(100, percent, false)
    }

    fun setNotificationChannel(channelID: String, channelName: String, descriptionResource: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            val channel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_LOW)
            channel.description =
                mContext.getString(descriptionResource)
            // Register the channel with the system
            val notificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }
}
