package ca.pkay.rcloneexplorer.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import ca.pkay.rcloneexplorer.BreadcrumbView;
import ca.pkay.rcloneexplorer.Dialogs.InputDialog;
import ca.pkay.rcloneexplorer.Dialogs.SortDialog;
import ca.pkay.rcloneexplorer.FileComparators;
import ca.pkay.rcloneexplorer.Items.DirectoryObject;
import ca.pkay.rcloneexplorer.Items.FileItem;
import ca.pkay.rcloneexplorer.MainActivity;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import ca.pkay.rcloneexplorer.RecyclerViewAdapters.FileExplorerRecyclerViewAdapter;
import es.dmoral.toasty.Toasty;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

public class ShareFragment extends Fragment implements  SwipeRefreshLayout.OnRefreshListener,
                                                        FileExplorerRecyclerViewAdapter.OnClickListener,
                                                        BreadcrumbView.OnClickListener {

    public interface onShareDestincationSelected {
        void onShareDestinationSelected(String remote, String path);
    }

    private static final String ARG_REMOTE = "remote_param";
    private static final String ARG_REMOTE_TYPE = "remote_type_param";
    private static final String SHARED_PREFS_SORT_ORDER = "ca.pkay.rcexplorer.sort_order";
    private onShareDestincationSelected listener;
    private Context context;
    private String remote;
    private String remoteType;
    private String originalToolbarTitle;
    private int sortOrder;
    private Rclone rclone;
    private Stack<String> pathStack;
    private DirectoryObject directoryObject;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AsyncTask fetchDirectoryTask;
    private boolean isDarkTheme;
    private FileExplorerRecyclerViewAdapter recyclerViewAdapter;
    private BreadcrumbView breadcrumbView;
    private Boolean isRunning;

    public ShareFragment() {
    }

    public static ShareFragment newInstance(String remote, String remoteType) {
        ShareFragment fragment = new ShareFragment();
        Bundle args = new Bundle();
        args.putString(ARG_REMOTE, remote);
        args.putString(ARG_REMOTE_TYPE, remoteType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) {
            return;
        }
        remote = getArguments().getString(ARG_REMOTE);
        remoteType = getArguments().getString(ARG_REMOTE_TYPE);
        String path = "//" + getArguments().getString(ARG_REMOTE);

        if (getContext() == null) {
            return;
        } else {
            context = getContext();
        }

        originalToolbarTitle = ((FragmentActivity) context).getTitle().toString();
        ((FragmentActivity) context).setTitle(remoteType);
        setHasOptionsMenu(true);

        SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARED_PREFS_TAG, Context.MODE_PRIVATE);
        sortOrder = sharedPreferences.getInt(SHARED_PREFS_SORT_ORDER, SortDialog.ALPHA_ASCENDING);

        rclone = new Rclone(getContext());
        pathStack = new Stack<>();
        directoryObject = new DirectoryObject();
        directoryObject.setPath(path);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_share_list, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);
        fetchDirectoryTask = new FetchDirectoryContent().execute();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        isDarkTheme = sharedPreferences.getBoolean(getString(R.string.pref_key_dark_theme), false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setItemAnimator(new LandingAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        View emptyFolderView = view.findViewById(R.id.empty_folder_view);
        View noSearchResultsView = view.findViewById(R.id.no_search_results_view);
        recyclerViewAdapter = new FileExplorerRecyclerViewAdapter(context, emptyFolderView, noSearchResultsView, this);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setMoveMode(true);
        recyclerViewAdapter.setCanSelect(false);

        breadcrumbView = ((FragmentActivity)context).findViewById(R.id.breadcrumb_view);
        breadcrumbView.setOnClickListener(this);
        breadcrumbView.setVisibility(View.VISIBLE);
        breadcrumbView.addCrumb(remote, "//" + remote);

        final TypedValue accentColorValue = new TypedValue ();
        context.getTheme().resolveAttribute (R.attr.colorAccent, accentColorValue, true);
        view.findViewById(R.id.move_bar).setBackgroundColor(accentColorValue.data);
        view.findViewById(R.id.move_bar).setVisibility(View.VISIBLE);
        view.findViewById(R.id.cancel_move).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((FragmentActivity) context).finish();
            }
        });
        view.findViewById(R.id.select_move).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onShareDestinationSelected(remote, directoryObject.getCurrentPath());
            }
        });
        view.findViewById(R.id.new_folder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCreateNewDirectory();
            }
        });

        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            ((FragmentActivity)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
        }
        else {
            ((FragmentActivity)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
        }

        isRunning = true;
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_share_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_sort:
                showSortMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof onShareDestincationSelected) {
            listener = (onShareDestincationSelected) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnShareDestinationSelected");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fetchDirectoryTask.cancel(true);
        breadcrumbView.clearCrumbs();
        breadcrumbView.setVisibility(View.GONE);
        ((FragmentActivity) context).setTitle(originalToolbarTitle);
        isRunning = false;
        context = null;
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
        // Don't do anything
    }

    @Override
    public void onDirectoryClicked(FileItem fileItem) {
        breadcrumbView.addCrumb(fileItem.getName(), fileItem.getPath());
        swipeRefreshLayout.setRefreshing(true);
        pathStack.push(directoryObject.getCurrentPath());

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
        // Don't do anything
    }

    @Override
    public void onFileDeselected() {
        // Don't do anything
    }

    @Override
    public void onBreadCrumbClicked(String path) {
        if (directoryObject.getCurrentPath().equals(path)) {
            return;
        }
        swipeRefreshLayout.setRefreshing(false);
        if (fetchDirectoryTask != null) {
            fetchDirectoryTask.cancel(true);
        }
        directoryObject.setPath(path);
        //noinspection StatementWithEmptyBody
        while (!pathStack.pop().equals(path)) {
            // pop stack until we find path
        }
        breadcrumbView.removeCrumbsUpTo(path);
        recyclerViewAdapter.clear();
        if (directoryObject.isPathInCache(path) && directoryObject.isContentValid(path)) {
            directoryObject.restoreFromCache(path);
            sortDirectory();
            recyclerViewAdapter.newData(directoryObject.getDirectoryContent());
        } else {
            fetchDirectoryTask = new FetchDirectoryContent().execute();
        }
    }

    private void showSortMenu() {
        SortDialog sortDialog = new SortDialog();
        sortDialog.withContext(context)
                .setTitle(R.string.sort)
                .setNegativeButton(R.string.cancel)
                .setPositiveButton(R.string.ok)
                .setListener(new SortDialog.OnClickListener() {
                    @Override
                    public void onPositiveButtonClick(int sortById, int sortOrderId) {
                        if (!directoryObject.isDirectoryContentEmpty()) {
                            sortSelected(sortById, sortOrderId);
                        }
                    }
                })
                .setSortOrder(sortOrder)
                .setDarkTheme(isDarkTheme);
        if (getFragmentManager() != null) {
            sortDialog.show(getFragmentManager(), "sort dialog");
        }
    }

    private void sortSelected(int sortById, int sortOrderId) {
        List<FileItem> directoryContent = directoryObject.getDirectoryContent();
        switch (sortById) {
            case R.id.radio_sort_name:
                if (sortOrderId == R.id.radio_sort_ascending) {
                    Collections.sort(directoryContent, new FileComparators.SortAlphaAscending());
                    sortOrder = SortDialog.ALPHA_ASCENDING;
                } else {
                    Collections.sort(directoryContent, new FileComparators.SortAlphaDescending());
                    sortOrder = SortDialog.ALPHA_DESCENDING;
                }
                break;
            case R.id.radio_sort_date:
                if (sortOrderId == R.id.radio_sort_ascending) {
                    Collections.sort(directoryContent, new FileComparators.SortModTimeAscending());
                    sortOrder = SortDialog.MOD_TIME_ASCENDING;
                } else {
                    Collections.sort(directoryContent, new FileComparators.SortModTimeDescending());
                    sortOrder = SortDialog.MOD_TIME_DESCENDING;
                }
                break;
            case R.id.radio_sort_size:
                if (sortOrderId == R.id.radio_sort_ascending) {
                    Collections.sort(directoryContent, new FileComparators.SortSizeAscending());
                    sortOrder = SortDialog.SIZE_ASCENDING;
                } else {
                    Collections.sort(directoryContent, new FileComparators.SortSizeDescending());
                    sortOrder = SortDialog.SIZE_DESCENDING;
                }
                break;
        }
        directoryObject.setContent(directoryContent);
        recyclerViewAdapter.updateSortedData(directoryContent);
        if (sortOrder > 0) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.SHARED_PREFS_TAG, Context.MODE_PRIVATE);
            sharedPreferences.edit().putInt(SHARED_PREFS_SORT_ORDER, sortOrder).apply();
        }
    }

    private void sortDirectory() {
        List<FileItem> directoryContent = directoryObject.getDirectoryContent();
        switch (sortOrder) {
            case SortDialog.MOD_TIME_DESCENDING:
                Collections.sort(directoryContent, new FileComparators.SortModTimeDescending());
                sortOrder = SortDialog.MOD_TIME_ASCENDING;
                break;
            case SortDialog.MOD_TIME_ASCENDING:
                Collections.sort(directoryContent, new FileComparators.SortModTimeAscending());
                sortOrder = SortDialog.MOD_TIME_DESCENDING;
                break;
            case SortDialog.SIZE_DESCENDING:
                Collections.sort(directoryContent, new FileComparators.SortSizeDescending());
                sortOrder = SortDialog.SIZE_ASCENDING;
                break;
            case SortDialog.SIZE_ASCENDING:
                Collections.sort(directoryContent, new FileComparators.SortSizeAscending());
                sortOrder = SortDialog.SIZE_DESCENDING;
                break;
            case SortDialog.ALPHA_ASCENDING:
                Collections.sort(directoryContent, new FileComparators.SortAlphaAscending());
                sortOrder = SortDialog.ALPHA_ASCENDING;
                break;
            case SortDialog.ALPHA_DESCENDING:
            default:
                Collections.sort(directoryContent, new FileComparators.SortAlphaDescending());
                sortOrder = SortDialog.ALPHA_DESCENDING;
        }
        directoryObject.setContent(directoryContent);
    }

    public boolean onBackButtonPressed() {
        if (pathStack.isEmpty()) {
            return false;
        }
        swipeRefreshLayout.setRefreshing(false);
        fetchDirectoryTask.cancel(true);
        breadcrumbView.removeLastCrumb();
        String path = pathStack.pop();
        recyclerViewAdapter.clear();

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
        return true;
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
                            new MakeDirectoryTask().execute(newDir);
                        }
                    })
                    .show(getFragmentManager(), "input dialog");
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

        @Override
        protected void onCancelled() {
            super.onCancelled();
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
            if (!isRunning) {
                return;
            }
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
            if (null != fetchDirectoryTask) {
                fetchDirectoryTask.cancel(true);
            }
            fetchDirectoryTask = new FetchDirectoryContent().execute();
        }
    }
}
