package ca.pkay.rcloneexplorer.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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

    private final String SAVED_INPUT_HINT = "ca.pkay.rcexplorer.InputDialog.INPUT_HINT";
    private final String SAVED_INPUT_HINT_ID = "ca.pkay.rcexplorer.InputDialog.INPUT_HINT_ID";
    private final String SAVED_TAG = "ca.pkay.rcexplorer.InputDialog.TAG";
    private Context context;
    private TextInputEditText editText;
    private String title;
    private int titleId;
    private String message;
    private int messageId;
    private int positiveTextId;
    private int negativeTextId;
    private String filledText;
    private int inputType;
    private String inputHint;
    private int inputHintId;
    private String tag;
    private OnPositive onPositiveListener;

    public InputDialog() {}

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
            inputHint = savedInstanceState.getString(SAVED_INPUT_HINT);
            inputHintId = savedInstanceState.getInt(SAVED_INPUT_HINT_ID);
            tag = savedInstanceState.getString(SAVED_TAG);
        }

        if (getParentFragment() != null) {
            onPositiveListener = (OnPositive) getParentFragment();
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, R.style.RoundedCornersDialog);
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

        TextInputLayout textContainer = (TextInputLayout) view.findViewById(R.id.dialog_input_layout);
        if (inputHint != null) {
            textContainer.setHint(inputHint);
        } else if (inputHintId > 1) {
            textContainer.setHint(inputHintId);
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
        outState.putString(SAVED_INPUT_HINT, inputHint);
        outState.putInt(SAVED_INPUT_HINT_ID, inputHintId);
        outState.putString(SAVED_TAG, tag);
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

    public InputDialog setHint(String hint) {
        this.inputHint = hint;
        return this;
    }

    public InputDialog setHint(int hint) {
        this.inputHintId = hint;
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

    public InputDialog setTag(String tag) {
        this.tag = tag;
        return this;
    }
}
