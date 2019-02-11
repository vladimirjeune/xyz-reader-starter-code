package com.example.xyzreader.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.xyzreader.R;

// Found some behavior code on: http://www.devexchanges.info/2016/03/android-tip-custom-coordinatorlayout.html
public class BookTitlesBehavior extends CoordinatorLayout.Behavior<TextView> {

    private final static String TAG = "BookTitleBehavior";
    private final static int EXTRA_FINAL_PADDING = 80; // 72 + 8
    private final Context mContext;

    private int mFinalLeftAvatarPadding;
    private float mStartPosition;
    private float mToolbarPosition;

    private float mStartXPosition;
    private float mStartToolbarPosition;
    private float mStartTitleTextSize;
    private float mFinalTitleTextSize;
    private float mFinalTitleTextPadding;
    private float mFinalHeight;

    private int mStartHeight;

    public BookTitlesBehavior(Context context, AttributeSet attrs) {
        mContext = context;

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BookTitlesBehavior);
            mStartXPosition = a.getDimension(R.styleable.BookTitlesBehavior_startXPosition, 0);
            mStartToolbarPosition = a.getDimension(R.styleable.BookTitlesBehavior_startToolbarPosition, 0);
            mStartTitleTextSize = a.getDimension(R.styleable.BookTitlesBehavior_startTitleTextSize, 0);
            mFinalTitleTextSize = a.getDimension(R.styleable.BookTitlesBehavior_finalTitleTextSize, 0);
            mFinalTitleTextPadding = a.getDimension(R.styleable.BookTitlesBehavior_finalTitleTextPadding, 0);
            mFinalHeight = a.getDimension(R.styleable.BookTitlesBehavior_finalHeight, 0);

            a.recycle();
        }

        // Look into this
        bindDimensions() ;

    }

    private void bindDimensions() {
        // TODO: Find something to do here.
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull TextView child, @NonNull View dependency) {
        Log.d(TAG, "layoutDependsOn() called with: parent = [" + parent + "], child = [" + child + "], dependency = [" + dependency + "]");
        return dependency instanceof LinearLayout;
    }


    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull TextView child, @NonNull View dependency) {
//        return super.onDependentViewChanged(parent, child, dependency);  // RM
        initAppropriateProperties(child, dependency);

        final int maxScrollDistance = (int) (mStartToolbarPosition - getStatusBarHeight());
        float expandedPercentageFactor = dependency.getY() / maxScrollDistance;
        float distanceToAdd = (32 * (1f - expandedPercentageFactor));

        float heightToSubtract = (mStartHeight - mFinalHeight) * (1f - expandedPercentageFactor);

        child.setX(mStartXPosition + distanceToAdd);

        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        layoutParams.width = (int) (32 + heightToSubtract);
        child.setLayoutParams(layoutParams);

        return true;
    }

    private void initAppropriateProperties(TextView child, View dependency) {

        Log.d(TAG, "initAppropriateProperties() called with: child = [" + child + "], dependency = [" + dependency + "]");

        if (0 == mStartXPosition) {
            mStartXPosition = (int) (child.getX() + child.getWidth() / 2); // ? Need 2 b centered
        }

        if (0 == mStartToolbarPosition) {
            mStartToolbarPosition = dependency.getY();
        }

        if (0 == mStartTitleTextSize) {
            mStartTitleTextSize = child.getTextSize();  // TODO: Decide if want px or sp. This is PX.
            // int sizeInPx = child.getTextSize();
            // mStartTitleTextSize = pxToSp(SizeInPx);  // https://stackoverflow.com/questions/29664993/how-to-convert-dp-px-sp-among-each-other-especially-dp-and-sp/42108115#42108115
        }

        if (0 == mFinalTitleTextSize) {
            mFinalTitleTextSize = child.getTextSize();  // TODO: Decide if want px or sp. This is PX.
        }

        if (0 == mFinalTitleTextPadding) {
            mFinalTitleTextPadding = child.getPaddingLeft();  // TODO: Decide if want px or sp. This is PX.
        }

        if (0 == mStartHeight) {
            mStartHeight = child.getHeight();
        }

        if (0 == mFinalHeight) {
            mFinalHeight = mContext.getResources().getDimensionPixelOffset(R.dimen.toolbar_final_height);
        }

    }

    /**
     * PXTOSP - Translates pixels into SP for the current screen and user preferences.
     * Should probably be a Utility function.
     * @param pixels - float; a size in pixels that should be translated for text size
     * @return - Correct integer SP number for pixels
     */
    public int pxToSp(float pixels) {
        return (int) (pixels / mContext.getResources().getDisplayMetrics().scaledDensity);
    }


    /**
     * GETSTATUSBARHEIGHT - Get the height of the status bar
     * You should use what Chris Banes said, but trying to stay true to example for now
     */
    public int getStatusBarHeight() {
        int result = 0;

        int rsrcId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");

        if (rsrcId > 0) {
            result = mContext.getResources().getDimensionPixelSize(rsrcId);
        }

        return result;
    }


}
