package ca.pkay.rcloneexplorer.Settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import ca.pkay.rcloneexplorer.R;
import es.dmoral.toasty.Toasty;

public class LoggingSettingsFragment extends BaseLoggingSettingsFragment {

    private View crashReportsElement;
    private Switch crashReportsSwitch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        getViews(view);
        setDefaultStates();
        setClickListeners();

        return view;
    }

    private void getViews(View view) {
        crashReportsElement = view.findViewById(R.id.crash_reporting);
        crashReportsSwitch = view.findViewById(R.id.crash_reporting_switch);
    }

    private void setDefaultStates() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useLogs = sharedPreferences.getBoolean(getString(R.string.pref_key_logs), false);
        boolean crashReports = sharedPreferences.getBoolean(getString(R.string.pref_key_crash_reports), false);

        crashReportsSwitch.setChecked(crashReports);
    }

    private void setClickListeners() {
        crashReportsElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (crashReportsSwitch.isChecked()) {
                    crashReportsSwitch.setChecked(false);
                } else {
                    crashReportsSwitch.setChecked(true);
                }
            }
        });
        crashReportsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                crashReportsClicked(isChecked);
            }
        });
    }

    private void crashReportsClicked(boolean isChecked) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_key_crash_reports), isChecked);
        editor.apply();

        Toasty.info(context, getString(R.string.restart_required), Toast.LENGTH_SHORT, true).show();
    }
}
