package ca.pkay.rcloneexplorer.notifications

import android.content.Context
import ca.pkay.rcloneexplorer.BroadcastReceivers.DownloadCancelAction
import ca.pkay.rcloneexplorer.R
import ca.pkay.rcloneexplorer.Services.DownloadService
import ca.pkay.rcloneexplorer.notifications.support.GenericNotification

class DownloadNotifications(var context: Context): GenericNotification(context) {

    companion object {
        const val CHANNEL_ID = "ca.pkay.rcexplorer.DOWNLOAD_CHANNEL"
        const val CHANNEL_NAME = "Downloads"
        const val PERSISTENT_NOTIFICATION_ID = 167
    }

    override val sChannelId: String = CHANNEL_ID
    override val sChannelName: String = CHANNEL_NAME
    override val finishedGroup: String = "ca.pkay.rcexplorer.DOWNLOAD_FINISHED_GROUP"
    override val failedGroup: String = "ca.pkay.rcexplorer.DOWNLOAD_FAILED_GROUP"
    override val persistentId: Int = PERSISTENT_NOTIFICATION_ID
    override val finishedNotificationId: Int = 80
    override val failedNotificationId: Int = 138
    override val connectivityChangeId: Int = 235
    override val actionIcon: Int = R.drawable.ic_twotone_cloud_download_24
    override val serviceClass: Class<*> = DownloadService::class.java
    override val serviceCancelClass: Class<*> = DownloadCancelAction::class.java
    override val completeString: Int = R.string.download_complete
    override val failedString: Int = R.string.download_failed
    override val canceledString: Int = R.string.download_cancelled

}