package ca.pkay.rcloneexplorer.Services.support

class QueueItem(val title: String) {

    private val items = HashMap<String, Any>()

    fun get(key: String): Any? {
        return items[key]
    }

    fun set(key: String, value: Any) {
        items[key] = value
    }
}