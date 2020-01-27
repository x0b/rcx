package ca.pkay.rcloneexplorer.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import ca.pkay.rcloneexplorer.Items.FileItem;
import ca.pkay.rcloneexplorer.R;

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
    private View view;
    private Boolean isDarkTheme;
    private FileItem fileItem;
    private OnClickListener listener;

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
        LayoutInflater inflater = ((FragmentActivity)context).getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_open_as, null);
        setListeners();
        builder.setView(view);
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

    private void setListeners() {
        view.findViewById(R.id.open_as_text).setOnClickListener(v -> {
            listener.onClickText(fileItem);
            dismiss();
        });

        view.findViewById(R.id.open_as_audio).setOnClickListener(v -> {
            listener.onClickAudio(fileItem);
            dismiss();
        });

        view.findViewById(R.id.open_as_video).setOnClickListener(v -> {
            listener.onClickVideo(fileItem);
            dismiss();
        });

        view.findViewById(R.id.open_as_image).setOnClickListener(v -> {
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
