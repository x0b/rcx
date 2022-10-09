package ca.pkay.rcloneexplorer.notifications

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ca.pkay.rcloneexplorer.BroadcastReceivers.SyncCancelAction
import ca.pkay.rcloneexplorer.R
import ca.pkay.rcloneexplorer.Services.SyncService

class SyncServiceNotifications(var mContext: Context) {


    companion object {
        const val CHANNEL_ID = "ca.pkay.rcexplorer.sync_service"
        const val CHANNEL_SUCCESS_ID = "ca.pkay.rcexplorer.sync_service_success"
        const val CHANNEL_FAIL_ID = "ca.pkay.rcexplorer.sync_service_fail"

        const val PERSISTENT_NOTIFICATION_ID_FOR_SYNC = 162
        private const val OPERATION_FAILED_NOTIFICATION_ID = 89
        private const val OPERATION_SUCCESS_NOTIFICATION_ID = 698
    }

    private val OPERATION_FAILED_GROUP = "ca.pkay.rcexplorer.OPERATION_FAILED_GROUP"
    private val OPERATION_SUCCESS_GROUP = "ca.pkay.rcexplorer.OPERATION_SUCCESS_GROUP"

    fun showFailedNotification(
        content: String?,
        notificationId: Int,
        taskid: Long
    ) {
        val i = Intent(mContext, SyncService::class.java)
        i.action = SyncService.TASK_ACTION
        i.putExtra(SyncService.EXTRA_TASK_ID, taskid)
        var flags = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags = flags or FLAG_IMMUTABLE;
        }
        val retryPendingIntent = PendingIntent.getService(mContext, taskid.toInt(), i, flags)
        val builder = NotificationCompat.Builder(mContext, CHANNEL_FAIL_ID)
            .setSmallIcon(R.drawable.ic_twotone_cloud_error_24)
            .setContentTitle(mContext.getString(R.string.operation_failed))
            .setContentText(content)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(content)
            )
            .setGroup(OPERATION_FAILED_GROUP)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                R.drawable.ic_refresh,
                mContext.getString(R.string.retry_failed_sync),
                retryPendingIntent
            )
        val notificationManager = NotificationManagerCompat.from(mContext)
        notificationManager.notify(notificationId, builder.build())
        createSummaryNotificationForFailed()
    }

    fun showSuccessNotification(title: String, content: String?, notificationId: Int) {
        val builder = NotificationCompat.Builder(mContext, CHANNEL_SUCCESS_ID)
            .setSmallIcon(R.drawable.ic_twotone_cloud_done_24)
            .setContentTitle(mContext.getString(R.string.operation_success, title))
            .setContentText(content)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    content
                )
            )
            .setGroup(OPERATION_SUCCESS_GROUP)
            .setPriority(NotificationCompat.PRIORITY_LOW)
        val notificationManager = NotificationManagerCompat.from(mContext)
        notificationManager.notify(notificationId, builder.build())
        createSummaryNotificationForSuccess()
    }

    // this will show up if mul
    fun createSummaryNotificationForFailed() {
        val summaryNotification = NotificationCompat.Builder(mContext, CHANNEL_ID)
            .setContentTitle(mContext.getString(R.string.operation_failed)) //set content text to support devices running API level < 24
            .setContentText(mContext.getString(R.string.operation_failed))
            .setSmallIcon(R.drawable.ic_twotone_cloud_error_24)
            .setGroup(OPERATION_FAILED_GROUP)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .build()
        val notificationManager = NotificationManagerCompat.from(mContext)
        notificationManager.notify(OPERATION_FAILED_NOTIFICATION_ID, summaryNotification)
    }

    fun createSummaryNotificationForSuccess() {
        val summaryNotification = NotificationCompat.Builder(mContext, CHANNEL_ID)
            .setContentTitle(mContext.getString(R.string.operation_success)) //set content text to support devices running API level < 24
            .setContentText(mContext.getString(R.string.operation_success))
            .setSmallIcon(R.drawable.ic_twotone_cloud_done_24)
            .setGroup(OPERATION_SUCCESS_GROUP)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .build()
        val notificationManager = NotificationManagerCompat.from(mContext)
        notificationManager.notify(OPERATION_SUCCESS_NOTIFICATION_ID, summaryNotification)
    }

    fun getPersistentNotification(title: String?): NotificationCompat.Builder {

        var flags = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags = PendingIntent.FLAG_IMMUTABLE
        }
        val foregroundIntent = Intent(mContext, SyncService::class.java)
        val pendingIntent =
            PendingIntent.getActivity(mContext, 0, foregroundIntent, flags)
        val cancelIntent = Intent(mContext, SyncCancelAction::class.java)
        val cancelPendingIntent =
            PendingIntent.getBroadcast(mContext, 0, cancelIntent, flags)
        return NotificationCompat.Builder(mContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_twotone_rounded_cloud_sync_24)
            .setContentTitle(mContext.getString(R.string.syncing_service, title))
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_cancel_download,
                mContext.getString(R.string.cancel),
                cancelPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
    }

    fun updateSyncNotification(
        title: String?,
        content: String?,
        bigTextArray: ArrayList<String?>?,
        percent: Int
    ) {
        val builder = GenericSyncNotification(mContext).updateGenericNotification(
            mContext.getString(R.string.syncing_service, title),
            content,
            R.drawable.ic_twotone_rounded_cloud_sync_24,
            bigTextArray!!,
            percent,
            SyncService::class.java,
            SyncCancelAction::class.java,
            CHANNEL_ID
        )
        val notificationManagerCompat = NotificationManagerCompat.from(mContext)
        notificationManagerCompat.notify(PERSISTENT_NOTIFICATION_ID_FOR_SYNC, builder!!.build())
    }
}