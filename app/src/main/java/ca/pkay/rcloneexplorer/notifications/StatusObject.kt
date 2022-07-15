package ca.pkay.rcloneexplorer.notifications

import android.content.Context
import android.text.format.Formatter
import ca.pkay.rcloneexplorer.R
import org.json.JSONObject

class StatusObject(var mContext: Context){

    var notificationPercent: Int = 0
    var notificationContent: String = ""
    var notificationBigText = ArrayList<String>()
    lateinit var mStats: JSONObject


    fun getSpeed(): String {
        return Formatter.formatFileSize(mContext, mStats.getLong("speed")) + "/s"
    }

    fun getSize(): String {
        return Formatter.formatFileSize(mContext, mStats.getLong("bytes"))
    }

    fun getTotalSize(): String {
        return Formatter.formatFileSize(mContext, mStats.getLong("totalBytes"))
    }

    fun getPercentage(): Double {
        return mStats.getLong("bytes").toDouble() / mStats.getLong("totalBytes") * 100
    }

    fun getTransfers(): Int {
        return mStats.getInt("transfers")
    }

    fun getTotalTransfers(): Int {
        return mStats.getInt("totalTransfers")
    }



    //Todo: rename this. It's bad style
    fun readStuff(logline: JSONObject) {
        clearObject()
        mStats = logline.getJSONObject("stats")

        //available stats:
        //bytes,checks,deletedDirs,deletes,elapsedTime,errors,eta,fatalError,renames,retryError
        //speed,totalBytes,totalChecks,totalTransfers,transferTime,transfers

        //available stats:
        //bytes,checks,deletedDirs,deletes,elapsedTime,errors,eta,fatalError,renames,retryError
        //speed,totalBytes,totalChecks,totalTransfers,transferTime,transfers


        val speed = getSpeed()
        val size = getSize()
        val allsize = getTotalSize()
        val percent: Double = getPercentage()

        notificationContent = String.format(
            mContext.getString(R.string.sync_notification_short),
            size,
            allsize,
            mStats.get("eta")
        )
        notificationBigText.clear()
        notificationBigText.add(
            String.format(
                mContext.getString(R.string.sync_notification_transferred),
                size,
                allsize
            )
        )
        notificationBigText.add(
            String.format(
                mContext.getString(R.string.sync_notification_speed),
                speed
            )
        )
        notificationBigText.add(
            String.format(
                mContext.getString(R.string.sync_notification_remaining),
                mStats.get("eta")
            )
        )
        if (mStats.getInt("errors") > 0) {
            notificationBigText.add(
                String.format(
                    mContext.getString(R.string.sync_notification_errors),
                    mStats.getInt("errors")
                )
            )
        }
        //notificationBigText.add(String.format("Checks:      %d / %d", stats.getInt("checks"),  stats.getInt("totalChecks")));
        //notificationBigText.add(String.format("Transferred: %s / %s", size, allsize));
        //notificationBigText.add(String.format("Checks:      %d / %d", stats.getInt("checks"),  stats.getInt("totalChecks")));
        //notificationBigText.add(String.format("Transferred: %s / %s", size, allsize));
        notificationBigText.add(
            String.format(
                mContext.getString(R.string.sync_notification_elapsed),
                mStats.getInt("elapsedTime")
            )
        )
        notificationPercent = percent.toInt()
    }

    fun clearObject() {
        notificationPercent = 0
        notificationContent = ""
        notificationBigText = ArrayList<String>()
    }

    override fun toString(): String {
        return "StatusObject(getSpeed=${getSpeed()}, getSize=${getSize()}, getTotalSize=${getTotalSize()}, getTransfers=${getTransfers()}, getTotalTransfers=${getTotalTransfers()})"
    }


}