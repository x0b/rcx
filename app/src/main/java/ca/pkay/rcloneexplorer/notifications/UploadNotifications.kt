package ca.pkay.rcloneexplorer.notifications

import android.content.Context
import ca.pkay.rcloneexplorer.BroadcastReceivers.UploadCancelAction
import ca.pkay.rcloneexplorer.R
import ca.pkay.rcloneexplorer.Services.UploadService
import ca.pkay.rcloneexplorer.notifications.support.GenericNotification

class UploadNotifications(var context: Context): GenericNotification(context) {

    companion object {
        const val CHANNEL_ID = "ca.pkay.rcexplorer.UPLOAD_CHANNEL"
        const val CHANNEL_NAME = "Uploads"
        const val PERSISTENT_NOTIFICATION_ID = 90
    }

    override val sChannelId: String = CHANNEL_ID
    override val sChannelName: String =CHANNEL_NAME
    override val finishedGroup: String = "ca.pkay.rcexplorer.UPLOAD_FINISHED_GROUP"
    override val failedGroup: String = "ca.pkay.rcexplorer.UPLOAD_FAILED_GROUP"
    override val persistentId: Int = PERSISTENT_NOTIFICATION_ID
    override val finishedNotificationId: Int = 41
    override val failedNotificationId: Int = 14
    override val connectivityChangeId: Int = 94
    override val actionIcon: Int = R.drawable.ic_twotone_cloud_upload_24
    override val serviceClass: Class<*> = UploadService::class.java
    override val serviceCancelClass: Class<*> = UploadCancelAction::class.java
    override val completeString: Int = R.string.upload_complete
    override val failedString: Int = R.string.upload_failed
    override val canceledString: Int = R.string.upload_cancelled

}