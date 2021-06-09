package ca.pkay.rcloneexplorer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import static ca.pkay.rcloneexplorer.ActivityHelper.tryStartActivity;


public class AboutActivity extends AppCompatActivity {

    private static final String TAG = "AboutActivity";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(RuntimeConfiguration.attach(this, newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityHelper.applyTheme(this);
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

        findViewById(R.id.changelog).setOnClickListener(v -> showChangelog());
        findViewById(R.id.contributors).setOnClickListener(v -> showContributors());
        findViewById(R.id.open_source_libraries).setOnClickListener(v -> showOpenSourceLibraries());
        findViewById(R.id.star_on_github).setOnClickListener(v -> openAppGitHubLink());
        findViewById(R.id.report_bug).setOnClickListener(v -> reportBug());
        findViewById(R.id.author_github_link).setOnClickListener(v -> openAuthorGitHubLink());
        findViewById(R.id.maintainer_github_link).setOnClickListener(v -> openMaintainerGithubLink());
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_issue_url)));
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
}
