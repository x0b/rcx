package ca.pkay.rcloneexplorer.Activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ca.pkay.rcloneexplorer.R
import ca.pkay.rcloneexplorer.Services.SyncService
import ca.pkay.rcloneexplorer.Services.SyncService.TASK_ACTION

class ShortcutServiceActivity : AppCompatActivity() {

    private val TAG = "ShortcutServiceActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        moveTaskToBack(true)

        Log.e(TAG, "Recieved start signal!")

        if(intent.action == TASK_ACTION) {
            Log.e(TAG, "Recieved valid intent.")
            val id = intent.extras?.getLong(SyncService.EXTRA_TASK_ID)
            val i = SyncService.createInternalStartIntent(this, id?:0)
            startService(i)
            Toast.makeText(this, getString(R.string.shortcut_start_service), Toast.LENGTH_SHORT).show()
        }

        Log.e(TAG, "Finish up.")
        finish()
    }
}