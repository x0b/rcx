package ca.pkay.rcloneexplorer.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.shehabic.droppy.DroppyClickCallbackInterface;
import com.shehabic.droppy.DroppyMenuPopup;

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

public class FileExplorerFragment extends Fragment implements   FileExplorerRecyclerViewAdapter.OnClickListener,
                                                                SwipeRefreshLayout.OnRefreshListener,
                                                                BreadcrumbView.OnClickListener {

    private static final String ARG_REMOTE = "remote_param";
    private static final String ARG_REMOTE_TYPE = "remote_type_param";
    private static final String SHARED_PREFS_SORT_ORDER = "ca.pkay.rcexplorer.sort_order";
    private OnFileClickListener listener;
    private List<FileItem> directoryContent;
    private Stack<String> pathStack;
    private Map<String, List<FileItem>> directoryCache;
    private BreadcrumbView breadcrumbView;
    private Rclone rclone;
    private String remote;
    private String remoteType;
    private String path;
    private FileExplorerRecyclerViewAdapter recyclerViewAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AsyncTask fetchDirectoryTask;
    private SortOrder sortOrder;

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
        getActivity().setTitle(remoteType);
        setHasOptionsMenu(true);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(MainActivity.SHARED_PREFS_TAG, Context.MODE_PRIVATE);
        sortOrder = SortOrder.fromInt(sharedPreferences.getInt(SHARED_PREFS_SORT_ORDER, -1));

        rclone = new Rclone((AppCompatActivity) getActivity());
        pathStack = new Stack<>();
        directoryCache = new HashMap<>();

        fetchDirectoryTask = new FetchDirectoryContent().execute();
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

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.sort, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sort) {
            showSortMenu();
            return true;
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
        recyclerViewAdapter.newData(directoryContent);
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
        listener = null;
    }

    public boolean onBackButtonPressed() {
        if (pathStack.isEmpty() || directoryCache.isEmpty()) {
            return false;
        } else {
            fetchDirectoryTask.cancel(true);
            path = pathStack.pop();
            directoryContent = directoryCache.get(path);
            breadcrumbView.removeLastCrumb();
            recyclerViewAdapter.newData(directoryContent);
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
            recyclerViewAdapter.newData(directoryContent);
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        path = fileItem.getPath();
        recyclerViewAdapter.clear();
        fetchDirectoryTask = new FetchDirectoryContent().execute();

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
        while (!pathStack.pop().equals(path)) {
            // pop stack until we find path
        }
        directoryContent = directoryCache.get(path);
        breadcrumbView.removeCrumbsUpTo(path);
        recyclerViewAdapter.newData(directoryContent);
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
}
