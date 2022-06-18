package ca.pkay.rcloneexplorer.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager

class WifiConnectivitiyUtil {

    companion object {

        /**
         * Check if wifi is connected. Also respects if wifi is metered.
         */
        fun checkWifiOnAndConnected(mContext: Context): Boolean {
            val wifiMgr = mContext.getSystemService(Context.WIFI_SERVICE) as WifiManager? ?: return false
            return if (wifiMgr.isWifiEnabled) {
                // Wi-Fi adapter is ON
                // WifiManager requires location access. This is not available, so we query the metered instead.
                val cm = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                (!cm.isActiveNetworkMetered)
            } else {
                FLog.e("SyncService.TAG", "Wifi not turned on.")
                false // Wi-Fi adapter is OFF
            }
        }
    }
}