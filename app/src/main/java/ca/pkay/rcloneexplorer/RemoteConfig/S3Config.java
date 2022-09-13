package ca.pkay.rcloneexplorer.RemoteConfig;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import es.dmoral.toasty.Toasty;

public class S3Config extends Fragment {

    private Context context;
    private Rclone rclone;
    private TextInputLayout remoteNameInputLayout;
    private TextInputLayout accountInputLayout;
    private TextInputLayout keyInputLayout;
    private EditText remoteName;
    private EditText account;
    private EditText key;
    private EditText endpoint;
    private Spinner vendor;
    private View vendorLine;

    public S3Config() {}

    public static S3Config newInstance() { return new S3Config(); }

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
        accountInputLayout.setHint(getString(R.string.account_id_hint));
        account = accountTemplate.findViewById(R.id.edit_text);

        View keyTemplate = View.inflate(context, R.layout.config_form_template_edit_text, null);
        keyTemplate.setPadding(0, 0,0 , padding);
        ((ViewGroup) formContent).addView(keyTemplate);
        keyInputLayout = keyTemplate.findViewById(R.id.text_input_layout);
        keyInputLayout.setHint(getString(R.string.application_key_hint));
        key = keyTemplate.findViewById(R.id.edit_text);

        View endpointTemplate = View.inflate(context, R.layout.config_form_template_edit_text, null);
        endpointTemplate.setPadding(0, 0, 0, padding);
        ((ViewGroup) formContent).addView(endpointTemplate);
        TextInputLayout endpointInputLayout = endpointTemplate.findViewById(R.id.text_input_layout);
        endpointInputLayout.setHint(getString(R.string.endpoint_hint));
        endpoint = endpointTemplate.findViewById(R.id.edit_text);
        endpointTemplate.findViewById(R.id.helper_text).setVisibility(View.VISIBLE);

        view.findViewById(R.id.next).setOnClickListener(v -> setUpRemote());

        view.findViewById(R.id.cancel).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        View vendorTemplate = View.inflate(context, R.layout.config_form_template_spinner, null);
        ((ViewGroup) formContent).addView(vendorTemplate);
        vendor = vendorTemplate.findViewById(R.id.spinner);
        vendorLine = vendorTemplate.findViewById(R.id.spinner_line);
        String[] options = new String[]{ getString(R.string.s3_spinner_prompt),
                "AWS",
                "Alibaba",
                "Ceph",
                "DigitalOcean",
                "Dreamhost",
                "IBMCOS",
                "Minio",
                "Netease",
                "Scaleway",
                "StackPath",
                "TencentCOS",
                "Wasabi",
                "Other"};
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


        /*

        Todo:

        region,
        location_constraint,
        acl,
        server_side_encryption,
        sse_kms_key_id,
        storage_class
        */

        ArrayList<String> options = new ArrayList<>();
        options.add(name);
        options.add("s3");
        options.add("access_key_id");
        options.add(accountString);
        options.add("secret_access_key");
        options.add(keyString);
        options.add("provider");
        options.add(vendor.getSelectedItem().toString());

        if(vendor.getSelectedItem().toString().equals(getString(R.string.s3_spinner_prompt))) {
            Toasty.error(context, getString(R.string.s3_spinner_wrong_selection)).show();
            return;
        }


        if (!endpointString.trim().isEmpty()) {
            options.add("endpoint");
            options.add(endpointString);
        }

        RemoteConfigHelper.setupAndWait(context, options);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
