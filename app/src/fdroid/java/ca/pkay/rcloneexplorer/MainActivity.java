package ca.pkay.rcloneexplorer;

import android.content.SharedPreferences;

public class MainActivity extends BaseMainActivity {
    @Override
    protected boolean updateCheck() {
        return false;
    }

    @Override
    protected void checkEnableCrashReports(SharedPreferences sharedPreferences) {

    }

    @Override
    protected void checkSubscribeToUpdates(SharedPreferences sharedPreferences) {

    }
}
