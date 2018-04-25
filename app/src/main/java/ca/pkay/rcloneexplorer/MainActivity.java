package ca.pkay.rcloneexplorer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import ca.pkay.rcloneexplorer.BroadcastReceivers.NetworkStateReceiver;
import ca.pkay.rcloneexplorer.Dialogs.InputDialog;
import ca.pkay.rcloneexplorer.Dialogs.LoadingDialog;
import ca.pkay.rcloneexplorer.Fragments.FileExplorerFragment;
import ca.pkay.rcloneexplorer.Fragments.RemotesFragment;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity
        implements  NavigationView.OnNavigationItemSelectedListener,
                    RemotesFragment.OnRemoteClickListener {

    public static final String SHARED_PREFS_TAG = "ca.pkay.rcexplorer";
    private static final int READ_REQUEST_CODE = 42; // code when opening rclone config file
    private static final int REQUEST_PERMISSION_CODE = 62; // code when requesting permissions
    private NavigationView navigationView;
    private Rclone rclone;
    private Fragment fragment;
    private Context context;
    private NetworkStateReceiver networkStateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        requestPermissions();

        rclone = new Rclone(this);

        networkStateReceiver = new NetworkStateReceiver();
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkStateReceiver, intentFilter);

        findViewById(R.id.locked_config_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askForConfigPassword();
            }
        });

        if (!rclone.isRcloneBinaryCreated()) {
            new CreateRcloneBinary().execute();
        } else if (rclone.isConfigEncrypted()) {
            askForConfigPassword();
        } else {
            startRemotesFragment();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // result from file picker (for importing config file)
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri;
            if (data != null) {
                uri = data.getData();
                new CopyConfigFile().execute(uri);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        File dir = getExternalCacheDir();
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                new File(dir, aChildren).delete();
            }
        }
        unregisterReceiver(networkStateReceiver);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (fragment != null && fragment instanceof FileExplorerFragment) {
            if (((FileExplorerFragment) fragment).onBackButtonPressed())
                return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_remotes:
                startRemotesFragment();
                break;
            case R.id.nav_import:
                if (rclone.isConfigFileCreated()) {
                    warnUserAboutOverwritingConfiguration();
                } else {
                    importConfigFile();
                }
                break;
            case R.id.nav_about:
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutIntent);
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public NetworkStateReceiver getNetworkStateReceiver() {
        return networkStateReceiver;
    }

    private void startRemotesFragment() {
        fragment = RemotesFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();

        for (int i = 0; i < fragmentManager.getBackStackEntryCount(); i++) {
            fragmentManager.popBackStack();
        }

        fragmentManager.beginTransaction().replace(R.id.flFragment, fragment).commit();

        navigationView.getMenu().getItem(0).setChecked(true);
    }

    private void warnUserAboutOverwritingConfiguration() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.replace_config_file_question);
        builder.setMessage(R.string.config_file_lost_statement);
        builder.setPositiveButton(R.string.continue_statement, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                importConfigFile();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }

    private void askForConfigPassword() {
        findViewById(R.id.locked_config).setVisibility(View.VISIBLE);
        new InputDialog()
                .setContext(context)
                .setTitle(R.string.config_password_protected)
                .setMessage(R.string.please_enter_password)
                .setNegativeButton(R.string.cancel)
                .setPositiveButton(R.string.okay_confirmation)
                .setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .setOnPositiveListener(new InputDialog.OnPositive() {
                    @Override
                    public void onPositive(String input) {
                        new DecryptConfig().execute(input);
                    }
                })
                .show(getSupportFragmentManager(), "input dialog");
    }

    public void importConfigFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    public void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
        }
    }

    @Override
    public void onRemoteClick(RemoteItem remote) {
        fragment = FileExplorerFragment.newInstance(remote.getName(), remote.getType());
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.flFragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();

        navigationView.getMenu().getItem(0).setChecked(false);
    }

    @SuppressLint("StaticFieldLeak")
    private class CreateRcloneBinary extends AsyncTask<Void, Void, Boolean> {

        private LoadingDialog loadingDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog = new LoadingDialog()
                    .setContext(context)
                    .setTitle(R.string.creating_rclone_binary)
                    .setCanCancel(false);
            loadingDialog.show(getSupportFragmentManager(), "loading dialog");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                rclone.createRcloneBinary();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (!success) {
                Toasty.error(context, getString(R.string.error_creating_rclone_binary), Toast.LENGTH_LONG, true).show();
                finish();
                System.exit(0);
            }
            loadingDialog.dismiss();
            startRemotesFragment();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class CopyConfigFile extends AsyncTask<Uri, Void, Boolean> {

        private LoadingDialog loadingDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.locked_config).setVisibility(View.GONE);
            loadingDialog = new LoadingDialog()
                    .setContext(context)
                    .setTitle(R.string.copying_rclone_config)
                    .setCanCancel(false);
            loadingDialog.show(getSupportFragmentManager(), "loading dialog");
        }

        @Override
        protected Boolean doInBackground(Uri... uris) {
            try {
                rclone.copyConfigFile(uris[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            loadingDialog.dismiss();
            if (!success) {
                return;
            }
            if (rclone.isConfigEncrypted()) {
                askForConfigPassword();
            } else {
                startRemotesFragment();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class DecryptConfig extends AsyncTask<String, Void, Boolean> {

        private LoadingDialog loadingDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog = new LoadingDialog()
                    .setContext(context)
                    .setTitle(R.string.working)
                    .setCanCancel(false);
            loadingDialog.show(getSupportFragmentManager(), "loading dialog");
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            return rclone.decryptConfig(strings[0]);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            loadingDialog.dismiss();
            if (!success) {
                Toasty.error(context, getString(R.string.error_unlocking_config), Toast.LENGTH_LONG, true).show();
                askForConfigPassword();
            } else {
                findViewById(R.id.locked_config).setVisibility(View.GONE);
                startRemotesFragment();
            }
        }
    }
}
