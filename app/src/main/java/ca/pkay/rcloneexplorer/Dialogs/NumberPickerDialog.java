package ca.pkay.rcloneexplorer.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.Spinner;

import ca.pkay.rcloneexplorer.R;

public class NumberPickerDialog extends DialogFragment {

    public static final int UNITS_STORAGE = 0;
    public static final int UNITS_TIME = 1;
    public static final int UNITS_MB = 10;
    public static final int UNITS_GB = 11;
    public static final int UNITS_S = 20;
    public static final int UNITS_M = 21;
    public static final int UNITS_H = 22;
    private final String SAVED_TITLE = "ca.pkay.rcexplorer.NumberPickerDialog.TITLE";
    private final String SAVED_IS_DARK_THEME = "ca.pkay.rcexplorer.NumberPickerDialog.IS_DARK_THEME";
    private final String SAVED_OPTION_UNITS = "ca.pkay.rcexplorer.NumberPickerDialog.OPTION_UNITS";
    private final String SAVED_SET_VALUE = "ca.pkay.rcexplorer.NumberPickerDialog.SET_VALUE";
    private Context context;
    private Spinner spinner;
    private NumberPicker numberPicker;
    private int title;
    private boolean isDarkTheme;
    private int optionUnits;
    private int defaultValue;
    private OnValueSelected listener;

    public interface OnValueSelected {
        void onValueSelected(String tag, int number, int units);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            title = savedInstanceState.getInt(SAVED_TITLE);
            isDarkTheme = savedInstanceState.getBoolean(SAVED_IS_DARK_THEME);
            optionUnits = savedInstanceState.getInt(SAVED_OPTION_UNITS);
            defaultValue = savedInstanceState.getInt(SAVED_SET_VALUE);
        }

        listener = (OnValueSelected) getParentFragment();

        AlertDialog.Builder builder;
        if (isDarkTheme) {
            builder = new AlertDialog.Builder(context, R.style.DarkDialogTheme);
        } else {
            builder = new AlertDialog.Builder(context);
        }

        LayoutInflater inflater = ((FragmentActivity)context).getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_number_picker, null);

        numberPicker = view.findViewById(R.id.number_picker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(1000);
        numberPicker.setValue(defaultValue);

        spinner = view.findViewById(R.id.spinner);

        String[] options;
        if (optionUnits == UNITS_STORAGE) {
            options = new String[] {"MB", "GB"};
        } else if (optionUnits == UNITS_TIME) {
            options = new String[] {"seconds", "minutes", "hours"};
        } else {
            options = new String[1];
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_dropdown_item, options);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(adapter);

        builder.setPositiveButton(R.string.select, (dialog, which) -> valueSelected());
        builder.setTitle(title);
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_TITLE, title);
        outState.putBoolean(SAVED_IS_DARK_THEME, isDarkTheme);
        outState.putInt(SAVED_OPTION_UNITS, optionUnits);
        outState.putInt(SAVED_SET_VALUE, numberPicker.getValue());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private void valueSelected() {
        numberPicker.clearFocus();
        int number = numberPicker.getValue();
        String unitString = spinner.getSelectedItem().toString();
        int unit;

        switch (unitString) {
            case "MB":
                unit = UNITS_MB;
                break;
            case "GB":
                unit = UNITS_GB;
                break;
            case "seconds":
                unit = UNITS_S;
                break;
            case "minutes":
                unit = UNITS_M;
                break;
            case "hours":
                unit = UNITS_H;
                break;
            default:
                unit = -1;
        }

        listener.onValueSelected(getTag(), number, unit);
    }

    public NumberPickerDialog setDarkTheme(boolean isDarkTheme) {
        this.isDarkTheme = isDarkTheme;
        return this;
    }

    public NumberPickerDialog setTitle(int title) {
        this.title = title;
        return this;
    }

    public NumberPickerDialog setNumberUnits(int units) {
        optionUnits = units;
        return this;
    }

    public NumberPickerDialog setDefaultValue(int defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }
}
