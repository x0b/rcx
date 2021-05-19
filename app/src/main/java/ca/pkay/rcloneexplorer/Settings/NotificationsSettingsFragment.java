package ca.pkay.rcloneexplorer.Settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.databinding.NotificationSettingsFragmentBinding;
import es.dmoral.toasty.Toasty;

public class NotificationsSettingsFragment extends Fragment {

    private Context context;
    private NotificationSettingsFragmentBinding binding;
    private View notificationsElement;
    private View appUpdatesElement;
    private SwitchCompat appUpdatesSwitch;
    private View betaAppUpdatesElement;
    private SwitchCompat betaAppUpdatesSwitch;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NotificationsSettingsFragment() {
    }

    public static NotificationsSettingsFragment newInstance() {
        return new NotificationsSettingsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = NotificationSettingsFragmentBinding.inflate(inflater, container, false);
        getViews(binding);
        setDefaultStates();
        setClickListeners();

        if (getActivity() != null) {
            getActivity().setTitle(getString(R.string.notifications_pref_title));
        }

        return binding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void getViews(NotificationSettingsFragmentBinding binding) {
        notificationsElement = binding.notifications;
        appUpdatesElement = binding.appUpdates;
        appUpdatesSwitch = binding.appUpdatesSwitch;
        betaAppUpdatesElement = binding.betaAppUpdates;
        betaAppUpdatesSwitch = binding.betaAppUpdatesSwitch;
    }

    private void setDefaultStates() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean appUpdates = sharedPreferences.getBoolean(getString(R.string.pref_key_app_updates), true);
        boolean betaUpdates = sharedPreferences.getBoolean(getString(R.string.pref_key_app_updates_beta), false);

        appUpdatesSwitch.setChecked(appUpdates);
        betaAppUpdatesSwitch.setChecked(betaUpdates);

        if (appUpdates) {
            betaAppUpdatesElement.setVisibility(View.VISIBLE);
        } else {
            betaAppUpdatesElement.setVisibility(View.GONE);
        }
    }

    private void setClickListeners() {

        notificationsElement.setOnClickListener(v -> onNotificationsClicked());
        appUpdatesElement.setOnClickListener(v -> {
            if (appUpdatesSwitch.isChecked()) {
                appUpdatesSwitch.setChecked(false);
            } else {
                appUpdatesSwitch.setChecked(true);
            }
        });
        appUpdatesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> onAppUpdatesClicked(isChecked));
        betaAppUpdatesElement.setOnClickListener(v -> {
            if (betaAppUpdatesSwitch.isChecked()) {
                betaAppUpdatesSwitch.setChecked(false);
            } else {
                betaAppUpdatesSwitch.setChecked(true);
            }
        });
        betaAppUpdatesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> onBetaAppUpdatesClicked(isChecked));
    }

    private void onNotificationsClicked() {
        Intent intent = new Intent();
        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");

        //for Android 5-7
        intent.putExtra("app_package", context.getPackageName());
        intent.putExtra("app_uid", context.getApplicationInfo().uid);

        // for Android O
        intent.putExtra("android.provider.extra.APP_PACKAGE", context.getPackageName());

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toasty.error(context, "Couldn't find activity to start", Toast.LENGTH_SHORT, true).show();
        }
    }

    private void onAppUpdatesClicked(boolean isChecked) {
        if (isChecked) {
            betaAppUpdatesElement.setVisibility(View.VISIBLE);
        } else {
            betaAppUpdatesSwitch.setChecked(false);
            betaAppUpdatesElement.setVisibility(View.GONE);
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_key_app_updates), isChecked);
        editor.putLong(getString(R.string.pref_key_update_last_check), 0L);
        editor.apply();
    }

    private void onBetaAppUpdatesClicked(boolean isChecked) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_key_app_updates_beta), isChecked);
        editor.putLong(getString(R.string.pref_key_update_last_check), 0L);
        editor.apply();
    }
}
