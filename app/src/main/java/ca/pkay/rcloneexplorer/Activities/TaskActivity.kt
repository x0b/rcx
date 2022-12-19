package ca.pkay.rcloneexplorer.Activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import ca.pkay.rcloneexplorer.Database.DatabaseHandler
import ca.pkay.rcloneexplorer.FilePicker
import ca.pkay.rcloneexplorer.Fragments.FolderSelectorCallback
import ca.pkay.rcloneexplorer.Fragments.RemoteFolderPickerFragment
import ca.pkay.rcloneexplorer.Items.RemoteItem
import ca.pkay.rcloneexplorer.Items.SyncDirectionObject
import ca.pkay.rcloneexplorer.Items.Task
import ca.pkay.rcloneexplorer.R
import ca.pkay.rcloneexplorer.Rclone
import ca.pkay.rcloneexplorer.util.ActivityHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import es.dmoral.toasty.Toasty
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

class TaskActivity : AppCompatActivity(), FolderSelectorCallback {


    private lateinit var rcloneInstance: Rclone
    private lateinit var dbHandler: DatabaseHandler

    private lateinit var syncDescription: TextView
    private lateinit var remotePath: EditText
    private lateinit var localPath: EditText
    private lateinit var remoteDropdown: Spinner
    private lateinit var syncDirection: Spinner
    private lateinit var fab: FloatingActionButton

    private lateinit var switchWifi: Switch
    private lateinit var switchMD5sum: Switch


    private var existingTask: Task? = null
    private var remotePathHolder = ""


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_FP_LOCAL -> {
                if (data != null) {
                    localPath.setText(data.getStringExtra(FilePicker.FILE_PICKER_RESULT))
                }
                localPath.clearFocus()
            }
            REQUEST_CODE_FP_REMOTE -> if (data != null) {
                var path = data.data.toString()
                try {
                    path = URLDecoder.decode(path, "UTF-8")
                } catch (e: UnsupportedEncodingException) {
                }

                // Todo: check if this provider is still valid; search other occurences
                Log.e("TaskActivity provider", "recieved path: $path")
                val provider = "content://io.github.x0b.rcx.vcp/tree/rclone/remotes/"
                if (path.startsWith(provider)) {
                    val parts = path.substring(provider.length).split(":").toTypedArray()
                    remotePath.setText(parts[1])
                    var i = 0
                    for (remote in remoteItems) {
                        if (remote == parts[0]) {
                            remoteDropdown.setSelection(i)
                        }
                        i++
                    }
                } else {
                    Toasty.error(this, "This Remote is not a RCX-Remote.").show()
                }
            }
        }
        fab.visibility = View.VISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityHelper.applyTheme(this)
        setContentView(R.layout.activity_task)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        remotePath = findViewById(R.id.task_remote_path_textfield)
        localPath = findViewById(R.id.task_local_path_textfield)
        remoteDropdown = findViewById(R.id.task_remote_spinner)
        syncDirection = findViewById(R.id.task_direction_spinner)
        syncDescription = findViewById(R.id.descriptionSyncDirection)
        fab = findViewById(R.id.fab)
        switchWifi = findViewById(R.id.task_wifionly)
        switchMD5sum = findViewById(R.id.task_md5sum)

        rcloneInstance = Rclone(this)
        dbHandler = DatabaseHandler(this)
        val extras = intent.extras
        val taskId: Long
        if (extras != null) {
            taskId = extras.getLong(ID_EXTRA)
            if (taskId != 0L) {
                existingTask = dbHandler.getTask(taskId)
                if (existingTask == null) {
                    Toasty.error(
                        this,
                        this.resources.getString(R.string.taskactivity_task_not_found)
                    ).show()
                    finish()
                }
            }
        }
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            //Todo fix error when no remotes are available
            if (existingTask == null) {
                saveTask()
            } else {
                persistTaskChanges()
            }
        }

        findViewById<TextView>(R.id.task_title_textfield).text = existingTask?.title
        switchWifi.isChecked = existingTask?.wifionly ?: false
        switchMD5sum.isChecked = existingTask?.md5sum ?: false
        prepareSyncDirectionDropdown()
        prepareLocal()
        prepareRemote()

    }

    private val remoteItems: Array<String?>
        private get() {
            val remotes = arrayOfNulls<String>(rcloneInstance.remotes.size)
            for (i in rcloneInstance.remotes.indices) {
                remotes[i] = rcloneInstance.remotes[i].name
            }
            return remotes
        }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun persistTaskChanges() {
        val updatedTask = getTaskValues(existingTask!!.id)
        if (updatedTask != null) {
            dbHandler.updateTask(updatedTask)
            finish()
        }
    }

    private fun saveTask() {
        val newTask = getTaskValues(0)
        if (newTask != null) {
            dbHandler.createTask(newTask)
            finish()
        }
    }

    private fun getTaskValues(id: Long): Task? {
        val taskToPopulate = Task(id)
        taskToPopulate.title = findViewById<EditText>(R.id.task_title_textfield).text.toString()
        val remotename = remoteDropdown.selectedItem.toString()
        taskToPopulate.remoteId = remotename
        val direction = syncDirection.selectedItemPosition + 1
        for (ri in rcloneInstance.remotes) {
            if (ri.name == taskToPopulate.remoteId) {
                taskToPopulate.remoteType = ri.type
            }
        }
        taskToPopulate.remotePath = remotePath.text.toString()
        taskToPopulate.localPath = localPath.text.toString()
        taskToPopulate.direction = direction

        taskToPopulate.wifionly = switchWifi.isChecked
        taskToPopulate.md5sum = switchMD5sum.isChecked

        // Verify if data is completed
        if (localPath.text.toString() == "") {
            Toasty.error(
                this.applicationContext,
                getString(R.string.task_data_validation_error_no_local_path),
                Toast.LENGTH_SHORT,
                true
            ).show()
            return null
        }
        if (remotePath.text.toString() == "") {
            Toasty.error(
                this.applicationContext,
                getString(R.string.task_data_validation_error_no_remote_path),
                Toast.LENGTH_SHORT,
                true
            ).show()
            return null
        }
        return taskToPopulate
    }

    private fun startRemotePicker(remote: RemoteItem, initialPath: String) {
        val fragment: Fragment = RemoteFolderPickerFragment.newInstance(remote, this, initialPath)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.create_task_layout, fragment, "FILE_EXPLORER_FRAGMENT_TAG")
        transaction.addToBackStack("FILE_EXPLORER_FRAGMENT_TAG")
        transaction.commit()
        fab.visibility = View.GONE
    }

    override fun selectFolder(path: String) {
        remotePathHolder = path
        remotePath.setText(remotePathHolder)
        fab.visibility = View.VISIBLE
    }

    private fun prepareLocal() {
        existingTask.let {
            localPath.setText(it?.localPath ?: "")
        }
        localPath.onFocusChangeListener =
            View.OnFocusChangeListener { v: View?, hasFocus: Boolean ->
                if (hasFocus) {
                    val intent = Intent(this.applicationContext, FilePicker::class.java)
                    intent.putExtra(FilePicker.FILE_PICKER_PICK_DESTINATION_TYPE, true)
                    startActivityForResult(intent, REQUEST_CODE_FP_LOCAL)
                }
            }
    }
    private fun prepareRemote() {

        remotePathHolder = existingTask?.remotePath.toString()
        remoteDropdown.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, remoteItems)

        if (existingTask != null) {
            for ((i, remote) in remoteItems.withIndex()) {
                if (remote == existingTask!!.remoteId) {
                    remoteDropdown.setSelection(i)
                }
            }
        }

        remoteDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View, position: Int, id: Long) {
                remotePath.setText("")
                val remotename = remoteDropdown.selectedItem.toString()
                if(existingTask?.remoteId.equals(remotename)) {
                    remotePath.setText(remotePathHolder)
                }

            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }

        // Todo: This will break if the remote changed, but the path did not.
        //       Catch this issue by forcing the path to be emtpy
        remotePath.onFocusChangeListener = object : View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, p1: Boolean) {
                startRemotePicker(
                    rcloneInstance.getRemoteItemFromName(remoteDropdown.selectedItem.toString()), "/"
                )
                remotePath.clearFocus()
            }
        }
    }

    private fun prepareSyncDirectionDropdown() {
        val options = SyncDirectionObject.getOptionsArray(this)
        val directionAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)
        syncDirection.adapter = directionAdapter
        syncDirection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View,
                position: Int,
                id: Long
            ) {
                updateSpinnerDescription(position + 1)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        syncDirection.setSelection((((existingTask?.direction?.minus(1)) ?: 0)) )
    }

    private fun updateSpinnerDescription(value: Int) {
        var text = getString(R.string.description_sync_direction_sync_toremote)
        when (value) {
            SyncDirectionObject.SYNC_LOCAL_TO_REMOTE -> text =
                getString(R.string.description_sync_direction_sync_toremote)
            SyncDirectionObject.SYNC_REMOTE_TO_LOCAL -> text =
                getString(R.string.description_sync_direction_sync_tolocal)
            SyncDirectionObject.COPY_LOCAL_TO_REMOTE -> text =
                getString(R.string.description_sync_direction_copy_toremote)
            SyncDirectionObject.COPY_REMOTE_TO_LOCAL -> text =
                getString(R.string.description_sync_direction_copy_tolocal)
            SyncDirectionObject.SYNC_BIDIRECTIONAL -> text =
                getString(R.string.description_sync_direction_sync_bidirectional)
        }
        syncDescription.text = text
    }

    companion object {
        const val ID_EXTRA = "TASK_EDIT_ID"
        const val REQUEST_CODE_FP_LOCAL = 500
        const val REQUEST_CODE_FP_REMOTE = 444

    }
}