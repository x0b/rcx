package ca.pkay.rcloneexplorer.Settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import ca.pkay.rcloneexplorer.R;
import es.dmoral.toasty.Toasty;

public class NotificationsSettingsFragment extends Fragment {

    private Context context;
    private View notificationsElement;
    private View appUpdatesElement;
    private Switch appUpdatesSwitch;
    private View betaAppUpdatesElement;
    private Switch betaAppUpdatesSwitch;

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
        View view = inflater.inflate(R.layout.notification_settings_fragment, container, false);
        getViews(view);
        setDefaultStates();
        setClickListeners();

        if (getActivity() != null) {
            getActivity().setTitle(getString(R.string.notifications_pref_title));
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private void getViews(View view) {
        notificationsElement = view.findViewById(R.id.notifications);
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

    private void setClickListeners() {

        notificationsElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNotificationsClicked();
            }
        });
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
