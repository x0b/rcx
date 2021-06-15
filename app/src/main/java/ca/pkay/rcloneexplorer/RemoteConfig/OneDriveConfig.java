package ca.pkay.rcloneexplorer.RemoteConfig;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;
import ca.pkay.rcloneexplorer.InteractiveRunner;
import ca.pkay.rcloneexplorer.InteractiveRunner.Step;
import ca.pkay.rcloneexplorer.MainActivity;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import ca.pkay.rcloneexplorer.util.FLog;
import com.google.android.material.textfield.TextInputLayout;
import es.dmoral.toasty.Toasty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OneDriveConfig extends Fragment {

    private static final String TAG = "OneDriveConfig";
    private Context context;
    private Rclone rclone;
    private View authView;
    private View formView;
    private AsyncTask authTask;
    private TextInputLayout remoteNameInputLayout;
    private EditText remoteName;
    private EditText clientId;
    private EditText clientSecret;

    public OneDriveConfig() {}

    public static OneDriveConfig newInstance() { return new OneDriveConfig(); }

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

        View clientIdTemplate = View.inflate(context, R.layout.config_form_template_edit_text, null);
        clientIdTemplate.setPadding(0, 0, 0, padding);
        formContent.addView(clientIdTemplate);
        TextInputLayout clientIdInputLayout = clientIdTemplate.findViewById(R.id.text_input_layout);
        clientIdInputLayout.setHint(getString(R.string.onedrive_client_id_hint));
        clientId = clientIdTemplate.findViewById(R.id.edit_text);
        clientIdTemplate.findViewById(R.id.helper_text).setVisibility(View.VISIBLE);

        View clientSecretTemplate = View.inflate(context, R.layout.config_form_template_edit_text, null);
        clientSecretTemplate.setPadding(0, 0, 0, padding);
        formContent.addView(clientSecretTemplate);
        TextInputLayout clientSecretInputLayout = clientSecretTemplate.findViewById(R.id.text_input_layout);
        clientSecretInputLayout.setHint(getString(R.string.onedrive_client_secret_hint));
        clientSecret = clientSecretTemplate.findViewById(R.id.edit_text);
        clientSecretTemplate.findViewById(R.id.helper_text).setVisibility(View.VISIBLE);

        view.findViewById(R.id.next).setOnClickListener(v -> setUpRemote());

        view.findViewById(R.id.cancel).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
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
        options.add("onedrive");
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
            try {
                process = rclone.configInteractive();
            } catch (IOException e) {
                return false;
            }

            String name = options.get(0);
            String clientId = "";
            String clientSecret = "";
            if(options.size() >= 3) {
                clientId = options.get(1);
                clientSecret = options.get(2);
            }
            // recipe definition
            Step start = new Step("s/q>", new InteractiveRunner.StringAction("n"));
            start.addFollowing("name> ", name)
                    .addFollowing("Microsoft OneDrive", "onedrive", Step.STDOUT)
                    .addFollowing("client_id>", clientId, Step.STDOUT)
                    .addFollowing("client_secret>", clientSecret, Step.STDOUT)
                    .addFollowing("national cloud region", "global", Step.STDOUT)
                    .addFollowing("y/n> ", "n", Step.STDOUT)
                    .addFollowing("y/n> ", "y", Step.STDOUT)
                    .addFollowing(new OauthHelper.InitOauthStep(context))
                    .addFollowing(new OauthHelper.OauthFinishStep())
                    .addFollowing("OneDrive Personal or Business", "onedrive", Step.STDOUT)
                    .addFollowing("Chose drive to use:> ", "0", Step.STDOUT)
                    .addFollowing("y/n> ", "y", Step.STDOUT)
                    .addFollowing("y/e/d> ", "y", Step.STDOUT)
                    .addFollowing("e/n/d/r/c/s/q> ", "q", Step.STDOUT);

            InteractiveRunner.ErrorHandler errorHandler = exception -> {
                FLog.e(TAG, "onError: The recipe is probably bad.", exception);
                process.destroy();
            };

            InteractiveRunner interactiveRunner = new InteractiveRunner(start, errorHandler, process);
            interactiveRunner.runSteps();

            try {
                process.waitFor();
            } catch (InterruptedException e) {
                FLog.w(TAG, "force-stopped rclone process", e);
            }
            return 0 == process.exitValue();
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
