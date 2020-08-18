package ca.pkay.rcloneexplorer.Settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;

import ca.pkay.rcloneexplorer.BuildConfig;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.util.CrashLogger;
import ca.pkay.rcloneexplorer.util.FLog;
import es.dmoral.toasty.Toasty;

public class LoggingSettingsFragment extends Fragment {

    private Context context;
    private Switch useLogsSwitch;
    private View useLogsElement;
    private View crashReportsElement;
    private Switch crashReportsSwitch;
    private View testReportElement;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LoggingSettingsFragment() {
    }

    public static LoggingSettingsFragment newInstance() {
        return new LoggingSettingsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.logging_settings_fragment, container, false);
        getViews(view);
        setDefaultStates();
        setClickListeners();

        if (getActivity() != null) {
            getActivity().setTitle(getString(R.string.logging_settings_header));
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private void getViews(View view) {
        useLogsSwitch = view.findViewById(R.id.use_logs_switch);
        useLogsElement = view.findViewById(R.id.use_logs);
        crashReportsElement = view.findViewById(R.id.crash_reporting);
        crashReportsSwitch = view.findViewById(R.id.crash_reporting_switch);
        testReportElement = view.findViewById(R.id.send_test_report);
    }

    private void setDefaultStates() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useLogs = sharedPreferences.getBoolean(getString(R.string.pref_key_logs), false);
        boolean crashReports = sharedPreferences.getBoolean(getString(R.string.pref_key_crash_reports),
                getResources().getBoolean(R.bool.default_crash_log_enable));

        useLogsSwitch.setChecked(useLogs);
        crashReportsSwitch.setChecked(crashReports);
    }

    private void setClickListeners() {
        useLogsElement.setOnClickListener(v -> useLogsSwitch.setChecked(!useLogsSwitch.isChecked()));
        useLogsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> onUseLogsClicked(isChecked));
        crashReportsElement.setOnClickListener(v -> crashReportsSwitch.setChecked(!crashReportsSwitch.isChecked()));
        crashReportsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> crashReportsClicked(isChecked));
        testReportElement.setOnClickListener(v -> FLog.e(
                "TestReport",
                "Sending test report, %s, %s, %s",
                "/storage/0/private.file",
                "content://authority/private.file",
                "Non-filterd argument"));
    }

    private void onUseLogsClicked(boolean isChecked) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_key_logs), isChecked);
        editor.apply();
    }

    private void crashReportsClicked(boolean isChecked) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_key_crash_reports), isChecked);
        editor.apply();

        Toasty.info(context, getString(R.string.restart_required), Toast.LENGTH_SHORT, true).show();
    }
}
