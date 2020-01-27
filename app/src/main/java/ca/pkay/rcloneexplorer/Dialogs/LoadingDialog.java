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

import ca.pkay.rcloneexplorer.R;

public class LoadingDialog extends DialogFragment {

    public interface OnNegative {
        void onNegative();
    }

    private final String SAVED_CANCELABLE = "ca.pkay.rcexplorer.LoadingDialog.CANCELABLE";
    private final String SAVED_IS_DARK_THEME = "ca.pkay.rcexplorer.LoadingDialog.IS_DARK_THEME";
    private final String SAVED_TITLE = "ca.pkay.rcexplorer.LoadingDialog.TITLE";
    private final String SAVED_TITLE_ID = "ca.pkay.rcexplorer.LoadingDialog.TITLE_ID";
    private final String SAVED_NEGATIVE_TEXT = "ca.pkay.rcexplorer.NEGATIVE_TEXT";
    private final String SAVED_NEGATIVE_TEXT_ID = "ca.pkay.rcexplorer.NEGATIVE_TEXT_ID";
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
        if (savedInstanceState != null) {
            cancelable = savedInstanceState.getBoolean(SAVED_CANCELABLE);
            isDarkTheme = savedInstanceState.getBoolean(SAVED_IS_DARK_THEME);
            title = savedInstanceState.getString(SAVED_TITLE);
            titleId = savedInstanceState.getInt(SAVED_TITLE_ID);
            negativeText = savedInstanceState.getString(SAVED_NEGATIVE_TEXT);
            negativeTextId = savedInstanceState.getInt(SAVED_NEGATIVE_TEXT_ID);
        }

        AlertDialog.Builder builder;

        if (isDarkTheme) {
            builder = new AlertDialog.Builder(context, R.style.DarkDialogTheme);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        LayoutInflater inflater = ((FragmentActivity)context).getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_loading_indicator, null);
        builder.setCancelable(cancelable);
        setCancelable(cancelable);
        if (title != null) {
            builder.setTitle(title);
        } else if (titleId > 0) {
            builder.setTitle(titleId);
        }
        if (negativeText != null) {
            builder.setNegativeButton(negativeText, (dialog, which) -> {
                if(null != onNegativeListener) {
                    onNegativeListener.onNegative();
                }
            });
        } else if (negativeTextId > 0) {
            builder.setNeutralButton(negativeTextId, (dialog, which) -> {
                if(null != onNegativeListener) {
                    onNegativeListener.onNegative();
                }
            });
        }
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_CANCELABLE, cancelable);
        outState.putBoolean(SAVED_IS_DARK_THEME, isDarkTheme);
        outState.putString(SAVED_TITLE, title);
        outState.putInt(SAVED_TITLE_ID, titleId);
        outState.putString(SAVED_NEGATIVE_TEXT, negativeText);
        outState.putInt(SAVED_NEGATIVE_TEXT_ID, negativeTextId);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
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
