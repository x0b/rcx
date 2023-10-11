package de.felixnuesse.breadcrumbs

import android.content.Context
import android.content.res.Resources.NotFoundException
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import de.felixnuesse.breadcrumbs.databinding.BreadcrumbviewBinding

class BreadcrumbView : HorizontalScrollView {


    private lateinit var crumbHolder: LinearLayout
    private var crumbStack = java.util.ArrayDeque(listOf<CrumbView>())
    private var onClickListener: OnClickListener? = null

    private var binding = BreadcrumbviewBinding.inflate(LayoutInflater.from(context), this, true)

    private var arrowResourceId = noResource
    private var backgroundResourceId = noResource
    private var colorResourceId = noResource
    private var paddingDP = 0F

    companion object {
        private val TAG = "BreadcrumbView"
        private val noResource = -1
    }


    private lateinit var mContext: Context

    interface OnClickListener {
        fun onBreadCrumbClicked(path: String?)
    }


    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)  {
        init(context, attrs)
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)  {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        mContext = context
        crumbHolder = binding.crumbHolder
        crumbHolder.removeAllViews()

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BreadcrumbView)
        arrowResourceId = typedArray.getResourceId(R.styleable.BreadcrumbView_breadcrumbArrow, noResource)
        backgroundResourceId = typedArray.getResourceId(R.styleable.BreadcrumbView_breadcrumbBackground, noResource)
        paddingDP = typedArray.getDimension(R.styleable.BreadcrumbView_breadcrumbPadding, 0F)
        colorResourceId = typedArray.getResourceId(R.styleable.BreadcrumbView_breadcrumbColor, noResource)
    }


    fun setOnClickListener(listener: OnClickListener?) {
        onClickListener = listener
    }

    fun buildBreadCrumbsFromPath(path: String) {
        var index = 0
        while (path.indexOf("/", index).also { index = it } > 0) {
            val fullPath = path.substring(0, index)
            var displayPath: String
            val i = fullPath.lastIndexOf("/")
            displayPath = if (i > 0) {
                fullPath.substring(i + 1)
            } else {
                fullPath
            }
            addCrumb(displayPath, fullPath)
            index++
        }
        val i = path.lastIndexOf("/")
        if (i > 0) {
            val displayName = path.substring(i + 1)
            addCrumb(displayName, path)
        } else {
            addCrumb(path, path)
        }
    }

    fun clearCrumbs() {
        crumbHolder.removeAllViews()
        crumbStack.clear()
    }

    fun addCrumb(crumbTitle: String, path: String) {
        val crumb = CrumbView(context.applicationContext)
        crumb.setTitle(crumbTitle)
        crumb.setPath(path)
        if(arrowResourceId != noResource) {
            crumb.setArrowIcon(arrowResourceId)
        }
        if(backgroundResourceId != noResource) {
            crumb.setBackgroundId(backgroundResourceId)
        }
        if(colorResourceId != noResource) {
            crumb.setColor(colorResourceId)
        }

        val scale = context.resources.displayMetrics.density
        crumb.setContainerPadding((paddingDP * scale).toInt())


        crumb.setOnClickListener {
            onClickListener?.onBreadCrumbClicked(path)
            removeCrumbsUpTo(path)
        }


        if (crumbStack.size >= 1) {
            val previousCrumb = crumbStack.last()
            previousCrumb.setActive(false)
            previousCrumb.showArrow(true)

        }

        crumbStack.add(crumb)
        crumbHolder.addView(crumb)
        crumbHolder.post { smoothScrollTo(crumb.left - 50, 0) }
    }

    fun removeCrumbsUpTo(path: String) {
        if(crumbStack.size >= 1) {
            try{
                var currentTop = crumbStack.lastOrNull()
                while (currentTop != null) {
                    if(currentTop.getPath() == path){
                        setLastCrumbSelected()
                        return
                    }
                    crumbHolder.removeView(currentTop)
                    crumbStack.removeLastOccurrence(currentTop)
                    currentTop = crumbStack.lastOrNull()
                }
            } catch (e: NotFoundException) {
                setLastCrumbSelected()
                return
            }
        }
    }

    fun removeLastCrumb() {
        if(crumbStack.size >= 1) {
            val crumb = crumbStack.removeLast()
            crumbHolder.removeView(crumb)
        }
        setLastCrumbSelected()
    }

    private fun setLastCrumbSelected() {
        if(crumbStack.size >= 1) {
            crumbStack.last().setActive(true)
        }
    }
}