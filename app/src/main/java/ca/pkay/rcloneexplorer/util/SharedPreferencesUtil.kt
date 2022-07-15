package ca.pkay.rcloneexplorer.util

import android.content.Context
import androidx.preference.PreferenceManager

class SharedPreferencesUtil {


    companion object {
        public fun setLastOpenFragment(context: Context, lastFragmentId: Int) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context).edit()
            sharedPreferences.putInt("last_open_fragment", lastFragmentId)
            sharedPreferences.apply()
        }

        public fun getLastOpenFragment(context: Context, defaultSelection: Int): Int {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPreferences.getInt("last_open_fragment", defaultSelection)
        }
    }

}