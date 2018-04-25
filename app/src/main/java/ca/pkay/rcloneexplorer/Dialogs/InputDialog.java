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
import android.widget.EditText;
import android.widget.TextView;

import ca.pkay.rcloneexplorer.R;

public class InputDialog extends DialogFragment {

    public interface OnPositive {
        void onPositive(String input);
    }

    private Context context;
    private EditText editText;
    private String title;
    private int titleId;
    private String message;
    private int messageId;
    private String positiveText;
    private int positiveTextId;
    private String negativeText;
    private int negativeTextId;
    private String filledText;
    private int inputType;
    private OnPositive onPositiveListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
        if (positiveText != null) {
            builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String input = editText.getText().toString();
                    onPositiveListener.onPositive(input);
                }
            });
        } else if (positiveTextId > 1) {
            builder.setPositiveButton(positiveTextId, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String input = editText.getText().toString();
                    onPositiveListener.onPositive(input);
                }
            });
        }
        if (negativeText != null) {
            builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        } else if (negativeTextId > 1) {
            builder.setNegativeButton(negativeTextId, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
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

    public InputDialog setContext(Context context) {
        this.context = context;
        return this;
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

    public InputDialog setPositiveButton(String text) {
        positiveText = text;
        return this;
    }

    public InputDialog setPositiveButton(int id) {
        positiveTextId = id;
        return this;
    }

    public InputDialog setNegativeButton(String text) {
        negativeText = text;
        return this;
    }

    public InputDialog setNegativeButton(int id) {
        negativeTextId = id;
        return this;
    }

    public InputDialog setOnPositiveListener(OnPositive l) {
        onPositiveListener = l;
        return this;
    }

}
