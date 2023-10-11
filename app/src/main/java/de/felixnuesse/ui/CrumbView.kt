package de.felixnuesse.ui

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import ca.pkay.rcloneexplorer.databinding.CustomuiCrumbviewBinding


class CrumbView : LinearLayout {

    private var binding = CustomuiCrumbviewBinding.inflate(LayoutInflater.from(context), this, true)

    private var mTitle = ""
    private var mPath = ""

    private var mShowArrow = false
    private var mIsActive = true

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)  {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)  {}
    constructor(context: Context) : super(context) {}

    init {
        binding.icon.visibility = View.GONE
        binding.arrow.setColorFilter(binding.title.currentTextColor)
        binding.icon.setColorFilter(binding.title.currentTextColor)
    }

    fun setTitle(title: String) {
        mTitle = title
        binding.title.text = mTitle
        setActive(mIsActive)
    }

    fun setPath(path: String) {
        mPath = path
        setActive(mIsActive)
    }

    fun showArrow(showArrow: Boolean) {
        mShowArrow = showArrow
        if(mShowArrow) {
            binding.arrow.visibility = View.VISIBLE
        } else {
            binding.arrow.visibility = View.GONE
        }
    }

    fun showHome() {
        binding.icon.visibility = View.VISIBLE
    }

    fun setActive(isActive: Boolean) {
        mIsActive = isActive
        updateActiveState()
    }

    fun getPath(): String {
        return mPath
    }


    private fun updateActiveState() {
        val params = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )

        var padding = 0

        if(mIsActive) {
            binding.title.setTypeface(null, Typeface.BOLD)
            padding = (2 * resources.displayMetrics.density).toInt()
        } else {
            binding.title.setTypeface(null, Typeface.NORMAL)
            binding.root.background = null
        }
        params.setMargins(padding, 0, padding, 0)
        binding.title.layoutParams = params
    }

}