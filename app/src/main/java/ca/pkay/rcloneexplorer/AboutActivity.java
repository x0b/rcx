package ca.pkay.rcloneexplorer;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static ca.pkay.rcloneexplorer.StartActivity.tryStartActivity;


public class AboutActivity extends AppCompatActivity {

    private static final String TAG = "AboutActivity";
    private OkHttpClient client;
    private Handler handler;
    private Button serverVersionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();
        setContentView(R.layout.activity_about);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        Rclone rclone = new Rclone(this);

        ((TextView)findViewById(R.id.version_number)).setText(BuildConfig.VERSION_NAME);
        updateUpdateButton();
        ((TextView)findViewById(R.id.rclone_version)).setText(rclone.getRcloneVersion());

        findViewById(R.id.changelog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangelog();
            }
        });
        findViewById(R.id.contributors).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showContributors();
            }
        });
        findViewById(R.id.open_source_libraries).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOpenSourceLibraries();
            }
        });
        findViewById(R.id.star_on_github).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAppGitHubLink();
            }
        });
        findViewById(R.id.report_bug).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportBug();
            }
        });
        findViewById(R.id.author_github_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAuthorGitHubLink();
            }
        });
        findViewById(R.id.maintainer_github_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMaintainerGithubLink();
            }
        });
    }

    private void applyTheme() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int customPrimaryColor = sharedPreferences.getInt(getString(R.string.pref_key_color_primary), -1);
        int customAccentColor = sharedPreferences.getInt(getString(R.string.pref_key_color_accent), -1);
        Boolean isDarkTheme = sharedPreferences.getBoolean(getString(R.string.pref_key_dark_theme), false);
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
    protected void onResume() {
        super.onResume();
        handler = new UpdateMessageHandler();
        checkForUpdate(false);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void showChangelog() {
        Intent changelogIntent = new Intent(this, ChangelogActivity.class);
        startActivity(changelogIntent);
    }

    private void showContributors() {
        Intent contributorIntent = new Intent(this, ContributorActivity.class);
        startActivity(contributorIntent);
    }

    private void showOpenSourceLibraries() {
        Intent librariesIntent = new Intent(this, AboutLibsActivity.class);
        startActivity(librariesIntent);
    }

    private void openAppGitHubLink() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_app_url)));
        tryStartActivity(this, browserIntent);
    }

    private void reportBug() {
        String template = getString(R.string.github_issue_template,
                new Rclone(this).getRcloneVersion(),
                BuildConfig.VERSION_NAME,
                Build.VERSION.SDK_INT,
                Build.MODEL,
                TextUtils.join(";", Build.SUPPORTED_ABIS)
        );
        String baseUri = getString(R.string.github_issue_url);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(baseUri + Uri.encode(template)));
        tryStartActivity(this, browserIntent);
    }

    private void openAuthorGitHubLink() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_author_url)));
        tryStartActivity(this, browserIntent);
    }

    private void openMaintainerGithubLink() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_maintainer_url)));
        tryStartActivity(this, browserIntent);
    }

    private void openReleaseLink() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_latest_release_url)));
        tryStartActivity(this, browserIntent);
    }

    private void checkForUpdate(boolean force) {
        Context context = this;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(!force && !sharedPreferences.getBoolean(getString(R.string.pref_key_app_updates), true)){
            Log.i(TAG, "checkForUpdate: Not checking, updates are disabled");
            return;
        }
        client = new OkHttpClient();
        long lastUpdateCheck = sharedPreferences.getLong(context.getString(R.string.pref_key_update_last_check), 0);

        long now = System.currentTimeMillis();
        if(lastUpdateCheck + 1000 * 60 * 60 * 12 > now){
            Log.i(TAG, "checkForUpdate: recent check to new, not checking for updates");
            return;
        }

        String url = context.getString(R.string.app_relase_api_url);
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new UpdateRequestResultHandler(
                sharedPreferences, context.getString(R.string.pref_key_update_last_check),
                context.getString(R.string.pref_key_update_release_version),
                handler));
    }

    private static class UpdateRequestResultHandler implements Callback {

        private SharedPreferences sharedPreferences;
        private String lastUpdateKey;
        private String serverVersionKey;
        private Handler updateHandler;

        public UpdateRequestResultHandler(SharedPreferences sharedPreferences, String lastUpdateKey, String serverVersionKey, Handler updateHandler) {
            this.sharedPreferences = sharedPreferences;
            this.lastUpdateKey = lastUpdateKey;
            this.serverVersionKey = serverVersionKey;
            this.updateHandler = updateHandler;
        }

        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            Log.w(TAG, "onFailure: Update check failed", e);
            updateLastUpdateRequest();
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            updateLastUpdateRequest();
            try {
                JSONObject json = new JSONObject(response.body().string());
                String tagName = json.getString("tag_name");
                String currentVersion = BuildConfig.VERSION_NAME.split("-")[0];
                if(tagName.contains(currentVersion)){
                    Log.i(TAG, "onResponse: App is up-to-date");
                    sharedPreferences.edit().putString(BuildConfig.VERSION_NAME, tagName).apply();
                } else {
                    Log.i(TAG, "onResponse: App version != release");
                    sharedPreferences.edit().putString(serverVersionKey, tagName).apply();
                    updateHandler.sendMessage(new Message());
                }
            } catch (JSONException e) {
                Log.e(TAG, "onResponse: Could not read release ", e);
            }
        }

        private void updateLastUpdateRequest(){
            long now = System.currentTimeMillis();
            sharedPreferences.edit().putLong(lastUpdateKey, now).apply();

        }
    }

    private void updateUpdateButton(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String serverVersion = preferences.getString(getString(R.string.pref_key_update_release_version), BuildConfig.VERSION_NAME);
        serverVersionView = findViewById(R.id.server_version_number);
        if(!BuildConfig.VERSION_NAME.equals(serverVersion)){
            serverVersionView.setText("Update " + serverVersion);
            serverVersionView.setVisibility(View.VISIBLE);
            serverVersionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openReleaseLink();
                }
            });
        } else {
            serverVersionView.setText(getText(R.string.about_check_updates));
            serverVersionView.setVisibility(View.VISIBLE);
            serverVersionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkForUpdate(true);
                }
            });
        }

        serverVersionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openReleaseLink();
            }
        });
    }

    @SuppressLint("HandlerLeak")
    private class UpdateMessageHandler extends Handler {

        public UpdateMessageHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            updateUpdateButton();
        }
    }
}
