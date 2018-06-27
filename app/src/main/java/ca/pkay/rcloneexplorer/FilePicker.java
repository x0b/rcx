package ca.pkay.rcloneexplorer;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.leinardi.android.speeddial.SpeedDialView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Stack;

import ca.pkay.rcloneexplorer.Dialogs.InputDialog;
import ca.pkay.rcloneexplorer.Dialogs.SortDialog;
import ca.pkay.rcloneexplorer.RecyclerViewAdapters.FilePickerAdapter;
import es.dmoral.toasty.Toasty;

public class FilePicker extends AppCompatActivity implements    FilePickerAdapter.OnClickListener,
                                                                InputDialog.OnPositive,
                                                                SortDialog.OnClickListener {

    public static final String FILE_PICKER_PICK_DESTINATION_TYPE = "ca.pkay.rcexplorer.FILE_PICKER_PICK_DEST_TYPE";
    public static final String FILE_PICKER_RESULT = "ca.pkay.rcexplorer.FILE_PICKER_RESULT";
    private static final String SHARED_PREFS_SORT_ORDER = "ca.pkay.rcexplorer.sort_order";
    private FilePickerAdapter filePickerAdapter;
    private ActionBar actionBar;
    private File root;
    private File current;
    private ArrayList<File> fileList;
    private Stack<File> pathStack;
    private boolean isDarkTheme;
    private int sortOrder;
    private SpeedDialView speedDialView;
    private boolean destinationPickerType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();
        destinationPickerType = getIntent().getBooleanExtra(FILE_PICKER_PICK_DESTINATION_TYPE, false);
        setContentView(R.layout.activity_file_picker);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(R.string.file_picker_root_title);
        }

        pathStack = new Stack<>();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sortOrder = sharedPreferences.getInt(SHARED_PREFS_SORT_ORDER, SortDialog.ALPHA_ASCENDING);

        current = Environment.getExternalStorageDirectory();
        root = current;
        fileList = new ArrayList<>(Arrays.asList(current.listFiles()));
        Collections.sort(fileList, new FileComparators.SortFileAlphaAscending());

        RecyclerView recyclerView = findViewById(R.id.file_picker_list);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        filePickerAdapter = new FilePickerAdapter(this, fileList, destinationPickerType, findViewById(R.id.empty_folder_view));
        recyclerView.setAdapter(filePickerAdapter);

        speedDialView = findViewById(R.id.fab);
        if (!destinationPickerType) {
            speedDialView.setVisibility(View.INVISIBLE);
        }
        speedDialView.setMainFabOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabClicked();
            }
        });
    }

    private void applyTheme() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int customPrimaryColor = sharedPreferences.getInt(getString(R.string.pref_key_color_primary), -1);
        int customAccentColor = sharedPreferences.getInt(getString(R.string.pref_key_color_accent), -1);
        isDarkTheme = sharedPreferences.getBoolean(getString(R.string.pref_key_dark_theme), false);
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

    private void fabClicked() {
        if (destinationPickerType) {
            setResultData();
        } else if (filePickerAdapter.isDataSelected()) {
            setResultData(filePickerAdapter.getSelectedFiles());
        } else {
            Toasty.info(this, getString(R.string.nothing_selected), Toast.LENGTH_SHORT, true).show();
            finish();
        }
    }

    private void setResultData(ArrayList<File> result) {
        Intent intent = new Intent();
        intent.putExtra(FILE_PICKER_RESULT, result);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void setResultData() {
        Intent intent = new Intent();
        intent.putExtra(FILE_PICKER_RESULT, current.getPath());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.file_picker_menu, menu);
        if (destinationPickerType) {
            menu.removeItem(R.id.action_select_all);
        } else {
            menu.removeItem(R.id.action_new_folder);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select_all:
                filePickerAdapter.toggleSelectAll();
                return true;
            case R.id.action_sort:
                showSortMenu();
                return true;
            case R.id.action_new_folder:
                newFolderDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (pathStack.isEmpty()) {
            super.onBackPressed();
            if (filePickerAdapter.isDataSelected()) {
                setResultData(filePickerAdapter.getSelectedFiles());
            } else {
                finish();
            }
        } else {
            current = pathStack.pop();
            if (current.equals(root)) {
                actionBar.setTitle(R.string.file_picker_root_title);
            } else {
                actionBar.setTitle(current.getName());
            }
            fileList.clear();
            fileList.addAll(Arrays.asList(current.listFiles()));
            sortDirectory();
            filePickerAdapter.setNewData(fileList);

            if (destinationPickerType) {
                speedDialView.show();
            }
        }
    }

    @Override
    public void onDirectoryClicked(File file) {
        actionBar.setTitle(file.getName());
        pathStack.push(current);
        current = file;
        fileList.clear();
        fileList.addAll(Arrays.asList(file.listFiles()));
        sortDirectory();
        filePickerAdapter.setNewData(fileList);

        if (destinationPickerType) {
            speedDialView.show();
        }
    }

    @Override
    public void onFileClicked(File file) {
        setResultData(new ArrayList<>(Collections.singletonList(file)));
    }

    @Override
    public void onSelectionChanged(boolean isSelected) {
        if (isSelected && speedDialView.getVisibility() == View.INVISIBLE) {
            speedDialView.setVisibility(View.VISIBLE);
            speedDialView.show();
        } else if (!isSelected && speedDialView.getVisibility() == View.VISIBLE) {
            speedDialView.hide();
            speedDialView.setVisibility(View.INVISIBLE);
        }
    }

    private void newFolderDialog() {
        new InputDialog()
                .setTitle(R.string.create_new_folder)
                .setMessage(R.string.type_new_folder_name)
                .setNegativeButton(R.string.cancel)
                .setPositiveButton(R.string.okay_confirmation)
                .setDarkTheme(isDarkTheme)
                .show(getSupportFragmentManager(), "input dialog");
    }

    /*
     * Input Dialog callback
     */
    @Override
    public void onPositive(String tag, String input) {
        if (input.trim().isEmpty()) {
            return;
        }
        
        File newDir = new File(current.getAbsolutePath() + "/" + input);
        if (newDir.mkdir()) {
            fileList.clear();
            fileList.addAll(Arrays.asList(current.listFiles()));
            sortDirectory();
            filePickerAdapter.setNewData(fileList);
        }
    }

    private void showSortMenu() {
        SortDialog sortDialog = new SortDialog();
        sortDialog
                .setTitle(R.string.sort)
                .setNegativeButton(R.string.cancel)
                .setPositiveButton(R.string.ok)
                .setSortOrder(sortOrder)
                .setDarkTheme(isDarkTheme);
        if (getFragmentManager() != null) {
            sortDialog.show(getSupportFragmentManager(), "sort dialog");
        }
    }

    /*
     * Sort Dialog callback
     */
    @Override
    public void onPositiveButtonClick(int sortById, int sortOrderId) {
        sortSelected(sortById, sortOrderId);
    }

    private void sortDirectory() {
        switch (sortOrder) {
            case SortDialog.MOD_TIME_DESCENDING:
                Collections.sort(fileList, new FileComparators.SortFileModTimeDescending());
                sortOrder = SortDialog.MOD_TIME_ASCENDING;
                break;
            case SortDialog.MOD_TIME_ASCENDING:
                Collections.sort(fileList, new FileComparators.SortFileModTimeAscending());
                sortOrder = SortDialog.MOD_TIME_DESCENDING;
                break;
            case SortDialog.SIZE_DESCENDING:
                Collections.sort(fileList, new FileComparators.SortFileSizeDescending());
                sortOrder = SortDialog.SIZE_ASCENDING;
                break;
            case SortDialog.SIZE_ASCENDING:
                Collections.sort(fileList, new FileComparators.SortFileSizeAscending());
                sortOrder = SortDialog.SIZE_DESCENDING;
                break;
            case SortDialog.ALPHA_ASCENDING:
                Collections.sort(fileList, new FileComparators.SortFileAlphaAscending());
                sortOrder = SortDialog.ALPHA_ASCENDING;
                break;
            case SortDialog.ALPHA_DESCENDING:
            default:
                Collections.sort(fileList, new FileComparators.SortFileAlphaDescending());
                sortOrder = SortDialog.ALPHA_DESCENDING;
        }
    }

    private void sortSelected(int sortById, int sortOrderId) {
        switch (sortById) {
            case R.id.radio_sort_name:
                if (sortOrderId == R.id.radio_sort_ascending) {
                    Collections.sort(fileList, new FileComparators.SortFileAlphaAscending());
                    sortOrder = SortDialog.ALPHA_ASCENDING;
                } else {
                    Collections.sort(fileList, new FileComparators.SortFileAlphaDescending());
                    sortOrder = SortDialog.ALPHA_DESCENDING;
                }
                break;
            case R.id.radio_sort_date:
                if (sortOrderId == R.id.radio_sort_ascending) {
                    Collections.sort(fileList, new FileComparators.SortFileModTimeAscending());
                    sortOrder = SortDialog.MOD_TIME_ASCENDING;
                } else {
                    Collections.sort(fileList, new FileComparators.SortFileModTimeDescending());
                    sortOrder = SortDialog.MOD_TIME_DESCENDING;
                }
                break;
            case R.id.radio_sort_size:
                if (sortOrderId == R.id.radio_sort_ascending) {
                    Collections.sort(fileList, new FileComparators.SortFileSizeAscending());
                    sortOrder = SortDialog.SIZE_ASCENDING;
                } else {
                    Collections.sort(fileList, new FileComparators.SortFileSizeDescending());
                    sortOrder = SortDialog.SIZE_DESCENDING;
                }
                break;
        }
        filePickerAdapter.updateData(fileList);

        if (sortOrder > 0) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            sharedPreferences.edit().putInt(SHARED_PREFS_SORT_ORDER, sortOrder).apply();
        }
    }
}
