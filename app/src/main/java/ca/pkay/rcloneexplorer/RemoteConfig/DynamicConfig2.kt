package ca.pkay.rcloneexplorer.RemoteConfig

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import ca.pkay.rcloneexplorer.R
import ca.pkay.rcloneexplorer.Rclone
import ca.pkay.rcloneexplorer.RemoteConfig.DynamicConfigOptions.ConfigType


class DynamicConfig2 : Fragment() {
    private var mContext: Context? = null
    private var rclone: Rclone? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (context == null) {
            return
        }
        mContext = context
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


        val rclone = Rclone(this.context)
        val prov = rclone.getProviders("webdav");


        prov.options.forEach {
            val textViewTitle = TextView(mContext)
            textViewTitle.contentDescription = it.name
            textViewTitle.text = it.name
            formContent.addView(textViewTitle)


            val textViewDescription = TextView(mContext)
            textViewDescription.contentDescription = it.help
            textViewDescription.text = it.help
            formContent.addView(textViewDescription)


            Log.e("TAG", "      Opt: ${it.name}")
            Log.e("TAG", "      Opt: ${it.type}")
            when (it.type) {
                "string" -> {
                    val input = EditText(mContext)
                    formContent.addView(input)
                }
                /*ConfigType.TYPE_SPINNER -> {
                    val input = Spinner(mContext)
                    val adapter = it.spinnerElements?.let { it1 ->
                        this.mContext?.let { it2 ->
                            ArrayAdapter(
                                it2,
                                android.R.layout.simple_spinner_item,
                                it1.toList()
                            )
                        }
                    }
                    input.adapter = adapter

                    formContent.addView(input)
                }*/
                else -> {

                }
            }


        }






    }

    private fun setUpRemote() {

    }
}