package ca.pkay.rcloneexplorer.RemoteConfig

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import ca.pkay.rcloneexplorer.R
import ca.pkay.rcloneexplorer.Rclone

class DynamicConfig2 : Fragment() {
    private var mContext: Context? = null
    private var rclone: Rclone? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (getContext() == null) {
            return
        }
        mContext = getContext()
        rclone = Rclone(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.remote_config_form, container, false)
        setUpForm(view)
        return view
    }

    private fun setUpForm(view: View) {
        val formContent = view.findViewById<ViewGroup>(R.id.form_content)
        val padding = resources.getDimensionPixelOffset(R.dimen.config_form_template)

        val textview = TextView(mContext);
        textview.contentDescription = "TEST"
        formContent.addView(textview)

    }

    private fun setUpRemote() {

    }
}