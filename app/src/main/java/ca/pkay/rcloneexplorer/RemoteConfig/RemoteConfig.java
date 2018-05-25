package ca.pkay.rcloneexplorer.RemoteConfig;

import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.widget.Toast;

import ca.pkay.rcloneexplorer.CustomColorHelper;
import ca.pkay.rcloneexplorer.R;
import es.dmoral.toasty.Toasty;

public class RemoteConfig extends AppCompatActivity implements RemotesConfigList.ProviderSelectedListener {

    private final String OUTSTATE_TITLE = "ca.pkay.rcexplorer.remoteConfig.TITLE";
    private Fragment fragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();
        setContentView(R.layout.activity_remote_config);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        if (savedInstanceState != null) {
            fragment = getSupportFragmentManager().findFragmentByTag("new config");
            if (fragment != null) {
                fragmentTransaction.replace(R.id.flFragment, fragment, "new config");
                fragmentTransaction.commit();

                String title = savedInstanceState.getString(OUTSTATE_TITLE);
                if (title != null) {
                    getSupportActionBar().setTitle(title);
                }
                return;
            }
        }
        fragment = RemotesConfigList.newInstance();
        fragmentTransaction.replace(R.id.flFragment, fragment, "config list");
        fragmentTransaction.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (getSupportActionBar() != null) {
            CharSequence title = getSupportActionBar().getTitle();
            if (title != null) {
                outState.putString(OUTSTATE_TITLE, title.toString());
            }
        }
    }

    private void applyTheme() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int customPrimaryColor = sharedPreferences.getInt(getString(R.string.pref_key_color_primary), -1);
        int customAccentColor = sharedPreferences.getInt(getString(R.string.pref_key_color_accent), -1);
        boolean isDarkTheme = sharedPreferences.getBoolean(getString(R.string.pref_key_dark_theme), false);
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
    public boolean onSupportNavigateUp() {
        if (fragment instanceof RemotesConfigList) {
            finish();
        } else {
            fragment = RemotesConfigList.newInstance();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.flFragment, fragment, "config list");
            fragmentTransaction.commit();

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.title_activity_remote_config);
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (fragment instanceof RemotesConfigList) {
            finish();
        } else {
            fragment = RemotesConfigList.newInstance();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.flFragment, fragment, "config list");
            fragmentTransaction.commit();

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.title_activity_remote_config);
            }
        }
    }

    @Override
    public void onProviderSelected(int provider) {
        if (provider < 0) {
            Toasty.error(this, getString(R.string.nothing_selected), Toast.LENGTH_SHORT, true).show();
            return;
        }

        String s = RemotesConfigList.providers.get(provider);
        String title;
        switch (s) {
            case "B2":
                fragment = B2Config.newInstance();
                title = "Backblaze B2";
                break;
            case "BOX":
                fragment = BoxConfig.newInstance();
                title = "Box";
                break;
            case "FTP":
                fragment = FtpConfig.newInstance();
                title = "FTP";
                break;
            case "HTTP":
                fragment = HttpConfig.newInstance();
                title = "HTTP";
                break;
            case "DROPBOX":
                fragment = DropboxConfig.newInstance();
                title = "Dropbox";
                break;
            case "HUBIC":
                fragment = HubicConfig.newInstance();
                title = "Hubic";
                break;
            case "PCLOUD":
                fragment = PcloudConfig.newInstance();
                title = "Pcloud";
                break;
            case "SFTP":
                fragment = SftpConfig.newInstance();
                title = "SFTP/SSH";
                break;
            case "YANDEX":
                fragment = YandexConfig.newInstance();
                title = "Yandex";
                break;
            case "WEBDAV":
                fragment = WebdavConfig.newInstance();
                title = "Webdav";
                break;
            case "ONEDRIVE":
                fragment = OneDriveConfig.newInstance();
                title = "Microsoft OneDrive";
                break;
            case "ALIAS":
                fragment = AliasConfig.newInstance();
                title = "Alias";
                break;
            case "CRYPT":
                fragment = CryptConfig.newInstance();
                title = "Crypt";
                break;
            case "QINGSTOR":
                fragment = QingstorConfig.newInstance();
                title = "QingStor";
                break;
            case "AZUREBLOB":
                fragment = Azureblob.newInstance();
                title = "Microsoft Azure Blob Storage";
                break;
            default:
                return;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.flFragment, fragment, "new config");
        transaction.commit();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }
}
