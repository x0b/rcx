package ca.pkay.rcloneexplorer.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.leinardi.android.speeddial.SpeedDialView;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.MainActivity;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import ca.pkay.rcloneexplorer.RecyclerViewAdapters.RemotesRecyclerViewAdapter;
import ca.pkay.rcloneexplorer.RemoteConfig.RemoteConfig;
import jp.wasabeef.recyclerview.animators.LandingAnimator;

public class RemotesFragment extends Fragment implements RemotesRecyclerViewAdapter.OnRemoteOptionsClick {

    private final int CONFIG_REQ_CODE = 171;
    private final int CONFIG_RECREATE_REQ_CODE = 156;
    private Rclone rclone;
    private RemotesRecyclerViewAdapter recyclerViewAdapter;
    private List<RemoteItem> remotes;
    private OnRemoteClickListener clickListener;
    private Context context;
    private boolean isDarkTheme;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RemotesFragment() {
    }

    @SuppressWarnings("unused")
    public static RemotesFragment newInstance() {
        return new RemotesFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext() == null) {
            return;
        }

        ((FragmentActivity) context).setTitle(getString(R.string.remotes_toolbar_title));
        rclone = new Rclone(getContext());
        remotes = rclone.getRemotes();
        Collections.sort(remotes);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view;
        if (!rclone.isConfigFileCreated()) {
            view = inflater.inflate(R.layout.empty_state_config_file, container, false);
            view.findViewById(R.id.empty_state_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (getActivity() != null) {
                        ((MainActivity) getActivity()).importConfigFile();
                    }
                }
            });

            SpeedDialView speedDialView = view.findViewById(R.id.fab);
            speedDialView.setMainFabOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, RemoteConfig.class);
                    startActivityForResult(intent, CONFIG_RECREATE_REQ_CODE);
                }
            });
            return view;
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        isDarkTheme = sharedPreferences.getBoolean(getString(R.string.pref_key_dark_theme), false);

        view = inflater.inflate(R.layout.fragment_remotes_list, container, false);

        final Context context = view.getContext();
        RecyclerView recyclerView =  view.findViewById(R.id.remotes_list);
        recyclerView.setItemAnimator(new LandingAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerViewAdapter = new RemotesRecyclerViewAdapter(remotes, clickListener, this);
        recyclerView.setAdapter(recyclerViewAdapter);

        SpeedDialView speedDialView = view.findViewById(R.id.fab);
        speedDialView.setMainFabOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, RemoteConfig.class);
                startActivityForResult(intent, CONFIG_REQ_CODE);
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CONFIG_REQ_CODE:
                remotes = rclone.getRemotes();
                Collections.sort(remotes);
                recyclerViewAdapter.newData(remotes);
                break;
            case CONFIG_RECREATE_REQ_CODE:
                Intent intent = new Intent(context, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof OnRemoteClickListener) {
            clickListener = (OnRemoteClickListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnRemoteClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        context = null;
        clickListener = null;
    }

    @Override
    public void onRemoteOptionsClicked(View view, RemoteItem remoteItem) {
        showRemoteMenu(view, remoteItem);
    }

    private void showRemoteMenu(View view, final RemoteItem remoteItem) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(R.menu.remote_options, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        deleteRemote(remoteItem);
                        break;
                    case R.id.action_pin:
                        if (remoteItem.isPinned()) {
                            unPinRemote(remoteItem);
                        } else {
                            pinRemote(remoteItem);
                        }
                        break;
                    default:
                        pinRemote(remoteItem);
                        return false;
                }
                return true;
            }
        });
        popupMenu.show();

        MenuItem pinAction = popupMenu.getMenu().findItem(R.id.action_pin);
        if (remoteItem.isPinned()) {
            pinAction.setTitle(R.string.unpin_from_the_top);
        } else {
            pinAction.setTitle(R.string.pin_to_the_top);
        }
    }

    private void pinRemote(RemoteItem remoteItem) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Set<String> stringSet = sharedPreferences.getStringSet(getString(R.string.shared_preferences_pinned_remotes), new HashSet<String>());
        Set<String> pinnedRemotes = new HashSet<>(stringSet); // bug in android means that we have to create a copy
        pinnedRemotes.add(remoteItem.getName());
        remoteItem.pin(true);

        editor.putStringSet(getString(R.string.shared_preferences_pinned_remotes), pinnedRemotes);
        editor.apply();

        int from = remotes.indexOf(remoteItem);
        Collections.sort(remotes);
        int to = remotes.indexOf(remoteItem);
        recyclerViewAdapter.moveDataItem(remotes, from, to);
    }

    private void unPinRemote(RemoteItem remoteItem) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Set<String> stringSet = sharedPreferences.getStringSet(getString(R.string.shared_preferences_pinned_remotes), new HashSet<String>());
        Set<String> pinnedRemotes = new HashSet<>(stringSet);
        if (pinnedRemotes.contains(remoteItem.getName())) {
            pinnedRemotes.remove(remoteItem.getName());
        }
        remoteItem.pin(false);

        editor.putStringSet(getString(R.string.shared_preferences_pinned_remotes), pinnedRemotes);
        editor.apply();

        int from = remotes.indexOf(remoteItem);
        Collections.sort(remotes);
        int to = remotes.indexOf(remoteItem);
        recyclerViewAdapter.moveDataItem(remotes, from, to);
    }

    private void deleteRemote(final RemoteItem remoteItem) {
        AlertDialog.Builder builder;
        if (isDarkTheme) {
            builder = new AlertDialog.Builder(context, R.style.DarkDialogTheme);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        builder.setTitle(R.string.delete_remote_title);
        builder.setMessage(remoteItem.getName());
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new DeleteRemote(remoteItem).execute();
            }
        });
        builder.show();
    }

    public interface OnRemoteClickListener {
        void onRemoteClick(RemoteItem remote);
    }

    @SuppressLint("StaticFieldLeak")
    private class DeleteRemote extends AsyncTask<Void, Void, Void> {

        private RemoteItem remoteItem;

        DeleteRemote(RemoteItem remoteItem) {
            this.remoteItem = remoteItem;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            rclone.deleteRemote(remoteItem.getName());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            Set<String> pinnedRemotes = sharedPreferences.getStringSet(getString(R.string.shared_preferences_pinned_remotes), new HashSet<String>());
            if (pinnedRemotes.contains(remoteItem.getName())) {
                pinnedRemotes.remove(remoteItem.getName());
                editor.putStringSet(getString(R.string.shared_preferences_pinned_remotes), new HashSet<>(pinnedRemotes));
                editor.apply();
            }

            recyclerViewAdapter.removeItem(remoteItem);
        }
    }
}
