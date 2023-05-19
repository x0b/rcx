package ca.pkay.rcloneexplorer.RemoteConfig;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.Locale;

import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.RuntimeConfiguration;
import ca.pkay.rcloneexplorer.util.ActivityHelper;
import es.dmoral.toasty.Toasty;

public class RemoteConfig extends AppCompatActivity implements RemotesConfigList.ProviderSelectedListener {

    private final String OUTSTATE_TITLE = "ca.pkay.rcexplorer.remoteConfig.TITLE";
    private Fragment fragment;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(RuntimeConfiguration.attach(this, newBase));
    }

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

        Log.e("TAG", "next?: "+provider);

        // Todo: Check if we can make the list dynamic too
        String s = getResources().getStringArray(R.array.provider_ids)[provider];
        String title = getResources().getStringArray(R.array.provider_names)[provider];
        Log.e("TAG", "next?: "+s);

        switch (s) {
            case "BOX":
                fragment = BoxConfig.newInstance();
                break;
            case "DROPBOX":
                fragment = DropboxConfig.newInstance();
                break;
            case "PCLOUD":
                fragment = PcloudConfig.newInstance();
                break;
            case "YANDEX":
                fragment = YandexConfig.newInstance();
                break;
            case "DRIVE":
                fragment = DriveConfig.newInstance();
                break;
            case "GOOGLE_PHOTOS":
                fragment = GooglePhotosConfig.newInstance();
                break;
            default:
                fragment = new DynamicConfig(s.toLowerCase(Locale.ROOT));
                break;
        }


        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.flFragment, fragment, "new config");
        transaction.commit();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }
}
