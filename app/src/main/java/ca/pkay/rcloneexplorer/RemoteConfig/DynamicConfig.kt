package ca.pkay.rcloneexplorer.RemoteConfig

import android.content.Context
import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.view.setPadding
import androidx.core.view.size
import androidx.fragment.app.Fragment
import ca.pkay.rcloneexplorer.R
import ca.pkay.rcloneexplorer.Rclone
import ca.pkay.rcloneexplorer.rclone.Provider
import ca.pkay.rcloneexplorer.rclone.ProviderOption
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale


class DynamicConfig(private val mProviderTitle: String) : Fragment() {

    constructor(providerTitle: String, useOauth: Boolean) : this(providerTitle) {
        this.useOauth = useOauth
    }

    private lateinit var mContext: Context
    private var rclone: Rclone? = null

    private var mFormView: ViewGroup? = null
    private var mAuthView: View? = null
    private var mCancelButton: Button? = null
    private var mCancelAuthButton: Button? = null
    private var mNextButton: Button? = null
    private var mRemoteName: EditText? = null
    private var mProvider: Provider? = null
    private var mShowAdvanced = false


    private var mAuthTask: AsyncTask<Void?, Void?, Boolean>? = null

    private var mOptionMap = hashMapOf<String, String>()
    var useOauth: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (context == null) {
            return
        }
        setHasOptionsMenu(true)
        mContext = context as Context
        rclone = Rclone(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.remote_config_form, container, false)
        mFormView = view.findViewById(R.id.form_content)
        mAuthView = view.findViewById(R.id.auth_screen)

        mRemoteName = view.findViewById(R.id.remote_name)
        mCancelButton = view.findViewById(R.id.cancel)
        mCancelAuthButton = view.findViewById(R.id.cancel_auth)
        mNextButton = view.findViewById(R.id.next)


        setUpForm()
        mCancelButton?.setOnClickListener { activity?.onBackPressed() }
        mNextButton?.setOnClickListener { setUpRemote() }
        mCancelAuthButton?.setOnClickListener {
            mAuthTask?.cancel(true)
            requireActivity().finish()
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_config_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_toggle_advanced -> {
                mShowAdvanced = mShowAdvanced.not()
                setUpForm()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mAuthTask?.cancel(true)
    }


    // Todo: required attribute is not honored (also apply to title!)
    // Todo: hidden attribute is not applied
    private fun setUpForm() {
        rclone = Rclone(this.context)
        mProvider = rclone!!.getProvider(mProviderTitle)
        if(mProvider == null) {
            Log.e(this::class.java.simpleName, "Unknown Provider: $mProviderTitle")
            Toast.makeText(this.mContext, R.string.dynamic_config_unknown_error, Toast.LENGTH_LONG).show()
            requireActivity().finish()
        }


        mFormView?.let { it.removeViews(1, it.size-1) }

        mProvider!!.options.forEach {

            if(it.advanced && !mShowAdvanced ) {
                return@forEach
            }

            val layout = LinearLayout(mContext)
            layout.orientation = LinearLayout.VERTICAL

            val textViewTitle = TextView(mContext)
            textViewTitle.contentDescription = it.name
            textViewTitle.text = it.getNameCapitalized()
            textViewTitle.typeface = Typeface.DEFAULT_BOLD
            layout.addView(textViewTitle)


            val textViewDescription = TextView(mContext)
            textViewDescription.contentDescription = it.help
            textViewDescription.text = it.help
            layout.addView(textViewDescription)

            when (it.type) {
                "string" -> {
                    if(it.isPassword) {
                        val input = getAttachedEditText(it.name, layout)
                        input.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

                        input.setText(mOptionMap[it.name])
                        setTextInputListener(input, it.name)

                    } else if(it.examples.size > 0) {
                        createSpinnerFromExample(it, it.name ,layout)
                    } else {
                        val input = getAttachedEditText(it.name, layout)
                        input.setText(mOptionMap[it.name])
                        setTextInputListener(input, it.name)
                    }


                }
                "bool" -> {
                    val input = CheckBox(mContext)
                    input.text = it.name
                    setCheckboxListener(input, it.name)

                    if(it.default.lowercase(Locale.ROOT).toBoolean()){
                        input.isChecked = true
                    }

                    if(mOptionMap.containsKey(it.name)) {
                        input.isChecked  = mOptionMap[it.name].toBoolean()
                    }

                    layout.addView(input)
                }
                else -> {
                    val unknownType = getAttachedEditText(it.name, layout)
                    //unknownType.hint = it.type
                    unknownType.setText(mOptionMap[it.name])
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
            mFormView?.addView(cardContainer)
        }
    }

    private fun getDPasPixel(dp: Int): Float {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f)
    }


    private fun createSpinnerFromExample(option: ProviderOption, hint: String, layout: LinearLayout): View {


        val padding = resources.getDimensionPixelOffset(R.dimen.cardPadding)
        val textinput = TextInputLayout(ContextThemeWrapper(activity, R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox_ExposedDropdownMenu))
        textinput.hint = hint
        textinput.boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
        textinput.setPadding(0, padding, 0, 0)

        var input = AutoCompleteTextView(textinput.context)
        input.setPadding(padding)
        val items = ArrayList<String>()

        option.examples.forEach { example ->
            items.add(example.Value)
        }
        val adapter = ArrayAdapter(
            this.mContext,
            android.R.layout.simple_spinner_item,
            items
        )

        input.setAdapter(adapter)
        input.isEnabled = false

        textinput.addView(input)
        layout.addView(textinput)
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


        val editText = TextInputEditText(textinput.context)
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

    private fun setCheckboxListener(input: CheckBox, option: String) {

        input.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            if(isChecked) {
                mOptionMap[option] = "true"
            } else {
                mOptionMap[option] = "false"
            }
        }
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

        if(useOauth){
            mAuthTask = ConfigCreate(
                options, mFormView!!, mAuthView!!,
                requireContext(), rclone!!
            ).execute()
        } else {
            RemoteConfigHelper.setupAndWait(context, options)
            requireActivity().finish()
        }
    }
}