package ca.pkay.rcloneexplorer.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ca.pkay.rcloneexplorer.BroadcastReceivers.SyncCancelAction
import ca.pkay.rcloneexplorer.BroadcastReceivers.UploadCancelAction
import ca.pkay.rcloneexplorer.R
import ca.pkay.rcloneexplorer.Services.SyncService

class UploadNotifications(var mContext: Context) {


    companion object {
        const val CHANNEL_ID = "ca.pkay.rcexplorer.UPLOAD_CHANNEL"
        const val UPLOAD_FINISHED_GROUP = "ca.pkay.rcexplorer.UPLOAD_FINISHED_GROUP"
        const val UPLOAD_FAILED_GROUP = "ca.pkay.rcexplorer.UPLOAD_FAILED_GROUP"
        const val CHANNEL_NAME = "Uploads"
        const val PERSISTENT_NOTIFICATION_ID = 90
        const val UPLOAD_FINISHED_NOTIFICATION_ID = 41
        const val UPLOAD_FAILED_NOTIFICATION_ID = 14
        const val CONNECTIVITY_CHANGE_NOTIFICATION_ID = 94
    }

    /**
     * Create initial Notification to be build for the service
     */
    fun createUploadNotification(
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
            UploadCancelAction::class.java,
            CHANNEL_ID
        )
    }

    /**
     * Update Service notification
     */
    fun updateUploadNotification(
        title: String?,
        content: String?,
        bigTextArray: java.util.ArrayList<String?>?,
        percent: Int
    ) {
        val notificationManagerCompat = NotificationManagerCompat.from(mContext)
        var builder = GenericSyncNotification(mContext).updateGenericNotification(
            title,
            content,
            R.drawable.ic_twotone_cloud_upload_24,
            bigTextArray!!,
            percent,
            SyncService::class.java,
            SyncCancelAction::class.java,
            CHANNEL_ID
        )
        builder?.let { notificationManagerCompat.notify(PERSISTENT_NOTIFICATION_ID, it.build()) }
    }


    private fun createSummaryNotificationForFinished() {
        val summaryNotification = NotificationCompat.Builder(mContext, CHANNEL_ID)
            .setContentTitle(mContext.getString(R.string.upload_complete)) //set content text to support devices running API level < 24
            .setContentText(mContext.getString(R.string.upload_complete))
            .setSmallIcon(R.drawable.ic_twotone_cloud_done_24)
            .setGroup(UPLOAD_FINISHED_GROUP)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .build()
        val notificationManager = NotificationManagerCompat.from(mContext)
        notificationManager.notify(UPLOAD_FINISHED_NOTIFICATION_ID, summaryNotification)
    }

    fun showUploadFinishedNotification(notificationID: Int, contentText: String) {
        createSummaryNotificationForFinished()
        val builder = NotificationCompat.Builder(mContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_twotone_cloud_done_24)
            .setContentTitle(mContext.getString(R.string.upload_complete))
            .setContentText(contentText)
            .setGroup(UPLOAD_FINISHED_GROUP)
            .setPriority(NotificationCompat.PRIORITY_LOW)
        val notificationManager = NotificationManagerCompat.from(mContext)
        notificationManager.notify(notificationID, builder.build())
    }


    fun createSummaryNotificationForFailed() {
        val summaryNotification = NotificationCompat.Builder(mContext, CHANNEL_ID)
            .setContentTitle(mContext.getString(R.string.upload_failed)) //set content text to support devices running API level < 24
            .setContentText(mContext.getString(R.string.upload_failed))
            .setSmallIcon(R.drawable.ic_twotone_cloud_error_24)
            .setGroup(UPLOAD_FAILED_GROUP)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .build()
        val notificationManager = NotificationManagerCompat.from(mContext)
        notificationManager.notify(UPLOAD_FAILED_NOTIFICATION_ID, summaryNotification)
    }

    fun showUploadFailedNotification(notificationID: Int, contentText: String) {
        createSummaryNotificationForFailed()
        val builder = NotificationCompat.Builder(mContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_twotone_cloud_error_24)
            .setContentTitle(mContext.getString(R.string.upload_failed))
            .setContentText(contentText)
            .setGroup(UPLOAD_FAILED_GROUP)
            .setPriority(NotificationCompat.PRIORITY_LOW)
        val notificationManager = NotificationManagerCompat.from(mContext)
        notificationManager.notify(notificationID, builder.build())
    }

    fun showConnectivityChangedNotification() {
        val builder = NotificationCompat.Builder(mContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_twotone_cloud_error_24)
            .setContentTitle(mContext.getString(R.string.upload_cancelled))
            .setContentText(mContext.getString(R.string.wifi_connections_isnt_available))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        val notificationManager = NotificationManagerCompat.from(mContext)
        notificationManager.notify(CONNECTIVITY_CHANGE_NOTIFICATION_ID, builder.build())
    }

}