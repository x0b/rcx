package ca.pkay.rcloneexplorer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private static final int SETTINGS_CODE = 71; // code when coming back from settings
    private static final int WRITE_REQUEST_CODE = 81; // code when exporting config
    private final String APP_SHORTCUT_REMOTE_NAME = "arg_remote_name";
    private final String APP_SHORTCUT_REMOTE_TYPE = "arg_remote_type";
    private final String FILE_EXPLORER_FRAGMENT_TAG = "ca.pkay.rcexplorer.MAIN_ACTIVITY_FILE_EXPLORER_TAG";
    private NavigationView navigationView;
    private Rclone rclone;
    private Fragment fragment;
    private Context context;
    private Boolean isDarkTheme;
    private NetworkStateReceiver networkStateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();
        context = this;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

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

        boolean appUpdates = sharedPreferences.getBoolean(getString(R.string.pref_key_app_updates), true);
        if (appUpdates) {
            FirebaseMessaging.getInstance().subscribeToTopic(getString(R.string.firebase_msg_app_updates_topic));
        }

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        int lastVersionCode = sharedPreferences.getInt(getString(R.string.pref_key_version_code), -1);
        int currentVersionCode = BuildConfig.VERSION_CODE;

        if (!rclone.isRcloneBinaryCreated()) {
            new CreateRcloneBinary().execute();
        } else if (lastVersionCode < currentVersionCode) {
            new CreateRcloneBinary().execute();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(getString(R.string.pref_key_version_code), currentVersionCode);
            editor.apply();
        } else if (rclone.isConfigEncrypted()) {
            askForConfigPassword();
        } else if (savedInstanceState != null) {
            fragment = getSupportFragmentManager().findFragmentByTag(FILE_EXPLORER_FRAGMENT_TAG);
            if (fragment instanceof FileExplorerFragment) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.flFragment, fragment, FILE_EXPLORER_FRAGMENT_TAG);
                transaction.commit();
            } else {
                startRemotesFragment();
            }
        } else if (bundle != null && bundle.containsKey(APP_SHORTCUT_REMOTE_NAME) && bundle.containsKey(APP_SHORTCUT_REMOTE_TYPE)) {
            String remoteName = bundle.getString(APP_SHORTCUT_REMOTE_NAME);
            String remoteType = bundle.getString(APP_SHORTCUT_REMOTE_TYPE);
            startRemote(remoteName, remoteType);
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

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        getWindow().setStatusBarColor(typedValue.data);

        // set recents app color to the primary color
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round);
        ActivityManager.TaskDescription taskDesc = new ActivityManager.TaskDescription(getString(R.string.app_name), bm, customPrimaryColor);
        setTaskDescription(taskDesc);
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
        } else if (requestCode == SETTINGS_CODE && resultCode == RESULT_OK) {
            boolean themeChanged = data.getBooleanExtra(SettingsActivity.THEME_CHANGED, false);
            if (themeChanged) {
                recreate();
            }
        } else if (requestCode == WRITE_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri uri;
            if (data != null) {
                uri = data.getData();
                try {
                    rclone.exportConfigFile(uri);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toasty.error(this, getString(R.string.error_exporting_config_file), Toast.LENGTH_SHORT, true).show();
                }
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
            case R.id.nav_export:
                if (rclone.isConfigFileCreated()) {
                    exportConfigFile();
                } else {
                    Toasty.info(this,  getString(R.string.no_config_file), Toast.LENGTH_SHORT, true).show();
                }
                break;
            case R.id.nav_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivityForResult(settingsIntent, SETTINGS_CODE);
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
    }

    private void warnUserAboutOverwritingConfiguration() {
        AlertDialog.Builder builder;
        if (isDarkTheme) {
            builder = new AlertDialog.Builder(this, R.style.DarkDialogTheme);
        } else {
            builder = new AlertDialog.Builder(this);
        }
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

    public void exportConfigFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        intent.putExtra(Intent.EXTRA_TITLE, "rclone.conf");
        startActivityForResult(intent, WRITE_REQUEST_CODE);
    }

    public void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
        }
    }

    private void addRemotesToShortcutList() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1) {
            return;
        }
        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
        if (shortcutManager == null) {
            return;
        }
        shortcutManager.removeAllDynamicShortcuts();

        List<RemoteItem> remoteItemList = rclone.getRemotes();
        List<ShortcutInfo> shortcutInfoList = new ArrayList<>();

        for (RemoteItem remoteItem : remoteItemList) {
            String id = remoteItem.getName().replaceAll(" ", "_");

            Intent intent = new Intent(Intent.ACTION_MAIN, Uri.EMPTY, this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(APP_SHORTCUT_REMOTE_NAME, remoteItem.getName());
            intent.putExtra(APP_SHORTCUT_REMOTE_TYPE, remoteItem.getType());

            ShortcutInfo shortcut = new ShortcutInfo.Builder(this, id)
                    .setShortLabel(remoteItem.getName())
                    .setIcon(Icon.createWithResource(context, getRemoteIcon(remoteItem.getType())))
                    .setIntent(intent)
                    .build();
            shortcutInfoList.add(shortcut);
            if (shortcutInfoList.size() == 4) {
                break;
            }
        }
        shortcutManager.setDynamicShortcuts(shortcutInfoList);
    }

    private void addRemoteToShortcutList(RemoteItem remoteItem) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1) {
            return;
        }
        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
        if (shortcutManager == null) {
            return;
        }

        String id = remoteItem.getName().replaceAll(" ", "_");

        List<ShortcutInfo> shortcutInfoList = shortcutManager.getDynamicShortcuts();
        for (ShortcutInfo shortcutInfo : shortcutInfoList) {
            if (shortcutInfo.getId().equals(id)) {
                shortcutManager.reportShortcutUsed(id);
                return;
            }
        }

        Intent intent = new Intent(Intent.ACTION_MAIN, Uri.EMPTY, this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(APP_SHORTCUT_REMOTE_NAME, remoteItem.getName());
        intent.putExtra(APP_SHORTCUT_REMOTE_TYPE, remoteItem.getType());

        ShortcutInfo shortcut = new ShortcutInfo.Builder(this, id)
                .setShortLabel(remoteItem.getName())
                .setIcon(Icon.createWithResource(context, getRemoteIcon(remoteItem.getType())))
                .setIntent(intent)
                .build();

        if (shortcutInfoList.size() >= 4) {
            ShortcutInfo removeId = shortcutInfoList.get(0);
            shortcutManager.removeDynamicShortcuts(Collections.singletonList(removeId.getId()));
        }
        shortcutManager.addDynamicShortcuts(Collections.singletonList(shortcut));
        shortcutManager.reportShortcutUsed(id);
    }

    private int getRemoteIcon(String remoteType) {
        switch (remoteType) {
            case "crypt":
                return R.mipmap.ic_shortcut_lock;
            case "amazon cloud drive":
                return R.mipmap.ic_shortcut_amazon;
            case "drive":
                return R.mipmap.ic_shortcut_drive;
            case "dropbox":
                return R.mipmap.ic_shortcut_dropbox;
            case "google cloud storage":
                return R.mipmap.ic_shortcut_google;
            case "onedrive":
                return R.mipmap.ic_shortcut_onedrive;
            case "s3":
                return R.mipmap.ic_shortcut_amazon;
            case "box":
                return R.mipmap.ic_shortcut_box;
            case "sftp":
                return R.mipmap.ic_shortcut_terminal;
            default:
                return R.mipmap.ic_shortcut_cloud;
        }
    }

    @Override
    public void onRemoteClick(RemoteItem remote) {
        startRemote(remote);
    }

    private void startRemote(RemoteItem remote) {
        fragment = FileExplorerFragment.newInstance(remote.getName(), remote.getType());
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.flFragment, fragment, FILE_EXPLORER_FRAGMENT_TAG);
        transaction.addToBackStack(null);
        transaction.commit();

        addRemoteToShortcutList(remote);
        navigationView.getMenu().getItem(0).setChecked(false);
    }

    private void startRemote(String remoteName, String remoteType) {
        fragment = FileExplorerFragment.newInstance(remoteName, remoteType);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.flFragment, fragment, FILE_EXPLORER_FRAGMENT_TAG);
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
            if (loadingDialog.isStateSaved()) {
                loadingDialog.dismissAllowingStateLoss();
            } else {
                loadingDialog.dismiss();
            }
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
            if (loadingDialog.isStateSaved()) {
                loadingDialog.dismissAllowingStateLoss();
            } else {
                loadingDialog.dismiss();
            }
            if (!success) {
                return;
            }
            if (rclone.isConfigEncrypted()) {
                askForConfigPassword();
            } else {
                addRemotesToShortcutList();
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
                addRemotesToShortcutList();
                startRemotesFragment();
            }
        }
    }
}
