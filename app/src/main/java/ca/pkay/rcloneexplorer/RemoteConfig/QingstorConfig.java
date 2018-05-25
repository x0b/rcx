package ca.pkay.rcloneexplorer.RemoteConfig;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.InputType;
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

public class QingstorConfig extends Fragment {

    private Context context;
    private Rclone rclone;
    private TextInputLayout remoteNameInputLayout;
    private TextInputLayout accessKeyLayout;
    private TextInputLayout secreAccessLayout;
    private TextInputLayout endpointLayout;
    private TextInputLayout connectionRetriesLayout;
    private EditText remoteName;
    private EditText accessKey;
    private EditText secretKey;
    private EditText endpoint;
    private EditText connectionRetries;
    private Spinner zone;
    private View zoneLine;

    public QingstorConfig() {}

    public static QingstorConfig newInstance() { return new QingstorConfig(); }

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

        View accessKeyTemplate = View.inflate(context, R.layout.config_form_template_edit_text, null);
        accessKeyTemplate.setPadding(0, 0, 0, padding);
        formContent.addView(accessKeyTemplate);
        accessKeyLayout = accessKeyTemplate.findViewById(R.id.text_input_layout);
        accessKeyLayout.setHint(getString(R.string.qingstor_access_key_hint));
        accessKey = accessKeyTemplate.findViewById(R.id.edit_text);
        accessKeyTemplate.findViewById(R.id.helper_text).setVisibility(View.VISIBLE);
        ((TextView)accessKeyTemplate.findViewById(R.id.helper_text)).setText(R.string.leave_blank_for_anon_access);

        View secretAccessTemplate = View.inflate(context, R.layout.config_form_template_edit_text, null);
        secretAccessTemplate.setPadding(0, 0, 0, padding);
        formContent.addView(secretAccessTemplate);
        secreAccessLayout = secretAccessTemplate.findViewById(R.id.text_input_layout);
        secreAccessLayout.setHint(getString(R.string.qingstor_secret_access_key_hint));
        secretKey = secretAccessTemplate.findViewById(R.id.edit_text);
        secretAccessTemplate.findViewById(R.id.helper_text).setVisibility(View.VISIBLE);
        ((TextView)secretAccessTemplate.findViewById(R.id.helper_text)).setText(R.string.leave_blank_for_anon_access);

        View endpointTemplate = View.inflate(context, R.layout.config_form_template_edit_text, null);
        endpointTemplate.setPadding(0, 0, 0, padding);
        formContent.addView(endpointTemplate);
        endpointLayout = endpointTemplate.findViewById(R.id.text_input_layout);
        endpointLayout.setHint(getString(R.string.qingstor_endpoint_hint));
        endpoint = endpointTemplate.findViewById(R.id.edit_text);
        endpointTemplate.findViewById(R.id.helper_text).setVisibility(View.VISIBLE);
        ((TextView)endpointTemplate.findViewById(R.id.helper_text)).setText(R.string.qingstor_endpoint_helper_text);

        View zoneTemplate = View.inflate(context, R.layout.config_form_template_spinner, null);
        zoneTemplate.setPadding(0, 0, 0, padding);
        formContent.addView(zoneTemplate);
        zone = zoneTemplate.findViewById(R.id.spinner);
        zoneLine = zoneTemplate.findViewById(R.id.spinner_line);
        String[] options = new String[]{getString(R.string.qingstor_zone_spinner_prompt), "pek3a", "sh1a", "gd2a"};
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
        zone.setAdapter(adapter);

        View connectionRetriesTemplate = View.inflate(context, R.layout.config_form_template_edit_text, null);
        connectionRetriesTemplate.setPadding(0, 0, 0, padding);
        formContent.addView(connectionRetriesTemplate);
        connectionRetriesLayout = connectionRetriesTemplate.findViewById(R.id.text_input_layout);
        connectionRetriesLayout.setHint(getString(R.string.qingstor_connection_retries_hint));
        connectionRetries = connectionRetriesTemplate.findViewById(R.id.edit_text);
        connectionRetries.setInputType(InputType.TYPE_CLASS_NUMBER);
        ((TextView)connectionRetriesTemplate.findViewById(R.id.helper_text)).setText(R.string.qingstor_connection_retires_helper);

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
        String accessKeyString = accessKey.getText().toString();
        String secretKeyString = secretKey.getText().toString();
        String endpointString = endpoint.getText().toString();
        String connectionRetriesString = endpoint.getText().toString();
        String zoneString = zone.getSelectedItem().toString();

        boolean error = false;

        if (name.trim().isEmpty()) {
            remoteNameInputLayout.setErrorEnabled(true);
            remoteNameInputLayout.setError(getString(R.string.remote_name_cannot_be_empty));
            error = true;
        } else {
            remoteNameInputLayout.setErrorEnabled(false);
        }
        if (error) {
            return;
        }

        ArrayList<String> options = new ArrayList<>();
        options.add(name);
        options.add("qingstor");
        options.add("env_auth");
        options.add("false");
        if (!accessKeyString.trim().isEmpty()) {
            options.add("access_key_id");
            options.add(accessKeyString);
        }
        if (!secretKeyString.trim().isEmpty()) {
            options.add("secret_access_key");
            options.add(secretKeyString);
        }
        if (!endpointString.trim().isEmpty()) {
            options.add("endpoint");
            options.add(endpointString);
        }
        if (!zoneString.equals(getString(R.string.qingstor_zone_spinner_prompt))) {
            options.add("zone");
            options.add(zoneString);
        }
        if (!connectionRetriesString.trim().isEmpty()) {
            options.add("connection_retries");
            options.add(connectionRetriesString);
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
