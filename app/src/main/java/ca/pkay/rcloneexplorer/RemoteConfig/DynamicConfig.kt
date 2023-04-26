package ca.pkay.rcloneexplorer.RemoteConfig

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import ca.pkay.rcloneexplorer.R
import ca.pkay.rcloneexplorer.Rclone
import ca.pkay.rcloneexplorer.rclone.Provider
import ca.pkay.rcloneexplorer.rclone.ProviderOption
import com.google.android.material.textfield.TextInputLayout

class DynamicConfig(val mProviderTitle: String) : Fragment() {
    private lateinit var mContext: Context
    private var rclone: Rclone? = null

    private var mCancelButton: Button? = null
    private var mNextButton: Button? = null
    private var mRemoteName: EditText? = null
    private var mProvider: Provider? = null


    private var mOptionMap = hashMapOf<String, String>()


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
        mRemoteName = view.findViewById(R.id.remote_name)
        mCancelButton = view.findViewById(R.id.cancel)
        mNextButton = view.findViewById(R.id.next)


        mCancelButton?.setOnClickListener { parentFragmentManager.popBackStackImmediate(); }
        mNextButton?.setOnClickListener { setUpRemote() }

        return view
    }

    private fun setUpForm(view: View) {
        val formContent = view.findViewById<ViewGroup>(R.id.form_content)


        val rclone = Rclone(this.context)
        mProvider = rclone.getProviders(mProviderTitle)
        if(mProvider == null) {
            Log.e("TAG", "Unknown Provider: $mProviderTitle")
            Toast.makeText(this.mContext, R.string.dynamic_config_unknown_error, Toast.LENGTH_LONG).show()
            requireActivity().finish()
        }


        mProvider!!.options.forEach {

            val layout = LinearLayout(mContext)
            layout.orientation = LinearLayout.VERTICAL

            val textViewTitle = TextView(mContext)
            textViewTitle.contentDescription = it.name
            textViewTitle.text = it.name
            textViewTitle.typeface = Typeface.DEFAULT_BOLD
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
                        val input = getAttachedEditText(it.name, layout)
                        input.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

                        setTextInputListener(input, it.name)

                    } else if(it.examples.size > 0) {
                        layout.addView(createSpinnerFromExample(it))
                    } else {
                        val input = getAttachedEditText(it.name, layout)
                        setTextInputListener(input, it.name)
                    }


                }
                else -> {
                    val unknownType = getAttachedEditText(it.name, layout)
                    //unknownType.hint = it.type
                    setTextInputListener(unknownType, it.name)
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
        val adapter = ArrayAdapter(
            this.mContext,
            android.R.layout.simple_spinner_item,
            items
        )


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
        cardLayout.bottomMargin = getDPasPixel(8).toInt()
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

    private fun getAttachedEditText(hint: String, layout: LinearLayout): EditText {
        val padding = resources.getDimensionPixelOffset(R.dimen.cardPadding)

        val textinput = TextInputLayout(mContext)
        textinput.hint = hint
        textinput.boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
        textinput.setPadding(0, padding, 0, 0)


        val editText = EditText(textinput.context)
        editText.setPadding(padding)

        textinput.addView(editText)
        layout.addView(textinput)
        return editText
    }

    private fun setTextInputListener(input: TextView, option: String) {

        input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                mOptionMap[option] = s.toString()
            }
            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun setUpRemote() {
        Log.e("TAG", "LOG")

        val options = java.util.ArrayList<String>()
        val name: String = mRemoteName?.text.toString()
        options.add(name)
        options.add(mProviderTitle)

        for ((key, value) in mOptionMap) {
            Log.e("TAG", "key: $key value: $value")
            options.add(key)
            options.add(value)
        }

        RemoteConfigHelper.setupAndWait(context, options)
        requireActivity().finish()
    }
}