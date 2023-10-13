package ca.pkay.rcloneexplorer.Items

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Trigger(var id: Long) {
    var title = ""
    var isEnabled = true
    var weekdays: Byte = 0b01111111 //treat as binary, so that each digit represents an boolean.
    var time = 0 //in seconds since 00:00
    var whatToTrigger = 0L
    var type = TRIGGER_TYPE_SCHEDULE


    /**
     * The weekday starts with monday.
     * Therefore is 0 monday, and 6 sunday.
     * @param weekday
     * @return
     */
    fun isEnabledAtDay(weekday: Int): Boolean {
        return weekdays.toInt() shr weekday and 1 == 1
    }

    /**
     * The weekday starts with monday.
     * Therefore is 0 monday, and 6 sunday.
     * @param weekday
     * @param enabled
     */
    fun setEnabledAtDay(weekday: Int, enabled: Boolean) {
        weekdays = if (enabled) {
            (weekdays.toInt() or (1 shl weekday)).toByte()
        } else {
            (weekdays.toInt() and (1 shl weekday).inv()).toByte()
        }
    }

    fun setWeekdays(weekdays: Byte) {
        this.weekdays = weekdays
    }

    override fun toString(): String {
        return "Trigger{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", isEnabled=" + isEnabled +
                ", weekdays=" + weekdays +
                ", time=" + time +
                ", whatToTrigger=" + whatToTrigger +
                ", type=" + type +
                '}'
    }

    private fun binary(i: Byte): String {
        return String.format("%8s", Integer.toBinaryString(i.toInt() and 0xFF)).replace(' ', '0')
    }

    fun asJSON(): String {
        return Json.encodeToString(this)
    }

    companion object {
        const val TABLE_NAME = "trigger_table"
        const val COLUMN_NAME_ID = "trigger_id"
        const val COLUMN_NAME_TITLE = "trigger_title"
        const val COLUMN_NAME_TIME = "trigger_time"
        const val COLUMN_NAME_WEEKDAY = "trigger_weekday"
        const val COLUMN_NAME_ENABLED = "trigger_enabled"
        const val COLUMN_NAME_TARGET = "trigger_target"
        const val COLUMN_NAME_TYPE = "trigger_type"

        const val TRIGGER_TYPE_SCHEDULE = 0
        const val TRIGGER_TYPE_INTERVAL = 1

        const val TRIGGER_ID_DOESNTEXIST = -1L

        const val TRIGGER_DAY_MON = 0
        const val TRIGGER_DAY_TUE = 1
        const val TRIGGER_DAY_WED = 2
        const val TRIGGER_DAY_THU = 3
        const val TRIGGER_DAY_FRI = 4
        const val TRIGGER_DAY_SAT = 5
        const val TRIGGER_DAY_SUN = 6
    }
}