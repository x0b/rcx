package ca.pkay.rcloneexplorer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import us.feras.mdv.MarkdownView;


public class ChangelogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();
        setContentView(R.layout.activity_changelog);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        MarkdownView markdownView = findViewById(R.id.markdownView);
        markdownView.loadMarkdownFile("file:///android_asset/changelog.md");
    }

    private void applyTheme() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int customPrimaryColor = sharedPreferences.getInt("pref_key_color_primary", -1);
        int customAccentColor = sharedPreferences.getInt("pref_key_color_accent", -1);
        getTheme().applyStyle(CustomColorHelper.getPrimaryColorTheme(this, customPrimaryColor), true);
        getTheme().applyStyle(CustomColorHelper.getAccentColorTheme(this, customAccentColor), true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
