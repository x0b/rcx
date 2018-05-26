package ca.pkay.rcloneexplorer.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
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

    private Context context;
    private boolean isDarkMode;
    private int[] colorChoices;
    private View visibleCheckmark;
    private int selectedColor;
    private int defaultColor;
    private int title;
    private OnClickListener listener;

    public ColorPickerDialog() {
        isDarkMode = false;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
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
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
                listener.onColorSelected(getSelectedColor());
            }
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

            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (visibleCheckmark != null) {
                        visibleCheckmark.setVisibility(View.INVISIBLE);
                    }
                    (v.findViewById(R.id.checkmark)).setVisibility(View.VISIBLE);
                    visibleCheckmark = v.findViewById(R.id.checkmark);
                    selectedColor = color;
                }
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

    public ColorPickerDialog withContext(Context context) {
        this.context = context;
        return this;
    }

    public ColorPickerDialog setListener(OnClickListener l) {
        listener = l;
        return this;
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
        colorChoices = context.getResources().getIntArray(arrayId);
        return this;
    }

    public ColorPickerDialog setDarkTheme(boolean darkTheme) {
        isDarkMode = darkTheme;
        return this;
    }

    public int getSelectedColor() {
        return selectedColor;
    }
}
