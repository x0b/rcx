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
import android.widget.TextView;

import ca.pkay.rcloneexplorer.R;

public class LinkDialog extends DialogFragment {

    private Context context;
    private String linkUrl;
    private boolean isDarkTheme;
    private Callback listener;

    public interface Callback {
        void onLinkClick(String url);
    }

    public LinkDialog() {}

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
        View view = inflater.inflate(R.layout.dialog_link, null);

        TextView textView = view.findViewById(R.id.text_view);
        textView.setText(linkUrl);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onLinkClick(linkUrl);
            }
        });

        builder.setView(view)
                .setPositiveButton(R.string.ok, null);

        return builder.create();
    }

    public LinkDialog withContext(Context context) {
        this.context = context;
        return this;
    }

    public LinkDialog isDarkTheme(boolean isDarkTheme) {
        this.isDarkTheme = isDarkTheme;
        return this;
    }

    public LinkDialog setLinkUrl(String url) {
        this.linkUrl = url;
        return this;
    }

    public LinkDialog setListener(Callback listener) {
        this.listener = listener;
        return this;
    }
}
