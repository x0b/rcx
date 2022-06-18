package ca.pkay.rcloneexplorer.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
        val pendingIntent = PendingIntent.getActivity(mContext, 0, foregroundIntent, 0)
        val cancelIntent = Intent(mContext, cancelClass)
        val cancelPendingIntent = PendingIntent.getBroadcast(mContext, 0, cancelIntent, 0)

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
            .setProgress(100, percent, false)
    }
}