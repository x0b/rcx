package ca.pkay.rcloneexplorer;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BreadcrumbView extends HorizontalScrollView {

    private LinearLayout childFrame;
    private TextView previousCrumb;
    private Context context;
    private int numberOfCrumbs;
    private int lastCrumbPosition;
    private OnClickListener onClickListener;

    public interface OnClickListener {
        void onBreadCrumbClicked(String path);
    }

    public BreadcrumbView(Context context) {
        super(context);
        init(context);
    }

    public BreadcrumbView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    private void init(Context context) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        childFrame = new LinearLayout(context);
        childFrame.setLayoutParams(layoutParams);
        this.context = context;
        addView(childFrame, layoutParams);
        numberOfCrumbs = 0;
        lastCrumbPosition = -1;
    }

    public void setOnClickListener(OnClickListener listener) {
        onClickListener = listener;
    }

    public void addCrumb(String crumbTitle, final String path) {
        if (numberOfCrumbs >= 1) {
            addArrow();
            previousCrumb.setTypeface(null, Typeface.NORMAL);
            previousCrumb.setElevation(0);
        }

        numberOfCrumbs++;
        lastCrumbPosition++;
        final TextView textViewCrumb = new TextView(context);
        textViewCrumb.setText(crumbTitle);
        textViewCrumb.setTypeface(null, Typeface.BOLD);
        textViewCrumb.setTextColor(getResources().getColor(R.color.white));
        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
        textViewCrumb.setBackgroundResource(outValue.resourceId);
        textViewCrumb.setPadding( 8, 8, 8, 0);
        textViewCrumb.setElevation(50);
        textViewCrumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickListener.onBreadCrumbClicked(path);
            }
        });
        previousCrumb = textViewCrumb;
        childFrame.addView(textViewCrumb);
        childFrame.post(new Runnable() {
            @Override
            public void run() {
                smoothScrollTo(textViewCrumb.getLeft() - 50, 0);
            }
        });
    }

    public void removeLastCrumb() {
        View child = childFrame.getChildAt(lastCrumbPosition--);
        View child2 = childFrame.getChildAt(lastCrumbPosition--);
        childFrame.removeView(child);
        childFrame.removeView(child2);
    }

    public void clearCrumbs() {
        childFrame.removeAllViews();
        numberOfCrumbs = 0;
    }

    private void addArrow() {
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(R.drawable.ic_arrow_right);
        childFrame.addView(imageView);
        lastCrumbPosition++;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        /*View child = childFrame.getChildAt(numberOfCrumbs);
        if (null != child) {
            smoothScrollTo(child.getLeft(), 0);
        }*/
    }
}

