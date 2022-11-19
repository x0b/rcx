package ca.pkay.rcloneexplorer.Settings;


import static ca.pkay.rcloneexplorer.util.ActivityHelper.DARK;
import static ca.pkay.rcloneexplorer.util.ActivityHelper.FOLLOW_SYSTEM;
import static ca.pkay.rcloneexplorer.util.ActivityHelper.LIGHT;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.util.ActivityHelper;

public class LookAndFeelSettingsFragment extends Fragment {

    private Context context;
    private OnThemeHasChanged listener;
    private View primaryColorElement;
    private ImageView primaryColorPreview;
    private View accentColorElement;
    private ImageView accentColorPreview;
    private Switch autoThemeSwitch;
    private Switch darkThemeSwitch;
    private Switch lightThemeSwitch;
    private Switch wrapFilenamesSwitch;
    private View wrapFilenamesElement;
    private int theme = FOLLOW_SYSTEM;

    public interface OnThemeHasChanged {
        void onThemeChanged();
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LookAndFeelSettingsFragment() {
    }

    public static LookAndFeelSettingsFragment newInstance() {
        return new LookAndFeelSettingsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment_look_and_feel, container, false);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        theme = sharedPreferences.getInt(getString(R.string.pref_key_dark_theme), FOLLOW_SYSTEM);

        getViews(view);
        setDefaultStates();
        setClickListeners();

        if (getActivity() != null) {
            getActivity().setTitle(getString(R.string.look_and_feel));
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof OnThemeHasChanged) {
            listener = (OnThemeHasChanged) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement listener");
        }
    }

    private void getViews(View view) {
        autoThemeSwitch = view.findViewById(R.id.auto_theme_switch);
        darkThemeSwitch = view.findViewById(R.id.dark_theme_switch);
        lightThemeSwitch = view.findViewById(R.id.light_theme_switch);
        wrapFilenamesSwitch = view.findViewById(R.id.wrap_filenames_switch);
        wrapFilenamesElement = view.findViewById(R.id.wrap_filenames);
    }

    private void setDefaultStates() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int theme = sharedPreferences.getInt(getString(R.string.pref_key_dark_theme), FOLLOW_SYSTEM);
        boolean isWrapFilenames = sharedPreferences.getBoolean(getString(R.string.pref_key_wrap_filenames), true);

        setThemeSelectionSwitches(theme);
        wrapFilenamesSwitch.setChecked(isWrapFilenames);
    }

    private void setClickListeners() {
        autoThemeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> selectTheme(FOLLOW_SYSTEM, isChecked));
        darkThemeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> selectTheme(DARK, isChecked));
        lightThemeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> selectTheme(LIGHT, isChecked));
        wrapFilenamesElement.setOnClickListener(v -> wrapFilenamesSwitch.setChecked(!wrapFilenamesSwitch.isChecked()));
        wrapFilenamesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> onWrapFilenamesClicked(isChecked));
    }

    //Todo: Update this to radiobuttons
    private void selectTheme(int mode, boolean isChecked) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if(isChecked) {
            editor.putInt(getString(R.string.pref_key_dark_theme), mode);
            editor.apply();
            setThemeSelectionSwitches(mode);
        }

        //listener.onThemeChanged();
        ActivityHelper.applyTheme(this.getActivity());
    }

    private void setThemeSelectionSwitches(int mode) {
        autoThemeSwitch.setChecked(false);
        darkThemeSwitch.setChecked(false);
        lightThemeSwitch.setChecked(false);

        switch(mode) {
            case LIGHT:
                lightThemeSwitch.setChecked(true);
                break;
            case DARK:
                darkThemeSwitch.setChecked(true);
                break;
            case FOLLOW_SYSTEM:
            default:
                autoThemeSwitch.setChecked(true);
                break;
        }
    }

    private void onWrapFilenamesClicked(boolean isChecked) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_key_wrap_filenames), isChecked);
        editor.apply();
    }
}
