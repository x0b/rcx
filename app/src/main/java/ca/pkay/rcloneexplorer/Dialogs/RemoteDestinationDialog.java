package ca.pkay.rcloneexplorer.Dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import ca.pkay.rcloneexplorer.MainActivity;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import ca.pkay.rcloneexplorer.RecyclerViewAdapters.FileExplorerRecyclerViewAdapter;
import es.dmoral.toasty.Toasty;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

public class RemoteDestinationDialog extends DialogFragment implements  SwipeRefreshLayout.OnRefreshListener,
                                                                        FileExplorerRecyclerViewAdapter.OnClickListener {

    public interface OnDestinationSelectedListener {
        void onDestinationSelected(String path);
    }

    private static final String SHARED_PREFS_SORT_ORDER = "ca.pkay.rcexplorer.sort_order";
    private Context context;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String remote;
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
    private OnDestinationSelectedListener listener;

    public RemoteDestinationDialog() {
        pathStack = new Stack<>();
        directoryObject = new DirectoryObject();
        isDarkTheme = false;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        rclone = new Rclone(context);
        String path = "//" + remote;
        directoryObject.setPath(path);
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARED_PREFS_TAG, Context.MODE_PRIVATE);
        sortOrder = sharedPreferences.getInt(SHARED_PREFS_SORT_ORDER, SortDialog.ALPHA_ASCENDING);

        LayoutInflater layoutInflater = ((FragmentActivity)context).getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_remote_dest, null);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);
        fetchDirectoryTask = new FetchDirectoryContent().execute();

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setItemAnimator(new LandingAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        View emptyFolderView = view.findViewById(R.id.empty_folder_view);
        View noSearchResultsView = view.findViewById(R.id.no_search_results_view);
        recyclerViewAdapter = new FileExplorerRecyclerViewAdapter(context, emptyFolderView, noSearchResultsView, this);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setMoveMode(true);
        recyclerViewAdapter.setCanSelect(false);

        final TypedValue accentColorValue = new TypedValue ();
        context.getTheme().resolveAttribute (R.attr.colorAccent, accentColorValue, true);
        view.findViewById(R.id.move_bar).setBackgroundColor(accentColorValue.data);
        view.findViewById(R.id.move_bar).setVisibility(View.VISIBLE);
        view.findViewById(R.id.cancel_move).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        view.findViewById(R.id.select_move).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDestinationSelected(directoryObject.getCurrentPath());
                dismiss();
            }
        });
        view.findViewById(R.id.new_folder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCreateNewDirectory();
            }
        });
        view.findViewById(R.id.previous_dir).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goUp();
            }
        });
        previousDirView = view.findViewById(R.id.previous_dir);
        previousDirView.setVisibility(View.GONE);
        previousDirLabel = view.findViewById(R.id.previous_dir_label);

        ((TextView)view.findViewById(R.id.dialog_title)).setText(title);

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogThemeFullScreen);
        builder.setView(view);
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    goUp();
                    return true;
                }
                return false;
            }
        });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        lockOrientation();
    }

    public RemoteDestinationDialog withContext(Context context) {
        this.context = context;
        return this;
    }

    public RemoteDestinationDialog setTitle(int title) {
        this.title = title;
        return this;
    }

    public RemoteDestinationDialog setRemote(String remote) {
        this.remote = remote;
        return this;
    }

    public RemoteDestinationDialog setDarkTheme(boolean isDarkTheme) {
        this.isDarkTheme = isDarkTheme;
        return this;
    }

    public RemoteDestinationDialog setPositiveButtonListener(OnDestinationSelectedListener listener) {
        this.listener = listener;
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
    public void onDirectoryClicked(FileItem fileItem) {
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

    private void onCreateNewDirectory() {
        if (getFragmentManager() != null) {
            new InputDialog()
                    .setContext(context)
                    .setTitle(R.string.create_new_folder)
                    .setMessage(R.string.type_new_folder_name)
                    .setNegativeButton(R.string.cancel)
                    .setPositiveButton(R.string.okay_confirmation)
                    .setDarkTheme(isDarkTheme)
                    .setOnPositiveListener(new InputDialog.OnPositive() {
                        @Override
                        public void onPositive(String input) {
                            if (input.trim().length() == 0) {
                                return;
                            }
                            String newDir;
                            if (directoryObject.getCurrentPath().equals("//" + remote)) {
                                newDir = input;
                            } else {
                                newDir = directoryObject.getCurrentPath() + "/" + input;
                            }
                            newDirTask = new MakeDirectoryTask().execute(newDir);
                        }
                    })
                    .show(getFragmentManager(), "input dialog");
        }
    }

    private void goUp() {
        if (pathStack.isEmpty()) {
            dismiss();
            return;
        }
        swipeRefreshLayout.setRefreshing(false);
        fetchDirectoryTask.cancel(true);
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

    private void lockOrientation() {
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            ((FragmentActivity)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
        }
        else {
            ((FragmentActivity)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        }
    }

    private void unlockOrientation() {
        ((FragmentActivity)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        unlockOrientation();
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
            fileItemList = rclone.getDirectoryContent(remote, directoryObject.getCurrentPath());
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
