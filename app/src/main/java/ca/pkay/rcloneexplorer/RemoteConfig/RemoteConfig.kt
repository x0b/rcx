package ca.pkay.rcloneexplorer.RemoteConfig;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONException;

import java.util.HashMap;

import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import ca.pkay.rcloneexplorer.RuntimeConfiguration;
import ca.pkay.rcloneexplorer.rclone.Provider;
import ca.pkay.rcloneexplorer.util.ActivityHelper;
import es.dmoral.toasty.Toasty;

public class RemoteConfig extends AppCompatActivity implements RemotesConfigList.ProviderSelectedListener {


    public static final int CONFIG_EDIT_CODE = 139;

    public static final String CONFIG_EDIT_TARGET = "CONFIG_EDIT_TARGET";

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

        String shouldEdit = getIntent().getStringExtra(CONFIG_EDIT_TARGET);
        if(shouldEdit != null) {
            if(!shouldEdit.isEmpty()){
                Rclone rclone = new Rclone(this);
                HashMap<String, String> config = rclone.getConfig(getIntent().getStringExtra(CONFIG_EDIT_TARGET));

                if (config != null) {
                    try {
                        Provider provider = rclone.getProvider(config.get("type"));
                        fragment = new DynamicConfig(provider.getName(), config);
                        startConfig(provider);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
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
        handleBackAction();
        return true;
    }

    @Override
    public void onBackPressed() {
        handleBackAction();
    }

    private void handleBackAction() {
        if (fragment instanceof RemotesConfigList) {
            finish();
        } else if (fragment instanceof DynamicConfig){
            if(((DynamicConfig) fragment).isEditConfig()){
                finish();
            } else {
                startProviderlist();
            }
        } else {
            startProviderlist();
        }
    }

    @Override
    public void onProviderSelected(Provider provider) {
        if (provider == null) {
            Toasty.error(this, getString(R.string.nothing_selected), Toast.LENGTH_SHORT, true).show();
            return;
        }

        switch (provider.getName()) {
            case "box":
            case "dropbox":
            case "pcloud":
            case "yandex":
            case "drive":
            case "google photos":
                fragment = new DynamicConfig(provider.getName(), true);
                break;
            default:
                fragment = new DynamicConfig(provider.getName());
                break;
        }
        startConfig(provider);
    }

    private void startConfig(Provider provider) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.flFragment, fragment, "new config");
        transaction.commit();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(provider.getNameCapitalized());
        }
    }

    private void startProviderlist() {
        fragment = RemotesConfigList.newInstance();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.flFragment, fragment, "config list");
        fragmentTransaction.commit();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_activity_remote_config);
        }
    }
}
