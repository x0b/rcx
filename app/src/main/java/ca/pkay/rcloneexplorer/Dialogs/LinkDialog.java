package ca.pkay.rcloneexplorer.Dialogs;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import ca.pkay.rcloneexplorer.R;
import es.dmoral.toasty.Toasty;

public class LinkDialog extends DialogFragment {

    private final String SAVED_LINK_URL = "ca.pkay.rcexplorer.LinkDialog.LINK_URL";
    private Context context;
    private String linkUrl;

    public LinkDialog() {}

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            linkUrl = savedInstanceState.getString(SAVED_LINK_URL);
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = ((FragmentActivity)context).getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_link, null);

        TextView textView = view.findViewById(R.id.text_view);
        textView.setText(linkUrl);
        textView.setOnClickListener(v -> {
            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("Copied link", linkUrl);
            if (clipboardManager == null) {
                return;
            }
            clipboardManager.setPrimaryClip(clipData);
            Toasty.info(context, getString(R.string.link_copied_to_clipboard), Toast.LENGTH_SHORT, true).show();
        });

        builder.setView(view)
                .setPositiveButton(R.string.ok, null);

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_LINK_URL, linkUrl);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public LinkDialog setLinkUrl(String url) {
        this.linkUrl = url;
        return this;
    }
}
