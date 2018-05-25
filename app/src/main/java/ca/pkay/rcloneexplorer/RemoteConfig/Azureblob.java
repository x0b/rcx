package ca.pkay.rcloneexplorer.RemoteConfig;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import es.dmoral.toasty.Toasty;

public class Azureblob extends Fragment {

    private Context context;
    private Rclone rclone;
    private TextInputLayout remoteNameInputLayout;
    private TextInputLayout accountInputLayout;
    private TextInputLayout keyInputLayout;
    private EditText remoteName;
    private EditText account;
    private EditText key;
    private EditText endpoint;

    public Azureblob() {}

    public static Azureblob newInstance() { return new Azureblob(); }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext() == null) {
            return;
        }
        context = getContext();
        rclone = new Rclone(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.remote_config_form, container, false);
        setUpForm(view);
        return view;
    }

    private void setUpForm(View view) {
        View formContent = view.findViewById(R.id.form_content);
        int padding = getResources().getDimensionPixelOffset(R.dimen.config_form_template);
        remoteNameInputLayout = view.findViewById(R.id.remote_name_layout);
        remoteNameInputLayout.setVisibility(View.VISIBLE);
        remoteName = view.findViewById(R.id.remote_name);

        View accountTemplate = View.inflate(context, R.layout.config_form_template_edit_text, null);
        accountTemplate.setPadding(0, 0, 0, padding);
        ((ViewGroup)formContent).addView(accountTemplate);
        accountInputLayout = accountTemplate.findViewById(R.id.text_input_layout);
        accountInputLayout.setHint(getString(R.string.azureblob_account_hint));
        account = accountTemplate.findViewById(R.id.edit_text);

        View keyTemplate = View.inflate(context, R.layout.config_form_template_edit_text, null);
        keyTemplate.setPadding(0, 0,0 , padding);
        ((ViewGroup) formContent).addView(keyTemplate);
        keyInputLayout = keyTemplate.findViewById(R.id.text_input_layout);
        keyInputLayout.setHint(getString(R.string.azureblob_key_hint));
        key = keyTemplate.findViewById(R.id.edit_text);

        View endpointTemplate = View.inflate(context, R.layout.config_form_template_edit_text, null);
        endpointTemplate.setPadding(0, 0, 0, padding);
        ((ViewGroup) formContent).addView(endpointTemplate);
        TextInputLayout endpointInputLayout = endpointTemplate.findViewById(R.id.text_input_layout);
        endpointInputLayout.setHint(getString(R.string.endpoint_hint));
        endpoint = endpointTemplate.findViewById(R.id.edit_text);
        endpointTemplate.findViewById(R.id.helper_text).setVisibility(View.VISIBLE);

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
    }

    private void setUpRemote() {
        String name = remoteName.getText().toString();
        String accountString = account.getText().toString();
        String keyString = key.getText().toString();
        String endpointString = endpoint.getText().toString();
        boolean error = false;

        if (name.trim().isEmpty()) {
            remoteNameInputLayout.setErrorEnabled(true);
            remoteNameInputLayout.setError(getString(R.string.remote_name_cannot_be_empty));
            error = true;
        } else {
            remoteNameInputLayout.setErrorEnabled(false);
        }
        if (accountString.trim().isEmpty()) {
            accountInputLayout.setErrorEnabled(true);
            accountInputLayout.setError(getString(R.string.required_field));
            error = true;
        } else {
            accountInputLayout.setErrorEnabled(false);
        }
        if (keyString.trim().isEmpty()) {
            keyInputLayout.setErrorEnabled(true);
            keyInputLayout.setError(getString(R.string.required_field));
            error = true;
        } else {
            keyInputLayout.setErrorEnabled(false);
        }
        if (error) {
            return;
        }

        ArrayList<String> options = new ArrayList<>();
        options.add(name);
        options.add("Azureblob");
        options.add("account");
        options.add(accountString);
        options.add("key");
        options.add(keyString);
        if (!endpointString.trim().isEmpty()) {
            options.add("endpoint");
            options.add(endpointString);
        }

        Process process = rclone.configCreate(options);
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (process.exitValue() != 0) {
            Toasty.error(context, getString(R.string.error_creating_remote), Toast.LENGTH_SHORT, true).show();
        } else {
            Toasty.success(context, getString(R.string.remote_creation_success), Toast.LENGTH_SHORT, true).show();
        }
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
