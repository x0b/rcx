package ca.pkay.rcloneexplorer.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;

import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.databinding.DialogPasswordGeneratorBinding;

public class PasswordGeneratorDialog extends DialogFragment {

    private final int MIN_PASSWORD_LENGTH = 4;
    private final int DEFAULT_PASSWORD_LENGTH = 16;
    private final int MAX_PASSWORD_LENGTH = 128;
    private final String SAVED_IS_DARK_THEME = "ca.pkay.rcexplorer.PasswordGeneratorDialog.IS_DARK_THEME";
    private final String SAVED_GENERATED_PASSWORD = "ca.pkay.rcexplorer.PasswordGeneratorDialog.GENERATED_PASSWORD";
    private final String SAVED_PASSWORD_LENGTH = "ca.pkay.rcexplorer.PasswordGeneratorDialog.PASSWORD_LENGTH";
    private final String SAVED_CHECKBOX_LOWERCASE = "ca.pkay.rcexplorer.PasswordGeneratorDialog.CHECKBOX_LOWERCASE";
    private final String SAVED_CHECKBOX_UPPERCASE = "ca.pkay.rcexplorer.PasswordGeneratorDialog.CHECKBOX_UPPERCASE";
    private final String SAVED_CHECKBOX_NUMBERS = "ca.pkay.rcexplorer.PasswordGeneratorDialog.CHECKBOX_NUMBERS";
    private final String SAVED_CHECKBOX_SPECIAL_CHARS = "ca.pkay.rcexplorer.PasswordGeneratorDialog.CHECKBOX_SPECIAL_CHARS";
    private Context context;
    private Callbacks callback;
    private String generatedPassword;
    private boolean isDarkTheme;
    private TextView passwordDisplay;
    private TextView passwordLength;
    private SeekBar seekBar;
    private CheckBox checkBoxLowerCase;
    private CheckBox checkBoxUpperCase;
    private CheckBox checkBoxNumbers;
    private CheckBox checkBoxSpecialChars;
    private DialogPasswordGeneratorBinding binding;

    private String[] lowerCase = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
    private String[] upperCase = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    private String[] numbers = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
    private String[] specialChars = {"!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "_", "-", "+", "{", "[", "}", "]", ":", ";", ",", ".", "?", "<", ">"};

    public interface Callbacks {
        void onPasswordSelected(String tag, String password);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            isDarkTheme = savedInstanceState.getBoolean(SAVED_IS_DARK_THEME);
        }

        callback = (Callbacks) getParentFragment();

        AlertDialog.Builder builder;
        if (isDarkTheme) {
            builder = new AlertDialog.Builder(context, R.style.DarkDialogTheme);
        } else {
            builder = new AlertDialog.Builder(context);
        }

        builder.setPositiveButton(R.string.set_password_button, (dialog, which) -> callback.onPasswordSelected(getTag(), generatedPassword));
        builder.setNegativeButton(R.string.cancel, null);

        binding = DialogPasswordGeneratorBinding.inflate(LayoutInflater.from(context));

        seekBar = binding.passwordLengthSeekbar;
        passwordLength = binding.passwordLength;
        checkBoxLowerCase = binding.checkboxLowerCase;
        checkBoxUpperCase = binding.checkboxUpperCase;
        checkBoxNumbers = binding.checkboxNumbers;
        checkBoxSpecialChars = binding.checkboxSpecialChars;

        passwordLength.setText(String.valueOf(DEFAULT_PASSWORD_LENGTH));

        seekBar.setProgress(DEFAULT_PASSWORD_LENGTH);
        seekBar.setMax(MAX_PASSWORD_LENGTH);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < MIN_PASSWORD_LENGTH) {
                    seekBar.setProgress(MIN_PASSWORD_LENGTH);
                    passwordLength.setText(String.valueOf(MIN_PASSWORD_LENGTH));
                } else {
                    passwordLength.setText(String.valueOf(progress));
                }
                generatePassword();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        checkBoxLowerCase.setOnCheckedChangeListener((buttonView, isChecked) -> generatePassword());
        checkBoxUpperCase.setOnCheckedChangeListener((buttonView, isChecked) -> generatePassword());
        checkBoxNumbers.setOnCheckedChangeListener((buttonView, isChecked) -> generatePassword());
        checkBoxSpecialChars.setOnCheckedChangeListener((buttonView, isChecked) -> generatePassword());

        binding.lowerCase.setOnClickListener(v -> checkBoxLowerCase.setChecked(!checkBoxLowerCase.isChecked()));
        binding.upperCase.setOnClickListener(v -> checkBoxUpperCase.setChecked(!checkBoxUpperCase.isChecked()));
        binding.numbers.setOnClickListener(v -> checkBoxNumbers.setChecked(!checkBoxNumbers.isChecked()));
        binding.specialChars.setOnClickListener(v -> checkBoxSpecialChars.setChecked(!checkBoxSpecialChars.isChecked()));
        binding.refreshPassword.setOnClickListener(v -> generatePassword());

        if (savedInstanceState != null) {
            checkBoxLowerCase.setChecked(savedInstanceState.getBoolean(SAVED_CHECKBOX_LOWERCASE));
            checkBoxUpperCase.setChecked(savedInstanceState.getBoolean(SAVED_CHECKBOX_UPPERCASE));
            checkBoxNumbers.setChecked(savedInstanceState.getBoolean(SAVED_CHECKBOX_NUMBERS));
            checkBoxSpecialChars.setChecked(savedInstanceState.getBoolean(SAVED_CHECKBOX_SPECIAL_CHARS));
            seekBar.setProgress(savedInstanceState.getInt(SAVED_PASSWORD_LENGTH));
            binding.password.setText(savedInstanceState.getString(SAVED_GENERATED_PASSWORD));
            generatedPassword = savedInstanceState.getString(SAVED_GENERATED_PASSWORD);
        } else {
            generatePassword();
        }

        builder.setView(binding.getRoot());
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_IS_DARK_THEME, isDarkTheme);
        outState.putString(SAVED_GENERATED_PASSWORD, generatedPassword);
        outState.putBoolean(SAVED_CHECKBOX_LOWERCASE, checkBoxLowerCase.isChecked());
        outState.putBoolean(SAVED_CHECKBOX_UPPERCASE, checkBoxUpperCase.isChecked());
        outState.putBoolean(SAVED_CHECKBOX_NUMBERS, checkBoxNumbers.isChecked());
        outState.putBoolean(SAVED_CHECKBOX_SPECIAL_CHARS, checkBoxSpecialChars.isChecked());
        outState.putInt(SAVED_PASSWORD_LENGTH, seekBar.getProgress());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public PasswordGeneratorDialog setDarkTheme(boolean isDarkTheme) {
        this.isDarkTheme = isDarkTheme;
        return this;
    }

    private void generatePassword() {
        if (!checkBoxLowerCase.isChecked() && !checkBoxUpperCase.isChecked() && !checkBoxNumbers.isChecked() && !checkBoxSpecialChars.isChecked()) {
            generatedPassword = "";
            binding.password.setText("");
            return;
        }

        ArrayList<String> charPool = new ArrayList<>();
        if (checkBoxLowerCase.isChecked()) {
            charPool.addAll(Arrays.asList(lowerCase));
        }
        if (checkBoxUpperCase.isChecked()) {
            charPool.addAll(Arrays.asList(upperCase));
        }
        if (checkBoxNumbers.isChecked()) {
            charPool.addAll(Arrays.asList(numbers));
        }
        if (checkBoxSpecialChars.isChecked()) {
            charPool.addAll(Arrays.asList(specialChars));
        }

        int length = Integer.parseInt(passwordLength.getText().toString());
        ArrayList<String> password = new ArrayList<>();
        SecureRandom random = new SecureRandom();

        while (password.size() < length) {
            password.add(charPool.get(random.nextInt(charPool.size())));
        }

        generatedPassword = TextUtils.join("", password);
        binding.password.setText(generatedPassword);
    }
}
