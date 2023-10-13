package ca.pkay.rcloneexplorer.workmanager

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import ca.pkay.rcloneexplorer.Items.Trigger

class SyncManager(private var mContext: Context) {


    public fun work(trigger: Trigger) {

        val uploadWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()

        val data = Data.Builder()
        data.putLong(SyncWorker.TASK_ID, trigger.whatToTrigger)

        uploadWorkRequest.setInputData(data.build())
        uploadWorkRequest.addTag(trigger.whatToTrigger.toString())
        var b = uploadWorkRequest.build()

        WorkManager
            .getInstance(mContext)
            .enqueue(b)

    }

    fun cancel() {
        WorkManager
            .getInstance(mContext)
            .cancelAllWork()
    }
    fun cancel(tag: String) {

        //Intent syncIntent = new Intent(context, SyncService.class);
        //syncIntent.setAction(TASK_CANCEL_ACTION);
        //syncIntent.putExtra(EXTRA_TASK_ID, intent.getLongExtra(EXTRA_TASK_ID, -1));
        //context.startService(syncIntent);
        Log.e("TAG", "CANCEL"+tag)
        WorkManager
            .getInstance(mContext)
            .cancelAllWorkByTag(tag)
    }
}