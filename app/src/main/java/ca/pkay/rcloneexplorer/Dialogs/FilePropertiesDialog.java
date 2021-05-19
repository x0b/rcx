package ca.pkay.rcloneexplorer.Dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import ca.pkay.rcloneexplorer.Items.FileItem;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import ca.pkay.rcloneexplorer.databinding.DialogFilePropertiesBinding;
import es.dmoral.toasty.Toasty;

public class FilePropertiesDialog extends DialogFragment {

    private final String SAVED_FILEITEM = "ca.pkay.rcexplorer.FilePropertiesDialog.FILE_ITEM";
    private final String SAVED_REMOTE = "ca.pkay.rcexplorer.FilePropertiesDialog.REMOTE";
    private final String SAVED_MD5 = "ca.pkay.rcexplorer.FilePropertiesDialog.MD5";
    private final String SAVED_SHA1 = "ca.pkay.rcexplorer.FilePropertiesDialog.SHA1";
    private final String SAVED_SHOW_HASH = "ca.pkay.rcexplorer.FilePropertiesDialog.SHOW_HASH";
    private final String SAVED_IS_DARK_THEME = "ca.pkay.rcexplorer.FilePropertiesDialog.IS_DARK_THEME";
    private FileItem fileItem;
    private RemoteItem remote;
    private Rclone rclone;
    private AsyncTask[] asyncTasks;
    private String md5String;
    private String sha1String;
    private Boolean showHash;
    private Boolean isDarkTheme;
    private Context context;
    DialogFilePropertiesBinding binding;

    public FilePropertiesDialog() {
        showHash = true;
        isDarkTheme = false;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            fileItem = savedInstanceState.getParcelable(SAVED_FILEITEM);
            remote = savedInstanceState.getParcelable(SAVED_REMOTE);
            md5String = savedInstanceState.getString(SAVED_MD5);
            sha1String = savedInstanceState.getString(SAVED_SHA1);
            showHash = savedInstanceState.getBoolean(SAVED_SHOW_HASH);
            isDarkTheme = savedInstanceState.getBoolean(SAVED_IS_DARK_THEME);
        }

        rclone = new Rclone(context);
        asyncTasks = new AsyncTask[2];
        AlertDialog.Builder builder;
        if (isDarkTheme) {
            builder = new AlertDialog.Builder(context, R.style.DarkDialogTheme);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        binding = DialogFilePropertiesBinding.inflate(LayoutInflater.from(context));

        binding.filename.setText(fileItem.getName());

        RemoteItem itemRemote = fileItem.getRemote();
        if (!itemRemote.isDirectoryModifiedTimeSupported() && fileItem.isDir()) {
            binding.fileModtimeLabel.setVisibility(View.GONE);
            binding.fileModtime.setVisibility(View.GONE);
        } else {
            binding.fileModtimeLabel.setVisibility(View.VISIBLE);
            binding.fileModtime.setVisibility(View.VISIBLE);
            binding.fileModtime.setText(fileItem.getFormattedModTime());
        }

        if (fileItem.isDir()) {
            binding.fileSize.setVisibility(View.GONE);
            binding.fileSizeLabel.setVisibility(View.GONE);
        } else {
            binding.fileSize.setVisibility(View.VISIBLE);
            binding.fileSize.setText(fileItem.getHumanReadableSize());
            binding.fileSizeLabel.setVisibility(View.VISIBLE);
        }

        if (showHash && !fileItem.isDir()) {
            binding.fileMd5Container.setVisibility(View.VISIBLE);
            binding.fileSha1Container.setVisibility(View.VISIBLE);
            binding.hashSeparator.setVisibility(View.VISIBLE);

            binding.fileMd5Container.setOnClickListener(v -> calculateMD5());
            binding.fileSha1Container.setOnClickListener(v -> calculateSHA1());

            if (md5String != null) {
                binding.fileMd5.setText(md5String);
            }
            if (sha1String != null) {
                binding.fileSha1.setText(sha1String);
            }
        } else {
            binding.fileMd5Container.setVisibility(View.GONE);
            binding.fileSha1Container.setVisibility(View.GONE);
            binding.hashSeparator.setVisibility(View.GONE);
        }

        builder.setView(binding.getRoot())
                .setPositiveButton(R.string.ok, null);
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVED_FILEITEM, fileItem);
        outState.putParcelable(SAVED_REMOTE, remote);
        outState.putBoolean(SAVED_SHOW_HASH, showHash);
        outState.putBoolean(SAVED_IS_DARK_THEME, isDarkTheme);
        if (md5String != null) {
            outState.putString(SAVED_MD5, md5String);
        }
        if (sha1String != null) {
            outState.putString(SAVED_SHA1, sha1String);
        }

        for (AsyncTask asyncTask : asyncTasks) {
            if (asyncTask != null) {
                asyncTask.cancel(true);
            }
        }
    }

    public FilePropertiesDialog setFile(FileItem fileItem) {
        this.fileItem = fileItem;
        return this;
    }

    public FilePropertiesDialog setRemote(RemoteItem remote) {
        this.remote = remote;
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
            binding.fileMd5.setTextIsSelectable(true);
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
            binding.fileSha1.setTextIsSelectable(true);
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
            binding.fileMd5.setText(R.string.calculating);
        }

        @Override
        protected String doInBackground(Void... voids) {
            return rclone.calculateMD5(remote, fileItem);
        }

        @Override
        protected void onPostExecute(String md5) {
            super.onPostExecute(md5);
            binding.fileMd5.setText(md5);

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
            binding.fileSha1.setText(R.string.calculating);
        }

        @Override
        protected String doInBackground(Void... voids) {
            return rclone.calculateSHA1(remote, fileItem);
        }

        @Override
        protected void onPostExecute(String sha1) {
            super.onPostExecute(sha1);
            binding.fileSha1.setText(sha1);

            if (!sha1.equals(getString(R.string.hash_error)) && !sha1.equals(getString(R.string.hash_unsupported))) {
                sha1String = sha1;
            } else {
                sha1String = null;
            }
        }
    }
}
