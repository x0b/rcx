package ca.pkay.rcloneexplorer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Switch;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroCustomLayoutFragment;
import com.github.appintro.AppIntroFragment;

import org.jetbrains.annotations.Nullable;

import static ca.pkay.rcloneexplorer.ActivityHelper.tryStartActivityForResult;

public class OnboardingActivity extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addSlide(AppIntroFragment.newInstance(
                getString(R.string.intro_welcome_title),
                getString(R.string.intro_welcome_description)));

        addSlide(AppIntroFragment.newInstance(
                getString(R.string.intro_storage_title),
                getString(R.string.intro_storage_description)));

        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.intro_community));

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            askForPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2, true);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(RuntimeConfiguration.attach(this, newBase));
    }


    @Override
    protected void onNextPressed(@Nullable Fragment currentFragment) {
        if (null == currentFragment || null == currentFragment.getTag()) {
            return;
        }
        if (currentFragment.getTag().endsWith(":1")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                tryStartActivityForResult(this, intent, 3101);
            }
        }
    }

    @Override
    protected void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        if (null == newFragment || null == newFragment.getTag() || null == newFragment.getView()) {
            return;
        }
        if (newFragment.getTag().endsWith(":2")) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            Switch crashLoggingSwitch = newFragment.getView().findViewById(R.id.switch_err_logs);
            if (crashLoggingSwitch == null) {
                return;
            }
            crashLoggingSwitch.setChecked(preferences.getBoolean(
                    getString(R.string.pref_key_crash_reports),
                    getResources().getBoolean(R.bool.default_crash_log_enable)));
            crashLoggingSwitch.setOnCheckedChangeListener((btn, checked) ->
                 PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putBoolean(getString(R.string.pref_key_crash_reports), checked)
                    .apply());
        }
    }

    @Override
    protected void onDonePressed(@Nullable Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(getString(R.string.pref_key_intro_v1_12_0), true)
                .apply();
        finish();
    }
}
