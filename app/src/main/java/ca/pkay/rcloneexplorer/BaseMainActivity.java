package ca.pkay.rcloneexplorer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import ca.pkay.rcloneexplorer.Dialogs.InputDialog;
import ca.pkay.rcloneexplorer.Dialogs.LoadingDialog;
import ca.pkay.rcloneexplorer.Fragments.FileExplorerFragment;
import ca.pkay.rcloneexplorer.Fragments.RemotesFragment;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.Settings.SettingsActivity;
import es.dmoral.toasty.Toasty;

public abstract class BaseMainActivity   extends AppCompatActivity
                            implements  NavigationView.OnNavigationItemSelectedListener,
                                        RemotesFragment.OnRemoteClickListener,
                                        RemotesFragment.AddRemoteToNavDrawer,
                                        InputDialog.OnPositive {

    private static final int READ_REQUEST_CODE = 42; // code when opening rclone config file
    private static final int REQUEST_PERMISSION_CODE = 62; // code when requesting permissions
    private static final int SETTINGS_CODE = 71; // code when coming back from settings
    private static final int WRITE_REQUEST_CODE = 81; // code when exporting config
    private final String FILE_EXPLORER_FRAGMENT_TAG = "ca.pkay.rcexplorer.MAIN_ACTIVITY_FILE_EXPLORER_TAG";
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private Rclone rclone;
    private Fragment fragment;
    private Context context;
    private Boolean isDarkTheme;
    private HashMap<Integer, RemoteItem> drawerPinnedRemoteIds;
    private int availableDrawerPinnedRemoteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (updateCheck()) {
            return;
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        checkEnableCrashReports(sharedPreferences);

        applyTheme();
        context = this;
        drawerPinnedRemoteIds = new HashMap<>();
        availableDrawerPinnedRemoteId = 2;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        requestPermissions();

        rclone = new Rclone(this);

        findViewById(R.id.locked_config_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askForConfigPassword();
            }
        });


        checkSubscribeToUpdates(sharedPreferences);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        int lastVersionCode = sharedPreferences.getInt(getString(R.string.pref_key_version_code), -1);
        String lastVersionName = sharedPreferences.getString(getString(R.string.pref_key_version_name), "");
        int currentVersionCode = BuildConfig.VERSION_CODE;
        String currentVersionName = BuildConfig.VERSION_NAME;

        if (!rclone.isRcloneBinaryCreated()) {
            new CreateRcloneBinary().execute();
        } else if (lastVersionCode < currentVersionCode || !lastVersionName.equals(currentVersionName)) {
            // In version code 24 there were changes to app shortcuts
            // Remove this in the long future
            if (lastVersionCode <= 23) {
                AppShortcutsHelper.removeAllAppShortcuts(this);
                AppShortcutsHelper.populateAppShortcuts(this, rclone.getRemotes());
            }

            new CreateRcloneBinary().execute();

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(getString(R.string.pref_key_version_code), currentVersionCode);
            editor.putString(getString(R.string.pref_key_version_name), currentVersionName);
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
        } else if (bundle != null && bundle.containsKey(AppShortcutsHelper.APP_SHORTCUT_REMOTE_NAME)) {
            String remoteName = bundle.getString(AppShortcutsHelper.APP_SHORTCUT_REMOTE_NAME);
            RemoteItem remoteItem = getRemoteItemFromName(remoteName);
            if (remoteItem != null) {
                AppShortcutsHelper.reportAppShortcutUsage(this, remoteItem.getName());
                startRemote(remoteItem, false);
            } else {
                Toasty.error(this, getString(R.string.remote_not_found), Toast.LENGTH_SHORT, true).show();
                finish();
            }
        } else {
            startRemotesFragment();
        }
    }

    protected abstract boolean updateCheck();

    protected abstract void checkEnableCrashReports(SharedPreferences sharedPreferences);

    protected abstract void checkSubscribeToUpdates(SharedPreferences sharedPreferences);

    @Override
    protected void onStart() {
        super.onStart();
        pinRemotesToDrawer();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home && !(fragment instanceof FileExplorerFragment)) {
            drawer.openDrawer(GravityCompat.START);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void openNavigationDrawer() {
        drawer.openDrawer(GravityCompat.START);
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
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (fragment != null && fragment instanceof FileExplorerFragment) {
            if (((FileExplorerFragment) fragment).onBackButtonPressed()) {
                return;
            } else {
                fragment = null;
            }
        }
        super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (drawerPinnedRemoteIds.containsKey(id)) {
            startPinnedRemote(drawerPinnedRemoteIds.get(id));
            return true;
        }

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
                    Toasty.info(this,  getString(R.string.no_config_found), Toast.LENGTH_SHORT, true).show();
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

    private void pinRemotesToDrawer() {
        Menu menu = navigationView.getMenu();
        MenuItem existingMenu = menu.findItem(1);
        if (existingMenu != null) {
            return;
        }

        SubMenu subMenu = menu.addSubMenu(R.id.drawer_pinned_header, 1, Menu.NONE, R.string.nav_drawer_pinned_header);

        List<RemoteItem> remoteItems = rclone.getRemotes();
        Collections.sort(remoteItems);
        for (RemoteItem remoteItem : remoteItems) {
            if (remoteItem.isDrawerPinned()) {
                MenuItem menuItem = subMenu.add(R.id.nav_pinned, availableDrawerPinnedRemoteId, Menu.NONE, remoteItem.getName());
                drawerPinnedRemoteIds.put(availableDrawerPinnedRemoteId, remoteItem);
                availableDrawerPinnedRemoteId++;
                menuItem.setIcon(remoteItem.getRemoteIcon());
            }
        }
    }

    private void startRemotesFragment() {
        fragment = RemotesFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();

        for (int i = 0; i < fragmentManager.getBackStackEntryCount(); i++) {
            fragmentManager.popBackStack();
        }

        if (!isFinishing()) {
            fragmentManager.beginTransaction().replace(R.id.flFragment, fragment).commitAllowingStateLoss();
        }
    }

    private RemoteItem getRemoteItemFromName(String remoteName) {
        List<RemoteItem> remoteItemList = rclone.getRemotes();
        for (RemoteItem remoteItem : remoteItemList) {
            if (remoteItem.getName().equals(remoteName)) {
                return remoteItem;
            }
        }
        return null;
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
                .setTitle(R.string.config_password_protected)
                .setMessage(R.string.please_enter_password)
                .setNegativeButton(R.string.cancel)
                .setPositiveButton(R.string.okay_confirmation)
                .setDarkTheme(isDarkTheme)
                .setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .show(getSupportFragmentManager(), "input dialog");
    }

    /*
     * Input Dialog callback
     */
    @Override
    public void onPositive(String tag, String input) {
        new DecryptConfig().execute(input);
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

    private void openAppUpdate() {
        Uri uri = Uri.parse(getString(R.string.app_latest_release_url));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private void openBetaUpdate(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public void onRemoteClick(RemoteItem remote) {
        startRemote(remote, true);
    }

    private void startRemote(RemoteItem remote, boolean addToBackStack) {
        fragment = FileExplorerFragment.newInstance(remote);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.flFragment, fragment, FILE_EXPLORER_FRAGMENT_TAG);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();

        AppShortcutsHelper.reportAppShortcutUsage(this, remote.getName());
        navigationView.getMenu().getItem(0).setChecked(false);
    }

    private void startPinnedRemote(RemoteItem remoteItem) {
        if (fragment != null && fragment instanceof FileExplorerFragment) {
            FragmentManager fragmentManager = getSupportFragmentManager();

            // this is the case when remote gets started from a shortcut
            // therefore back should exit the app, and not go into remotes screen
            if (fragmentManager.getBackStackEntryCount() == 0) {
                startRemote(remoteItem, false);
            } else {
                for (int i = 0; i < fragmentManager.getBackStackEntryCount(); i++) {
                    fragmentManager.popBackStack();
                }

                startRemote(remoteItem, true);
            }
        } else {
            startRemote(remoteItem, true);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void addRemoteToNavDrawer() {
        Menu menu = navigationView.getMenu();

        // remove all items and add them again so that it's in alpha order
        menu.removeItem(1);
        drawerPinnedRemoteIds.clear();
        availableDrawerPinnedRemoteId = 1;

        pinRemotesToDrawer();
    }

    @Override
    public void removeRemoteFromNavDrawer() {
        Menu menu = navigationView.getMenu();

        // remove all items and add them again so that it's in alpha order
        menu.removeItem(1);
        drawerPinnedRemoteIds.clear();
        availableDrawerPinnedRemoteId = 1;

        pinRemotesToDrawer();
    }

    @SuppressLint("StaticFieldLeak")
    private class CreateRcloneBinary extends AsyncTask<Void, Void, Boolean> {

        private LoadingDialog loadingDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog = new LoadingDialog()
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

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(getString(R.string.shared_preferences_pinned_remotes));
            editor.remove(getString(R.string.shared_preferences_drawer_pinned_remotes));
            editor.remove(getString(R.string.shared_preferences_hidden_remotes));
            editor.apply();

            if (rclone.isConfigEncrypted()) {
                pinRemotesToDrawer(); // this will clear any previous pinned remotes
                askForConfigPassword();
            } else {
                AppShortcutsHelper.removeAllAppShortcuts(context);
                AppShortcutsHelper.populateAppShortcuts(context, rclone.getRemotes());
                pinRemotesToDrawer();
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
                AppShortcutsHelper.removeAllAppShortcuts(context);
                AppShortcutsHelper.populateAppShortcuts(context, rclone.getRemotes());
                startRemotesFragment();
            }
        }
    }
}
