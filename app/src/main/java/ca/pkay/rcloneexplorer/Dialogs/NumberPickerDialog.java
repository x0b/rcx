package ca.pkay.rcloneexplorer.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
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
    private Context context;
    private Spinner spinner;
    private NumberPicker numberPicker;
    private int title;
    private boolean isDarkTheme;
    private String[] options;
    private int defaultValue;
    private OnValueSelected listener;

    public interface OnValueSelected {
        void onValueSelected(int number, int units);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_dropdown_item, options);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(adapter);

        builder.setPositiveButton(R.string.select, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                valueSelected();
            }
        });
        builder.setTitle(title);
        builder.setView(view);
        return builder.create();
    }

    private void valueSelected() {
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

        listener.onValueSelected(number, unit);
    }

    public NumberPickerDialog withContext(Context context) {
        this.context = context;
        return this;
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
        if (units == UNITS_STORAGE) {
            options = new String[] {"MB", "GB"};
        } else if (units == UNITS_TIME) {
            options = new String[] {"seconds", "minutes", "hours"};
        }
        return this;
    }

    public NumberPickerDialog setDefaultValue(int defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public NumberPickerDialog setListener(OnValueSelected listener) {
        this.listener = listener;
        return this;
    }
}
