package ca.pkay.rcloneexplorer.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkStateReceiver extends BroadcastReceiver {
    private Boolean isNetworkAvailable;

    @Override
    public void onReceive(Context context, Intent intent) {
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (networkInfo == null) {
            isNetworkAvailable = false;
        } else if (networkInfo.isConnected()) {
            isNetworkAvailable = true;
        }
    }

    public Boolean isNetworkAvailable() {
        return isNetworkAvailable;
    }
}
