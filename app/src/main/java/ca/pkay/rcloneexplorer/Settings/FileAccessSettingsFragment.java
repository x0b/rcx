package ca.pkay.rcloneexplorer.Settings;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import ca.pkay.rcloneexplorer.BuildConfig;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import ca.pkay.rcloneexplorer.RemoteConfig.RemoteConfigHelper;
import ca.pkay.rcloneexplorer.Services.RcdService;
import ca.pkay.rcloneexplorer.VirtualContentProvider;
import ca.pkay.rcloneexplorer.databinding.FragmentFileAccessSettingsBinding;
import ca.pkay.rcloneexplorer.databinding.FragmentPermissionItemBinding;
import es.dmoral.toasty.Toasty;
import io.github.x0b.safdav.file.SafConstants;

import java.util.List;

import static ca.pkay.rcloneexplorer.ActivityHelper.tryStartActivityForResult;

public class FileAccessSettingsFragment extends Fragment {

    public static final int onDocumentTreeOpened = 1001;
    private static final int onAllFilesSettingOpened = 1002;
    private static final String ANDROID_AUTHORITY = "com.android.externalstorage.documents";

    private Context context;
    private ViewGroup fileAccessAll;
    private View safEnabledView;
    private SwitchCompat safEnabledSwitch;
    private View vcpEnabledContainer;
    private SwitchCompat vcpEnabledSwitch;
    private View vcpDeclareLocalContainer;
    private SwitchCompat vcpDeclareLocalSwitch;
    private PermissionListAdapter permissionList;
    private Button addPermissionBtn;
    private RecyclerView listView;
    private View addButtonContainer;
    private View refreshLaContainer;
    private SwitchCompat refreshLaSwitch;
    private View openAllFilesPerm;
    private View vcpGrantAllContainer;
    private SwitchCompat vcpGrantAllSwitch;
    private Rclone rclone;
    private FragmentFileAccessSettingsBinding binding;

    public static FileAccessSettingsFragment newInstance() {
        return new FileAccessSettingsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFileAccessSettingsBinding.inflate(inflater, container, false);

        permissionList = new PermissionListAdapter(context.getContentResolver().getPersistedUriPermissions());
        listView = binding.filePermissionList;
        listView.setAdapter(permissionList);
        addButtonContainer = binding.permissionAddContainer;

        getViews(binding);
        setDefaultStates();
        setClickListeners();

        if (getActivity() != null) {
            getActivity().setTitle(getString(R.string.pref_header_file_access));
        }

        return binding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        rclone = new Rclone(context);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.context = null;
    }

    private void getViews(FragmentFileAccessSettingsBinding view) {
        safEnabledView = binding.enableSafView;
        safEnabledSwitch = binding.enableSafSwitch;
        fileAccessAll = binding.fileAccessSettingsAll;
        addPermissionBtn = binding.permissionAddButton;
        refreshLaContainer = binding.enableRefreshLaContainer;
        refreshLaSwitch = binding.enableRefreshLaSwitch;
        openAllFilesPerm = binding.openAllFilesSettingContainer;
        vcpEnabledContainer = binding.enableSafVcpView;
        vcpEnabledSwitch = binding.enableSafVcpSwitch;
        vcpDeclareLocalContainer = binding.vcpDeclareLocalContainer;
        vcpDeclareLocalSwitch = binding.vcpDeclareLocalSwitch;
        vcpGrantAllContainer = binding.vcpGrantAllContainer;
        vcpGrantAllSwitch = binding.vcpGrantAllSwitch;
    }

    private void setDefaultStates() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean safEnabled = sharedPreferences.getBoolean(getString(R.string.pref_key_enable_saf), false);
        safEnabledSwitch.setChecked(safEnabled);
        boolean refreshLaEnabled = sharedPreferences.getBoolean(getString(R.string.pref_key_refresh_local_aliases), true);
        refreshLaSwitch.setChecked(refreshLaEnabled);
        if(!safEnabled) {
            listView.setVisibility(View.GONE);
            addButtonContainer.setVisibility(View.GONE);
        }
        boolean vcpEnabled = sharedPreferences.getBoolean(getString(R.string.pref_key_enable_vcp), false);
        vcpEnabledSwitch.setChecked(vcpEnabled);
        boolean vcpDeclareLocal = sharedPreferences.getBoolean(getString(R.string.pref_key_vcp_declare_local), true);
        vcpDeclareLocalSwitch.setChecked(vcpDeclareLocal);
        boolean vcpGrantAll = sharedPreferences.getBoolean(getString(R.string.pref_key_vcp_grant_all), false);
        vcpGrantAllSwitch.setChecked(vcpGrantAll);
    }

    private void setClickListeners() {
        safEnabledView.setOnClickListener(v -> safEnabledSwitch.setChecked(!safEnabledSwitch.isChecked()));
        safEnabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> setSafEnabled(isChecked));
        addPermissionBtn.setOnClickListener(v -> addRoot());
        refreshLaContainer.setOnClickListener(v -> refreshLaSwitch.setChecked(!refreshLaSwitch.isChecked()));
        refreshLaSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> setRefreshLa(isChecked));
        openAllFilesPerm.setOnClickListener(v -> openAndroidRAllFilesSettings());
        vcpEnabledContainer.setOnClickListener(v -> vcpEnabledSwitch.setChecked(!vcpEnabledSwitch.isChecked()));
        vcpEnabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> setVcpEnabled(isChecked));
        vcpDeclareLocalContainer.setOnClickListener(v -> vcpDeclareLocalSwitch.setChecked(!vcpDeclareLocalSwitch.isChecked()));
        vcpDeclareLocalSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> setDeclareLocalEnabled(isChecked));
        vcpGrantAllContainer.setOnClickListener(v -> vcpGrantAllSwitch.setChecked(!vcpGrantAllSwitch.isChecked()));
        vcpGrantAllSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> setGrantAllEnabled(isChecked));
    }

    private void setSafEnabled(boolean isChecked) {
        if (isChecked) {
            listView.setVisibility(View.VISIBLE);
            addButtonContainer.setVisibility(View.VISIBLE);
            createSafRemote();
        } else {
            listView.setVisibility(View.GONE);
            addButtonContainer.setVisibility(View.GONE);
            rclone.deleteRemote(SafConstants.SAF_REMOTE_NAME);
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_key_enable_saf), isChecked);
        editor.apply();
    }

    private void setRefreshLa(boolean isChecked) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        pref.edit().putBoolean(getString(R.string.pref_key_refresh_local_aliases), isChecked)
                .remove(getString(R.string.pref_key_accessible_storage_locations)).apply();
    }

    private void openAndroidRAllFilesSettings() {
        // TODO @CompileSDK 30: Migrate to framework constants
        Intent intent = new Intent("android.settings.MANAGE_APP_ALL_FILES_ACCESS_PERMISSION");
        intent.setData(Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
        tryStartActivityForResult(this, intent, onAllFilesSettingOpened);
    }

    private void setVcpEnabled(boolean isChecked) {
        if (!isChecked) {
            Intent stopIntent = new Intent(context, RcdService.class);
            stopIntent.setAction(RcdService.ACTION_STOP_FOREGROUND);
            context.startService(stopIntent);
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_key_enable_vcp), isChecked).apply();
        Uri rootUri = VirtualContentProvider.getRootUri();
        context.getContentResolver().notifyChange(rootUri, null);
    }

    private void setDeclareLocalEnabled(boolean isChecked) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_key_vcp_declare_local), isChecked).apply();
    }

    private void setGrantAllEnabled(boolean isChecked) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_key_vcp_grant_all), isChecked).apply();
    }

    private void createSafRemote() {
        RemoteConfigHelper.enableSaf(getContext());
    }

    public void addRoot() {
        Intent treeOpenIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        treeOpenIntent.addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        tryStartActivityForResult(this, treeOpenIntent, onDocumentTreeOpened);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case onDocumentTreeOpened:
                onTreeResult(resultCode, data);
                break;
            case onAllFilesSettingOpened:
                Toasty.info(context, "All file access beta callback", Toast.LENGTH_LONG, true).show();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void onTreeResult(int resultCode, Intent data) {
        if (Activity.RESULT_OK == resultCode) {
            Uri uri = data.getData();
            if (null == uri || uri.getAuthority().equals(BuildConfig.VCP_AUTHORITY)) {
                Toasty.error(context, getString(R.string.saf_uri_permission_error), Toast.LENGTH_LONG, true).show();
                return;
            }
            final int takeFlags = data.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            ContentResolver contentResolver = context.getContentResolver();
            for (UriPermission uriPermission : contentResolver.getPersistedUriPermissions()) {
                if (uri.toString().startsWith(uriPermission.getUri().toString())) {
                    // granted uri is less specific than already granted ones, reject
                    Toasty.error(context, getString(R.string.saf_uri_permission_already_added), Toast.LENGTH_LONG, true).show();
                    return;
                }
            }
            if(!ANDROID_AUTHORITY.equals(uri.getAuthority())){
                String msg = "Third party providers are not tested well, proceed with caution.";
                Toasty.warning(context, msg, Toast.LENGTH_LONG, true).show();
            }
            contentResolver.takePersistableUriPermission(uri, takeFlags);
            permissionList.updatePermissions(context.getContentResolver().getPersistedUriPermissions());
            Toasty.normal(context, getString(R.string.saf_uri_permission_added), Toast.LENGTH_SHORT).show();

        } else if (Activity.RESULT_CANCELED == resultCode) {
            Toasty.normal(context, getString(R.string.saf_uri_permission_cancelled), Toast.LENGTH_SHORT).show();
        }
    }

    private static class PermissionsViewHolder extends RecyclerView.ViewHolder {

        TextView text;
        UriPermission permission;

        public PermissionsViewHolder(@NonNull FragmentPermissionItemBinding binding, final PermissionListAdapter adapter) {
            super(binding.getRoot());
            text = binding.permissionPath;
            binding.permissionRemove.setOnClickListener(v -> adapter.remove(getAdapterPosition()));
        }
    }

    private class PermissionListAdapter extends RecyclerView.Adapter<PermissionsViewHolder> {

        List<UriPermission> permissions;

        public PermissionListAdapter(List<UriPermission> permissions) {
            this.permissions = permissions;
        }

        @NonNull
        @Override
        public PermissionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            FragmentPermissionItemBinding binding = FragmentPermissionItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new PermissionsViewHolder(binding, this);
        }

        @Override
        public void onBindViewHolder(@NonNull PermissionsViewHolder holder, int position) {
            holder.permission = permissions.get(position);
            String authority = holder.permission.getUri().getAuthority();
            String permissionLabel = holder.permission.getUri().getPath();
            if (permissionLabel.startsWith("/tree/")) {
                permissionLabel = permissionLabel.substring(6);
            }
            if (!ANDROID_AUTHORITY.equals(authority)) {
                permissionLabel = authority + " (" + permissionLabel + ")";
            } else if ("primary:".equals(permissionLabel)) {
                permissionLabel = getString(R.string.pref_saf_permission_label_primary, permissionLabel);
            } else {
                permissionLabel = getString(R.string.pref_saf_permission_label_external, permissionLabel);
            }
            holder.text.setText(permissionLabel);
        }

        @Override
        public int getItemCount() {
            return permissions.size();
        }

        public void add(UriPermission permission) {
            permissions.add(permission);
            notifyDataSetChanged();
        }

        public void remove(int index) {
            UriPermission permission = permissions.get(index);
            context.getContentResolver().releasePersistableUriPermission(permission.getUri(),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            permissions.remove(index);
            notifyDataSetChanged();
        }

        public void updatePermissions(List<UriPermission> newPermissions) {
            this.permissions = newPermissions;
            notifyDataSetChanged();
        }
    }
}
