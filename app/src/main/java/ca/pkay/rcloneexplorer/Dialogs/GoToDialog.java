package ca.pkay.rcloneexplorer.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.databinding.DialogGoToBinding;

public class GoToDialog extends DialogFragment {

    public interface Callbacks {
        void onRootClicked(boolean isSetAsDefault);
        void onHomeClicked(boolean isSetAsDefault);
    }

    private final String SAVED_IS_DARK_THEME = "ca.pkay.rcexplorer.GoToDialog.IS_DARK_THEME";
    private Context context;
    private boolean isDarkTheme;
    private Callbacks listener;
    private CheckBox checkBox;
    private boolean isDefaultSet;
    private DialogGoToBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            isDarkTheme = savedInstanceState.getBoolean(SAVED_IS_DARK_THEME);
        }

        if (getParentFragment() != null) {
            listener = (Callbacks) getParentFragment();
        }

        AlertDialog.Builder builder;

        if (isDarkTheme) {
            builder = new AlertDialog.Builder(context, R.style.DarkDialogTheme);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        binding = DialogGoToBinding.inflate(LayoutInflater.from(context));
        builder.setTitle(R.string.dialog_go_to_title);
        binding.linearLayoutRoot.setOnClickListener(v -> {
            dismiss();
            listener.onRootClicked(isDefaultSet);
        });
        binding.linearLayoutHome.setOnClickListener(v -> {
            dismiss();
            listener.onHomeClicked(isDefaultSet);
        });
        binding.linearLayoutCheckbox.setOnClickListener(v -> binding.checkbox.setChecked(!binding.checkbox.isChecked()));
        binding.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> isDefaultSet = isChecked);
        builder.setView(binding.getRoot());
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_IS_DARK_THEME, isDarkTheme);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public GoToDialog isDarkTheme(boolean isDarkTheme) {
        this.isDarkTheme = isDarkTheme;
        return this;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
