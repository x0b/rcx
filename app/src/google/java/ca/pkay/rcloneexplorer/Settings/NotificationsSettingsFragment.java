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

import com.google.firebase.messaging.FirebaseMessaging;

import ca.pkay.rcloneexplorer.R;

public class NotificationsSettingsFragment extends BaseNotificationsSettingsFragment {

    private View appUpdatesElement;
    private Switch appUpdatesSwitch;
    private View betaAppUpdatesElement;
    private Switch betaAppUpdatesSwitch;

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
        appUpdatesElement = view.findViewById(R.id.app_updates);
        appUpdatesSwitch = view.findViewById(R.id.app_updates_switch);
        betaAppUpdatesElement = view.findViewById(R.id.beta_app_updates);
        betaAppUpdatesSwitch = view.findViewById(R.id.beta_app_updates_switch);
    }

    private void setDefaultStates() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean appUpdates = sharedPreferences.getBoolean(getString(R.string.pref_key_app_updates), false);
        boolean betaUpdates = sharedPreferences.getBoolean(getString(R.string.pref_key_app_updates_beta), false);

        appUpdatesSwitch.setChecked(appUpdates);
        betaAppUpdatesSwitch.setChecked(betaUpdates);

        if (appUpdates) {
            betaAppUpdatesElement.setVisibility(View.VISIBLE);
        } else {
            betaAppUpdatesElement.setVisibility(View.GONE);
        }
    }

    protected void setClickListeners() {
        appUpdatesElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (appUpdatesSwitch.isChecked()) {
                    appUpdatesSwitch.setChecked(false);
                } else {
                    appUpdatesSwitch.setChecked(true);
                }
            }
        });
        appUpdatesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onAppUpdatesClicked(isChecked);
            }
        });
        betaAppUpdatesElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (betaAppUpdatesSwitch.isChecked()) {
                    betaAppUpdatesSwitch.setChecked(false);
                } else {
                    betaAppUpdatesSwitch.setChecked(true);
                }
            }
        });
        betaAppUpdatesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onBetaAppUpdatesClicked(isChecked);
            }
        });
    }

    private void onAppUpdatesClicked(boolean isChecked) {
        if (isChecked) {
            FirebaseMessaging.getInstance().subscribeToTopic(getString(R.string.firebase_msg_app_updates_topic));
            betaAppUpdatesElement.setVisibility(View.VISIBLE);
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(getString(R.string.firebase_msg_app_updates_topic));
            betaAppUpdatesSwitch.setChecked(false);
            betaAppUpdatesElement.setVisibility(View.GONE);
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_key_app_updates), isChecked);
        editor.apply();
    }

    private void onBetaAppUpdatesClicked(boolean isChecked) {
        if (isChecked) {
            FirebaseMessaging.getInstance().subscribeToTopic(getString(R.string.firebase_msg_beta_app_updates_topic));
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(getString(R.string.firebase_msg_beta_app_updates_topic));
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_key_app_updates_beta), isChecked);
        editor.apply();
    }
}
