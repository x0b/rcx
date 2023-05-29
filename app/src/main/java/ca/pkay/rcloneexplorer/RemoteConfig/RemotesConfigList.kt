package ca.pkay.rcloneexplorer.RemoteConfig

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ca.pkay.rcloneexplorer.R
import ca.pkay.rcloneexplorer.Rclone
import ca.pkay.rcloneexplorer.rclone.Provider

class RemotesConfigList : Fragment() {
    interface ProviderSelectedListener {
        fun onProviderSelected(provider: Provider)
    }

    private var mRclone: Rclone? = null

    var mProviders: ArrayList<Provider> = arrayListOf()

    private var mSelectedProvider: Provider? = null
    private var mLastSelected: RadioButton? = null
    private var mProviderSelectedListener: ProviderSelectedListener? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mRclone = Rclone(this.context)
        mProviders = mRclone?.providers ?: arrayListOf()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_config_list, container, false)
        setClickListeners(view)
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mProviderSelectedListener = if (context is ProviderSelectedListener) {
            context
        } else {
            throw RuntimeException("$context must implement ProviderSelectedListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mProviderSelectedListener = null
    }

    private fun setSelected(radioButton: RadioButton, provider: Provider) {
        if (mLastSelected != null) {
            mLastSelected!!.isChecked = false
        }
        radioButton.isChecked = true
        mLastSelected = radioButton
        mSelectedProvider = provider
    }

    private fun setClickListeners(view: View) {
        val listContent = view.findViewById<ViewGroup>(R.id.config_content)


        view.findViewById<View>(R.id.cancel).setOnClickListener {
           requireActivity().finish()
        }


        view.findViewById<View>(R.id.next).setOnClickListener {
            mSelectedProvider?.let { it1 -> mProviderSelectedListener!!.onProviderSelected(it1) }
        }

        for (provider in mProviders) {
            val providerView = View.inflate(context, R.layout.config_list_item_template, null)

            (providerView.findViewById<View>(R.id.provider_tv) as TextView).text = provider.getNameCapitalized()
            (providerView.findViewById<View>(R.id.provider_summary) as TextView).text = provider.description
            (providerView.findViewById<View>(R.id.providerIcon) as ImageView).setImageDrawable(
                ContextCompat.getDrawable(requireContext(), getIconIfAvailable(provider.name))
            )

            providerView.findViewById<View>(R.id.provider).setOnClickListener { v: View ->
                val rb = v.findViewById<RadioButton>(R.id.provider_rb)
                setSelected(rb, provider)
            }
            listContent.addView(providerView)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): RemotesConfigList {
            return RemotesConfigList()
        }
    }

    private fun getIconIfAvailable(providerName: String ): Int {
        return when(providerName) {
            "box" -> R.drawable.ic_box
            "b2" -> R.drawable.ic_backblaze_b2_black
            "s3" -> R.drawable.ic_amazon
            "dropbox" -> R.drawable.ic_dropbox
            "pcloud" -> R.drawable.ic_pcloud
            "sftp" -> R.drawable.ic_terminal
            "yandex" -> R.drawable.ic_yandex_mono
            "webdav" -> R.drawable.ic_webdav
            "onedrive" -> R.drawable.ic_onedrive
            "alias" -> R.drawable.ic_rclone_logo
            "crypt" -> R.drawable.ic_lock_black
            "azureblob" -> R.drawable.ic_azure_storage_blob_logo
            "cache" -> R.drawable.ic_rclone_logo
            "local" -> R.drawable.ic_tablet_cellphone
            "drive" -> R.drawable.ic_google_drive
            "google photos" -> R.drawable.ic_google_photos
            "union" -> R.drawable.ic_union_24dp
            "mega" -> R.drawable.ic_mega_logo_black
            else -> {
                R.drawable.ic_cloud
            }
        }
    }
}