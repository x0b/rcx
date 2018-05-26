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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.pkay.rcloneexplorer.RecyclerViewAdapters.AboutLibrariesAdapter;

public class AboutLibsActivity extends AppCompatActivity implements AboutLibrariesAdapter.OnClickListener {

    private List<String> libraryNames;
    private Map<String, String> libraryUrls;
    private Map<String, String> libraryLicences;
    private Map<String, String> libraryLicenceUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();
        setContentView(R.layout.activity_about_libs);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        createData();
        RecyclerView recyclerView = findViewById(R.id.about_libs_list);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        AboutLibrariesAdapter aboutLibrariesAdapter = new AboutLibrariesAdapter(libraryNames, libraryUrls, libraryLicences, libraryLicenceUrls, this);
        recyclerView.setAdapter(aboutLibrariesAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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

    private void createData() {
        libraryNames = new ArrayList<>();
        libraryUrls = new HashMap<>();
        libraryLicences = new HashMap<>();
        libraryLicenceUrls = new HashMap<>();

        String androidSupportLibraries = "Android Support Libraries";
        libraryNames.add(androidSupportLibraries);
        libraryUrls.put(androidSupportLibraries, "https://developer.android.com/topic/libraries/support-library/");
        libraryLicences.put(androidSupportLibraries, "Licensed under Apache-2.0");
        libraryLicenceUrls.put(androidSupportLibraries, "http://www.apache.org/licenses/LICENSE-2.0");

        String floatingActionButtonSpeedDial = "Floating Action Button Speed Dial";
        libraryNames.add(floatingActionButtonSpeedDial);
        libraryUrls.put(floatingActionButtonSpeedDial, "https://github.com/leinardi/FloatingActionButtonSpeedDial");
        libraryLicences.put(floatingActionButtonSpeedDial, "Licensed under Apache-2.0");
        libraryLicenceUrls.put(floatingActionButtonSpeedDial, "https://github.com/leinardi/FloatingActionButtonSpeedDial/blob/master/LICENSE");

        String glide = "Glide";
        libraryNames.add(glide);
        libraryUrls.put(glide, "https://github.com/bumptech/glide");
        libraryLicences.put(glide, "BSD, part MIT and Apache 2.0");
        libraryLicenceUrls.put(glide, "https://github.com/bumptech/glide/blob/master/LICENSE");

        String markDownView = "MarkDown View";
        libraryNames.add(markDownView);
        libraryUrls.put(markDownView, "https://github.com/falnatsheh/MarkdownView");
        libraryLicences.put(markDownView, "Licensed under Apache-2.0");
        libraryLicenceUrls.put(markDownView, "https://github.com/falnatsheh/MarkdownView/blob/master/license.txt");

        String materialDesignIcons = "Material Design";
        libraryNames.add(materialDesignIcons);
        libraryUrls.put(materialDesignIcons, "https://github.com/Templarian/MaterialDesign");
        libraryLicences.put(materialDesignIcons, "Licensed under SIL Open Font 1.1");
        libraryLicenceUrls.put(materialDesignIcons, "http://scripts.sil.org/cms/scripts/page.php?item_id=OFL_web");

        String rclone = "Rclone";
        libraryNames.add(rclone);
        libraryUrls.put(rclone, "https://github.com/ncw/rclone");
        libraryLicences.put(rclone, "Licensed under MIT");
        libraryLicenceUrls.put(rclone, "https://github.com/ncw/rclone/blob/master/COPYING");

        String toasty = "Toasty";
        libraryNames.add(toasty);
        libraryUrls.put(toasty, "https://github.com/GrenderG/Toasty");
        libraryLicences.put(toasty, "Licensed under LGPL-3.0");
        libraryLicenceUrls.put(toasty, "https://github.com/GrenderG/Toasty/blob/master/LICENSE");
    }

    @Override
    public void onLibraryClick(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
