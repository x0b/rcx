package ca.pkay.rcloneexplorer.RemoteConfig;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import ca.pkay.rcloneexplorer.MainActivity;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import com.google.android.material.textfield.TextInputLayout;
import es.dmoral.toasty.Toasty;

import java.util.ArrayList;

public class DriveConfig extends Fragment {

    private String SAVED_SCOPE = "ca.pkay.rcexplorer.DriveConfig.SCOPE";
    private String SAVED_SCOPE_STRING = "ca.pkay.rcexplorer.DriveConfig.SCOPE_STRING";
    private Context context;
    private Rclone rclone;
    private View authView;
    private View formView;
    private AsyncTask authTask;
    private TextInputLayout remoteNameInputLayout;
    private EditText remoteName;
    private EditText clientId;
    private EditText clientSecret;
    private EditText rootFolderId;
    private TextView scope;
    private String scopeString;
    private boolean isDarkTheme;

    public DriveConfig() {}

    public static DriveConfig newInstance() { return new DriveConfig(); }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext() == null) {
            return;
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        isDarkTheme = sharedPreferences.getBoolean(getString(R.string.pref_key_dark_theme), false);

        rclone = new Rclone(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.remote_config_form, container, false);
        authView = view.findViewById(R.id.auth_screen);
        formView = view.findViewById(R.id.form);
        setUpForm(view);
        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!scope.getText().equals(getString(R.string.drive_scope__hint)) && scopeString != null) {
            outState.putString(SAVED_SCOPE, scope.getText().toString());
            outState.putString(SAVED_SCOPE_STRING, scopeString);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) {
            return;
        }

        String savedScope = savedInstanceState.getString(SAVED_SCOPE);
        String savedScopeString = savedInstanceState.getString(SAVED_SCOPE_STRING);

        if (savedScope != null && savedScopeString != null) {
            scope.setText(savedScope);
            scopeString = savedScopeString;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (authTask != null) {
            authTask.cancel(true);
        }
    }

    private void setUpForm(View view) {
        ViewGroup formContent = view.findViewById(R.id.form_content);
        int padding = getResources().getDimensionPixelOffset(R.dimen.config_form_template);
        remoteNameInputLayout = view.findViewById(R.id.remote_name_layout);
        remoteNameInputLayout.setVisibility(View.VISIBLE);
        remoteName = view.findViewById(R.id.remote_name);

        View clientIdTemplate = View.inflate(context, R.layout.config_form_template_edit_text, null);
        clientIdTemplate.setPadding(0, 0, 0, padding);
        formContent.addView(clientIdTemplate);
        TextInputLayout clientIdInputLayout = clientIdTemplate.findViewById(R.id.text_input_layout);
        clientIdInputLayout.setHint(getString(R.string.drive_client_id_hint));
        clientId = clientIdTemplate.findViewById(R.id.edit_text);
        clientIdTemplate.findViewById(R.id.helper_text).setVisibility(View.VISIBLE);

        View clientSecretTemplate = View.inflate(context, R.layout.config_form_template_edit_text, null);
        clientSecretTemplate.setPadding(0, 0, 0, padding);
        formContent.addView(clientSecretTemplate);
        TextInputLayout clientSecretInputLayout = clientSecretTemplate.findViewById(R.id.text_input_layout);
        clientSecretInputLayout.setHint(getString(R.string.drive_client_secret_hint));
        clientSecret = clientSecretInputLayout.findViewById(R.id.edit_text);
        clientSecretTemplate.findViewById(R.id.helper_text).setVisibility(View.VISIBLE);

        View scopeTemplate = View.inflate(context, R.layout.config_form_template_text_field, null);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, padding);
        scopeTemplate.setLayoutParams(params);
        formContent.addView(scopeTemplate);

        scope = view.findViewById(R.id.text_view);
        scope.setText(R.string.drive_scope__hint);
        scopeTemplate.setOnClickListener(v -> setScope());

        View rootFolderIdTemplate = View.inflate(context, R.layout.config_form_template_edit_text, null);
        rootFolderIdTemplate.setPadding(0, 0, 0, padding);
        formContent.addView(rootFolderIdTemplate);
        TextInputLayout endpointInputLayout = rootFolderIdTemplate.findViewById(R.id.text_input_layout);
        endpointInputLayout.setHint(getString(R.string.drive_root_folder_id));
        rootFolderId = rootFolderIdTemplate.findViewById(R.id.edit_text);
        rootFolderIdTemplate.findViewById(R.id.helper_text).setVisibility(View.VISIBLE);

        view.findViewById(R.id.next).setOnClickListener(v -> setUpRemote());

        view.findViewById(R.id.cancel).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        view.findViewById(R.id.cancel_auth).setOnClickListener(v -> {
            if (authTask != null) {
                authTask.cancel(true);
            }
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }

    private void setScope() {
        AlertDialog.Builder builder;
        if (isDarkTheme) {
            builder = new AlertDialog.Builder(context, R.style.DarkDialogTheme);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        builder.setTitle(R.string.drive_scope_title);
        builder.setItems(R.array.drive_scopes, (dialog, which) -> scopeSet(which));
        builder.show();
    }

    private void scopeSet(int selectedScope) {
        switch (selectedScope) {
            case 0:
                scopeString = "drive";
                break;
            case 1:
                scopeString = "drive.readonly";
                break;
            case 2:
                scopeString = "drive.file";
                break;
            case 3:
                scopeString = "drive.appfolder";
                break;
            case 4:
                scopeString = "drive.metadata.readonly";
                break;
        }

        scope.setText("scope: \"" + scopeString + "\"");
    }

    private void setUpRemote() {
        String name = remoteName.getText().toString();
        String clientIdString = clientId.getText().toString();
        String clientSecretString = clientSecret.getText().toString();
        String rootFolderIdString = rootFolderId.getText().toString();

        if (name.trim().isEmpty()) {
            remoteNameInputLayout.setErrorEnabled(true);
            remoteNameInputLayout.setError(getString(R.string.remote_name_cannot_be_empty));
            return;
        }

        ArrayList<String> options = new ArrayList<>();
        options.add(name);
        options.add("drive");
        if (!clientIdString.trim().isEmpty()) {
            options.add("client_id");
            options.add(clientIdString);
        }
        if (!clientSecretString.trim().isEmpty()) {
            options.add("client_secret");
            options.add(clientSecretString);
        }
        if (!rootFolderIdString.trim().isEmpty()) {
            options.add("root_folder_id");
            options.add(rootFolderIdString);
        }

        if (!scope.getText().equals(getString(R.string.drive_scope__hint)) && scopeString != null) {
            options.add("scope");
            options.add(scopeString);
        }

        authTask = new ConfigCreate(options).execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class ConfigCreate extends AsyncTask<Void, Void, Boolean> {

        private ArrayList<String> options;
        private Process process;

        ConfigCreate(ArrayList<String> options) {
            this.options = new ArrayList<>(options);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            authView.setVisibility(View.VISIBLE);
            formView.setVisibility(View.GONE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return OauthHelper.createOptionsWithOauth(options, rclone, context);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (process != null) {
                process.destroy();
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (!success) {
                Toasty.error(context, getString(R.string.error_creating_remote), Toast.LENGTH_SHORT, true).show();
            } else {
                Toasty.success(context, getString(R.string.remote_creation_success), Toast.LENGTH_SHORT, true).show();
            }
            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}
