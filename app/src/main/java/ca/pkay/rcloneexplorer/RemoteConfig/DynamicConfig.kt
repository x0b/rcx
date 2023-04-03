package ca.pkay.rcloneexplorer.RemoteConfig

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import ca.pkay.rcloneexplorer.R
import ca.pkay.rcloneexplorer.Rclone
import ca.pkay.rcloneexplorer.rclone.ProviderOption

class DynamicConfig : Fragment() {
    private lateinit var mContext: Context
    private var rclone: Rclone? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (context == null) {
            return
        }
        mContext = context as Context
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


        val rclone = Rclone(this.context)
        val prov = rclone.getProviders("webdav");


        prov.options.forEach {

            val layout = LinearLayout(mContext)
            layout.orientation = LinearLayout.VERTICAL

            val textViewTitle = TextView(mContext)
            textViewTitle.contentDescription = it.name
            textViewTitle.text = it.name
            layout.addView(textViewTitle)


            val textViewDescription = TextView(mContext)
            textViewDescription.contentDescription = it.help
            textViewDescription.text = it.help
            layout.addView(textViewDescription)


            Log.e("TAG", "      Opt: ${it.name}")
            Log.e("TAG", "      Opt: ${it.type}")
            when (it.type) {
                "string" -> {
                    if(it.isPassword) {
                        val input = EditText(mContext)
                        input.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        layout.addView(input)
                    } else if(it.examples.size > 0) {
                        layout.addView(createSpinnerFromExample(it))
                    } else {
                        val input = EditText(mContext)
                        layout.addView(input)
                    }


                }
                else -> {
                    val unknownType = EditText(mContext)
                    unknownType.hint = it.type
                    layout.addView(unknownType)
                }
            }


            //val divider = View(mContext)
            //divider.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
            //val lp = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getDPasPixel(1).toInt())
            //divider.layoutParams = lp
            //layout.addView(divider)


            val cardContainer = getCard()
            cardContainer.addView(layout)
            formContent.addView(cardContainer)


        }
    }

    private fun getDPasPixel(dp: Int): Float {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f)
    }


    private fun createSpinnerFromExample(option: ProviderOption): View {
        val input = Spinner(mContext)
        val items = ArrayList<String>()

        option.examples.forEach { example ->
            items.add(example.Value)
        }
        val adapter = this.mContext?.let { context ->
            ArrayAdapter(
                context,
                android.R.layout.simple_spinner_item,
                items
            )
        }


        input.adapter = adapter
        return input
    }

    private fun getCard(): CardView {
        val card = CardView(mContext)
        val cardLayout = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        cardLayout.topMargin = getDPasPixel(16).toInt()
        cardLayout.marginStart = getDPasPixel(8).toInt()
        cardLayout.marginEnd = getDPasPixel(8).toInt()
        card.layoutParams = cardLayout
        // todo: Fix theming for dark mode
        card.setCardBackgroundColor(resources.getColor(R.color.md_theme_light_secondaryContainer))
        card.radius = resources.getDimension(R.dimen.cardCornerRadius)
        card.setContentPadding(
            resources.getDimension(R.dimen.cardPadding).toInt(),
            resources.getDimension(R.dimen.cardPadding).toInt(),
            resources.getDimension(R.dimen.cardPadding).toInt(),
            resources.getDimension(R.dimen.cardPadding).toInt()
        )
        return card
    }
}