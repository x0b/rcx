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
import android.widget.EditText;
import android.widget.TextView;

import ca.pkay.rcloneexplorer.R;

public class InputDialog extends DialogFragment {

    public interface OnPositive {
        void onPositive(String tag, String input);
    }

    private final String SAVED_TITLE = "ca.pkay.rcexplorer.InputDialog.TITLE";
    private final String SAVED_TITLE_ID = "ca.pkay.rcexplorer.InputDialog.TITLE_ID";
    private final String SAVED_MESSAGE = "ca.pkay.rcexplorer.InputDialog.MESSAGE";
    private final String SAVED_MESSAGE_ID = "ca.pkay.rcexplorer.InputDialog.MESSAGE_ID";
    private final String SAVED_POSITIVE_TEXT_ID = "ca.pkay.rcexplorer.InputDialog.POSITIVE_TEXT_ID";
    private final String SAVED_NEGATIVE_TEXT_ID = "ca.pkay.rcexplorer.InputDialog.NEGATIVE_TEXT_ID";
    private final String SAVED_FILLED_TEXT = "ca.pkay.rcexplorer.InputDialog.FILLED_TEXT";
    private final String SAVED_INPUT_TYPE = "ca.pkay.rcexplorer.InputDialog.INPUT_TYPE";
    private final String SAVED_TAG = "ca.pkay.rcexplorer.InputDialog.TAG";
    private final String SAVED_IS_DARK_THEME = "ca.pkay.rcexplorer.InputDialog.IS_DARK_THEME";
    private Context context;
    private EditText editText;
    private String title;
    private int titleId;
    private String message;
    private int messageId;
    private int positiveTextId;
    private int negativeTextId;
    private String filledText;
    private int inputType;
    private Boolean isDarkTheme;
    private String tag;
    private OnPositive onPositiveListener;

    public InputDialog() {
        isDarkTheme = false;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            title = savedInstanceState.getString(SAVED_TITLE);
            titleId = savedInstanceState.getInt(SAVED_TITLE_ID);
            message = savedInstanceState.getString(SAVED_MESSAGE);
            messageId = savedInstanceState.getInt(SAVED_MESSAGE_ID);
            positiveTextId = savedInstanceState.getInt(SAVED_POSITIVE_TEXT_ID);
            negativeTextId = savedInstanceState.getInt(SAVED_NEGATIVE_TEXT_ID);
            filledText = savedInstanceState.getString(SAVED_FILLED_TEXT);
            inputType = savedInstanceState.getInt(SAVED_INPUT_TYPE);
            tag = savedInstanceState.getString(SAVED_TAG);
            isDarkTheme = savedInstanceState.getBoolean(SAVED_IS_DARK_THEME);
        }

        if (getParentFragment() != null) {
            onPositiveListener = (OnPositive) getParentFragment();
        }

        AlertDialog.Builder builder;

        if (isDarkTheme) {
            builder = new AlertDialog.Builder(context, R.style.DarkDialogTheme);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        LayoutInflater inflater = ((FragmentActivity)context).getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_input, null);
        editText = view.findViewById(R.id.dialog_input);
        builder.setView(view);
        if (title != null) {
            builder.setTitle(title);
        } else if (titleId > 1) {
            builder.setTitle(titleId);
        }
        if (message != null) {
            builder.setMessage(message);
        } else if (messageId > 1) {
            builder.setMessage(messageId);
        }
        if (positiveTextId > 1) {
            builder.setPositiveButton(positiveTextId, (dialog, which) -> {
                String input = editText.getText().toString();
                onPositiveListener.onPositive(tag, input);
            });
        }
        if (negativeTextId > 1) {
            builder.setNegativeButton(negativeTextId, (dialog, which) -> {

            });
        }
        if (filledText != null) {
            editText.setText(filledText, TextView.BufferType.EDITABLE);
            editText.setSelection(editText.getText().length());
        }
        if (inputType > 0) {
            editText.setInputType(inputType);
        }

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_TITLE, title);
        outState.putInt(SAVED_TITLE_ID, titleId);
        outState.putString(SAVED_MESSAGE, message);
        outState.putInt(SAVED_MESSAGE_ID, messageId);
        outState.putInt(SAVED_POSITIVE_TEXT_ID, positiveTextId);
        outState.putInt(SAVED_NEGATIVE_TEXT_ID, negativeTextId);
        outState.putString(SAVED_FILLED_TEXT, filledText);
        outState.putInt(SAVED_INPUT_TYPE, inputType);
        outState.putString(SAVED_TAG, tag);
        outState.putBoolean(SAVED_IS_DARK_THEME, isDarkTheme);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;

        if (context instanceof OnPositive) {
            onPositiveListener = (OnPositive) context;
        }
    }

    public InputDialog setFilledText(String text) {
        filledText = text;
        return this;
    }

    public InputDialog setInputType(int type) {
        inputType = type;
        return this;
    }

    public InputDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public InputDialog setTitle(int id) {
        this.titleId = id;
        return this;
    }

    public InputDialog setMessage(String message) {
        this.message = message;
        return this;
    }

    public InputDialog setMessage(int id) {
        this.messageId = id;
        return this;
    }

    public InputDialog setPositiveButton(int id) {
        positiveTextId = id;
        return this;
    }


    public InputDialog setNegativeButton(int id) {
        negativeTextId = id;
        return this;
    }

    public InputDialog setDarkTheme(Boolean darkTheme) {
        isDarkTheme = darkTheme;
        return this;
    }

    public InputDialog setTag(String tag) {
        this.tag = tag;
        return this;
    }
}
