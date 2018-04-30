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

import ca.pkay.rcloneexplorer.R;

public class LoadingDialog extends DialogFragment {

    public interface OnNegative {
        void onNegative();
    }


    private Context context;
    private OnNegative onNegativeListener;
    private Boolean cancelable;
    private Boolean isDarkTheme;
    private String title;
    private int titleId;
    private String negativeText;
    private int negativeTextId;

    public LoadingDialog() {
        cancelable = false;
        isDarkTheme = false;
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
        View view = inflater.inflate(R.layout.dialog_loading_indicator, null);
        builder.setCancelable(cancelable);
        if (title != null) {
            builder.setTitle(title);
        } else if (titleId > 0) {
            builder.setTitle(titleId);
        }
        if (negativeText != null) {
            builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onNegativeListener.onNegative();
                }
            });
        } else if (negativeTextId > 0) {
            builder.setNeutralButton(negativeTextId, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    onNegativeListener.onNegative();
                }
            });
        }
        builder.setView(view);
        return builder.create();
        }

    public LoadingDialog setContext(Context context) {
        this.context = context;
        return this;
    }

    public LoadingDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public LoadingDialog setTitle(int id) {
        titleId = id;
        return this;
    }


    public LoadingDialog setNegativeButton(String text) {
        negativeText = text;
        return this;
    }

    public LoadingDialog setNegativeButton(int text) {
        negativeTextId = text;
        return this;
    }


    public LoadingDialog setCanCancel(Boolean cancelable) {
        this.cancelable = cancelable;
        return this;
    }

    public LoadingDialog setDarkTheme(Boolean darkTheme) {
        isDarkTheme = darkTheme;
        return this;
    }


    public LoadingDialog setOnNegativeListener(OnNegative l) {
        onNegativeListener = l;
        return this;
    }
}
