package ca.pkay.rcloneexplorer.notifications

import android.content.Context
import android.text.format.Formatter
import ca.pkay.rcloneexplorer.R
import org.json.JSONObject

class StatusObject {

    var notificationPercent: Int = 0
    var notificationContent: String = ""
    var notificationBigText = ArrayList<String>()


    fun readStuff(context: Context, logline: JSONObject) {
        //available stats:
        //bytes,checks,deletedDirs,deletes,elapsedTime,errors,eta,fatalError,renames,retryError
        //speed,totalBytes,totalChecks,totalTransfers,transferTime,transfers

        //available stats:
        //bytes,checks,deletedDirs,deletes,elapsedTime,errors,eta,fatalError,renames,retryError
        //speed,totalBytes,totalChecks,totalTransfers,transferTime,transfers
        var stats = logline.getJSONObject("stats")

        val speed = Formatter.formatFileSize(context, stats.getLong("speed")) + "/s"
        val size = Formatter.formatFileSize(context, stats.getLong("bytes"))
        val allsize = Formatter.formatFileSize(context, stats.getLong("totalBytes"))
        val percent: Double = stats.getLong("bytes").toDouble() / stats.getLong("totalBytes") * 100

        notificationContent = String.format(
            context.getString(R.string.sync_notification_short),
            size,
            allsize,
            stats.get("eta")
        )
        notificationBigText.clear()
        notificationBigText.add(
            String.format(
                context.getString(R.string.sync_notification_transferred),
                size,
                allsize
            )
        )
        notificationBigText.add(
            String.format(
                context.getString(R.string.sync_notification_speed),
                speed
            )
        )
        notificationBigText.add(
            String.format(
                context.getString(R.string.sync_notification_remaining),
                stats.get("eta")
            )
        )
        if (stats.getInt("errors") > 0) {
            notificationBigText.add(
                String.format(
                    context.getString(R.string.sync_notification_errors),
                    stats.getInt("errors")
                )
            )
        }
        //notificationBigText.add(String.format("Checks:      %d / %d", stats.getInt("checks"),  stats.getInt("totalChecks")));
        //notificationBigText.add(String.format("Transferred: %s / %s", size, allsize));
        //notificationBigText.add(String.format("Checks:      %d / %d", stats.getInt("checks"),  stats.getInt("totalChecks")));
        //notificationBigText.add(String.format("Transferred: %s / %s", size, allsize));
        notificationBigText.add(
            String.format(
                context.getString(R.string.sync_notification_elapsed),
                stats.getInt("elapsedTime")
            )
        )
        notificationPercent = percent.toInt()
    }


}