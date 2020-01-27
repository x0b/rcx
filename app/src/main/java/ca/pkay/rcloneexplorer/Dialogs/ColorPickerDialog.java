package ca.pkay.rcloneexplorer.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.AlertDialog;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import ca.pkay.rcloneexplorer.R;

public class ColorPickerDialog extends DialogFragment {

    public interface OnClickListener {
        void onColorSelected(int color);
    }

    private final String SAVED_IS_DARK_MODE = "ca.pkay.rcexplorer.ColorPickerDialog.IS_DARK_MODE";
    private final String SAVED_SELECTED_COLOR = "ca.pkay.rcexplorer.ColorPickerDialog.SELECTED_COLOR";
    private final String SAVED_DEFAULT_COLOR = "ca.pkay.rcexplorer.ColorPickerDialog.DEFAULT_COLOR";
    private final String SAVED_TITLE = "ca.pkay.rcexplorer.ColorPickerDialog.TITLE";
    private final String SAVED_COLOR_CHOICES = "ca.pkay.rcexplorer.ColorPickerDialog.COLOR_CHOICES";
    private Context context;
    private boolean isDarkMode;
    private int[] colorChoices;
    private View visibleCheckmark;
    private int selectedColor;
    private int defaultColor;
    private int title;
    private int colorChoicesArrayId;
    private OnClickListener listener;

    public ColorPickerDialog() {
        isDarkMode = false;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            isDarkMode = savedInstanceState.getBoolean(SAVED_IS_DARK_MODE);
            selectedColor = savedInstanceState.getInt(SAVED_SELECTED_COLOR);
            defaultColor = savedInstanceState.getInt(SAVED_DEFAULT_COLOR);
            title = savedInstanceState.getInt(SAVED_TITLE);
            colorChoicesArrayId = savedInstanceState.getInt(SAVED_COLOR_CHOICES);
        }

        colorChoices = context.getResources().getIntArray(colorChoicesArrayId);

        AlertDialog.Builder builder;
        if (isDarkMode) {
            builder = new AlertDialog.Builder(context, R.style.DarkDialogTheme);
        } else {
            builder = new AlertDialog.Builder(context);
        }

        LayoutInflater layoutInflater = ((FragmentActivity)context).getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_color_picker, null);

        createLayout(view);

        builder.setTitle(title);
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            dismiss();
            listener.onColorSelected(getSelectedColor());
        });
        builder.setView(view);
        return builder.create();
    }

    private void createLayout(View view) {
        LinearLayout dialogLayout = view.findViewById(R.id.color_picker_dialog);
        LayoutInflater layoutInflater = ((FragmentActivity)context).getLayoutInflater();

        View row = layoutInflater.inflate(R.layout.color_picker_row, null);
        LinearLayout rowLayout = row.findViewById(R.id.color_picker_row);

        int i = 0;
        for (final int color : colorChoices) {
            View item = layoutInflater.inflate(R.layout.color_picker_item, null);
            ImageView colorOption = item.findViewById(R.id.color_option);
            colorOption.setColorFilter(color);

            if (color == defaultColor) {
                (item.findViewById(R.id.checkmark)).setVisibility(View.VISIBLE);
                visibleCheckmark = item.findViewById(R.id.checkmark);
                selectedColor = defaultColor;
            }

            item.setOnClickListener(v -> {
                if (visibleCheckmark != null) {
                    visibleCheckmark.setVisibility(View.INVISIBLE);
                }
                (v.findViewById(R.id.checkmark)).setVisibility(View.VISIBLE);
                visibleCheckmark = v.findViewById(R.id.checkmark);
                selectedColor = color;
                defaultColor = selectedColor;
            });

            rowLayout.addView(item);

            i++;
            if (i == 5) {
                dialogLayout.addView(row);
                row = layoutInflater.inflate(R.layout.color_picker_row, null);
                rowLayout = row.findViewById(R.id.color_picker_row);
                i = 0;
            }
        }

        while (i < 5) { // add dummy items so that layout is even
            View item = layoutInflater.inflate(R.layout.color_picker_item, null);
            ImageView colorOption = item.findViewById(R.id.color_option);
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.cardColor, typedValue, true);
            colorOption.setColorFilter(typedValue.data);
            rowLayout.addView(item);
            i++;
        }
        dialogLayout.addView(row);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_IS_DARK_MODE, isDarkMode);
        outState.putInt(SAVED_SELECTED_COLOR, selectedColor);
        outState.putInt(SAVED_DEFAULT_COLOR, defaultColor);
        outState.putInt(SAVED_TITLE, title);
        outState.putInt(SAVED_COLOR_CHOICES, colorChoicesArrayId);
    }

    public ColorPickerDialog setTitle(int title) {
        this.title = title;
        return this;
    }

    public ColorPickerDialog setDefaultColor(int color) {
        defaultColor = color;
        return this;
    }

    public ColorPickerDialog setColorChoices(int arrayId) {
        colorChoicesArrayId = arrayId;
        return this;
    }

    public ColorPickerDialog setDarkTheme(boolean darkTheme) {
        isDarkMode = darkTheme;
        return this;
    }

    public ColorPickerDialog setListener(OnClickListener listener) {
        this.listener = listener;
        return this;
    }

    public int getSelectedColor() {
        return selectedColor;
    }
}
