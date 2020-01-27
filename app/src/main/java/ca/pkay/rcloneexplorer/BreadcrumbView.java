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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BreadcrumbView extends HorizontalScrollView {

    private LinearLayout childFrame;
    private TextView previousCrumb;
    private Context context;
    private int numberOfCrumbs;
    private List<String> pathList;
    private Map<String, TextView> textViewMap;
    private Map<String, ImageView> imageViewMap;
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
        childFrame.removeAllViews();
        this.context = context;
        addView(childFrame, layoutParams);
        pathList = new ArrayList<>();
        textViewMap = new HashMap<>();
        imageViewMap = new HashMap<>();
        numberOfCrumbs = 0;
    }

    public void setOnClickListener(OnClickListener listener) {
        onClickListener = listener;
    }

    public void buildBreadCrumbsFromPath(String path) {
        int index = 0;
        while ((index = path.indexOf("/", index)) > 0) {
            String fullPath = path.substring(0, index);
            String displayPath;

            int i = fullPath.lastIndexOf("/");
            if (i > 0) {
                displayPath = fullPath.substring(i + 1);
            } else {
                displayPath = fullPath;
            }
            addCrumb(displayPath, fullPath);
            index++;
        }

        int i = path.lastIndexOf("/");
        if (i > 0) {
            String displayName = path.substring(i + 1);
            addCrumb(displayName, path);
        } else {
            addCrumb(path, path);
        }
    }

    public void addCrumb(String crumbTitle, final String path) {
        if (numberOfCrumbs >= 1) {
            addArrow(path);
            previousCrumb.setTypeface(null, Typeface.NORMAL);
            previousCrumb.setElevation(0);
        }

        numberOfCrumbs++;
        final TextView textViewCrumb = new TextView(context);
        textViewCrumb.setText(crumbTitle);
        textViewCrumb.setTypeface(null, Typeface.BOLD);
        textViewCrumb.setTextColor(getResources().getColor(R.color.white));
        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
        textViewCrumb.setBackgroundResource(outValue.resourceId);
        textViewCrumb.setPadding( 8, 8, 8, 0);
        textViewCrumb.setElevation(50);
        textViewCrumb.setOnClickListener(view -> onClickListener.onBreadCrumbClicked(path));
        previousCrumb = textViewCrumb;
        pathList.add(path);
        textViewMap.put(path, textViewCrumb);
        childFrame.addView(textViewCrumb);
        childFrame.post(() -> smoothScrollTo(textViewCrumb.getLeft() - 50, 0));
    }

    public void removeCrumbsUpTo(String path) {
        int crumbIndex = pathList.indexOf(path);
        int lastCrumbIndex = pathList.size() - 1;

        for (int i = lastCrumbIndex; i > crumbIndex; i--) {
            String p = pathList.get(i);
            View textView = textViewMap.get(p);
            View imageView = imageViewMap.get(p);

            pathList.remove(p);
            textViewMap.remove(p);
            imageViewMap.remove(p);

            childFrame.removeView(textView);
            childFrame.removeView(imageView);
        }
    }

    public void removeLastCrumb() {
        int lastCrumbIndex = pathList.size() - 1;
        String path = pathList.get(lastCrumbIndex);
        View textView = textViewMap.get(path);
        View imageView = imageViewMap.get(path);

        pathList.remove(path);
        textViewMap.remove(path);
        imageViewMap.remove(path);

        childFrame.removeView(textView);
        childFrame.removeView(imageView);
    }

    public void clearCrumbs() {
        childFrame.removeAllViews();
        numberOfCrumbs = 0;
    }

    private void addArrow(String path) {
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(R.drawable.ic_arrow_right);
        childFrame.addView(imageView);
        imageViewMap.put(path, imageView);
    }
}

