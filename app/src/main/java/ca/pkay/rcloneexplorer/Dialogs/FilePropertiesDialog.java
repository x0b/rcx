package ca.pkay.rcloneexplorer.Dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import ca.pkay.rcloneexplorer.Items.FileItem;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import es.dmoral.toasty.Toasty;

public class FilePropertiesDialog extends DialogFragment {

    private FileItem fileItem;
    private String remote;
    private View view;
    private Rclone rclone;
    private AsyncTask[] asyncTasks;
    private String md5String;
    private String sha1String;
    private Boolean showHash;
    private Boolean isDarkTheme;
    private Context context;

    public FilePropertiesDialog() {
        showHash = true;
        isDarkTheme = false;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        asyncTasks = new AsyncTask[2];
        AlertDialog.Builder builder;
        if (isDarkTheme) {
            builder = new AlertDialog.Builder(context, R.style.DarkDialogTheme);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        LayoutInflater inflater = ((FragmentActivity)context).getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_file_properties, null);

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

        if (showHash && !fileItem.isDir()) {
            view.findViewById(R.id.file_md5_container).setVisibility(View.VISIBLE);
            view.findViewById(R.id.file_sha1_container).setVisibility(View.VISIBLE);
            view.findViewById(R.id.hash_separator).setVisibility(View.VISIBLE);

            view.findViewById(R.id.file_md5_container).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    calculateMD5();
                }
            });
            view.findViewById(R.id.file_sha1_container).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    calculateSHA1();
                }
            });
        } else {
            view.findViewById(R.id.file_md5_container).setVisibility(View.GONE);
            view.findViewById(R.id.file_sha1_container).setVisibility(View.GONE);
            view.findViewById(R.id.hash_separator).setVisibility(View.GONE);
        }

        builder.setView(view)
                .setPositiveButton(R.string.ok, null);
        return builder.create();
    }

    public FilePropertiesDialog withContext(Context context) {
        this.context = context;
        return this;
    }

    public FilePropertiesDialog setFile(FileItem fileItem) {
        this.fileItem = fileItem;
        return this;
    }

    public FilePropertiesDialog setRemote(String remote) {
        this.remote = remote;
        return this;
    }

    public FilePropertiesDialog setRclone(Rclone rclone) {
        this.rclone = rclone;
        return this;
    }

    public FilePropertiesDialog withHashCalculations(Boolean showHash) {
        this.showHash = showHash;
        return this;
    }

    public FilePropertiesDialog setDarkTheme(Boolean darkTheme) {
        isDarkTheme = darkTheme;
        return this;
    }

    private void calculateMD5() {
        // md5 already calculated
        if (md5String != null && !md5String.isEmpty()) {
            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("Copied hash", md5String);
            ((TextView)view.findViewById(R.id.file_md5)).setTextIsSelectable(true);
            if (clipboardManager == null) {
                return;
            }
            clipboardManager.setPrimaryClip(clipData);
            Toasty.info(context, getString(R.string.hash_copied_confirmation), Toast.LENGTH_SHORT, true).show();
        } else { // calculate md5
            if (asyncTasks[0] != null) {
                asyncTasks[0].cancel(true);
            }
            asyncTasks[0] = new CalculateMD5().execute();
        }
    }

    private void calculateSHA1() {
        // sha1 already calculated
        if (sha1String != null && !sha1String.isEmpty()) {
            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("Copied hash", sha1String);
            ((TextView)view.findViewById(R.id.file_sha1)).setTextIsSelectable(true);
            if (clipboardManager == null) {
                return;
            }
            clipboardManager.setPrimaryClip(clipData);
            Toasty.info(context, getString(R.string.hash_copied_confirmation), Toast.LENGTH_SHORT, true).show();
        } else { // calculate sha1
            if (asyncTasks[1] != null) {
                asyncTasks[1].cancel(true);
            }
            asyncTasks[1] = new CalculateSHA1().execute();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        for (AsyncTask asyncTask : asyncTasks) {
            if (asyncTask != null) {
                asyncTask.cancel(true);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class CalculateMD5 extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ((TextView)view.findViewById(R.id.file_md5)).setText(R.string.calculating);
        }

        @Override
        protected String doInBackground(Void... voids) {
            return rclone.calculateMD5(remote, fileItem);
        }

        @Override
        protected void onPostExecute(String md5) {
            super.onPostExecute(md5);
            ((TextView)view.findViewById(R.id.file_md5)).setText(md5);

            if (!md5.equals(getString(R.string.hash_error)) && !md5.equals(getString(R.string.hash_unsupported))) {
                md5String = md5;
            } else {
                md5String = null;
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class CalculateSHA1 extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ((TextView)view.findViewById(R.id.file_sha1)).setText(R.string.calculating);
        }

        @Override
        protected String doInBackground(Void... voids) {
            return rclone.calculateSHA1(remote, fileItem);
        }

        @Override
        protected void onPostExecute(String sha1) {
            super.onPostExecute(sha1);
            ((TextView)view.findViewById(R.id.file_sha1)).setText(sha1);

            if (!sha1.equals(getString(R.string.hash_error)) && !sha1.equals(getString(R.string.hash_unsupported))) {
                sha1String = sha1;
            } else {
                sha1String = null;
            }
        }
    }
}
