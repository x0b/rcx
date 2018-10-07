package ca.pkay.rcloneexplorer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.messaging.FirebaseMessaging;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends BaseMainActivity {

    @Override
    protected boolean updateCheck() {
        if (getIntent() != null) {
            String s = getIntent().getStringExtra(getString(R.string.firebase_msg_app_updates_topic));
            if (s != null && s.equals("true")) {
                openAppUpdate();
                finish();
                return true;
            }

            s = getIntent().getStringExtra(getString(R.string.firebase_msg_beta_app_updates_topic));
            if (s != null) {
                openBetaUpdate(s);
                finish();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void checkEnableCrashReports(SharedPreferences sharedPreferences) {
        boolean enableCrashReports = sharedPreferences.getBoolean(getString(R.string.pref_key_crash_reports), false);
        if (enableCrashReports) {
            Fabric.with(this, new Crashlytics());
        }
    }

    @Override
    protected void checkSubscribeToUpdates(SharedPreferences sharedPreferences) {
        boolean appUpdates = sharedPreferences.getBoolean(getString(R.string.pref_key_app_updates), false);
        if (appUpdates) {
            FirebaseMessaging.getInstance().subscribeToTopic(getString(R.string.firebase_msg_app_updates_topic));
        }
    }

    private void openAppUpdate() {
        Uri uri = Uri.parse(getString(R.string.app_latest_release_url));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private void openBetaUpdate(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

}
