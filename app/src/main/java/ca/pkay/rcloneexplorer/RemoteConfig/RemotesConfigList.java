package ca.pkay.rcloneexplorer.RemoteConfig;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import ca.pkay.rcloneexplorer.R;

public class RemotesConfigList extends Fragment {

    public interface ProviderSelectedListener {
        void onProviderSelected(int provider);
    }

    public static final ArrayList<String> providers = new ArrayList<>(Arrays.asList("AZUREBLOB", "QINGSTOR", "ALIAS", "CRYPT", "ONEDRIVE", "WEBDAV", "B2", "BOX", "FTP", "HTTP", "HUBIC", "PCLOUD", "SFTP", "YANDEX", "DROPBOX"));
    private int[] selected = {-1};
    private RadioButton lastSelected;
    private ProviderSelectedListener listener;
    private Context context;

    public RemotesConfigList() {}

    public static RemotesConfigList newInstance() { return new RemotesConfigList(); }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_config_list, container, false);
        setClickListeners(view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ProviderSelectedListener) {
            listener = (ProviderSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ProviderSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private void setSelected(RadioButton radioButton, String provider) {
        if (lastSelected != null) {
            lastSelected.setChecked(false);
        }
        radioButton.setChecked(true);
        lastSelected = radioButton;
        selected[0] = providers.indexOf(provider);
    }

    private void setClickListeners(View view) {
        ViewGroup listContent = view.findViewById(R.id.config_content);

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });
        view.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onProviderSelected(selected[0]);
            }
        });

        View providerBox = View.inflate(context, R.layout.config_list_item_template, null);
        ((TextView)providerBox.findViewById(R.id.provider_tv)).setText(R.string.provider_box);
        providerBox.findViewById(R.id.provider).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.provider_rb);
                setSelected(rb, "BOX");
            }
        });

        View providerB2 = View.inflate(context, R.layout.config_list_item_template, null);
        ((TextView)providerB2.findViewById(R.id.provider_tv)).setText(R.string.provider_b2);
        providerB2.findViewById(R.id.provider).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.provider_rb);
                setSelected(rb, "B2");
            }
        });

        View providerDropbox = View.inflate(context, R.layout.config_list_item_template, null);
        ((TextView)providerDropbox.findViewById(R.id.provider_tv)).setText(R.string.provider_dropbox);
        providerDropbox.findViewById(R.id.provider).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.provider_rb);
                setSelected(rb, "DROPBOX");
            }
        });

        View providerFTP = View.inflate(context, R.layout.config_list_item_template, null);
        ((TextView)providerFTP.findViewById(R.id.provider_tv)).setText(R.string.provider_ftp);
        providerFTP.findViewById(R.id.provider).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.provider_rb);
                setSelected(rb, "FTP");
            }
        });

        View providerHTTP = View.inflate(context, R.layout.config_list_item_template, null);
        ((TextView)providerHTTP.findViewById(R.id.provider_tv)).setText(R.string.provider_http);
        providerHTTP.findViewById(R.id.provider).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.provider_rb);
                setSelected(rb, "HTTP");
            }
        });

        View providerHubic = View.inflate(context, R.layout.config_list_item_template, null);
        ((TextView)providerHubic.findViewById(R.id.provider_tv)).setText(R.string.provider_hubic);
        providerHubic.findViewById(R.id.provider).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.provider_rb);
                setSelected(rb, "HUBIC");
            }
        });

        View providerPcloud = View.inflate(context, R.layout.config_list_item_template, null);
        ((TextView)providerPcloud.findViewById(R.id.provider_tv)).setText(R.string.provider_pcloud);
        providerPcloud.findViewById(R.id.provider).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.provider_rb);
                setSelected(rb, "PCLOUD");
            }
        });

        View providerSFTP = View.inflate(context, R.layout.config_list_item_template, null);
        ((TextView)providerSFTP.findViewById(R.id.provider_tv)).setText(R.string.provider_sftp);
        providerSFTP.findViewById(R.id.provider).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.provider_rb);
                setSelected(rb, "SFTP");
            }
        });

        View providerYandex = View.inflate(context, R.layout.config_list_item_template, null);
        ((TextView)providerYandex.findViewById(R.id.provider_tv)).setText(R.string.provider_yandex);
        providerYandex.findViewById(R.id.provider).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.provider_rb);
                setSelected(rb, "BOX");
            }
        });

        View providerWebdav = View.inflate(context, R.layout.config_list_item_template, null);
        ((TextView)providerWebdav.findViewById(R.id.provider_tv)).setText(R.string.provider_webdav);
        providerWebdav.findViewById(R.id.provider).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.provider_rb);
                setSelected(rb, "WEBDAV");
            }
        });

        View providerOneDrive = View.inflate(context, R.layout.config_list_item_template, null);
        ((TextView)providerOneDrive.findViewById(R.id.provider_tv)).setText(R.string.provider_onedrive);
        providerOneDrive.findViewById(R.id.provider).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.provider_rb);
                setSelected(rb, "ONEDRIVE");
            }
        });

        View providerAlias = View.inflate(context, R.layout.config_list_item_template, null);
        ((TextView)providerAlias.findViewById(R.id.provider_tv)).setText(R.string.provider_alias);
        providerAlias.findViewById(R.id.provider).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.provider_rb);
                setSelected(rb, "ALIAS");
            }
        });

        View providerCrypt = View.inflate(context, R.layout.config_list_item_template, null);
        ((TextView)providerCrypt.findViewById(R.id.provider_tv)).setText(R.string.provider_crypt);
        providerCrypt.findViewById(R.id.provider).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.provider_rb);
                setSelected(rb, "CRYPT");
            }
        });

        View providerQingstor = View.inflate(context, R.layout.config_list_item_template, null);
        ((TextView)providerQingstor.findViewById(R.id.provider_tv)).setText(R.string.provider_qingstor);
        providerQingstor.findViewById(R.id.provider).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.provider_rb);
                setSelected(rb, "QINGSTOR");
            }
        });

        View providerAzureblob = View.inflate(context, R.layout.config_list_item_template, null);
        ((TextView)providerAzureblob.findViewById(R.id.provider_tv)).setText(R.string.provider_azureblob);
        providerAzureblob.findViewById(R.id.provider).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton rb = v.findViewById(R.id.provider_rb);
                setSelected(rb, "AZUREBLOB");
            }
        });

        listContent.addView(providerAlias);
        listContent.addView(providerB2);
        listContent.addView(providerBox);
        listContent.addView(providerCrypt);
        listContent.addView(providerDropbox);
        listContent.addView(providerFTP);
        listContent.addView(providerHubic);
        listContent.addView(providerHTTP);
        listContent.addView(providerAzureblob);
        listContent.addView(providerOneDrive);
        listContent.addView(providerPcloud);
        listContent.addView(providerQingstor);
        listContent.addView(providerSFTP);
        listContent.addView(providerWebdav);
        listContent.addView(providerYandex);

    }
}
