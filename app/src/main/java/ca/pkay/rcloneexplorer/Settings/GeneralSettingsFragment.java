package ca.pkay.rcloneexplorer.Settings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import ca.pkay.rcloneexplorer.AppShortcutsHelper;
import ca.pkay.rcloneexplorer.Items.RemoteItem;
import ca.pkay.rcloneexplorer.R;
import ca.pkay.rcloneexplorer.Rclone;
import ca.pkay.rcloneexplorer.util.FLog;
import es.dmoral.toasty.Toasty;

public class GeneralSettingsFragment extends Fragment {

    private static final String TAG = "GeneralSettingsFragment";
    private Context context;
    private View appShortcutsElement;
    private View showThumbnailsElement;
    private Switch showThumbnailsSwitch;
    private View wifiOnlyElement;
    private Switch wifiOnlySwitch;
    private boolean isDarkTheme;

    private Switch useProxySwitch;
    private View useProxyElement;
    private View proxyProtocolElement;
    private TextView proxyProtocolSummary;
    private View proxyHostElement;
    private TextView proxyHostSummary;
    private View proxyPortElement;
    private TextView proxyPortSummary;
    private View thumbnailSizeElement;
    private TextView thumbnailSizeSummary;
    private View localeElement;
    private TextView localeSummary;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public GeneralSettingsFragment() {
    }

    public static GeneralSettingsFragment newInstance() {
        return new GeneralSettingsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.general_settings_fragment, container, false);
        getViews(view);
        setDefaultStates();
        setClickListeners();

        if (getActivity() != null) {
            getActivity().setTitle(getString(R.string.pref_header_general));
        }

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
        this.context = null;
    }

    private void getViews(View view) {
        appShortcutsElement = view.findViewById(R.id.app_shortcuts);
        showThumbnailsElement = view.findViewById(R.id.show_thumbnails);
        showThumbnailsSwitch = view.findViewById(R.id.show_thumbnails_switch);
        wifiOnlyElement = view.findViewById(R.id.wifi_only);
        wifiOnlySwitch = view.findViewById(R.id.wifi_only_switch);
        useProxyElement = view.findViewById(R.id.use_proxy);
        useProxySwitch = view.findViewById(R.id.use_proxy_switch);
        proxyProtocolElement = view.findViewById(R.id.proxy_protocol);
        proxyProtocolSummary = view.findViewById(R.id.proxy_protocol_summary);
        proxyHostElement = view.findViewById(R.id.proxy_host);
        proxyHostSummary = view.findViewById(R.id.proxy_host_summary);
        proxyPortElement = view.findViewById(R.id.proxy_port);
        proxyPortSummary = view.findViewById(R.id.proxy_port_summary);
        thumbnailSizeElement = view.findViewById(R.id.thumbnail_size);
        thumbnailSizeSummary = view.findViewById(R.id.thumbnail_size_summary);
        localeElement = view.findViewById(R.id.locale_container);
        localeSummary = view.findViewById(R.id.locale_summary);
    }
    
    private void setDefaultStates() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean showThumbnails = sharedPreferences.getBoolean(getString(R.string.pref_key_show_thumbnails), false);
        boolean isWifiOnly = sharedPreferences.getBoolean(getString(R.string.pref_key_wifi_only_transfers), false);
        isDarkTheme = sharedPreferences.getBoolean(getString(R.string.pref_key_dark_theme), false);
        boolean useProxy = sharedPreferences.getBoolean(getString(R.string.pref_key_use_proxy), false);
        String proxyProtocol = sharedPreferences.getString(getString(R.string.pref_key_proxy_protocol), "http");
        String proxyHost = sharedPreferences.getString(getString(R.string.pref_key_proxy_host), "localhost");
        int proxyPort = sharedPreferences.getInt(getString(R.string.pref_key_proxy_port), 8080);
        // TODO: build ui
        //String noProxyHosts = sharedPreferences.getString(getString(R.string.pref_key_no_proxy_hosts), "localhost");

        showThumbnailsSwitch.setChecked(showThumbnails);
        wifiOnlySwitch.setChecked(isWifiOnly);
        useProxySwitch.setChecked(useProxy);
        proxyProtocolSummary.setText(proxyProtocol);
        proxyHostSummary.setText(proxyHost);
        proxyPortSummary.setText(String.valueOf(proxyPort));
        if(!useProxy) {
            proxyProtocolElement.setVisibility(View.GONE);
            proxyHostElement.setVisibility(View.GONE);
            proxyPortElement.setVisibility(View.GONE);
        }

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1) {
            appShortcutsElement.setVisibility(View.GONE);
        }
        long thumbnailSizeLimit = sharedPreferences.getLong(getString(R.string.pref_key_thumbnail_size_limit),
                getResources().getInteger(R.integer.default_thumbnail_size_limit));
        thumbnailSizeSummary.setText(getString(R.string.pref_thumbnails_size_summary,
                thumbnailSizeLimit / (1024 * 1024d)));
        if(showThumbnails) {
            thumbnailSizeElement.setVisibility(View.VISIBLE);
        }

        if (sharedPreferences.contains(getString(R.string.pref_key_locale))) {
            String localeTag = sharedPreferences.getString(getString(R.string.pref_key_locale), "en-US");
            Locale locale = Locale.forLanguageTag(localeTag);
            localeSummary.setText(locale.getDisplayLanguage());
        } else {
            localeSummary.setText(getString(R.string.pref_locale_not_set));
        }
    }
    
    private void setClickListeners() {
        showThumbnailsElement.setOnClickListener(v -> {
            if (showThumbnailsSwitch.isChecked()) {
                showThumbnailsSwitch.setChecked(false);
            } else {
                showThumbnailsSwitch.setChecked(true);
            }
        });
        showThumbnailsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> showThumbnails(isChecked));
        appShortcutsElement.setOnClickListener(v -> showAppShortcutDialog());
        wifiOnlyElement.setOnClickListener(v -> {
            if (wifiOnlySwitch.isChecked()) {
                wifiOnlySwitch.setChecked(false);
            } else {
                wifiOnlySwitch.setChecked(true);
            }
        });
        wifiOnlySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> setWifiOnlyTransfers(isChecked));
        useProxyElement.setOnClickListener(v -> {
            if(useProxySwitch.isChecked()) {
                useProxySwitch.setChecked(false);
            } else {
                useProxySwitch.setChecked(true);
            }
        });
        useProxySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> setUseProxy(isChecked));
        proxyProtocolElement.setOnClickListener(v -> showProxyProtocolMenu());
        proxyHostElement.setOnClickListener(v -> showProxyHostMenu());
        proxyPortElement.setOnClickListener(v -> showProxyPortMenu());
        thumbnailSizeElement.setOnClickListener(v -> showThumbnailSizeDialog());
        localeElement.setOnClickListener(v -> showLocaleDialog());
    }

    private void showThumbnails(boolean isChecked) {
        if (isChecked) {
            thumbnailSizeElement.setVisibility(View.VISIBLE);
        } else {
            thumbnailSizeElement.setVisibility(View.GONE);
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_key_show_thumbnails), isChecked);
        editor.apply();
    }

    private void showAppShortcutDialog() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1) {
            return;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> appShortcuts = sharedPreferences.getStringSet(getString(R.string.shared_preferences_app_shortcuts), new HashSet<>());

        AlertDialog.Builder builder;
        if (isDarkTheme) {
            builder = new AlertDialog.Builder(context, R.style.DarkDialogTheme);
        } else {
            builder = new AlertDialog.Builder(context);
        }

        builder.setTitle(R.string.app_shortcuts_settings_dialog_title);

        Rclone rclone = new Rclone(context);
        final ArrayList<RemoteItem> remotes = new ArrayList<>(rclone.getRemotes());
        RemoteItem.prepareDisplay(context, remotes);
        Collections.sort(remotes, (a, b) -> a.getDisplayName().compareTo(b.getDisplayName()));
        final CharSequence[] options = new CharSequence[remotes.size()];
        int i = 0;
        for (RemoteItem remoteItem : remotes) {
            options[i++] = remoteItem.getDisplayName();
        }

        final ArrayList<String> userSelected = new ArrayList<>();
        boolean[] checkedItems = new boolean[options.length];
        i = 0;
        for (RemoteItem item : remotes) {
            String s = item.getName().toString();
            String hash = AppShortcutsHelper.getUniqueIdFromString(s);
            if (appShortcuts.contains(hash)) {
                userSelected.add(item.getName().toString());
                checkedItems[i] = true;
            }
            i++;
        }

        builder.setMultiChoiceItems(options, checkedItems, (dialog, which, isChecked) -> {
            if (userSelected.size() >= 4 && isChecked) {
                Toasty.info(context, getString(R.string.app_shortcuts_max_toast), Toast.LENGTH_SHORT, true).show();
                //((AlertDialog)dialog).getListView().setItemChecked(which, false); This doesn't work
            }
            if (isChecked) {
                userSelected.add(options[which].toString());
            } else {
                userSelected.remove(options[which].toString());
            }
        });

        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.select, (dialog, which) -> setAppShortcuts(remotes, userSelected));

        builder.show();
    }

    private void setAppShortcuts(ArrayList<RemoteItem> remoteItems, ArrayList<String> appShortcuts) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1) {
            return;
        }

        if (appShortcuts.size() > 4) {
            appShortcuts = new ArrayList<>(appShortcuts.subList(0, 4));
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> savedAppShortcutIds = sharedPreferences.getStringSet(getString(R.string.shared_preferences_app_shortcuts), new HashSet<>());
        Set<String> updatedAppShortcutIDds = new HashSet<>(savedAppShortcutIds);

        // Remove app shortcuts first
        ArrayList<String> appShortcutIds= new ArrayList<>();
        for (String s : appShortcuts) {
            appShortcutIds.add(AppShortcutsHelper.getUniqueIdFromString(s));
        }
        List<String> removedIds = new ArrayList<>(savedAppShortcutIds);
        removedIds.removeAll(appShortcutIds);
        if (!removedIds.isEmpty()) {
            AppShortcutsHelper.removeAppShortcutIds(context, removedIds);
        }

        updatedAppShortcutIDds.removeAll(removedIds);

        // add new app shortcuts
        for (String appShortcut : appShortcuts) {
            String id = AppShortcutsHelper.getUniqueIdFromString(appShortcut);
            if (updatedAppShortcutIDds.contains(id)) {
                continue;
            }

            RemoteItem remoteItem = null;
            for (RemoteItem item : remoteItems) {
                if (item.getName().equals(appShortcut)) {
                    remoteItem = item;
                    break;
                }
            }
            if (remoteItem == null) {
                continue;
            }

            AppShortcutsHelper.addRemoteToAppShortcuts(context, remoteItem, id);
            updatedAppShortcutIDds.add(id);
        }

        editor.putStringSet(getString(R.string.shared_preferences_app_shortcuts), updatedAppShortcutIDds);
        editor.apply();
    }

    private void setWifiOnlyTransfers(boolean isChecked) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_key_wifi_only_transfers), isChecked);
        editor.apply();
    }

    private void setUseProxy(boolean isChecked) {
        if(isChecked) {
            proxyProtocolElement.setVisibility(View.VISIBLE);
            proxyHostElement.setVisibility(View.VISIBLE);
            proxyPortElement.setVisibility(View.VISIBLE);
        } else {
            proxyProtocolElement.setVisibility(View.GONE);
            proxyHostElement.setVisibility(View.GONE);
            proxyPortElement.setVisibility(View.GONE);
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_key_use_proxy), isChecked);
        editor.apply();
    }

    private void showProxyProtocolMenu() {
        AlertDialog.Builder builder;
        if (isDarkTheme) {
            builder = new AlertDialog.Builder(context, R.style.DarkDialogTheme);
        } else {
            builder = new AlertDialog.Builder(context);
        }

        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final List<String> proxyProtocols = Arrays.asList(context.getResources().getStringArray(R.array.proxy_protocols));

        int initialSelection = proxyProtocols.indexOf(
                                pref.getString(context.getString(R.string.pref_key_proxy_protocol), "http"));

        builder.setTitle(R.string.pref_proxy_protocol_dlg_title);
        final int[] userSelected = new int[1];
        builder.setSingleChoiceItems(R.array.proxy_protocols, initialSelection, (dialog, which) -> userSelected[0] = which);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.select, (dialog, which) -> {
            String protocol = proxyProtocols.get(userSelected[0]);
            pref.edit().putString(getString(R.string.pref_key_proxy_protocol), protocol).apply();
            if(proxyProtocolSummary != null) {
                proxyProtocolSummary.setText(protocol);
            }
        });

        builder.show();
    }

    private void showProxyHostMenu() {
        AlertDialog.Builder builder;
        if (isDarkTheme) {
            builder = new AlertDialog.Builder(context, R.style.DarkDialogTheme);
        } else {
            builder = new AlertDialog.Builder(context);
        }

        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final EditText proxyHostEdit = new EditText(context);
        String initialText = pref.getString(context.getString(R.string.pref_key_proxy_host), "localhost");
        proxyHostEdit.setText(initialText);

        builder.setTitle(R.string.pref_proxy_host_dlg_title);
        builder.setView(proxyHostEdit);

        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.select, (dialog, which) -> {
            String host = proxyHostEdit.getText().toString();
            pref.edit().putString(getString(R.string.pref_key_proxy_host), host).apply();
            if(null != proxyHostSummary) {
                proxyHostSummary.setText(host);
            }
        });

        builder.show();
    }

    private void showProxyPortMenu() {
        AlertDialog.Builder builder;
        if (isDarkTheme) {
            builder = new AlertDialog.Builder(context, R.style.DarkDialogTheme);
        } else {
            builder = new AlertDialog.Builder(context);
        }

        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final EditText proxyPortEdit = new EditText(context);
        proxyPortEdit.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        int initialPort = pref.getInt(context.getString(R.string.pref_key_proxy_port), 8080);
        proxyPortEdit.setText(String.valueOf(initialPort));

        builder.setTitle(R.string.pref_proxy_port_dlg_title);
        builder.setView(proxyPortEdit);

        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.select, (dialog, which) -> {
            String portString = proxyPortEdit.getText().toString();
            int port;
            try {
                port = Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                FLog.e(TAG, "showProxyPortMenu: invalid port", e);
                return;
            }
            pref.edit().putInt(getString(R.string.pref_key_proxy_port), port).apply();
            if(null != proxyPortSummary) {
                proxyPortSummary.setText(String.valueOf(port));
            }
        });

        builder.show();
    }

    private void showThumbnailSizeDialog() {
        AlertDialog.Builder builder;
        if (isDarkTheme) {
            builder = new AlertDialog.Builder(context, R.style.DarkDialogTheme);
        } else {
            builder = new AlertDialog.Builder(context);
        }

        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final EditText thumbnailSizeEdit = new EditText(context);
        thumbnailSizeEdit.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        long size = pref.getLong(context.getString(R.string.pref_key_thumbnail_size_limit),
                getResources().getInteger(R.integer.default_thumbnail_size_limit));
        thumbnailSizeEdit.setText(String.valueOf(size / (1024 * 1024d)));

        builder.setTitle(R.string.pref_thumbnails_dlg_title);
        builder.setView(thumbnailSizeEdit);

        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.select, (dialog, which) -> {
            String sizeString = thumbnailSizeEdit.getText().toString();
            long size1;
            double sizeMb;
            try {
                sizeMb = Double.parseDouble(sizeString);
                size1 = (long)(sizeMb * 1024 * 1024);
            } catch (NumberFormatException e) {
                FLog.e(TAG, "showThumbnailSizeDialog: invalid size", e);
                return;
            }
            pref.edit().putLong(getString(R.string.pref_key_thumbnail_size_limit), size1).apply();
            if(null != thumbnailSizeSummary) {
                thumbnailSizeSummary.setText(getResources().getString(R.string.pref_thumbnails_size_summary, sizeMb));
            }
        });

        builder.show();
    }

    private void showLocaleDialog() {
        AlertDialog.Builder builder;
        if (isDarkTheme) {
            builder = new AlertDialog.Builder(context, R.style.DarkDialogTheme);
        } else {
            builder = new AlertDialog.Builder(context);
        }

        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final List<String> locales = Arrays.asList(context.getResources().getStringArray(R.array.locales));

        int initialSelection = locales.indexOf(
                pref.getString(context.getString(R.string.pref_key_locale), "en-US"));

        builder.setTitle(R.string.pref_locale_dlg_title);
        final int[] userSelected = new int[1];
        builder.setSingleChoiceItems(R.array.locales, initialSelection, (dialog, which) -> userSelected[0] = which);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.select, (dialog, which) -> {
            String locale = locales.get(userSelected[0]);
            pref.edit().putString(getString(R.string.pref_key_locale), locale).apply();
            if(null != localeSummary) {
                localeSummary.setText(Locale.forLanguageTag(locale).getDisplayLanguage());
                Toasty.normal(context, getString(R.string.pref_locale_restart_notice), Toast.LENGTH_LONG).show();
            }
        });

        builder.show();
    }
}
