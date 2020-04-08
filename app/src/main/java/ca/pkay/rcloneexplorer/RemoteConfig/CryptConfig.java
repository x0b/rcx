package ca.pkay.rcloneexplorer.RemoteConfig;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ca.pkay.rcloneexplorer.Dialogs.PasswordGeneratorDialog;
import ca.pkay.rcloneexplorer.Dialogs.RemoteDestinationDialog;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import es.dmoral.toasty.Toasty;

public class CryptConfig extends Fragment implements    PasswordGeneratorDialog.Callbacks,
                                                        RemoteDestinationDialog.OnDestinationSelectedListener {

    private final String SAVED_DIR_ENCRYPT = "ca.pkay.rcexplorer.CryptConfig.DIR_ENCRYPT";
    private final String SAVED_REMOTE_PATH = "ca.pkay.rcexplorer.CryptConfig.REMOTE_PATH";
    private final String SAVED_PASSWORD = "ca.pkay.rcexplorer.CryptConfig.PASSWORD";
    private final String SAVED_PASSWORD2 = "ca.pkay.rcexplorer.CryptConfig.PASSWORD2";
    private final String SAVED_SELECTED_REMOTE = "ca.pkay.rcexplorer.CryptConfig.SELECTED_REMOTE";
    private Context context;
    private Rclone rclone;
    private TextInputLayout remoteNameInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputLayout password2InputLayout;
    private EditText remoteName;
    private EditText password;
    private EditText password2;
    private TextView remote;
    private View remoteSelectorLine;
    private RemoteItem selectedRemote;
    private String remotePath;
    private Spinner filenameEncryption;
    private View spinnerLine;
    private String directoryEncryption;
    private boolean isDarkTheme;

    public CryptConfig() {}

    public static CryptConfig newInstance() { return new CryptConfig(); }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext() == null) {
            return;
        }
        context = getContext();
        rclone = new Rclone(context);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        isDarkTheme = sharedPreferences.getBoolean(getString(R.string.pref_key_dark_theme), false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.remote_config_form, container, false);
        setUpForm(view);
        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (selectedRemote != null) {
            outState.putParcelable(SAVED_SELECTED_REMOTE, selectedRemote);
        }
        if (directoryEncryption != null) {
            outState.putString(SAVED_DIR_ENCRYPT, directoryEncryption);
        }
        if (remotePath != null) {
            outState.putString(SAVED_REMOTE_PATH, remotePath);
        }
        outState.putString(SAVED_PASSWORD, password.getText().toString());
        outState.putString(SAVED_PASSWORD2, password2.getText().toString());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) {
            return;
        }

        selectedRemote = savedInstanceState.getParcelable(SAVED_SELECTED_REMOTE);

        String savedDirEncryption = savedInstanceState.getString(SAVED_DIR_ENCRYPT);
        if (savedDirEncryption != null) {
            directoryEncryption = savedDirEncryption;
        }
        String savedRemotePath = savedInstanceState.getString(SAVED_REMOTE_PATH);
        if (savedRemotePath != null) {
            remotePath = savedRemotePath;
            remote.setText(remotePath);
        }
        String savedPassword = savedInstanceState.getString(SAVED_PASSWORD);
        if (savedPassword != null) {
            password.setText(savedPassword);
        }
        String savedPassword2 = savedInstanceState.getString(SAVED_PASSWORD2);
        if (savedPassword2 != null) {
            password2.setText(savedPassword2);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpForm(final View view) {
        ViewGroup formContent = view.findViewById(R.id.form_content);
        int padding = getResources().getDimensionPixelOffset(R.dimen.config_form_template);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, padding);
        remoteNameInputLayout = view.findViewById(R.id.remote_name_layout);
        remoteNameInputLayout.setVisibility(View.VISIBLE);
        remoteName = view.findViewById(R.id.remote_name);

        View remoteSelectorTemplate = View.inflate(context, R.layout.config_form_template_text_field, null);
        remoteSelectorTemplate.setLayoutParams(params);
        formContent.addView(remoteSelectorTemplate);
        View remoteSelector = remoteSelectorTemplate.findViewById(R.id.remote_selector);
        remoteSelectorLine = remoteSelectorTemplate.findViewById(R.id.text_view_line);
        remote = remoteSelectorTemplate.findViewById(R.id.text_view);
        remote.setText(R.string.encrypt_remote_hint);
        remoteSelector.setOnClickListener(v -> setRemote());

        View passwordTemplate = View.inflate(context, R.layout.config_form_template_password, null);
        passwordTemplate.setPadding(0, 0, 0, padding);
        formContent.addView(passwordTemplate);
        passwordInputLayout = passwordTemplate.findViewById(R.id.pass_input_layout);
        passwordInputLayout.setHintEnabled(true);
        passwordInputLayout.setHint(getString(R.string.crypt_pass_hint));
        password = passwordTemplate.findViewById(R.id.pass);
        password.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if(event.getAction() == MotionEvent.ACTION_UP) {
                if(event.getRawX() >= (password.getRight() - password.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    showPasswordGenerator(0);
                    return true;
                }
            }
            return false;
        });

        View password2Template = View.inflate(context, R.layout.config_form_template_password, null);
        password2Template.setPadding(0, 0, 0, padding);
        formContent.addView(password2Template);
        password2InputLayout = password2Template.findViewById(R.id.pass_input_layout);
        password2InputLayout.setHintEnabled(true);
        password2InputLayout.setHint(getString(R.string.crypt_pass2_hint));
        TextView pass2HelperText = password2Template.findViewById(R.id.helper_text);
        pass2HelperText.setVisibility(View.VISIBLE);
        pass2HelperText.setText(R.string.crypt_pass2_helper_text);
        password2 = password2Template.findViewById(R.id.pass);
        password2.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if(event.getAction() == MotionEvent.ACTION_UP) {
                if(event.getRawX() >= (password.getRight() - password.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    showPasswordGenerator(1);
                    return true;
                }
            }
            return false;
        });

        View showPasswordsTemplate = View.inflate(context, R.layout.config_form_show_password, null);
        showPasswordsTemplate.setPadding(0, 0, 0, padding);
        formContent.addView(showPasswordsTemplate);
        final CheckBox showPasswordsSwitch = showPasswordsTemplate.findViewById(R.id.show_password_checkbox);
        TextView showPasswords = showPasswordsTemplate.findViewById(R.id.show_password);
        showPasswordsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                password2.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                password2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });
        showPasswords.setOnClickListener(v -> showPasswordsSwitch.setChecked(!showPasswordsSwitch.isChecked()));

        View spinnerTemplate = View.inflate(context, R.layout.config_form_template_spinner, null);
        spinnerTemplate.setPadding(0, 0, 0, padding);
        formContent.addView(spinnerTemplate);
        filenameEncryption = spinnerTemplate.findViewById(R.id.spinner);
        spinnerLine = spinnerTemplate.findViewById(R.id.spinner_line);
        String[] options = getResources().getStringArray(R.array.filename_encryption_spinner_labels);
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
        filenameEncryption.setAdapter(adapter);

        View switchTemplate = View.inflate(context, R.layout.config_form_template_switch, null);
        switchTemplate.setLayoutParams(params);
        formContent.addView(switchTemplate);
        directoryEncryption = "false";
        ((Switch)switchTemplate.findViewById(R.id.flip_switch)).setChecked(false);
        switchTemplate.findViewById(R.id.switch_layout).setOnClickListener(v -> {
            Switch s = v.findViewById(R.id.flip_switch);
            s.setChecked(!s.isChecked());
        });
        ((Switch)switchTemplate.findViewById(R.id.flip_switch)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            Switch s = buttonView.findViewById(R.id.flip_switch);
            directoryEncryption = (s.isChecked()) ? "true" : "false";
        });
        ((TextView)switchTemplate.findViewById(R.id.switch_text)).setText(R.string.directory_name_encryption_hint);

        view.findViewById(R.id.next).setOnClickListener(v -> setUpRemote());

        view.findViewById(R.id.cancel).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }

    private void showPasswordGenerator(int editTextField) {
        PasswordGeneratorDialog passwordGeneratorDialog = new PasswordGeneratorDialog()
                .setDarkTheme(isDarkTheme);
        String tag = (editTextField == 0) ? "password" : "password2";
        if (getFragmentManager() != null) {
            passwordGeneratorDialog.show(getChildFragmentManager(), tag);
        }
    }

    @Override
    public void onPasswordSelected(String tag, String generatedPassword) {
        if (generatedPassword.trim().isEmpty()) {
            return;
        }
        switch (tag) {
            case "password":
                password.setText(generatedPassword);
                break;
            case "password2":
                password2.setText(generatedPassword);
                break;
        }
    }

    private void setRemote() {
        final List<RemoteItem> remotes = rclone.getRemotes();
        if (remotes.isEmpty()) {
            Toasty.info(context, getString(R.string.crypt_config_error_no_remotes), Toast.LENGTH_SHORT, true).show();
            return;
        }

        RemoteItem.prepareDisplay(context, remotes);
        Collections.sort(remotes, (a, b) -> a.getDisplayName().compareTo(b.getDisplayName()));
        String[] options = new String[remotes.size()];
        int i = 0;
        for (RemoteItem remote : remotes) {
            options[i++] = remote.getDisplayName();
        }
        
        AlertDialog.Builder builder;
        if (isDarkTheme) {
            builder = new AlertDialog.Builder(context, R.style.DarkDialogTheme);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        builder.setTitle(R.string.select_remote)
                .setNegativeButton(R.string.cancel, (dialog, which) -> selectedRemote = null)
                .setPositiveButton(R.string.select, (dialog, which) -> {
                    if (selectedRemote == null) {
                        Toasty.info(context, getString(R.string.nothing_selected), Toast.LENGTH_SHORT, true).show();
                        setRemote();
                    } else {
                        setPath();
                    }
                })
                .setSingleChoiceItems(options, -1, (dialog, which) -> selectedRemote = remotes.get(which))
                .show();
    }

    private void setPath() {
        RemoteDestinationDialog remoteDestinationDialog = new RemoteDestinationDialog()
                .setDarkTheme(isDarkTheme)
                .setRemote(selectedRemote)
                .setTitle(R.string.select_path_to_crypt);
        remoteDestinationDialog.show(getChildFragmentManager(), "remote destination dialog");

    }

    /*
     * RemoteDestinationDialog callback
     */
    @Override
    public void onDestinationSelected(String path) {
        remotePath = RemoteConfigHelper.getRemotePath(path, selectedRemote);
        remote.setText(remotePath);
    }

    private void setUpRemote() {
        String name = remoteName.getText().toString();
        String passString = password.getText().toString();
        String pass2String = password2.getText().toString();
        String[] encOptions = getResources().getStringArray(R.array.filename_encryption_spinner_values);
        String filenameEncryptionString = encOptions[filenameEncryption.getSelectedItemPosition()];
        boolean error = false;

        if (name.trim().isEmpty()) {
            remoteNameInputLayout.setErrorEnabled(true);
            remoteNameInputLayout.setError(getString(R.string.remote_name_cannot_be_empty));
            error = true;
        } else {
            remoteNameInputLayout.setErrorEnabled(false);
        }
        if (remotePath == null) {
            remoteSelectorLine.setBackgroundColor(Color.parseColor("#B14525"));
            error = true;
        }
        if (filenameEncryption.getSelectedItemPosition() < 1) {
            spinnerLine.setBackgroundColor(Color.parseColor("#B14525"));
            error = true;
        }
        if (passString.trim().isEmpty()) {
            passwordInputLayout.setErrorEnabled(true);
            passwordInputLayout.setError(getString(R.string.required_field));
            error = true;
        } else {
            passwordInputLayout.setErrorEnabled(false);
        }
        if (error) {
            return;
        }

        ArrayList<String> options = new ArrayList<>();
        options.add(name);
        options.add("crypt");
        options.add("remote");
        options.add(remotePath);
        options.add("password");
        options.add(rclone.obscure(passString));
        if (!pass2String.trim().isEmpty()) {
            options.add("password2");
            options.add(rclone.obscure(pass2String));
        }
        options.add("filename_encryption");
        options.add(filenameEncryptionString);
        options.add("directory_name_encryption");
        options.add(directoryEncryption);

        RemoteConfigHelper.setupAndWait(context, options);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
