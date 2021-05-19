package ca.pkay.rcloneexplorer.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.databinding.DialogSortBinding;

public class SortDialog extends DialogFragment {

    public interface OnClickListener {
        void onPositiveButtonClick(int sortById, int sortOrderId);
    }

    public final static int ALPHA_DESCENDING = 1;
    public final static int ALPHA_ASCENDING = 2;
    public final static int MOD_TIME_DESCENDING = 3;
    public final static int MOD_TIME_ASCENDING = 4;
    public final static int SIZE_DESCENDING = 5;
    public final static int SIZE_ASCENDING = 6;

    private Context context;
    private int title;
    private int positiveText;
    private int negativeText;
    private int sortOrder;
    private Boolean isDarkTheme;
    private OnClickListener listener;
    private DialogSortBinding binding;

    public SortDialog() {
        isDarkTheme = false;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getParentFragment() != null) {
            listener = (OnClickListener) getParentFragment();
        }

        if (savedInstanceState != null) {
            isDarkTheme = savedInstanceState.getBoolean("isDarkTheme");
            title = savedInstanceState.getInt("title");
            positiveText = savedInstanceState.getInt("positiveText");
            negativeText = savedInstanceState.getInt("negativeText");
            sortOrder = savedInstanceState.getInt("sortOrder");
        }

        AlertDialog.Builder builder;
        if (isDarkTheme) {
            builder = new AlertDialog.Builder(context, R.style.DarkDialogTheme);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        binding = DialogSortBinding.inflate(LayoutInflater.from(context));

        if (title > 0) {
            builder.setTitle(title);
        }
        if (positiveText > 0) {
            builder.setPositiveButton(positiveText, (dialog, which) -> {
                int sortById = binding.radioGroupSortBy.getCheckedRadioButtonId();
                int sortOrderId = binding.radioGroupSortOrder.getCheckedRadioButtonId();
                listener.onPositiveButtonClick(sortById, sortOrderId);
            });
        }
        if (negativeText > 0) {
            builder.setNeutralButton(negativeText, null);
        }

        setRadioButtons();

        builder.setView(binding.getRoot());
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;

        if (context instanceof OnClickListener) {
            listener = (OnClickListener) context;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isDarkTheme", isDarkTheme);
        outState.putInt("title", title);
        outState.putInt("positiveText", positiveText);
        outState.putInt("negativeText", negativeText);
        outState.putInt("sortOrder", sortOrder);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public SortDialog setTitle(int title) {
        this.title = title;
        return this;
    }

    public SortDialog setPositiveButton(int text) {
        positiveText = text;
        return this;
    }

    public SortDialog setNegativeButton(int text) {
        negativeText = text;
        return this;
    }

    public SortDialog setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }

    public SortDialog setDarkTheme(boolean darkTheme) {
        isDarkTheme = darkTheme;
        return this;
    }

    private void setRadioButtons() {
        switch (sortOrder) {
            case MOD_TIME_DESCENDING:
                binding.radioSortDate.setChecked(true);
                binding.radioSortDescending.setChecked(true);
                break;
            case MOD_TIME_ASCENDING:
                binding.radioSortDate.setChecked(true);
                binding.radioSortAscending.setChecked(true);
                break;
            case SIZE_DESCENDING:
                binding.radioSortSize.setChecked(true);
                binding.radioSortDescending.setChecked(true);
                break;
            case SIZE_ASCENDING:
                binding.radioSortSize.setChecked(true);
                binding.radioSortAscending.setChecked(true);
                break;
            case ALPHA_DESCENDING:
                binding.radioSortName.setChecked(true);
                binding.radioSortDescending.setChecked(true);
                break;
            case ALPHA_ASCENDING:
            default:
                binding.radioSortName.setChecked(true);
                binding.radioSortAscending.setChecked(true);
        }
    }
}
