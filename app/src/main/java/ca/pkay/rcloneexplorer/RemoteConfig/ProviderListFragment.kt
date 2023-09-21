package ca.pkay.rcloneexplorer.RemoteConfig

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.pkay.rcloneexplorer.R
import ca.pkay.rcloneexplorer.Rclone
import ca.pkay.rcloneexplorer.RecyclerViewAdapters.RemoteConfigListItemAdapter
import ca.pkay.rcloneexplorer.RemoteConfig.ProviderListFragment.SelectionChangedListener
import ca.pkay.rcloneexplorer.rclone.Provider


class ProviderListFragment(private val mPreselection: String?) : Fragment() {
    interface ProviderSelectedListener {
        fun onProviderSelected(provider: Provider)
    }

    fun interface SelectionChangedListener {
        fun onProviderChanged(provider: Provider)
    }

    private var mRclone: Rclone? = null

    private var mProviders: List<Provider> = listOf()
    private var mProviderFilter = ""
    private var mSelectedProvider: Provider? = null
    private var mProviderSelectedListener: ProviderSelectedListener? = null

    private var mSelectionChangeListener = SelectionChangedListener { mSelectedProvider = it }

    private var mRootView: View? = null


    companion object {
        @JvmStatic
        fun newProviderListConfig(): ProviderListFragment {
            return ProviderListFragment(null)
        }

        @JvmStatic
        fun newProviderListConfig(preselection: String?): ProviderListFragment {
            return ProviderListFragment(preselection)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mRclone = Rclone(this.context)
        mProviders = mRclone?.providers ?: listOf()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_config_list, container, false)
        setClickListeners(view)
        mRootView = view
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

        view.findViewById<View>(R.id.next).setOnClickListener {
            mSelectedProvider?.let { it1 -> mProviderSelectedListener!!.onProviderSelected(it1) }
        }


        val recyclerView: RecyclerView = view.findViewById(R.id.config_content)
        recyclerView.adapter = updateList()
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    fun updateList(): RemoteConfigListItemAdapter {
        var filteredProvider = mProviders
        if(mProviderFilter.isNotBlank()) {
            filteredProvider = filteredProvider.filter {
                it.name.contains(mProviderFilter.lowercase()) ||
                        it.description.contains(mProviderFilter.lowercase())
            }
        }

        return RemoteConfigListItemAdapter(
            ArrayList(filteredProvider.sortedWith(compareBy { it.name })),
            requireContext(),
            mSelectionChangeListener,
            mPreselection
        )

    }

    fun setSearchterm (term: String) {
        mProviderFilter = term

        if(mRootView != null) {
            val recyclerView: RecyclerView = (mRootView as View).findViewById(R.id.config_content)
            recyclerView.adapter = updateList()
            recyclerView.layoutManager = LinearLayoutManager(context)
        }
    }
}