package de.felixnuesse.breadcrumbs

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.core.widget.ImageViewCompat
import de.felixnuesse.breadcrumbs.databinding.CrumbviewBinding


class CrumbView : LinearLayout {

    private var binding = CrumbviewBinding.inflate(LayoutInflater.from(context), this, true)

    private var mTitle = ""
    private var mPath = ""

    private var mShowArrow = false
    private var mIsActive = true
    private var mUseHighlight = false
    private var mHighlightColor = 0
    private var mDefaultTextColor = 0
    private var mPadding = 0

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)  {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)  {}
    constructor(context: Context) : super(context) {}

    init {
        mDefaultTextColor = binding.title.currentTextColor
        binding.arrow.setColorFilter(mDefaultTextColor)
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

    fun setActive(isActive: Boolean) {
        mIsActive = isActive
        updateActiveState()
    }

    fun getPath(): String {
        return mPath
    }


    fun setArrowIcon(resourceId: Int) {
        binding.arrow.setImageResource(resourceId)
    }

    fun setBackgroundId(resourceId: Int) {
        binding.root.setBackgroundResource(resourceId)
    }

    fun setContainerPadding(padding: Int) {
        mPadding = padding
        if(mShowArrow) {
            binding.root.setPadding(mPadding,mPadding, 0, mPadding)
        } else {
            binding.root.setPadding(mPadding,mPadding, mPadding, mPadding)
        }
    }

    fun setColor(resourceId: Int) {
        var color = ContextCompat.getColor(context, resourceId)
        binding.title.setTextColor(color)
        binding.arrow.setColorFilter(color)
    }

    fun setHighlight(resourceId: Int) {
        mUseHighlight = true
        mHighlightColor = resourceId
        updateActiveState()
    }


    private fun updateActiveState() {
        if(mIsActive) {
            binding.title.setTypeface(null, Typeface.BOLD)
            if(mUseHighlight) {
                binding.title.setTextColor(resources.getColor(mHighlightColor))
            }
        } else {
            binding.title.setTypeface(null, Typeface.NORMAL)
            binding.title.setTextColor(mDefaultTextColor)

        }
    }

}