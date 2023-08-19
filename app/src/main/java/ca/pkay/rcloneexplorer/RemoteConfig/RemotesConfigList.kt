package ca.pkay.rcloneexplorer.RemoteConfig

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.pkay.rcloneexplorer.R
import ca.pkay.rcloneexplorer.Rclone
import ca.pkay.rcloneexplorer.RecyclerViewAdapters.RemoteConfigListItemAdapter
import ca.pkay.rcloneexplorer.rclone.Provider

class RemotesConfigList : Fragment() {
    interface ProviderSelectedListener {
        fun onProviderSelected(provider: Provider)
    }

    fun interface SelectionChangedListener {
        fun onProviderChanged(provider: Provider)
    }

    private var mRclone: Rclone? = null

    private var mProviders: ArrayList<Provider> = arrayListOf()
    private var mSelectedProvider: Provider? = null
    private var mProviderSelectedListener: ProviderSelectedListener? = null

    private var mSelectionChangeListener = SelectionChangedListener { mSelectedProvider = it }




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

    private fun setClickListeners(view: View) {

        view.findViewById<View>(R.id.cancel).setOnClickListener {
           requireActivity().finish()
        }

        view.findViewById<View>(R.id.next).setOnClickListener {
            mSelectedProvider?.let { it1 -> mProviderSelectedListener!!.onProviderSelected(it1) }
        }

        val customAdapter = RemoteConfigListItemAdapter(
            mProviders,
            requireContext(),
            mSelectionChangeListener
        )

        val recyclerView: RecyclerView = view.findViewById(R.id.config_content)
        recyclerView.adapter = customAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    companion object {
        @JvmStatic
        fun newInstance(): RemotesConfigList {
            return RemotesConfigList()
        }
    }
}