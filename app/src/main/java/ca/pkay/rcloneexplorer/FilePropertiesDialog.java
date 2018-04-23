package ca.pkay.rcloneexplorer;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import ca.pkay.rcloneexplorer.Items.FileItem;

public class FilePropertiesDialog extends DialogFragment {

    private FileItem fileItem;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.file_properties_popup, null);

        ((TextView)view.findViewById(R.id.filename)).setText(fileItem.getName());
        ((TextView)view.findViewById(R.id.file_modtime)).setText(fileItem.getHumanReadableModTime());
        if (fileItem.isDir()) {
            view.findViewById(R.id.file_size).setVisibility(View.GONE);
            view.findViewById(R.id.file_size_label).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.file_size).setVisibility(View.VISIBLE);
            view.findViewById(R.id.file_size_label).setVisibility(View.VISIBLE);
            ((TextView)view.findViewById(R.id.file_size)).setText(fileItem.getHumanReadableSize());
        }

        builder.setView(view)
                .setPositiveButton(R.string.ok, null);
        return builder.create();
    }

    public void setFile(FileItem fileItem) {
        this.fileItem = fileItem;
    }
}
