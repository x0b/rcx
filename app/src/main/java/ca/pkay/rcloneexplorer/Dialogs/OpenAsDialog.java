package ca.pkay.rcloneexplorer.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import ca.pkay.rcloneexplorer.Items.FileItem;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.databinding.DialogOpenAsBinding;

public class OpenAsDialog extends DialogFragment {

    public interface OnClickListener {
        void onClickText(FileItem fileItem);
        void onClickAudio(FileItem fileItem);
        void onClickVideo(FileItem fileItem);
        void onClickImage(FileItem fileItem);
    }

    private final String SAVED_FILE_ITEM = "ca.pkay.rcexplorer.OpenAsDialog.FILE_ITEM";
    private final String SAVED_IS_DARK_THEME = "ca.pkay.rcexplorer.OpenAsDialog.IS_DARK_THEME";
    private Context context;
    private Boolean isDarkTheme;
    private FileItem fileItem;
    private OnClickListener listener;
    private DialogOpenAsBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            isDarkTheme = savedInstanceState.getBoolean(SAVED_IS_DARK_THEME);
            fileItem = savedInstanceState.getParcelable(SAVED_FILE_ITEM);
        }

        listener = (OnClickListener) getParentFragment();

        AlertDialog.Builder builder;

        if (isDarkTheme) {
            builder = new AlertDialog.Builder(context, R.style.DarkDialogTheme);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        binding = DialogOpenAsBinding.inflate(LayoutInflater.from(context));
        setListeners();
        builder.setView(binding.getRoot());
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_IS_DARK_THEME, isDarkTheme);
        outState.putParcelable(SAVED_FILE_ITEM, fileItem);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setListeners() {
        binding.openAsText.setOnClickListener(v -> {
            listener.onClickText(fileItem);
            dismiss();
        });

        binding.openAsAudio.setOnClickListener(v -> {
            listener.onClickAudio(fileItem);
            dismiss();
        });

        binding.openAsVideo.setOnClickListener(v -> {
            listener.onClickVideo(fileItem);
            dismiss();
        });

        binding.openAsImage.setOnClickListener(v -> {
            listener.onClickImage(fileItem);
            dismiss();
        });
    }

    public OpenAsDialog setDarkTheme(Boolean darkTheme) {
        isDarkTheme = darkTheme;
        return this;
    }

    public OpenAsDialog setFileItem(FileItem fileItem) {
        this.fileItem = fileItem;
        return this;
    }
}
