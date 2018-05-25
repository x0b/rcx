package ca.pkay.rcloneexplorer.RemoteConfig;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import ca.pkay.rcloneexplorer.MainActivity;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import es.dmoral.toasty.Toasty;

public class BoxConfig extends Fragment {

    private Context context;
    private Rclone rclone;
    private View authView;
    private View formView;
    private AsyncTask authTask;
    private TextInputLayout remoteNameInputLayout;
    private EditText remoteName;
    private EditText clientId;
    private EditText clientSecret;

    public BoxConfig() {}

    public static BoxConfig newInstance() { return new BoxConfig(); }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext() == null) {
            return;
        }
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

        View clientTemplate = View.inflate(context, R.layout.config_form_template_edit_text, null);
        clientTemplate.setPadding(0, 0, 0, padding);
        formContent.addView(clientTemplate);
        TextInputLayout clientIdInputLayout = clientTemplate.findViewById(R.id.text_input_layout);
        clientIdInputLayout.setHint(getString(R.string.box_client_id_hint));
        clientId = clientTemplate.findViewById(R.id.edit_text);
        clientTemplate.findViewById(R.id.helper_text).setVisibility(View.VISIBLE);

        View clientSecretTemplate = View.inflate(context, R.layout.config_form_template_edit_text, null);
        clientSecretTemplate.setPadding(0, 0, 0, padding);
        formContent.addView(clientSecretTemplate);
        TextInputLayout clientSecretInputLayout = clientSecretTemplate.findViewById(R.id.text_input_layout);
        clientSecretInputLayout.setHint(getString(R.string.box_client_secret_hint));
        clientSecret = clientSecretTemplate.findViewById(R.id.edit_text);
        clientSecretTemplate.findViewById(R.id.helper_text).setVisibility(View.VISIBLE);

        view.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpRemote();
            }
        });

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });

        view.findViewById(R.id.launch_browser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://127.0.0.1:53682/auth";
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(context, Uri.parse(url));
            }
        });

        view.findViewById(R.id.cancel_auth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (authTask != null) {
                    authTask.cancel(true);
                }
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });
    }

    private void setUpRemote() {
        String name = remoteName.getText().toString();
        String clientIdString = clientId.getText().toString();
        String clientSecretString = clientSecret.getText().toString();

        if (name.trim().isEmpty()) {
            remoteNameInputLayout.setErrorEnabled(true);
            remoteNameInputLayout.setError(getString(R.string.remote_name_cannot_be_empty));
            return;
        }

        ArrayList<String> options = new ArrayList<>();
        options.add(name);
        options.add("box");
        if (!clientIdString.trim().isEmpty()) {
            options.add("client_id");
            options.add(clientIdString);
        }
        if (!clientSecretString.trim().isEmpty()) {
            options.add("client_secret");
            options.add(clientSecretString);
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
            process = rclone.configCreate(options);

            String url = "http://127.0.0.1:53682/auth";

            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(context, Uri.parse(url));

            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return process.exitValue() == 0;
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
