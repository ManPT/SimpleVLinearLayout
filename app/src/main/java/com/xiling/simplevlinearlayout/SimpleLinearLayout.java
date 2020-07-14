package com.xiling.simplevlinearlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class SimpleLinearLayout extends ViewGroup {

    private int mWidth = 0, mHeight = 0;
    private int screenWidth, screenHeight;


    public SimpleLinearLayout(Context context) {
        super(context);
        init(context);
    }

    public SimpleLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SimpleLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);
        screenWidth = outMetrics.widthPixels;
        screenHeight = outMetrics.heightPixels;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        switch (widthMode) {
            case MeasureSpec.AT_MOST:
                // 一般当childView设置其宽、高为wrap_content时
                break;
            case MeasureSpec.EXACTLY:
                // 精准值模式，一般当childView设置其宽、高为精确值、match_parent时
                mWidth = widthMeasureSpec;
                break;
            case MeasureSpec.UNSPECIFIED:
                //表示子布局想要多大就多大，一般出现在AadapterView的item
                break;
        }

        switch (heightMode) {
            case MeasureSpec.AT_MOST:
                // 一般当childView设置其宽、高为wrap_content时
                break;
            case MeasureSpec.EXACTLY:
                // 精准值模式，一般当childView设置其宽、高为精确值、match_parent时
                heightMode = heightMeasureSpec;
                break;
            case MeasureSpec.UNSPECIFIED:
                //表示子布局想要多大就多大，一般出现在AadapterView的item
                break;
        }
        int count = getChildCount();
        // 如果没有子View，不进行测量
        if (count <= 0) {
            setMeasuredDimension(mWidth, mHeight);
            return;
        }

        int childWidth = 0, childHeight = 0;
        mHeight = 0;
        for (int position = 0; position < count; position++) {
            View childView = getChildAt(position);
            // 测量子view
           // measureChild(childView,widthMeasureSpec,heightMeasureSpec);
            measureChildWithMargins(childView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            LayoutParams layoutParams = (LayoutParams) childView.getLayoutParams();
            if (widthMode != MeasureSpec.EXACTLY) {
                // 开启自动测量宽度
                int childMarginLeft = layoutParams.leftMargin;
                int childMarginRight = layoutParams.rightMargin;
                childWidth = childView.getMeasuredWidth() + childMarginLeft + childMarginRight + getPaddingLeft() + getPaddingRight();
                // 取子View中，宽度最大的作为View的宽度
                if (childWidth > mWidth) {
                    if (childWidth > screenWidth) {
                        mWidth = screenWidth;
                    } else {
                        mWidth = childWidth;
                    }
                }
            }

            if (heightMode != MeasureSpec.EXACTLY) {
                // 开启自动测量高度
                childHeight = childView.getMeasuredHeight() + layoutParams.topMargin + layoutParams.bottomMargin;
                mHeight += childHeight;
            }
        }

        setMeasuredDimension(mWidth, mHeight + getPaddingTop() + getPaddingBottom());

    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        if (count <= 0) {
            return;
        }
        int left = 0, top = getPaddingTop(), right, bottom;
        View childView;
        for (int position = 0; position < count; position++) {
            childView = getChildAt(position);
            LayoutParams layoutParams = (LayoutParams) childView.getLayoutParams();
            left = layoutParams.leftMargin + getPaddingLeft();
            top += layoutParams.topMargin;
            right = childView.getMeasuredWidth() + getPaddingRight() + layoutParams.leftMargin;
            bottom = childView.getMeasuredHeight() + top;
            childView.layout(left, top, right, bottom);
            top += childView.getMeasuredHeight() + layoutParams.bottomMargin;
        }
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {


        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }


}
