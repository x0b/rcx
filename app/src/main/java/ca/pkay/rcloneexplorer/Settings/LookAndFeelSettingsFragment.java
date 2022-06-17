package ca.pkay.rcloneexplorer.Settings;

import static ca.pkay.rcloneexplorer.util.ThemeHelper.DARK;
import static ca.pkay.rcloneexplorer.util.ThemeHelper.FOLLOW_SYSTEM;
import static ca.pkay.rcloneexplorer.util.ThemeHelper.LIGHT;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;

import ca.pkay.rcloneexplorer.ActivityHelper;
import ca.pkay.rcloneexplorer.Dialogs.ColorPickerDialog;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.util.ThemeHelper;

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
        primaryColorElement = view.findViewById(R.id.primary_color);
        primaryColorPreview = view.findViewById(R.id.primary_color_preview);
        accentColorElement = view.findViewById(R.id.accent_color);
        accentColorPreview = view.findViewById(R.id.accent_color_preview);
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
        primaryColorElement.setOnClickListener(v -> showPrimaryColorPicker());
        accentColorElement.setOnClickListener(v -> showAccentColorPicker());
        autoThemeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> selectTheme(FOLLOW_SYSTEM, isChecked));
        darkThemeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> selectTheme(DARK, isChecked));
        lightThemeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> selectTheme(LIGHT, isChecked));
        wrapFilenamesElement.setOnClickListener(v -> wrapFilenamesSwitch.setChecked(!wrapFilenamesSwitch.isChecked()));
        wrapFilenamesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> onWrapFilenamesClicked(isChecked));
    }

    private void showPrimaryColorPicker() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int defaultColor = sharedPreferences.getInt(getString(R.string.pref_key_color_primary), R.color.colorPrimary);

        ColorPickerDialog colorPickerDialog = new ColorPickerDialog()
                .setTitle(R.string.primary_color_picker_title)
                .setColorChoices(R.array.primary_color_choices)
                .setDefaultColor(defaultColor)
                .setDarkTheme(ThemeHelper.isDarkTheme(this.getActivity()))
                .setListener(this::onPrimaryColorSelected);

        colorPickerDialog.show(getChildFragmentManager(), "primary color picker");
    }

    private void showAccentColorPicker() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int defaultColor = sharedPreferences.getInt(getString(R.string.pref_key_color_accent), R.color.colorAccent);

        ColorPickerDialog colorPickerDialog = new ColorPickerDialog()
                .setTitle(R.string.accent_color_picker_title)
                .setColorChoices(R.array.accent_color_choices)
                .setDefaultColor(defaultColor)
                .setDarkTheme(ThemeHelper.isDarkTheme(this.getActivity()))
                .setListener(this::onAccentColorSelected);

        colorPickerDialog.show(getChildFragmentManager(), "accent color picker");
    }

    private void onPrimaryColorSelected(int color) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getString(R.string.pref_key_color_primary), color);
        editor.apply();

        primaryColorPreview.setColorFilter(color);

        listener.onThemeChanged();
    }

    private void onAccentColorSelected(int color) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getString(R.string.pref_key_color_accent), color);
        editor.apply();

        accentColorPreview.setColorFilter(color);

        listener.onThemeChanged();
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
