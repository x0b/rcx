package ca.pkay.rcloneexplorer.Services

import android.app.IntentService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import ca.pkay.rcloneexplorer.Database.DatabaseHandler
import ca.pkay.rcloneexplorer.Items.RemoteItem
import ca.pkay.rcloneexplorer.Items.SyncDirectionObject
import ca.pkay.rcloneexplorer.Items.Task
import ca.pkay.rcloneexplorer.Log2File
import ca.pkay.rcloneexplorer.R
import ca.pkay.rcloneexplorer.Rclone
import ca.pkay.rcloneexplorer.notifications.GenericSyncNotification
import ca.pkay.rcloneexplorer.notifications.ReportNotifications
import ca.pkay.rcloneexplorer.notifications.SyncServiceNotifications
import ca.pkay.rcloneexplorer.notifications.support.StatusObject
import ca.pkay.rcloneexplorer.util.FLog
import ca.pkay.rcloneexplorer.util.SyncLog
import ca.pkay.rcloneexplorer.util.WifiConnectivitiyUtil
import ca.pkay.rcloneexplorer.util.WifiConnectivitiyUtil.Companion.dataConnection
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.InterruptedIOException
import java.util.Random
import kotlin.concurrent.thread

class SyncService : IntentService("ca.pkay.rcexplorer.SYNC_SERCVICE") {
    internal enum class FAILURE_REASON {
        NONE, NO_UNMETERED, NO_CONNECTION, RCLONE_ERROR
    }

    private var rclone: Rclone? = null
    private var log2File: Log2File? = null
    private var connectivityChanged = false
    var notificationManager = SyncServiceNotifications(this)
    override fun onCreate() {
        super.onCreate()
        GenericSyncNotification(this).setNotificationChannel(
            SyncServiceNotifications.CHANNEL_ID,
            getString(R.string.sync_service_notification_channel_title),
            getString(R.string.sync_service_notification_channel_description)
        )
        GenericSyncNotification(this).setNotificationChannel(
            SyncServiceNotifications.CHANNEL_SUCCESS_ID,
            getString(R.string.sync_service_notification_channel_success_title),
            getString(R.string.sync_service_notification_channel_success_description)
        )
        GenericSyncNotification(this).setNotificationChannel(
            SyncServiceNotifications.CHANNEL_FAIL_ID,
            getString(R.string.sync_service_notification_channel_fail_title),
            getString(R.string.sync_service_notification_channel_fail_description)
        )
        GenericSyncNotification(this).setNotificationChannel(
            ReportNotifications.CHANNEL_REPORT_ID,
            getString(R.string.sync_service_notification_channel_report_title),
            getString(R.string.sync_service_notification_channel_report_description)
        )
        rclone = Rclone(this)
        log2File = Log2File(this)
        registerBroadcastReceivers()
    }

    override fun onHandleIntent(intent: Intent?) {
        log("onHandleIntent")
        if (intent == null) {
            return
        }
        log("With Intent: " + intent.action)
        if (intent.action == TASK_CANCEL_ACTION) {
            val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
            log("With Intent: $taskId")
            val notificationManagerCompat = NotificationManagerCompat.from(this)
            notificationManagerCompat.cancel(taskId.toInt())
            discardOfServiceIfRequired()
        }
        if (intent.action == TASK_SYNC_ACTION) {
            startForeground(
                SyncServiceNotifications.PERSISTENT_NOTIFICATION_ID_FOR_SYNC,
                notificationManager.getPersistentNotification("SyncService").build()
            )
            val task = handleTaskStartIntent(intent)
            notificationManager.setCancelId(task!!.id!!)
            handleTaskNonblocking(task)
        }
    }

    private fun handleTaskNonblocking(internalTask: InternalTaskItem?) {
        if (mCurrentProcesses[internalTask!!.id] != null) {
            log("No identical runs!")
            SyncLog.error(
                this,
                getString(R.string.operation_no_identical_title),
                getString(R.string.operation_no_identical, internalTask.title)
            )
            notificationManager.showFailedNotificationOrReport(
                getString(R.string.operation_no_identical_title),
                getString(R.string.operation_no_identical, internalTask.title),
                System.currentTimeMillis().toInt(),
                internalTask.id!!
            )
            return
        }

        thread(start = true, isDaemon = false) {
            handleTask(internalTask)
        }

    }

    private fun handleTask(internalTask: InternalTaskItem?) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val isLoggingEnable = sharedPreferences.getBoolean(getString(R.string.pref_key_logs), false)
        var title = internalTask!!.title
        log("Sync: $title")
        var failureReason = FAILURE_REASON.NONE
        if (internalTask.title == "") {
            title = internalTask.remotePath
        }
        val notificationID = Random().nextInt()
        val statusObject = StatusObject(this)
        val connection = dataConnection(this.applicationContext)
        var rcloneProcess: Process? = null
        if (internalTask.transferOnWiFiOnly && connection === WifiConnectivitiyUtil.Connection.METERED) {
            failureReason = FAILURE_REASON.NO_UNMETERED
        } else if (connection === WifiConnectivitiyUtil.Connection.DISCONNECTED || connection === WifiConnectivitiyUtil.Connection.NOT_AVAILABLE) {
            failureReason = FAILURE_REASON.NO_CONNECTION
        } else {
            rcloneProcess = rclone!!.sync(
                internalTask.remoteItem,
                internalTask.localPath,
                internalTask.remotePath,
                internalTask.syncDirection,
                internalTask.md5sum
            )
            mCurrentProcesses[internalTask.id ?: -1] = rcloneProcess
            if (rcloneProcess != null) {
                try {
                    val reader = BufferedReader(InputStreamReader(rcloneProcess.errorStream))
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {

                        try {
                            val logline = JSONObject(line)

                            //todo: migrate this to StatusObject, so that we can handle everything properly.
                            if (logline.getString("level") == "error") {
                                if (isLoggingEnable) {
                                    log2File!!.log(line)
                                }
                                statusObject.parseLoglineToStatusObject(logline)
                            } else if (logline.getString("level") == "warning") {
                                statusObject.parseLoglineToStatusObject(logline)
                            }

                            notificationManager.updateSyncNotification(
                                title?: "",
                                statusObject.notificationContent,
                                statusObject.notificationBigText,
                                statusObject.notificationPercent,
                                notificationID
                            )
                        } catch (e: JSONException) {
                            FLog.e(TAG, "SyncService-Error: the offending line: $line")
                            //FLog.e(TAG, "onHandleIntent: error reading json", e)
                        }
                    }
                } catch (e: InterruptedIOException) {
                    FLog.e(TAG, "onHandleIntent: I/O interrupted, stream closed", e)
                } catch (e: IOException) {
                    FLog.e(TAG, "onHandleIntent: error reading stdout", e)
                }
                try {
                    rcloneProcess.waitFor()
                } catch (e: InterruptedException) {
                    FLog.e(TAG, "onHandleIntent: error waiting for process", e)
                }
            } else {
                log("Sync: No Rclone Process!")
            }
            notificationManager.cancelSyncNotification(notificationID)
            sendUploadFinishedBroadcast(internalTask.remoteItem!!.name, internalTask.remotePath)
        }
        val notificationId = System.currentTimeMillis().toInt()
        if (internalTask.silentRun) {
            if (internalTask.transferOnWiFiOnly && connectivityChanged || rcloneProcess == null || rcloneProcess.exitValue() != 0) {
                var content = getString(R.string.operation_failed_unknown, title)
                when (failureReason) {
                    FAILURE_REASON.NONE -> if (connectivityChanged) {
                        content = getString(R.string.operation_failed_data_change, title)
                    }

                    FAILURE_REASON.NO_UNMETERED -> content =
                        getString(R.string.operation_failed_no_unmetered, title)

                    FAILURE_REASON.NO_CONNECTION -> content =
                        getString(R.string.operation_failed_no_connection, title)

                    FAILURE_REASON.RCLONE_ERROR -> content =
                        getString(R.string.operation_failed_unknown_rclone_error, title)
                }
                //Todo: check if we should also add errors on success
                statusObject.printErrors()
                val errors = statusObject.getAllErrorMessages()
                if (errors.isNotEmpty()) {
                    content += """
                        
                        
                        
                        ${statusObject.getAllErrorMessages()}
                        """.trimIndent()
                }
                SyncLog.error(this, getString(R.string.operation_failed), "$title: $content")
                notificationManager.showFailedNotificationOrReport(
                    title!!,
                    content,
                    notificationId,
                    internalTask.id!!
                )
            } else {
                var message = resources.getQuantityString(
                    R.plurals.operation_success_description,
                    statusObject.getTotalTransfers(),
                    title,
                    statusObject.getTotalSize(),
                    statusObject.getTotalTransfers()
                )
                if (statusObject.getTotalTransfers() == 0) {
                    message = resources.getString(R.string.operation_success_description_zero)
                }
                if (statusObject.getDeletions() > 0) {
                    message += """
                        
                        ${
                        getString(
                            R.string.operation_success_description_deletions_prefix,
                            statusObject.getDeletions()
                        )
                    }
                        """.trimIndent()
                }
                SyncLog.info(this, getString(R.string.operation_success, title), message)
                notificationManager.showSuccessNotificationOrReport(
                    title!!,
                    message,
                    notificationId,
                    internalTask.id!!
                )
            }
        }
        mCurrentProcesses.remove(internalTask.id)
        discardOfServiceIfRequired()
    }

    private fun discardOfServiceIfRequired() {
        if (mCurrentProcesses.isEmpty()) {
            stopForeground(true)
        }
    }

    private fun handleTaskStartIntent(intent: Intent): InternalTaskItem? {
        var action = intent.action
        if (action == null) {
            // equals might fail otherwise when internal tasks send an intent without action.
            action = ""
        }
        return if (action == TASK_SYNC_ACTION) {
            val db = DatabaseHandler(this)
            for (task in db.allTasks) {
                if (task.id == intent.getLongExtra(EXTRA_TASK_ID, -1)) {
                    val path = task.localPath
                    val silentRun = intent.getBooleanExtra(EXTRA_TASK_SILENT, true)
                    val remoteItem = RemoteItem(task.remoteId, task.remoteType, "")
                    val taskIntent = Intent()
                    taskIntent.setClass(this.applicationContext, SyncService::class.java)
                    taskIntent.putExtra(REMOTE_ARG, remoteItem)
                    taskIntent.putExtra(LOCAL_PATH_ARG, path)
                    taskIntent.putExtra(SYNC_DIRECTION_ARG, task.direction)
                    taskIntent.putExtra(REMOTE_PATH_ARG, task.remotePath)
                    taskIntent.putExtra(TASK_NAME, task.title)
                    taskIntent.putExtra(EXTRA_TASK_ID, task.id)
                    taskIntent.putExtra(SHOW_RESULT_NOTIFICATION, silentRun)
                    taskIntent.putExtra(TASK_WIFI_ONLY, task.wifionly)
                    taskIntent.putExtra(TASK_MD5SUM, task.md5sum)
                    return InternalTaskItem.newInstance(taskIntent, this)
                }
            }
            null
        } else {
            InternalTaskItem.newInstance(intent, this)
        }
    }

    private fun registerBroadcastReceivers() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)
        registerReceiver(connectivityChangeBroadcastReceiver, intentFilter)
    }

    private val connectivityChangeBroadcastReceiver: BroadcastReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                connectivityChanged = true
                stopSelf()
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        for (p in mCurrentProcesses.values) {
            p.destroy()
        }
        unregisterReceiver(connectivityChangeBroadcastReceiver)
    }

    private fun sendUploadFinishedBroadcast(remote: String, path: String?) {
        val intent = Intent()
        intent.action = getString(R.string.background_service_broadcast)
        intent.putExtra(getString(R.string.background_service_broadcast_data_remote), remote)
        intent.putExtra(getString(R.string.background_service_broadcast_data_path), path)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    /**
     * Okay this is a bit embarrasing. I did not write down WHY i needed this internal class.
     * I assume it was because we have multiple vectors on how this service can be started,
     * and because they use different methods to do stuff, we need to unify it here.
     */
    private class InternalTaskItem {
        var id: Long? = null
        var remoteItem: RemoteItem? = null
        var remotePath: String? = null
        var localPath: String? = null
        var title: String? = null
        var syncDirection = SyncDirectionObject.SYNC_LOCAL_TO_REMOTE
        var silentRun = true
        var md5sum = Task.TASK_MD5SUM_DEFAULT

        // this should not use the task default. It should fallback to the settings.
        // reason: tasks should not use the settings-setting, but everything else should.
        var transferOnWiFiOnly = Task.TASK_WIFIONLY_DEFAULT

        companion object {
            fun newInstance(intent: Intent, context: Context): InternalTaskItem {

                // todo: define this default value somewhere central. Dont hardcode it.
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                val transferOnWiFiOnly = sharedPreferences.getBoolean(
                    context.getString(R.string.pref_key_wifi_only_transfers),
                    false
                )
                val itt = InternalTaskItem()
                itt.id = intent.getLongExtra(EXTRA_TASK_ID, -1)
                itt.remoteItem = intent.getParcelableExtra(REMOTE_ARG)
                itt.remotePath = opt(intent.getStringExtra(REMOTE_PATH_ARG), "") as String
                itt.localPath = opt(intent.getStringExtra(LOCAL_PATH_ARG), "") as String
                itt.title = opt(intent.getStringExtra(TASK_NAME), "") as String
                itt.syncDirection = intent.getIntExtra(SYNC_DIRECTION_ARG, 1)
                itt.silentRun = intent.getBooleanExtra(SHOW_RESULT_NOTIFICATION, true)
                itt.md5sum = intent.getBooleanExtra(TASK_MD5SUM, Task.TASK_MD5SUM_DEFAULT)
                itt.transferOnWiFiOnly = intent.getBooleanExtra(TASK_WIFI_ONLY, transferOnWiFiOnly)
                return itt
            }

            private fun opt(preferred: Any?, alternate: Any): Any {
                return preferred ?: alternate
            }
        }
    }

    private fun log(message: String) {
        FLog.e(TAG, "SyncService: $message")
    }

    private fun logD(message: String) {
        FLog.d(TAG, "SyncService: $message")
    }

    companion object {
        //those Extras do not follow the above schema, because they are exposed to external applications
        //That means shorter values make it easier to use. There is no other technical reason
        const val TASK_SYNC_ACTION = "START_TASK"
        const val TASK_CANCEL_ACTION = "CANCEL_TASK"
        const val EXTRA_TASK_ID = "task"
        const val EXTRA_TASK_SILENT = "notification"
        private const val TAG = "SyncService"
        const val REMOTE_ARG = "ca.pkay.rcexplorer.SYNC_SERVICE_REMOTE_ARG"
        const val REMOTE_PATH_ARG = "ca.pkay.rcexplorer.SYNC_SERVICE_REMOTE_PATH_ARG"
        const val LOCAL_PATH_ARG = "ca.pkay.rcexplorer.SYNC_LOCAL_PATH_ARG"
        const val SYNC_DIRECTION_ARG = "ca.pkay.rcexplorer.SYNC_DIRECTION_ARG"
        const val SHOW_RESULT_NOTIFICATION = "ca.pkay.rcexplorer.SHOW_RESULT_NOTIFICATION"
        const val TASK_NAME = "ca.pkay.rcexplorer.TASK_NAME"
        const val TASK_WIFI_ONLY = "ca.pkay.rcexplorer.TASK_WIFI_ONLY"
        const val TASK_MD5SUM = "ca.pkay.rcexplorer.TASK_MD5SUM"
        private val mCurrentProcesses: HashMap<Long, Process> = HashMap()
        @JvmStatic
        fun createInternalStartIntent(context: Context?, id: Long): Intent {
            val i = Intent(context, SyncService::class.java)
            i.action = TASK_SYNC_ACTION
            i.putExtra(EXTRA_TASK_ID, id)
            return i
        }
    }
}