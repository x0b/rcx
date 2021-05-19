package ca.pkay.rcloneexplorer.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;

import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import ca.pkay.rcloneexplorer.databinding.DialogServeBinding;

public class ServeDialog extends DialogFragment {

    private Context context;
    private boolean isDarkTheme;
    private Callback callback;
    private DialogServeBinding binding;

    public interface Callback {
        void onServeOptionsSelected(int protocol, boolean allowRemoteAccess, String user, String password);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getParentFragment() != null) {
            callback = (Callback) getParentFragment();
        }

        if (savedInstanceState != null) {
            isDarkTheme = savedInstanceState.getBoolean("isDarkTheme");
        }

        AlertDialog.Builder builder;
        if (isDarkTheme) {
            builder = new AlertDialog.Builder(context, R.style.DarkDialogTheme);
        } else {
            builder = new AlertDialog.Builder(context);
        }

        binding = DialogServeBinding.inflate(LayoutInflater.from(context));

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (pref.contains(getString(R.string.pref_choice_serve_dialog_allow_ext))) {
            boolean checked = pref.getBoolean(getString(R.string.pref_choice_serve_dialog_allow_ext), false);
            binding.checkboxAllowRemoteAccess.setChecked(checked);
        }

        binding.textInputLayoutUser.setHint("Username");
        binding.textInputLayoutPassword.setHint("Password");

        builder.setTitle(R.string.serve_dialog_title);
        builder.setPositiveButton(R.string.ok, (dialog, which) -> sendCallback());
        builder.setNegativeButton(R.string.cancel, null);
        builder.setView(binding.getRoot());

        binding.checkboxAllowRemoteAccess.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isChecked) {
                Snackbar.make(btn, R.string.serve_dialog_remote_notice_enabled, Snackbar.LENGTH_LONG)
                        .show();
            } else {
                Snackbar.make(btn, R.string.serve_dialog_remote_notice_disabled, Snackbar.LENGTH_LONG)
                        .show();
            }
        });

        return builder.show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isDarkTheme", isDarkTheme);
        outState.putInt("protocol", binding.radioGroupProtocol.getCheckedRadioButtonId());
        outState.putBoolean("allowRemoteAccess", binding.checkboxAllowRemoteAccess.isChecked());
        if (!binding.editTextUser.getText().toString().trim().isEmpty()) {
            outState.putString("user", binding.editTextUser.getText().toString());
        }
        if (!binding.editTextPassword.getText().toString().trim().isEmpty()) {
            outState.putString("password", binding.editTextPassword.getText().toString());
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) {
            return;
        }

        binding.checkboxAllowRemoteAccess.setChecked(savedInstanceState.getBoolean("allowRemoteAccess", false));
        String savedUser = savedInstanceState.getString("user");
        if (savedUser != null) {
            binding.editTextUser.setText(savedUser);
        }

        String savedPassword = savedInstanceState.getString("password");
        if (savedPassword != null) {
            binding.editTextPassword.setText(savedPassword);
        }

        int savedProtocol = savedInstanceState.getInt("protocol", -1);
        if (savedProtocol == R.id.radio_http || savedProtocol == R.id.radio_dlna
                || savedProtocol == R.id.radio_webdav || savedProtocol == R.id.radio_ftp) {
            binding.radioGroupProtocol.check(savedProtocol);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;

        if (context instanceof Callback) {
            callback = (Callback) context;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public ServeDialog setDarkTheme(boolean isDarkTheme) {
        this.isDarkTheme = isDarkTheme;
        return this;
    }

    private void sendCallback() {
        int selectedProtocolId = binding.radioGroupProtocol.getCheckedRadioButtonId();
        int selectedProtocol;
        switch (selectedProtocolId) {
            case R.id.radio_ftp:
                selectedProtocol = Rclone.SERVE_PROTOCOL_FTP;
                break;
            case R.id.radio_dlna:
                selectedProtocol = Rclone.SERVE_PROTOCOL_DLNA;
                break;
            case R.id.radio_webdav:
                selectedProtocol = Rclone.SERVE_PROTOCOL_WEBDAV;
                break;
            case R.id.radio_http:
            default:
                selectedProtocol = Rclone.SERVE_PROTOCOL_HTTP;
                break;
        }

        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(getString(R.string.pref_choice_serve_dialog_allow_ext), binding.checkboxAllowRemoteAccess.isChecked())
                .apply();

        callback.onServeOptionsSelected(selectedProtocol,
                binding.checkboxAllowRemoteAccess.isChecked(),
                binding.editTextUser.getText().toString(),
                binding.editTextPassword.getText().toString());
    }
}
