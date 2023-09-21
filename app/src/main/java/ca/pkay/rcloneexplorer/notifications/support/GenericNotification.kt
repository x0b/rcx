package ca.pkay.rcloneexplorer.notifications.support

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ca.pkay.rcloneexplorer.R
import ca.pkay.rcloneexplorer.notifications.GenericSyncNotification

abstract class GenericNotification(var mContext: Context) {

    abstract val sChannelId: String
    abstract val sChannelName: String
    abstract val finishedGroup: String
    abstract val failedGroup: String
    abstract val persistentId: Int
    abstract val finishedNotificationId: Int
    abstract val failedNotificationId: Int
    abstract val connectivityChangeId: Int

    abstract val actionIcon: Int
    abstract val serviceClass: Class<*>
    abstract val serviceCancelClass: Class<*>

    abstract val completeString: Int
    abstract val failedString: Int
    abstract val canceledString: Int

    /**
     * Create initial Notification to be build for the service
     */
    fun createNotification(
        title: String?,
        bigTextArray: java.util.ArrayList<String?>?
    ): NotificationCompat.Builder? {
        return GenericSyncNotification(mContext).updateGenericNotification(
            title,
            title,
            actionIcon,
            bigTextArray!!,
            0,
            serviceClass,
            serviceCancelClass,
            sChannelId
        )
    }

    /**
     * Update Service notification
     */
    fun updateNotification(
        title: String?,
        content: String?,
        bigTextArray: java.util.ArrayList<String?>?,
        percent: Int
    ) {
        if(content?.isBlank() == true || content == null){
            return
        }
        var builder = GenericSyncNotification(mContext).updateGenericNotification(
            title,
            content,
            actionIcon,
            bigTextArray!!,
            percent,
            serviceClass,
            serviceCancelClass,
            sChannelId
        )
        builder?.let {
            notify(it, persistentId)
        }
    }

    fun showFinishedNotification(notificationID: Int, contentText: String) {
        createSummaryNotificationForFinished()
        val builder = NotificationCompat.Builder(mContext, sChannelId)
            .setSmallIcon(R.drawable.ic_twotone_cloud_done_24)
            .setContentTitle(mContext.getString(completeString))
            .setContentText(contentText)
            .setGroup(finishedGroup)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
        notify(builder, notificationID)
    }

    private fun createSummaryNotificationForFinished() {
        val summaryNotification = NotificationCompat.Builder(mContext,
            sChannelId
        )
            .setContentTitle(mContext.getString(completeString)) //set content text to support devices running API level < 24
            .setContentText(mContext.getString(completeString))
            .setSmallIcon(R.drawable.ic_twotone_cloud_done_24)
            .setGroup(finishedGroup)
            .setGroupSummary(true)
            .setAutoCancel(true)
        notify(summaryNotification, finishedNotificationId)
    }

    fun showFailedNotification(notificationID: Int, contentText: String) {
        createSummaryNotificationForFailed()
        val builder = NotificationCompat.Builder(mContext, sChannelId)
            .setSmallIcon(R.drawable.ic_twotone_cloud_error_24)
            .setContentTitle(mContext.getString(failedString))
            .setContentText(contentText)
            .setGroup(failedGroup)
            .setPriority(NotificationCompat.PRIORITY_LOW)
        notify(builder, notificationID)
    }

    fun createSummaryNotificationForFailed() {
        val summaryNotification = NotificationCompat.Builder(mContext, sChannelId)
            .setContentTitle(mContext.getString(failedString)) //set content text to support devices running API level < 24
            .setContentText(mContext.getString(failedString))
            .setSmallIcon(R.drawable.ic_twotone_cloud_error_24)
            .setGroup(failedGroup)
            .setGroupSummary(true)
            .setAutoCancel(true)
        notify(summaryNotification, failedNotificationId)
    }

    fun showConnectivityChangedNotification() {
        val builder = NotificationCompat.Builder(mContext, sChannelId)
            .setSmallIcon(R.drawable.ic_twotone_cloud_error_24)
            .setContentTitle(mContext.getString(canceledString))
            .setContentText(mContext.getString(R.string.wifi_connections_isnt_available))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        notify(builder, connectivityChangeId)
    }

    fun cancelPersistent() {
        val notificationManagerCompat = NotificationManagerCompat.from(mContext)
        notificationManagerCompat.cancel(this.persistentId)
    }

    private fun notify(builder: NotificationCompat.Builder, id: Int) {
        val notificationManager = NotificationManagerCompat.from(mContext)
        notificationManager.notify(id, builder.build())
    }
}