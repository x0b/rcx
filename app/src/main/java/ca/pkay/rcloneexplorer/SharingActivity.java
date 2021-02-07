package ca.pkay.rcloneexplorer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import ca.pkay.rcloneexplorer.Dialogs.Dialogs;
import ca.pkay.rcloneexplorer.Dialogs.LoadingDialog;
import ca.pkay.rcloneexplorer.Fragments.ShareFragment;
import ca.pkay.rcloneexplorer.Fragments.ShareRemotesFragment;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.Services.UploadService;
import ca.pkay.rcloneexplorer.util.FLog;
import es.dmoral.toasty.Toasty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static ca.pkay.rcloneexplorer.ActivityHelper.tryStartService;

public class SharingActivity extends AppCompatActivity implements   ShareRemotesFragment.OnRemoteClickListener,
                                                                    ShareFragment.OnShareDestinationSelected {

    private static final String TAG = "SharingActivity";
    private boolean isDarkTheme;
    private Fragment fragment;
    private ArrayList<String> uploadList;
    private boolean isDataReady;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(RuntimeConfiguration.attach(this, newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityHelper.applyTheme(this);
        isDarkTheme = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.pref_key_dark_theme), false);
        setContentView(R.layout.activity_sharing);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Rclone rclone = new Rclone(this);
        uploadList = new ArrayList<>();

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            copyFile(intent);
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            copyFiles(intent);
        } else {
            finish();
            return;
        }

        if (rclone.isConfigEncrypted() || !rclone.isConfigFileCreated() || rclone.getRemotes().isEmpty()) {
            AlertDialog.Builder builder;
            if (isDarkTheme) {
                builder = new AlertDialog.Builder(this, R.style.DarkDialogTheme);
            } else {
                builder = new AlertDialog.Builder(this);
            }
            builder
                    .setTitle(R.string.app_not_configured)
                    .setMessage(R.string.open_app_to_configure)
                    .setPositiveButton(R.string.ok, (dialog, which) -> finish())
                    .show();
        } else {
            startRemotesFragment();
        }
    }

    @Override
    public void onBackPressed() {
        if (fragment != null && fragment instanceof ShareFragment) {
            if (((ShareFragment)fragment).onBackButtonPressed()) {
                return;
            }
        }
        super.onBackPressed();
    }

    private void startRemotesFragment() {
        fragment = ShareRemotesFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();

        for (int i = 0; i < fragmentManager.getBackStackEntryCount(); i++) {
            fragmentManager.popBackStack();
        }

        fragmentManager.beginTransaction().replace(R.id.flFragment, fragment).commit();
    }

    @Override
    public void onRemoteClick(RemoteItem remote) {
        startRemote(remote);
    }

    private void startRemote(RemoteItem remoteItem) {
        fragment = ShareFragment.newInstance(remoteItem);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.flFragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onShareDestinationSelected(RemoteItem remote, String path) {
        new UploadTask(this, remote, path).execute();
    }

    private void copyFile(Intent intent) {
        Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (uri == null) {
            finish();
        }
        isDataReady = false;
        new CopyFile(this, uri).execute();
    }

    private void copyFiles(Intent intent) {
        ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (uris == null) {
            finish();
        }
        isDataReady = false;
        new CopyFile(this, uris).execute();
    }


    @SuppressLint("StaticFieldLeak")
    private class UploadTask extends AsyncTask<Void, Void, Void> {

        RemoteItem remote;
        String path;
        Context context;
        LoadingDialog loadingDialog;

        UploadTask(Context context, RemoteItem remote, String path) {
            this.context = context;
            this.remote = remote;
            this.path = path;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog = new LoadingDialog()
                    .setTitle(R.string.loading)
                    .setDarkTheme(isDarkTheme)
                    .setNegativeButton(R.string.cancel)
                    .setOnNegativeListener(() -> cancel(true));
            loadingDialog.show(getSupportFragmentManager(), "loading dialog");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while (!isDataReady) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    FLog.e(TAG, "UploadTask/doInBackground: error waiting for data", e);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Dialogs.dismissSilently(loadingDialog);

            for (String uploadFile : uploadList) {
                Intent intent = new Intent(context, UploadService.class);
                intent.putExtra(UploadService.LOCAL_PATH_ARG, uploadFile);
                intent.putExtra(UploadService.UPLOAD_PATH_ARG, path);
                intent.putExtra(UploadService.REMOTE_ARG, remote);
                tryStartService(context, intent);
            }
            finish();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class CopyFile extends AsyncTask<Void, Void, Boolean> {

        private static final String TAG = "SharingActvty/CopyFile";
        private Context context;
        private ArrayList<Uri> uris;

        CopyFile(Context context, Uri uri) {
            this.context = context;
            uris = new ArrayList<>();
            uris.add(uri);
        }

        CopyFile(Context context, ArrayList<Uri> uris) {
            this.context = context;
            this.uris = uris;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean success = true;
            for (Uri uri : uris) {
                String fileName;
                if (null == uri || null == uri.getScheme() || null == uri.getPath()) {
                    FLog.w(TAG, "Can't copy invalid uri %s", uri);
                    success = false;
                    continue;
                }

                try {
                    fileName = resolveName(uri);
                } catch (SecurityException e) {
                    success = false;
                    continue;
                }
                // todo: encrypt external cache
                File cacheDir = getExternalCacheDir();
                File outFile = new File(cacheDir, fileName);
                try (InputStream in = getContentResolver().openInputStream(uri);
                     FileOutputStream out = new FileOutputStream(outFile)) {
                    if (null == in) {
                        success = false;
                        continue;
                    }
                    uploadList.add(outFile.getAbsolutePath());
                    byte[] buf = new byte[4096];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.flush();
                } catch (IOException e) {
                    FLog.e(TAG, "Copy error: ", e);
                    success = false;
                }
            }
            return success;
        }

        @NonNull
        private String resolveName(@NonNull Uri uri) {
            String[] projection = {OpenableColumns.DISPLAY_NAME};
            try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
                if (null != cursor && cursor.moveToFirst()) {
                    String name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    if (null != name) {
                        return name;
                    }
                }
            }
            List<String> segments = uri.getPathSegments();
            if (segments.size() >= 1) {
                return segments.get(segments.size() - 1);
            }
            return "unnamed";
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (!success) {
                Toasty.error(context, getString(R.string.error_retrieving_files), Toast.LENGTH_LONG, true).show();
                finish();
            }
            isDataReady = true;
        }
    }
}
