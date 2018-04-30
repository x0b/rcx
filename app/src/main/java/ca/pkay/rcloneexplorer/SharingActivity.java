package ca.pkay.rcloneexplorer;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ca.pkay.rcloneexplorer.Dialogs.LoadingDialog;
import ca.pkay.rcloneexplorer.Fragments.RemotesFragment;
import ca.pkay.rcloneexplorer.Fragments.ShareFragment;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.Services.UploadService;

public class SharingActivity extends AppCompatActivity implements   RemotesFragment.OnRemoteClickListener,
                                                                    ShareFragment.OnShareDestincationSelected {

    private boolean isDarkTheme;
    private Rclone rclone;
    private Fragment fragment;
    private String uploadFile;
    private boolean isDataReady;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();
        setContentView(R.layout.activity_sharing);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rclone = new Rclone(this);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        copyFile(intent);

        if (!rclone.isRcloneBinaryCreated() || rclone.isConfigEncrypted() || !rclone.isConfigFileCreated()) {
            AlertDialog.Builder builder;
            if (isDarkTheme) {
                builder = new AlertDialog.Builder(this, R.style.DarkDialogTheme);
            } else {
                builder = new AlertDialog.Builder(this);
            }
            builder
                    .setTitle(R.string.app_not_configured)
                    .setMessage(R.string.open_app_to_configure)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        } else {
            startRemotesFragment();
        }
    }

    private void applyTheme() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int customPrimaryColor = sharedPreferences.getInt(getString(R.string.pref_key_color_primary), -1);
        int customAccentColor = sharedPreferences.getInt(getString(R.string.pref_key_color_accent), -1);
        isDarkTheme = sharedPreferences.getBoolean(getString(R.string.pref_key_dark_theme), false);
        getTheme().applyStyle(CustomColorHelper.getPrimaryColorTheme(this, customPrimaryColor), true);
        getTheme().applyStyle(CustomColorHelper.getAccentColorTheme(this, customAccentColor), true);
        if (isDarkTheme) {
            getTheme().applyStyle(R.style.DarkTheme, true);
        } else {
            getTheme().applyStyle(R.style.LightTheme, true);
        }

        // set recents app color to the primary color
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round);
        ActivityManager.TaskDescription taskDesc = new ActivityManager.TaskDescription(getString(R.string.app_name), bm, customPrimaryColor);
        setTaskDescription(taskDesc);
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
        fragment = RemotesFragment.newInstance();
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
        fragment = ShareFragment.newInstance(remoteItem.getName(), remoteItem.getType());
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.flFragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onShareDestinationSelected(String remote, String path) {
        new UploadTask(this, remote, path).execute();
    }

    private void copyFile(Intent intent) {
        Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (uri == null) {
            finish();
        }
        isDataReady = false;
        new CopyFile(uri).execute();
    }


    @SuppressLint("StaticFieldLeak")
    private class UploadTask extends AsyncTask<Void, Void, Void> {

        String remote;
        String path;
        Context context;
        LoadingDialog loadingDialog;

        UploadTask(Context context, String remote, String path) {
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
                    .setContext(context)
                    .setNegativeButton(R.string.cancel)
                    .setOnNegativeListener(new LoadingDialog.OnNegative() {
                        @Override
                        public void onNegative() {
                            cancel(true);
                        }
                    });
            loadingDialog.show(getSupportFragmentManager(), "loading dialog");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while (!isDataReady) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            loadingDialog.dismiss();
            ArrayList<String> uploadList = new ArrayList<>();
            uploadList.add(uploadFile);
            Intent intent = new Intent(context, UploadService.class);
            intent.putStringArrayListExtra(UploadService.LOCAL_PATH_ARG, uploadList);
            intent.putExtra(UploadService.UPLOAD_PATH_ARG, path);
            intent.putExtra(UploadService.REMOTE_ARG, remote);
            startService(intent);
            finish();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class CopyFile extends AsyncTask<Void, Void, Void> {

        private Uri uri;

        CopyFile(Uri uri) {
            this.uri = uri;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Cursor returnCursor = getContentResolver().query(uri, null, null, null, null);
            if (returnCursor == null) {
                return null;
            }
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            String fileName = returnCursor.getString(nameIndex);
            returnCursor.close();

            File cacheDir = getExternalCacheDir();
            InputStream inputStream;
            try {
                inputStream = getContentResolver().openInputStream(uri);
                if (inputStream == null) {
                    return null;
                }
                File outFile = new File(cacheDir, fileName);
                uploadFile = outFile.getAbsolutePath();
                FileOutputStream fileOutputStream = new FileOutputStream(outFile);
                byte[] buffer = new byte[4096];
                int offset;
                while ((offset = inputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, offset);
                }
                inputStream.close();
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            isDataReady = true;
        }
    }
}
