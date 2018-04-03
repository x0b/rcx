package ca.pkay.rcloneexplorer.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.shehabic.droppy.DroppyClickCallbackInterface;
import com.shehabic.droppy.DroppyMenuPopup;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import ca.pkay.rcloneexplorer.BreadcrumbView;
import ca.pkay.rcloneexplorer.FileComparators;
import ca.pkay.rcloneexplorer.Items.FileItem;
import ca.pkay.rcloneexplorer.MainActivity;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import ca.pkay.rcloneexplorer.RecyclerViewAdapters.FileExplorerRecyclerViewAdapter;
import ca.pkay.rcloneexplorer.Services.DownloadService;
import yogesh.firzen.filelister.FileListerDialog;
import yogesh.firzen.filelister.OnFileSelectedListener;

public class FileExplorerFragment extends Fragment implements   FileExplorerRecyclerViewAdapter.OnClickListener,
                                                                SwipeRefreshLayout.OnRefreshListener,
                                                                BreadcrumbView.OnClickListener {

    private static final String ARG_REMOTE = "remote_param";
    private static final String ARG_REMOTE_TYPE = "remote_type_param";
    private static final String SHARED_PREFS_SORT_ORDER = "ca.pkay.rcexplorer.sort_order";
    private String originalToolbarTitle;
    private OnFileClickListener listener;
    private List<FileItem> directoryContent;
    private Stack<String> pathStack;
    private Map<String, List<FileItem>> directoryCache;
    private List<FileItem> moveList;
    private BreadcrumbView breadcrumbView;
    private Rclone rclone;
    private String remote;
    private String remoteType;
    private String path;
    private FileExplorerRecyclerViewAdapter recyclerViewAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AsyncTask fetchDirectoryTask;
    private Boolean isRunning;
    private SortOrder sortOrder;
    private Boolean isInMoveMode;

    private enum SortOrder {
        AlphaDescending(1),
        AlphaAscending(2),
        ModTimeDescending(3),
        ModTimeAscending(4),
        SizeDescending(5),
        SizeAscending(6);

        private final int value;

        SortOrder(int value) { this.value = value; }
        public int getValue() { return this.value; }
        public static SortOrder fromInt(int n) {
            switch (n) {
                case 1: return AlphaDescending;
                case 2: return AlphaAscending;
                case 3: return ModTimeDescending;
                case 4: return ModTimeAscending;
                case 5: return SizeDescending;
                case 6: return SizeAscending;
                default: return AlphaDescending;
            }
        }
    }

    public interface OnFileClickListener {
        void onFileClicked(FileItem file);
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FileExplorerFragment() {
    }

    @SuppressWarnings("unused")
    public static FileExplorerFragment newInstance(String remote, String remoteType) {
        FileExplorerFragment fragment = new FileExplorerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_REMOTE, remote);
        args.putString(ARG_REMOTE_TYPE, remoteType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            remote = getArguments().getString(ARG_REMOTE);
            remoteType = getArguments().getString(ARG_REMOTE_TYPE);
            path = "//" + getArguments().getString(ARG_REMOTE);
        }
        originalToolbarTitle = getActivity().getTitle().toString();
        getActivity().setTitle(remoteType);
        setHasOptionsMenu(true);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(MainActivity.SHARED_PREFS_TAG, Context.MODE_PRIVATE);
        sortOrder = SortOrder.fromInt(sharedPreferences.getInt(SHARED_PREFS_SORT_ORDER, -1));

        rclone = new Rclone(getContext());
        pathStack = new Stack<>();
        directoryCache = new HashMap<>();
        fetchDirectoryTask = new FetchDirectoryContent().execute();

        isInMoveMode = false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_explorer_list, container, false);

        swipeRefreshLayout = view.findViewById(R.id.file_explorer_srl);
        swipeRefreshLayout.setOnRefreshListener(this);
        if (null != directoryContent && null != fetchDirectoryTask) {
            swipeRefreshLayout.setRefreshing(false);
        } else {
            swipeRefreshLayout.setRefreshing(true);
        }

        Context context = view.getContext();
        RecyclerView recyclerView = view.findViewById(R.id.file_explorer_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerViewAdapter = new FileExplorerRecyclerViewAdapter(directoryContent, this);
        recyclerView.setAdapter(recyclerViewAdapter);

        breadcrumbView = getActivity().findViewById(R.id.breadcrumb_view);
        breadcrumbView.setOnClickListener(this);
        breadcrumbView.setVisibility(View.VISIBLE);
        breadcrumbView.addCrumb(remote, "//" + remote);

        setBottomBarClickListeners(view);

        isRunning = true;
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.file_explorer, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sort) {
            showSortMenu();
            return true;
        }
        if (id == R.id.action_select_all) {
            recyclerViewAdapter.toggleSelectAll();
        }

        return super.onOptionsItemSelected(item);
    }

    /*
     * Swipe to refresh
     */
    @Override
    public void onRefresh() {
        if (null != fetchDirectoryTask) {
            fetchDirectoryTask.cancel(true);
        }
        fetchDirectoryTask = new FetchDirectoryContent().execute();
    }

    private void setBottomBarClickListeners(final View view) {
        view.findViewById(R.id.file_download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDownloadClicked();
            }
        });

        view.findViewById(R.id.file_move).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMoveClicked();
            }
        });

        view.findViewById(R.id.file_rename).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRenameClicked();
            }
        });

        view.findViewById(R.id.file_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDeleteClicked();
            }
        });

        view.findViewById(R.id.cancel_move).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().setTitle(remoteType);
                recyclerViewAdapter.setMoveMode(false);
                isInMoveMode = false;
                view.findViewById(R.id.move_bar).setVisibility(View.GONE);
                getActivity().findViewById(R.id.action_select_all).setVisibility(View.VISIBLE);
                recyclerViewAdapter.refreshData();
            }
        });

        view.findViewById(R.id.select_move).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().setTitle(remoteType);
                view.findViewById(R.id.move_bar).setVisibility(View.GONE);
                getActivity().findViewById(R.id.action_select_all).setVisibility(View.VISIBLE);
                recyclerViewAdapter.setMoveMode(false);
                isInMoveMode = false;
                String oldPath = moveList.get(0).getPath();
                int index = oldPath.lastIndexOf(moveList.get(0).getName());
                directoryCache.remove(moveList.get(0).getPath().substring(0, index - 1));
                new MoveTask().execute();
            }
        });

        view.findViewById(R.id.new_folder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCreateNewDirectory();
            }
        });
    }

    private void showSortMenu() {
        DroppyMenuPopup droppyMenu;
        DroppyMenuPopup.Builder droppyBuilder = new DroppyMenuPopup.Builder(getContext(), getActivity().findViewById(R.id.action_sort));
        droppyMenu = droppyBuilder.fromMenu(R.menu.sort_menu)
                .triggerOnAnchorClick(false)
                .setXOffset(5)
                .setYOffset(5)
                .setOnClick(new DroppyClickCallbackInterface() {
                    @Override
                    public void call(View v, int id) {
                        sortDirectory(id);
                    }
                })
                .build();
        droppyMenu.show();
    }

    private void sortDirectory(int id) {
        switch (id) {
            case R.id.sort_alpha:
                if (sortOrder == SortOrder.AlphaDescending) {
                    Collections.sort(directoryContent, new FileComparators.SortAlphaAscending());
                    sortOrder = SortOrder.AlphaAscending;
                } else {
                    Collections.sort(directoryContent, new FileComparators.SortAlphaDescending());
                    sortOrder = SortOrder.AlphaDescending;
                }
                break;
            case R.id.sort_date:
                if (sortOrder == SortOrder.ModTimeDescending) {
                    Collections.sort(directoryContent, new FileComparators.SortModTimeAscending());
                    sortOrder = SortOrder.ModTimeAscending;
                } else {
                    Collections.sort(directoryContent, new FileComparators.SortModTimeDescending());
                    sortOrder = SortOrder.ModTimeDescending;
                }
                break;
            case R.id.sort_size:
                if (sortOrder == SortOrder.SizeDescending) {
                    Collections.sort(directoryContent, new FileComparators.SortSizeAscending());
                    sortOrder = SortOrder.SizeAscending;
                } else {
                    Collections.sort(directoryContent, new FileComparators.SortSizeDescending());
                    sortOrder = SortOrder.SizeDescending;
                }
                break;
        }
        recyclerViewAdapter.updateData(directoryContent);
        if (null != sortOrder) {
            SharedPreferences sharedPreferences = getContext().getSharedPreferences(MainActivity.SHARED_PREFS_TAG, Context.MODE_PRIVATE);
            sharedPreferences.edit().putInt(SHARED_PREFS_SORT_ORDER, sortOrder.getValue()).apply();
        }
    }

    private void sortDirectory() {
        switch (sortOrder) {
            case ModTimeDescending:
                Collections.sort(directoryContent, new FileComparators.SortModTimeDescending());
                sortOrder = SortOrder.ModTimeAscending;
                break;
            case ModTimeAscending:
                Collections.sort(directoryContent, new FileComparators.SortModTimeAscending());
                sortOrder = SortOrder.ModTimeDescending;
                break;
            case SizeDescending:
                Collections.sort(directoryContent, new FileComparators.SortSizeDescending());
                sortOrder = SortOrder.SizeAscending;
                break;
            case SizeAscending:
                Collections.sort(directoryContent, new FileComparators.SortSizeAscending());
                sortOrder = SortOrder.SizeDescending;
                break;
            case AlphaAscending:
                Collections.sort(directoryContent, new FileComparators.SortAlphaAscending());
                sortOrder = SortOrder.AlphaAscending;
                break;
            case AlphaDescending:
            default:
                Collections.sort(directoryContent, new FileComparators.SortAlphaDescending());
                sortOrder = SortOrder.AlphaDescending;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFileClickListener) {
            listener = (OnFileClickListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFileClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fetchDirectoryTask.cancel(true);
        breadcrumbView.clearCrumbs();
        breadcrumbView.setVisibility(View.GONE);
        getActivity().setTitle(originalToolbarTitle);
        listener = null;
        isRunning = false;
    }

    public boolean onBackButtonPressed() {
        // TODO cancel isInMoveMode
        if (recyclerViewAdapter.isInSelectMode()) {
            recyclerViewAdapter.cancelSelection();
        } else if (pathStack.isEmpty() || directoryCache.isEmpty()) {
            return false;
        }
        fetchDirectoryTask.cancel(true);
        breadcrumbView.removeLastCrumb();
        path = pathStack.pop();
        if (directoryCache.containsKey(path)) {
            directoryContent = directoryCache.get(path);
            recyclerViewAdapter.newData(directoryContent);
        } else {
            fetchDirectoryTask = new FetchDirectoryContent().execute();
        }
        return true;
    }

    @Override
    public void onFileClicked(FileItem fileItem) {
        listener.onFileClicked(fileItem);
    }

    @Override
    public void onDirectoryClicked(FileItem fileItem) {
        breadcrumbView.addCrumb(fileItem.getName(), fileItem.getPath());
        swipeRefreshLayout.setRefreshing(true);
        pathStack.push(path);
        directoryCache.put(path, new ArrayList<>(directoryContent));

        if (null != fetchDirectoryTask) {
            fetchDirectoryTask.cancel(true);
        }
        if (directoryCache.containsKey(fileItem.getPath())) {
            path = fileItem.getPath();
            directoryContent = directoryCache.get(path);
            sortDirectory();
            recyclerViewAdapter.newData(directoryContent);
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        path = fileItem.getPath();
        recyclerViewAdapter.clear();
        fetchDirectoryTask = new FetchDirectoryContent().execute();

    }

    @Override
    public void onFilesSelected(boolean longClick) {
        int numOfSelected = recyclerViewAdapter.getNumberOfSelectedItems();

        if (numOfSelected > 0) { // something is selected
            getActivity().setTitle(numOfSelected + " selected");
            getActivity().findViewById(R.id.bottom_bar).setVisibility(View.VISIBLE);
            if (numOfSelected > 1) {
                getActivity().findViewById(R.id.file_rename).setAlpha(.5f);
                getActivity().findViewById(R.id.file_rename).setClickable(false);
            } else {
                getActivity().findViewById(R.id.file_rename).setAlpha(1f);
                getActivity().findViewById(R.id.file_rename).setClickable(true);
            }
        } else if (!isInMoveMode) {
            getActivity().setTitle(remoteType);
            getActivity().findViewById(R.id.bottom_bar).setVisibility(View.GONE);
        }
    }

    @Override
    public void onBreadCrumbClicked(String path) {
        if (this.path.equals(path)) {
            return;
        }

        if (null != fetchDirectoryTask) {
            fetchDirectoryTask.cancel(true);
        }
        this.path = path;
        //noinspection StatementWithEmptyBody
        while (!pathStack.pop().equals(path)) {
            // pop stack until we find path
        }
        breadcrumbView.removeCrumbsUpTo(path);
        if (directoryCache.containsKey(path)) {
            directoryContent = directoryCache.get(path);
            recyclerViewAdapter.newData(directoryContent);
        } else {
            fetchDirectoryTask = new FetchDirectoryContent().execute();
        }
    }

    private void onDeleteClicked() {
        if (!recyclerViewAdapter.isInSelectMode()) {
            return;
        }

        final List<FileItem> deleteList = new ArrayList<>(recyclerViewAdapter.getSelectedItems());

        new AlertDialog.Builder(getContext())
                .setTitle("Delete " + deleteList.size() + " items?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        recyclerViewAdapter.cancelSelection();
                        new DeleteFilesTask().execute(deleteList);
                    }
                })
                .show();
    }

    private void onRenameClicked() {
        if (!recyclerViewAdapter.isInSelectMode() || recyclerViewAdapter.getNumberOfSelectedItems() > 1) {
            return;
        }

        List<FileItem> list = recyclerViewAdapter.getSelectedItems();
        final FileItem renameItem = list.get(0);

        new MaterialDialog.Builder(getContext())
                .title("Rename a file")
                .content("Please type new file name")
                .input(null, renameItem.getName(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (renameItem.getName().equals(input.toString())) {
                            return;
                        }
                        recyclerViewAdapter.cancelSelection();
                        String newFilePath;
                        if (path.equals("//" + remote)) {
                            newFilePath = input.toString();
                        } else {
                            newFilePath = path + "/" + input;
                        }
                        new RenameFileTask().execute(renameItem.getPath(), newFilePath);
                    }
                })
                .negativeText("Cancel")
                .show();
    }

    private void onDownloadClicked() {
        if (!recyclerViewAdapter.isInSelectMode()) {
            return;
        }
        final ArrayList<FileItem> downloadList = new ArrayList<>(recyclerViewAdapter.getSelectedItems());
        File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        FileListerDialog fileListerDialog = FileListerDialog.createFileListerDialog(getContext());
        fileListerDialog.setFileFilter(FileListerDialog.FILE_FILTER.DIRECTORY_ONLY);
        fileListerDialog.setDefaultDir(downloads);
        fileListerDialog.setOnFileSelectedListener(new OnFileSelectedListener() {
            @Override
            public void onFileSelected(File file, String path) {
                recyclerViewAdapter.cancelSelection();
                Intent intent = new Intent(getContext(), DownloadService.class);
                intent.putParcelableArrayListExtra(DownloadService.DOWNLOAD_LIST_ARG, downloadList);
                intent.putExtra(DownloadService.DOWNLOAD_PATH_ARG, path);
                intent.putExtra(DownloadService.REMOTE_ARG, remote);
                getContext().startService(intent);
            }
        });
        fileListerDialog.show();
    }

    private void onMoveClicked() {
        if (recyclerViewAdapter.getNumberOfSelectedItems() < 1) {
            return;
        }
        moveList = new ArrayList<>(recyclerViewAdapter.getSelectedItems());
        recyclerViewAdapter.cancelSelection();
        recyclerViewAdapter.setMoveMode(true);
        isInMoveMode = true;
        getActivity().setTitle("Select destination");
        getActivity().findViewById(R.id.move_bar).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.action_select_all).setVisibility(View.GONE);
    }

    private void onCreateNewDirectory() {
        new MaterialDialog.Builder(getContext())
                .title("Create new folder")
                .content("Please type new folder name")
                .negativeText("Cancel")
                .input(null, null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (input.toString().trim().length() == 0) {
                            return;
                        }
                        String newDir = path + "/" + input.toString();
                        new MakeDirectoryTask().execute(newDir);
                    }
                })
                .show();
    }

    /***********************************************************************************************
     * AsyncTask classes
     ***********************************************************************************************/
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
            fileItemList = rclone.getDirectoryContent(remote, path);
            return fileItemList;
        }

        @Override
        protected void onPostExecute(List<FileItem> fileItems) {
            super.onPostExecute(fileItems);
            directoryContent = fileItems;
            sortDirectory();
            directoryCache.put(path, new ArrayList<>(directoryContent));

            if (recyclerViewAdapter != null) {
                recyclerViewAdapter.newData(fileItems);
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
    private class RenameFileTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected Void doInBackground(String... strings) {
            String oldFileName = strings[0];
            String newFileName = strings[1];

            rclone.moveTo(remote, oldFileName, newFileName);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!isRunning) {
                return;
            }
            if (null != fetchDirectoryTask) {
                fetchDirectoryTask.cancel(true);
            }
            swipeRefreshLayout.setRefreshing(false);

            fetchDirectoryTask = new FetchDirectoryContent().execute();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class MoveTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            rclone.moveTo(remote, moveList, path);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!isRunning) {
                return;
            }
            if (null != fetchDirectoryTask) {
                fetchDirectoryTask.cancel(true);
            }
            swipeRefreshLayout.setRefreshing(false);

            fetchDirectoryTask = new FetchDirectoryContent().execute();
            moveList.clear();
        }
    }
    
    @SuppressLint("StaticFieldLeak")
    private class DeleteFilesTask extends AsyncTask<List, Void, Void> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);

        }

        @Override
        protected Void doInBackground(List[] lists) {
            List<FileItem> list = lists[0];
            rclone.deleteItems(remote, list);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!isRunning) {
                return;
            }
            if (null != fetchDirectoryTask) {
                fetchDirectoryTask.cancel(true);
            }
            swipeRefreshLayout.setRefreshing(false);

            fetchDirectoryTask = new FetchDirectoryContent().execute();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class MakeDirectoryTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected Void doInBackground(String... strings) {
            String newDir = strings[0];
            rclone.makeDirectory(remote, newDir);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            swipeRefreshLayout.setRefreshing(false);
            if (null != fetchDirectoryTask) {
                fetchDirectoryTask.cancel(true);
            }
            fetchDirectoryTask = new FetchDirectoryContent().execute();
        }
    }
}
