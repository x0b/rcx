package ca.pkay.rcloneexplorer.notifications

import android.content.Context
import android.text.format.Formatter
import android.util.Log
import ca.pkay.rcloneexplorer.R
import org.json.JSONObject

class StatusObject(var mContext: Context){

    var notificationPercent: Int = 0
    var notificationContent: String = ""
    var notificationBigText = ArrayList<String>()
    var mErrorList = ArrayList<ErrorObject>()
    var mStats = JSONObject()
    var mLogline = JSONObject()


    fun getSpeed(): String {
        return Formatter.formatFileSize(mContext, mStats.optLong("speed", 0)) + "/s"
    }

    fun getSize(): String {
        return Formatter.formatFileSize(mContext, mStats.optLong("bytes", 0))
    }

    fun getTotalSize(): String {
        return Formatter.formatFileSize(mContext, mStats.optLong("totalBytes", 0))
    }

    fun getPercentage(): Double {
        return mStats.optLong("bytes", 0).toDouble() / mStats.optLong("totalBytes", 0) * 100
    }

    fun getTransfers(): Int {
        return mStats.optInt("transfers", 0)
    }

    fun getTotalTransfers(): Int {
        return mStats.optInt("totalTransfers", 0)
    }

    fun getDeletions(): Int {
        return mStats.optInt("deletes", 0) + mStats.optInt("deletedDirs", 0)
    }

    fun getErrorMessage(): String {
        if(mLogline.has("msg") && mLogline.getString("level") == "error") {
            return mLogline.getString("msg")
        }
        return ""
    }

    fun getErrorObject(): String {
        if(mLogline.has("msg") && mLogline.getString("level") == "error") {
            return mLogline.optString("object", "")
        }
        return ""
    }

    //Todo: rename this. It's bad style
    fun readStuff(logline: JSONObject) {
        clearObject()
        mLogline = logline

        Log.e("Tast", logline.toString())
        if(mLogline.getString("level") == "error") {
            var e = ErrorObject(getErrorObject(), getErrorMessage())
            Log.e("TAG", e.mErrorObject + " - " + e.mErrorMessage)
            mErrorList.add(e)
        }

        if(mLogline.has("stats")) {
            mStats = mLogline.getJSONObject("stats")

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

            if (getDeletions() > 0) {
                notificationBigText.add(
                        String.format(
                                mContext.getString(R.string.sync_notification_deletions),
                                getDeletions()
                        )
                )
            }

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
    }

    fun clearObject() {
        notificationPercent = 0
        notificationContent = ""
        notificationBigText = ArrayList<String>()
        mLogline = JSONObject()
    }

    fun printErrors(){
        mErrorList.forEach {
            Log.e("TAG", it.mErrorObject + " - " + it.mErrorMessage)
        }
    }

    fun getAllErrorMessages(): String{
        var all = ""
        mErrorList.forEach {
            all += it.mErrorMessage + "\n"
            all += mContext.getString(R.string.status_offendingfile) + it.mErrorObject + "\n"
        }
        return all
    }

    override fun toString(): String {
        return "StatusObject(getSpeed=${getSpeed()}, getSize=${getSize()}, getTotalSize=${getTotalSize()}, getTransfers=${getTransfers()}, getTotalTransfers=${getTotalTransfers()}, getErrorMessage=${getErrorMessage()}, getDeletions=${getDeletions()})"
    }
}