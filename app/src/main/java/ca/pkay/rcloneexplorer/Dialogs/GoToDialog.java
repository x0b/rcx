package ca.pkay.rcloneexplorer.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import ca.pkay.rcloneexplorer.R;

public class GoToDialog extends DialogFragment {

    public interface Callbacks {
        void onRootClicked(boolean isSetAsDefault);
        void onHomeClicked(boolean isSetAsDefault);
    }

    private Context context;
    private Callbacks listener;
    private CheckBox checkBox;
    private boolean isDefaultSet;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getParentFragment() != null) {
            listener = (Callbacks) getParentFragment();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = ((FragmentActivity)context).getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_go_to, null);
        builder.setTitle(R.string.dialog_go_to_title);
        checkBox = view.findViewById(R.id.checkbox);
        view.findViewById(R.id.linearLayout_root).setOnClickListener(v -> {
            dismiss();
            listener.onRootClicked(isDefaultSet);
        });
        view.findViewById(R.id.linearLayout_home).setOnClickListener(v -> {
            dismiss();
            listener.onHomeClicked(isDefaultSet);
        });
        view.findViewById(R.id.linearLayout_checkbox).setOnClickListener(v -> checkBox.setChecked(!checkBox.isChecked()));
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> isDefaultSet = isChecked);
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }
}
