package ca.pkay.rcloneexplorer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        new LibsBuilder()
                .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                .withActivityTitle(getString(R.string.credits_libraries))
                .withAutoDetect(false)
                .withLibraries()
                .withExcludedLibraries()
                .start(this);
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
