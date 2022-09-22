package ca.pkay.rcloneexplorer.Items

class Task(var id: Long) {
    var title = ""
    var remoteId = ""
    var remoteType = 0
    var remotePath = ""
    var localPath = ""
    var direction = 0
    var md5sum = TASK_MD5SUM_DEFAULT
    var wifionly = TASK_WIFIONLY_DEFAULT

    override fun toString(): String {
        return "$title: $remoteId: $remoteType: $remotePath: $localPath: $direction"
    }

    companion object {
        var TABLE_NAME = "task_table"
        var COLUMN_NAME_ID = "task_id"
        var COLUMN_NAME_TITLE = "task_title"
        var COLUMN_NAME_REMOTE_ID = "task_remote_id"
        var COLUMN_NAME_REMOTE_TYPE = "task_remote_type"
        var COLUMN_NAME_REMOTE_PATH = "task_remote_path"
        var COLUMN_NAME_LOCAL_PATH = "task_local_path"
        var COLUMN_NAME_SYNC_DIRECTION = "task_direction"
        var COLUMN_NAME_MD5SUM = "task_use_md5sum"
        var COLUMN_NAME_WIFI_ONLY = "task_use_only_wifi"

        const val TASK_MD5SUM_DEFAULT = false
        const val TASK_WIFIONLY_DEFAULT = false
    }
}