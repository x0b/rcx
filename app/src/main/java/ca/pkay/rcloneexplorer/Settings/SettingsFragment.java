package ca.pkay.rcloneexplorer.Settings;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.databinding.SettingsFragmentBinding;

public class SettingsFragment extends Fragment {

    public final static int GENERAL_SETTINGS = 1;
    public final static int FILE_ACCESS_SETTINGS = 2;
    public final static int LOOK_AND_FEEL_SETTINGS = 3;
    public final static int LOGGING_SETTINGS = 4;
    public final static int NOTIFICATION_SETTINGS = 5;
    private OnSettingCategorySelectedListener clickListener;
    private SettingsFragmentBinding binding;

    public interface OnSettingCategorySelectedListener {
        void onSettingCategoryClicked(int category);
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SettingsFragment() {
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SettingsFragmentBinding.inflate(inflater, container, false);
        setClickListeners(binding);

        if (getActivity() != null) {
            getActivity().setTitle(getString(R.string.settings));
        }

        return binding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSettingCategorySelectedListener) {
            clickListener = (OnSettingCategorySelectedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement listener");
        }
    }

    private void setClickListeners(SettingsFragmentBinding view) {
        binding.generalSettings.setOnClickListener(v -> clickListener.onSettingCategoryClicked(GENERAL_SETTINGS));
        binding.loggingSettings.setOnClickListener(v -> clickListener.onSettingCategoryClicked(LOGGING_SETTINGS));
        binding.lookAndFeelSettings.setOnClickListener(v -> clickListener.onSettingCategoryClicked(LOOK_AND_FEEL_SETTINGS));
        binding.notificationSettings.setOnClickListener(v -> clickListener.onSettingCategoryClicked(NOTIFICATION_SETTINGS));
        binding.fileAccessSettings.setOnClickListener(v -> clickListener.onSettingCategoryClicked(FILE_ACCESS_SETTINGS));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
