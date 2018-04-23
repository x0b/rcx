package ca.pkay.rcloneexplorer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
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
    private String remote;
    private View view;
    private Rclone rclone;
    private AsyncTask[] asyncTasks;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        asyncTasks = new AsyncTask[2];
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.file_properties_popup, null);

        ((TextView)view.findViewById(R.id.filename)).setText(fileItem.getName());
        ((TextView)view.findViewById(R.id.file_modtime)).setText(fileItem.getHumanReadableModTime());
        if (fileItem.isDir()) {
            view.findViewById(R.id.file_size).setVisibility(View.GONE);
            view.findViewById(R.id.file_size_label).setVisibility(View.GONE);

            view.findViewById(R.id.file_md5_container).setVisibility(View.GONE);
            view.findViewById(R.id.file_sha1_container).setVisibility(View.GONE);
            view.findViewById(R.id.hash_separator).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.file_size).setVisibility(View.VISIBLE);
            view.findViewById(R.id.file_size_label).setVisibility(View.VISIBLE);
            ((TextView)view.findViewById(R.id.file_size)).setText(fileItem.getHumanReadableSize());

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
        }

        builder.setView(view)
                .setPositiveButton(R.string.ok, null);
        return builder.create();
    }

    public void setFile(FileItem fileItem) {
        this.fileItem = fileItem;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }

    public void setRclone(Rclone rclone) {
        this.rclone = rclone;
    }

    private void calculateMD5() {
        if (asyncTasks[0] != null) {
            asyncTasks[0].cancel(true);
        }
        asyncTasks[0] = new CalculateMD5().execute();
    }

    private void calculateSHA1() {
        if (asyncTasks[1] != null) {
            asyncTasks[1].cancel(true);
        }
        asyncTasks[1] = new CalculateSHA1().execute();
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

            if (md5.equals(getString(R.string.hash_error)) || md5.equals(getString(R.string.hash_unsupported))) {
                ((TextView)view.findViewById(R.id.file_md5)).setTextIsSelectable(false);
            } else {
                ((TextView)view.findViewById(R.id.file_md5)).setTextIsSelectable(true);
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

            if (sha1.equals(getString(R.string.hash_error)) || sha1.equals(getString(R.string.hash_unsupported))) {
                ((TextView)view.findViewById(R.id.file_sha1)).setTextIsSelectable(false);
            } else {
                ((TextView)view.findViewById(R.id.file_sha1)).setTextIsSelectable(true);
            }
        }
    }
}
