package ca.pkay.rcloneexplorer.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ca.pkay.rcloneexplorer.BroadcastReceivers.DownloadCancelAction
import ca.pkay.rcloneexplorer.BroadcastReceivers.SyncCancelAction
import ca.pkay.rcloneexplorer.R
import ca.pkay.rcloneexplorer.Services.SyncService

class DownloadNotifications(var mContext: Context) {


    companion object {
        const val CHANNEL_ID = "ca.pkay.rcexplorer.DOWNLOAD_CHANNEL"
        private const val DOWNLOAD_FINISHED_GROUP = "ca.pkay.rcexplorer.DOWNLOAD_FINISHED_GROUP"
        private const val DOWNLOAD_FAILED_GROUP = "ca.pkay.rcexplorer.DOWNLOAD_FAILED_GROUP"
        private const val CHANNEL_NAME = "Downloads"
        const val PERSISTENT_NOTIFICATION_ID = 167
        private const val FAILED_DOWNLOAD_NOTIFICATION_ID = 138
        private const val DOWNLOAD_FINISHED_NOTIFICATION_ID = 80
        private const val CONNECTIVITY_CHANGE_NOTIFICATION_ID = 235
    }

    /**
     * Create initial Notification to be build for the service
     */
    fun createDownloadNotification(
        title: String?,
        bigTextArray: java.util.ArrayList<String?>?
    ): NotificationCompat.Builder? {
        return GenericSyncNotification(mContext).updateGenericNotification(
            title,
            title,
            R.drawable.ic_twotone_cloud_upload_24,
            bigTextArray!!,
            0,
            SyncService::class.java,
            SyncCancelAction::class.java,
            CHANNEL_ID
        )
    }

    /**
     * Update Service notification
     */
    fun updateDownloadNotification(
        title: String?,
        content: String?,
        bigTextArray: java.util.ArrayList<String?>?,
        percent: Int
    ) {
        val notificationManagerCompat = NotificationManagerCompat.from(mContext)
        var builder = GenericSyncNotification(mContext).updateGenericNotification(
            title,
            content,
            R.drawable.ic_twotone_cloud_download_24,
            bigTextArray!!,
            percent,
            SyncService::class.java,
            DownloadCancelAction::class.java,
            CHANNEL_ID
        )
        builder?.let { notificationManagerCompat.notify(PERSISTENT_NOTIFICATION_ID, it.build()) }
    }


    fun showConnectivityChangedNotification() {
        val builder = NotificationCompat.Builder(mContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_twotone_cloud_error_24)
            .setContentTitle(mContext.getString(R.string.download_cancelled))
            .setContentText(mContext.getString(R.string.wifi_connections_isnt_available))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        val notificationManager = NotificationManagerCompat.from(mContext)
        notificationManager.notify(CONNECTIVITY_CHANGE_NOTIFICATION_ID, builder.build())
    }

    fun showDownloadFinishedNotification(notificationID: Int, contentText: String) {
        createSummaryNotificationForFinished()
        val builder = NotificationCompat.Builder(mContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_twotone_cloud_done_24)
            .setContentTitle(mContext.getString(R.string.download_complete))
            .setContentText(contentText)
            .setGroup(DOWNLOAD_FINISHED_GROUP)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
        val notificationManager = NotificationManagerCompat.from(mContext)
        notificationManager.notify(notificationID, builder.build())
    }

    fun createSummaryNotificationForFinished() {
        val summaryNotification = NotificationCompat.Builder(mContext, CHANNEL_ID)
            .setContentTitle(mContext.getString(R.string.download_complete)) //set content text to support devices running API level < 24
            .setContentText(mContext.getString(R.string.download_complete))
            .setSmallIcon(R.drawable.ic_twotone_cloud_done_24)
            .setGroup(DOWNLOAD_FINISHED_GROUP)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .build()
        val notificationManager = NotificationManagerCompat.from(mContext)
        notificationManager.notify(DOWNLOAD_FINISHED_NOTIFICATION_ID, summaryNotification)
    }

    fun showDownloadFailedNotification(notificationId: Int, contentText: String) {
        createSummaryNotificationForFailed()
        val builder = NotificationCompat.Builder(mContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_twotone_cloud_error_24)
            .setContentTitle(mContext.getString(R.string.download_failed))
            .setContentText(contentText)
            .setGroup(DOWNLOAD_FAILED_GROUP)
            .setPriority(NotificationCompat.PRIORITY_LOW)
        val notificationManager = NotificationManagerCompat.from(mContext)
        notificationManager.notify(notificationId, builder.build())
    }

    fun createSummaryNotificationForFailed() {
        val summaryNotification = NotificationCompat.Builder(mContext, CHANNEL_ID)
            .setContentTitle(mContext.getString(R.string.download_failed)) //set content text to support devices running API level < 24
            .setContentText(mContext.getString(R.string.download_failed))
            .setSmallIcon(R.drawable.ic_twotone_cloud_error_24)
            .setGroup(DOWNLOAD_FAILED_GROUP)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .build()
        val notificationManager = NotificationManagerCompat.from(mContext)
        notificationManager.notify(FAILED_DOWNLOAD_NOTIFICATION_ID, summaryNotification)
    }
}