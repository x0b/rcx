package ca.pkay.rcloneexplorer;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;


public class AboutActivity extends AppCompatActivity {

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
        ((TextView)findViewById(R.id.rclone_version)).setText(rclone.getRcloneVersion());

        findViewById(R.id.changelog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangelog();
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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void showChangelog() {
        Intent changelogIntent = new Intent(this, ChangelogActivity.class);
        startActivity(changelogIntent);
    }

    private void showOpenSourceLibraries() {
        Intent librariesIntent = new Intent(this, AboutLibsActivity.class);
        startActivity(librariesIntent);
    }

    private void openAppGitHubLink() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_app_url)));
        startActivity(browserIntent);
    }

    private void reportBug() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_issue_url)));
        startActivity(browserIntent);
    }

    private void openAuthorGitHubLink() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_author_url)));
        startActivity(browserIntent);
    }
}
