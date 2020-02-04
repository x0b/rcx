package ca.pkay.rcloneexplorer.RemoteConfig;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class MegaConfig extends Fragment {

    private Context context;
    private Rclone rclone;
    private TextInputLayout remoteNameInputLayout;
    private TextInputLayout userInputLayout;
    private TextInputLayout passInputLayout;
    private EditText remoteName;
    private EditText user;
    private EditText pass;

    public MegaConfig() {}

    public static MegaConfig newInstance() { return new MegaConfig(); }

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
        ViewGroup formContent = view.findViewById(R.id.form_content);
        int padding = getResources().getDimensionPixelOffset(R.dimen.config_form_template);
        remoteNameInputLayout = view.findViewById(R.id.remote_name_layout);
        remoteNameInputLayout.setVisibility(View.VISIBLE);
        remoteName = view.findViewById(R.id.remote_name);

        View userInputTemplate = View.inflate(context, R.layout.config_form_template_edit_text, null);
        userInputTemplate.setPadding(0, 0, 0, padding);
        formContent.addView(userInputTemplate);
        userInputLayout = userInputTemplate.findViewById(R.id.text_input_layout);
        userInputLayout.setHint(getString(R.string.provider_mega_config_user));
        user = userInputTemplate.findViewById(R.id.edit_text);

        View passInputTemplate = View.inflate(context, R.layout.config_form_template_password, null);
        passInputTemplate.setPadding(0, 0, 0, padding);
        formContent.addView(passInputTemplate);
        passInputLayout = passInputTemplate.findViewById(R.id.pass_input_layout);
        passInputLayout.setHint(getString(R.string.provider_mega_config_password));
        pass = passInputTemplate.findViewById(R.id.pass);

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
        String userString = user.getText().toString();
        String passString = pass.getText().toString();
        boolean error = false;

        if (name.trim().isEmpty()) {
            remoteNameInputLayout.setErrorEnabled(true);
            remoteNameInputLayout.setError(getString(R.string.remote_name_cannot_be_empty));
            error = true;
        } else {
            remoteNameInputLayout.setErrorEnabled(false);
        }
        if (userString.trim().isEmpty()) {
            userInputLayout.setErrorEnabled(true);
            userInputLayout.setError(getString(R.string.required_field));
            error = true;
        } else {
            userInputLayout.setErrorEnabled(false);
        }
        if (passString.trim().isEmpty()) {
            passInputLayout.setErrorEnabled(true);
            passInputLayout.setError(getString(R.string.required_field));
            error = true;
        } else {
            passInputLayout.setErrorEnabled(false);
        }
        if (error) {
            return;
        }

        ArrayList<String> options = new ArrayList<>();
        options.add(name);
        options.add("mega");
        options.add("user");
        options.add(userString);

        String obscuredPass = rclone.obscure(passString);
        options.add("pass");
        options.add(obscuredPass);

        RemoteConfigHelper.setupAndWait(context, options);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
