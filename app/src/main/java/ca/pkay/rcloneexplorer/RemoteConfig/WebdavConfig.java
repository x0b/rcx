package ca.pkay.rcloneexplorer.RemoteConfig;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import es.dmoral.toasty.Toasty;

public class WebdavConfig extends Fragment {

    private Context context;
    private Rclone rclone;
    private TextInputLayout remoteNameInputLayout;
    private TextInputLayout urlInputLayout;
    private TextInputLayout userInputLayout;
    private TextInputLayout passInputLayout;
    private EditText remoteName;
    private EditText url;
    private EditText user;
    private EditText pass;
    private Spinner vendor;
    private View vendorLine;

    public WebdavConfig() {}

    public static WebdavConfig newInstance() { return new WebdavConfig(); }

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

        View urlInputTemplate = View.inflate(context, R.layout.config_form_template_edit_text, null);
        urlInputTemplate.setPadding(0, 0, 0, padding);
        formContent.addView(urlInputTemplate);
        urlInputLayout = urlInputTemplate.findViewById(R.id.text_input_layout);
        urlInputLayout.setHint(getString(R.string.url_hint));
        url = urlInputTemplate.findViewById(R.id.edit_text);

        View userInputTemplate = View.inflate(context, R.layout.config_form_template_edit_text, null);
        userInputTemplate.setPadding(0, 0, 0, padding);
        formContent.addView(userInputTemplate);
        userInputLayout = userInputTemplate.findViewById(R.id.text_input_layout);
        userInputLayout.setHint(getString(R.string.webdav_user_hint));
        user = userInputTemplate.findViewById(R.id.edit_text);

        View passTemplate = View.inflate(context, R.layout.config_form_template_password, null);
        passTemplate.setPadding(0, 0, 0, padding);
        formContent.addView(passTemplate);
        passInputLayout = passTemplate.findViewById(R.id.pass_input_layout);
        passInputLayout.setHint(getString(R.string.webdav_pass_hint));
        pass = passTemplate.findViewById(R.id.pass);

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

        View vendorTemplate = View.inflate(context, R.layout.config_form_template_spinner, null);
        formContent.addView(vendorTemplate);
        vendor = vendorTemplate.findViewById(R.id.spinner);
        vendorLine = vendorTemplate.findViewById(R.id.spinner_line);
        String[] options = new String[]{getString(R.string.webdav_spinner_prompt), "Nextcloud", "Owncloud", "Sharepoint", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.spinner_dropdown_item, options) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                if (position == 0) {
                    textView.setTextColor(Color.GRAY);
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        vendor.setAdapter(adapter);
    }

    private void setUpRemote() {
        String name = remoteName.getText().toString();
        String userString = user.getText().toString();
        String urlString = url.getText().toString();
        String passString = pass.getText().toString();
        String vendorString = vendor.getSelectedItem().toString();
        int color = ((ColorDrawable)vendorLine.getBackground()).getColor();

        boolean error = false;

        if (name.trim().isEmpty()) {
            remoteNameInputLayout.setErrorEnabled(true);
            remoteNameInputLayout.setError(getString(R.string.remote_name_cannot_be_empty));
            error = true;
        } else {
            remoteNameInputLayout.setErrorEnabled(false);
        }
        if (urlString.trim().isEmpty()) {
            urlInputLayout.setErrorEnabled(true);
            urlInputLayout.setError(getString(R.string.required_field));
            error = true;
        } else {
            urlInputLayout.setErrorEnabled(false);
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
        if (vendorString.equals(getString(R.string.webdav_spinner_prompt))) {
            vendorLine.setBackgroundColor(Color.parseColor("#B14525"));
            error = true;
        } else {
            vendorLine.setBackgroundColor(color);
        }
        if (error) {
            return;
        }

        ArrayList<String> options = new ArrayList<>();
        options.add(name);
        options.add("webdav");
        options.add("url");
        options.add(urlString);
        options.add("vendor");
        options.add(vendorString.toLowerCase());
        options.add("user");
        options.add(userString);

        String obscuredPass = rclone.obscure(passString);
        options.add("pass");
        options.add(obscuredPass);

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
