package ca.pkay.rcloneexplorer.RecyclerViewAdapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ca.pkay.rcloneexplorer.R
import ca.pkay.rcloneexplorer.RemoteConfig.ProviderListFragment
import ca.pkay.rcloneexplorer.rclone.Provider

class RemoteConfigListItemAdapter(
    private val mProvider: ArrayList<Provider>,
    private val mContext: Context,
    private var mSelectionChange: ProviderListFragment.SelectionChangedListener,
    private val mPreselection: String?
): RecyclerView.Adapter<RemoteConfigListItemAdapter.ViewHolder>() {

    private var mLastSelected: RadioButton? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.config_list_item_template, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val provider = mProvider[position]

        viewHolder.titleTextview.text = provider.getNameCapitalized()
        viewHolder.descriptionTextview.text = provider.description
        viewHolder.icon.setImageDrawable(
            ContextCompat.getDrawable(mContext, getIconIfAvailable(provider.name))
        )

        viewHolder.isSelectedButton.setOnClickListener { v: View ->
            val rb = v.findViewById<RadioButton>(R.id.provider_rb)
            setSelected(rb, provider)
        }

        if(mPreselection != null) {
            if(provider.name == mPreselection.lowercase()){
                val rb = viewHolder.isSelectedButton.findViewById<RadioButton>(R.id.provider_rb)
                setSelected(rb, provider)
            }
        }
    }

    override fun getItemCount() = mProvider.size

    private fun setSelected(radioButton: RadioButton, provider: Provider) {
        if (mLastSelected != null) {
            mLastSelected!!.isChecked = false
        }
        radioButton.isChecked = true
        mLastSelected = radioButton
        mSelectionChange.onProviderChanged(provider)
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

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val isSelectedButton: RadioButton
        val titleTextview: TextView
        val descriptionTextview: TextView
        val icon: ImageView

        init {
            isSelectedButton = view.findViewById(R.id.provider_rb)
            titleTextview = view.findViewById(R.id.provider_tv)
            descriptionTextview = view.findViewById(R.id.provider_summary)
            icon = view.findViewById(R.id.providerIcon)
        }
    }
}
