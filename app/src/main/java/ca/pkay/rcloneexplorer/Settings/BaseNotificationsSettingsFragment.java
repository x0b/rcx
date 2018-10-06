package ca.pkay.rcloneexplorer.Settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import ca.pkay.rcloneexplorer.R;
import es.dmoral.toasty.Toasty;

public abstract class BaseNotificationsSettingsFragment extends Fragment {

    protected Context context;
    private View notificationsElement;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BaseNotificationsSettingsFragment() {
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
    }

    private void setClickListeners() {
        notificationsElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNotificationsClicked();
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
}
