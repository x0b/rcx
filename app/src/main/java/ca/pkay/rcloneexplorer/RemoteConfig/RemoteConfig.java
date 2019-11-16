package ca.pkay.rcloneexplorer.RemoteConfig;

import android.os.Bundle;
import android.util.TypedValue;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import ca.pkay.rcloneexplorer.ActivityHelper;
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
        ActivityHelper.applyTheme(this);
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        getWindow().setStatusBarColor(typedValue.data);
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
            case "CACHE":
                fragment = CacheConfig.newInstance();
                title = "Cache";
                break;
            case "LOCAL":
                fragment = LocalConfig.newInstance();
                title = "Local";
                break;
            case "DRIVE":
                fragment = DriveConfig.newInstance();
                title = "Drive";
                break;
            case "UNION":
                fragment = UnionConfig.newInstance();
                title = "Union";
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
