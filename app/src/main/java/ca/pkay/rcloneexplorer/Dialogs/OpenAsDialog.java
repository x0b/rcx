package ca.pkay.rcloneexplorer.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import ca.pkay.rcloneexplorer.R;

public class OpenAsDialog extends DialogFragment {

    public interface OnClickListener {
        void onClickText();
        void onClickAudio();
        void onClickVideo();
        void onClickImage();
    }

    private Context context;
    private View view;
    private OnClickListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = ((FragmentActivity)context).getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_open_as, null);
        setListeners();
        builder.setView(view);
        return builder.create();
    }

    private void setListeners() {
        view.findViewById(R.id.open_as_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClickText();
            }
        });

        view.findViewById(R.id.open_as_audio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClickAudio();
            }
        });

        view.findViewById(R.id.open_as_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClickVideo();
            }
        });

        view.findViewById(R.id.open_as_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClickImage();
            }
        });
    }

    public OpenAsDialog setOnClickListener(OnClickListener l) {
        listener = l;
        return this;
    }

    public OpenAsDialog setContext(Context context) {
        this.context = context;
        return  this;
    }
}
