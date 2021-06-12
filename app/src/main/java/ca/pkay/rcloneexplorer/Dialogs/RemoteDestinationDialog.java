package ca.pkay.rcloneexplorer.Dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import ca.pkay.rcloneexplorer.FileComparators;
import ca.pkay.rcloneexplorer.Items.DirectoryObject;
import ca.pkay.rcloneexplorer.Items.FileItem;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import ca.pkay.rcloneexplorer.RecyclerViewAdapters.FileExplorerRecyclerViewAdapter;
import ca.pkay.rcloneexplorer.util.LargeParcel;
import es.dmoral.toasty.Toasty;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

public class RemoteDestinationDialog extends DialogFragment implements  SwipeRefreshLayout.OnRefreshListener,
                                                                        FileExplorerRecyclerViewAdapter.OnClickListener,
                                                                        InputDialog.OnPositive,
                                                                        GoToDialog.Callbacks {

    public interface OnDestinationSelectedListener {
        void onDestinationSelected(String path);
    }

    private static final String SHARED_PREFS_SORT_ORDER = "ca.pkay.rcexplorer.sort_order";
    private final String SAVED_REMOTE = "ca.pkay.rcexplorer.RemoteDestinationDialog.REMOTE";
    private final String SAVED_IS_DARK_THEME = "ca.pkay.rcexplorer.RemoteDestinationDialog.IS_DARK_THEME";
    private final String SAVED_PATH = "ca.pkay.rcexplorer.RemoteDestinationDialog.PATH";
    private final String SAVED_CONTENT = "ca.pkay.rcexplorer.RemoteDestinationDialog.CONTENT";
    private final String SAVED_PREVIOUS_DIR_TEXT = "ca.pkay.rcexplorer.RemoteDestinationDialog.PREVIOUS_DIR_TEXT";
    private final String SAVED_TITLE = "ca.pkay.rcexplorer.RemoteDestinationDialog.TITLE";
    private Context context;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RemoteItem remote;
    private boolean isDarkTheme;
    private FileExplorerRecyclerViewAdapter recyclerViewAdapter;
    private Rclone rclone;
    private Stack<String> pathStack;
    private DirectoryObject directoryObject;
    private AsyncTask fetchDirectoryTask;
    private AsyncTask newDirTask;
    private int sortOrder;
    private View previousDirView;
    private TextView previousDirLabel;
    private int title;
    private boolean startAtRoot;
    private OnDestinationSelectedListener listener;

    public RemoteDestinationDialog() {
        pathStack = new Stack<>();
        directoryObject = new DirectoryObject();
        isDarkTheme = false;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getParentFragment() != null) {
            listener = (OnDestinationSelectedListener) getParentFragment();
        }

        if (savedInstanceState != null) {
            remote = savedInstanceState.getParcelable(SAVED_REMOTE);
            isDarkTheme = savedInstanceState.getBoolean(SAVED_IS_DARK_THEME, false);
            directoryObject.setPath(savedInstanceState.getString(SAVED_PATH));
            if (savedInstanceState.containsKey(SAVED_CONTENT)) {
                directoryObject.setContent(savedInstanceState.<FileItem>getParcelableArrayList(SAVED_CONTENT));
            }
            buildStackFromPath(remote.getName(), directoryObject.getCurrentPath());
            title = savedInstanceState.getInt(SAVED_TITLE);
        } else {
            String path = "//" + remote.getName();
            directoryObject.setPath(path);
        }

        rclone = new Rclone(context);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sortOrder = sharedPreferences.getInt(SHARED_PREFS_SORT_ORDER, SortDialog.ALPHA_ASCENDING);
        boolean goToDefaultSet = sharedPreferences.getBoolean(getString(R.string.pref_key_go_to_default_set), false);

        if (goToDefaultSet) {
            startAtRoot = sharedPreferences.getBoolean(getString(R.string.pref_key_start_at_root), false);
        }

        LayoutInflater layoutInflater = ((FragmentActivity)context).getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_remote_dest, null);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setItemAnimator(new LandingAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        View emptyFolderView = view.findViewById(R.id.empty_folder_view);
        View noSearchResultsView = view.findViewById(R.id.no_search_results_view);
        recyclerViewAdapter = new FileExplorerRecyclerViewAdapter(context, emptyFolderView, noSearchResultsView, this);
        recyclerViewAdapter.disableFileOptions();
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setMoveMode(true);
        recyclerViewAdapter.setCanSelect(false);

        if (remote.isRemoteType(RemoteItem.SFTP) && !goToDefaultSet & savedInstanceState == null) {
            showSFTPgoToDialog();
        } else {
            if (directoryObject.isDirectoryContentEmpty()) {
                fetchDirectoryTask = new FetchDirectoryContent().execute();
                swipeRefreshLayout.setRefreshing(true);
            } else {
                recyclerViewAdapter.newData(directoryObject.getDirectoryContent());
            }
        }

        final TypedValue accentColorValue = new TypedValue ();
        context.getTheme().resolveAttribute (R.attr.colorAccent, accentColorValue, true);
        view.findViewById(R.id.move_bar).setBackgroundColor(accentColorValue.data);
        view.findViewById(R.id.move_bar).setVisibility(View.VISIBLE);
        view.findViewById(R.id.cancel_move).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.select_move).setOnClickListener(v -> {
            listener.onDestinationSelected(directoryObject.getCurrentPath());
            dismiss();
        });
        view.findViewById(R.id.new_folder).setOnClickListener(v -> onCreateNewDirectory());
        view.findViewById(R.id.previous_dir).setOnClickListener(v -> goUp());
        previousDirView = view.findViewById(R.id.previous_dir);
        previousDirView.setVisibility(View.GONE);
        previousDirLabel = view.findViewById(R.id.previous_dir_label);

        if (savedInstanceState != null) {
            String restoredPreviousDirLabel = savedInstanceState.getString(SAVED_PREVIOUS_DIR_TEXT);
            if (restoredPreviousDirLabel != null && !restoredPreviousDirLabel.trim().isEmpty()) {
                previousDirLabel.setText(restoredPreviousDirLabel);
                previousDirView.setVisibility(View.VISIBLE);
            }
        }

        ((TextView)view.findViewById(R.id.dialog_title)).setText(title);

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogThemeFullScreen);
        builder.setView(view);
        builder.setOnKeyListener((dialog, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                goUp();
                return true;
            }
            return false;
        });
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVED_REMOTE, remote);
        outState.putBoolean(SAVED_IS_DARK_THEME, isDarkTheme);
        outState.putString(SAVED_PATH, directoryObject.getCurrentPath());
        ArrayList<FileItem> content = new ArrayList<>(directoryObject.getDirectoryContent());
        outState.putParcelableArrayList(SAVED_CONTENT, content);
        outState.putString(SAVED_PREVIOUS_DIR_TEXT, previousDirLabel.getText().toString());
        outState.putInt(SAVED_TITLE, title);
        if (LargeParcel.calculateBundleSize(outState) > 250 * 1024) {
            outState.remove(SAVED_CONTENT);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;

        if (context instanceof OnDestinationSelectedListener) {
            listener = (OnDestinationSelectedListener) context;
        }
    }

    private void buildStackFromPath(String remote, String path) {
        String root = "//" + remote;
        if (root.equals(path)) {
            return;
        }
        pathStack.push(root);

        int index = 0;

        while ((index = path.indexOf("/", index)) > 0) {
            String p = path.substring(0, index);
            pathStack.push(p);
            index++;
        }
    }

    public RemoteDestinationDialog setTitle(int title) {
        this.title = title;
        return this;
    }

    public RemoteDestinationDialog setRemote(RemoteItem remote) {
        this.remote = remote;
        return this;
    }

    public RemoteDestinationDialog setDarkTheme(boolean isDarkTheme) {
        this.isDarkTheme = isDarkTheme;
        return this;
    }

    @Override
    public void onRefresh() {
        if (fetchDirectoryTask != null) {
            fetchDirectoryTask.cancel(true);
        }
        fetchDirectoryTask = new FetchDirectoryContent().execute();
    }

    @Override
    public void onFileClicked(FileItem fileItem) {
        // don't do anything
    }

    @Override
    public void onDirectoryClicked(FileItem fileItem, int position) {
        swipeRefreshLayout.setRefreshing(true);
        pathStack.push(directoryObject.getCurrentPath());
        String prev = directoryObject.getCurrentPath();
        if (prev.startsWith("//")) {
            prev = prev.substring(2);
        }
        previousDirView.setVisibility(View.VISIBLE);
        previousDirLabel.setText(prev);

        if (fetchDirectoryTask != null) {
            fetchDirectoryTask.cancel(true);
        }
        if (directoryObject.isPathInCache(fileItem.getPath()) && directoryObject.isContentValid(fileItem.getPath())) {
            String path = fileItem.getPath();
            directoryObject.restoreFromCache(path);
            sortDirectory();
            recyclerViewAdapter.newData(directoryObject.getDirectoryContent());
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        directoryObject.setPath(fileItem.getPath());
        recyclerViewAdapter.clear();
        fetchDirectoryTask = new FetchDirectoryContent().execute();
    }

    @Override
    public void onFilesSelected() {
        // don't do anything
    }

    @Override
    public void onFileDeselected() {
        // don't do anything
    }

    @Override
    public void onFileOptionsClicked(View view, FileItem fileItem) {
        // don't do anything
    }

    @Override
    public String[] getThumbnailServerParams() {
        return new String[0];
    }

    private void onCreateNewDirectory() {
        if (getFragmentManager() != null) {
            new InputDialog()
                    .setTitle(R.string.create_new_folder)
                    .setMessage(R.string.type_new_folder_name)
                    .setNegativeButton(R.string.cancel)
                    .setPositiveButton(R.string.okay_confirmation)
                    .setDarkTheme(isDarkTheme)
                    .show(getChildFragmentManager(), "input dialog");
        }
    }

    /*
     * Input Dialog callback
     */
    @Override
    public void onPositive(String tag, String input) {
        if (input.trim().length() == 0) {
            return;
        }
        String newDir;
        if (directoryObject.getCurrentPath().equals("//" + remote.getName())) {
            newDir = input;
        } else {
            newDir = directoryObject.getCurrentPath() + "/" + input;
        }
        newDirTask = new MakeDirectoryTask().execute(newDir);
    }

    /*
     * Go To Dialog Callback
     */
    @Override
    public void onRootClicked(boolean isSetAsDefault) {
        startAtRoot = true;
        directoryObject.clear();
        String path = "//" + remote.getName();
        directoryObject.setPath(path);
        swipeRefreshLayout.setRefreshing(true);
        fetchDirectoryTask = new FetchDirectoryContent().execute();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (isSetAsDefault) {
            editor.putBoolean(getString(R.string.pref_key_go_to_default_set), true);
            editor.putBoolean(getString(R.string.pref_key_start_at_root), true);
        } else {
            editor.putBoolean(getString(R.string.pref_key_go_to_default_set), false);
        }
        editor.apply();
    }

    /*
     * Go To Dialog Callback
     */
    @Override
    public void onHomeClicked(boolean isSetAsDefault) {
        startAtRoot = false;
        directoryObject.clear();
        String path = "//" + remote.getName();
        directoryObject.setPath(path);
        swipeRefreshLayout.setRefreshing(true);
        fetchDirectoryTask = new FetchDirectoryContent().execute();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (isSetAsDefault) {
            editor.putBoolean(getString(R.string.pref_key_go_to_default_set), true);
            editor.putBoolean(getString(R.string.pref_key_start_at_root), false);
        } else {
            editor.putBoolean(getString(R.string.pref_key_go_to_default_set), false);
        }
        editor.apply();
    }

    private void showSFTPgoToDialog() {
        GoToDialog goToDialog = new GoToDialog()
                .isDarkTheme(isDarkTheme);
        goToDialog.show(getChildFragmentManager(), "go to dialog");
    }

    private void goUp() {
        if (pathStack.isEmpty()) {
            dismiss();
            return;
        }
        swipeRefreshLayout.setRefreshing(false);
        if (fetchDirectoryTask != null) {
            fetchDirectoryTask.cancel(true);
        }
        String path = pathStack.pop();
        recyclerViewAdapter.clear();

        if (!pathStack.isEmpty()) {
            String prev = pathStack.peek();
            if (prev.startsWith("//")) {
                prev = prev.substring(2);
            }
            previousDirView.setVisibility(View.VISIBLE);
            previousDirLabel.setText(prev);
        } else {
            previousDirView.setVisibility(View.GONE);
        }

        if (fetchDirectoryTask != null) {
            fetchDirectoryTask.cancel(true);
        }

        if (directoryObject.isPathInCache(path) && directoryObject.isContentValid(path)) {
            directoryObject.restoreFromCache(path);
            sortDirectory();
            recyclerViewAdapter.newData(directoryObject.getDirectoryContent());
        } else {
            directoryObject.setPath(path);
            fetchDirectoryTask = new FetchDirectoryContent().execute();
        }
    }

    private void sortDirectory() {
        List<FileItem> directoryContent = directoryObject.getDirectoryContent();
        switch (sortOrder) {
            case SortDialog.MOD_TIME_DESCENDING:
                Collections.sort(directoryContent, new FileComparators.SortModTimeDescending());
                break;
            case SortDialog.MOD_TIME_ASCENDING:
                Collections.sort(directoryContent, new FileComparators.SortModTimeAscending());
                break;
            case SortDialog.SIZE_DESCENDING:
                Collections.sort(directoryContent, new FileComparators.SortSizeDescending());
                break;
            case SortDialog.SIZE_ASCENDING:
                Collections.sort(directoryContent, new FileComparators.SortSizeAscending());
                break;
            case SortDialog.ALPHA_ASCENDING:
                Collections.sort(directoryContent, new FileComparators.SortAlphaAscending());
                break;
            case SortDialog.ALPHA_DESCENDING:
            default:
                Collections.sort(directoryContent, new FileComparators.SortAlphaDescending());
        }
        directoryObject.setContent(directoryContent);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (fetchDirectoryTask != null) {
            fetchDirectoryTask.cancel(true);
        }
        if (newDirTask != null) {
            newDirTask.cancel(true);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class FetchDirectoryContent extends AsyncTask<Void, Void, List<FileItem>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (null != swipeRefreshLayout) {
                swipeRefreshLayout.setRefreshing(true);
            }
        }

        @Override
        protected List<FileItem> doInBackground(Void... voids) {
            List<FileItem> fileItemList;
            fileItemList = rclone.getDirectoryContent(remote, directoryObject.getCurrentPath(), startAtRoot);
            return fileItemList;
        }

        @Override
        protected void onPostExecute(List<FileItem> fileItems) {
            super.onPostExecute(fileItems);
            if (fileItems == null) {
                Toasty.error(context, getString(R.string.error_getting_dir_content), Toast.LENGTH_SHORT, true).show();
                fileItems = new ArrayList<>();
            }
            directoryObject.setContent(fileItems);
            sortDirectory();

            if (recyclerViewAdapter != null) {
                recyclerViewAdapter.newData(directoryObject.getDirectoryContent());
            }

            if (null != swipeRefreshLayout) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class MakeDirectoryTask extends AsyncTask<String, Void, Boolean> {

        private String pathWhenTaskStarted;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pathWhenTaskStarted = directoryObject.getCurrentPath();
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            String newDir = strings[0];
            return rclone.makeDirectory(remote, newDir);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                Toasty.success(context, getString(R.string.make_directory_success), Toast.LENGTH_SHORT, true).show();
            } else {
                Toasty.error(context, getString(R.string.error_mkdir), Toast.LENGTH_SHORT, true).show();
            }
            if (!pathWhenTaskStarted.equals(directoryObject.getCurrentPath())) {
                directoryObject.removePathFromCache(pathWhenTaskStarted);
                return;
            }
            swipeRefreshLayout.setRefreshing(false);
            if (fetchDirectoryTask != null) {
                fetchDirectoryTask.cancel(true);
            }
            fetchDirectoryTask = new FetchDirectoryContent().execute();
        }
    }
}
