package de.felixnuesse.ui

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.TooltipCompat
import ca.pkay.rcloneexplorer.R
import ca.pkay.rcloneexplorer.databinding.CustomuiCrumbviewBinding


class CrumbView : LinearLayout {

    private var binding = CustomuiCrumbviewBinding.inflate(LayoutInflater.from(context), this, true)

    private var mTitle = ""
    private var mPath = ""

    private var mShowArrow = false
    private var mIsActive = true

    private var mTextDefaultColor = binding.title.currentTextColor


    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)  {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)  {}
    constructor(context: Context) : super(context) {}

    init {
        binding.icon.visibility = View.GONE
        binding.arrow.setColorFilter(mTextDefaultColor)
        binding.icon.setColorFilter(mTextDefaultColor)
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
            padding = (2 * resources.displayMetrics.density).toInt()
            binding.root.setBackgroundResource(R.drawable.pill)
            binding.arrow.setColorFilter(mTextDefaultColor)
            binding.icon.setColorFilter(mTextDefaultColor)
            binding.title.setTextColor(mTextDefaultColor)
            TooltipCompat.setTooltipText(binding.root, mPath);
        } else {
            binding.root.background = null
            var color = resources.getColor(R.color.textColorHighlight)
            binding.arrow.setColorFilter(color)
            binding.icon.setColorFilter(color)
            binding.title.setTextColor(color)
            TooltipCompat.setTooltipText(binding.root, null)
        }
        params.setMargins(padding, 0, padding, 0)
        binding.title.layoutParams = params
    }
}