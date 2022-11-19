package ca.pkay.rcloneexplorer.util

import android.app.PendingIntent
import android.os.Build

class FlagsUtil {

    companion object {

        fun getFlagImmutable() : Int {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }
        }

        fun getFlagImmutable(flag: Int) : Int {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE and flag
            } else {
                flag
            }
        }
    }

}