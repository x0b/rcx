package ca.pkay.rcloneexplorer.BroadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.edit
import ca.pkay.rcloneexplorer.notifications.SyncServiceNotifications
import ca.pkay.rcloneexplorer.notifications.dataStore
import kotlinx.coroutines.runBlocking


class ClearReportBroadcastReciever: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent) {
        val action = intent.action
        if (action == SyncServiceNotifications.REPORT_SUCCESS_DELETE_INTENT) {
            if(context != null){
                runBlocking {
                    context.dataStore.edit { settings ->
                        settings[SyncServiceNotifications.NOTIFICATION_CACHE_SUCCESS] = ""
                    }
                }
            }
        }
    }
}