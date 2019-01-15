package com.weisc.alarm.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;

public class DrawableTextView extends android.support.v7.widget.AppCompatTextView {

    private int mDrawableWidth;
    private int mDrawableHeight;

    private Drawable mLeftDrawable;
    private Drawable mTopDrawable;
    private Drawable mRightDrawable;
    private Drawable mBottomDrawable;

    public DrawableTextView(Context context) {
        super(context);
    }

    public DrawableTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DrawableTextView, 0, 0);
        for (int i = 0; i < typedArray.getIndexCount(); i++) {
            int attr = typedArray.getIndex(i);
            if (attr == R.styleable.DrawableTextView_drawableLeft) {
                mLeftDrawable = typedArray.getDrawable(attr);
            } else if (attr == R.styleable.DrawableTextView_drawableRight) {
                mRightDrawable = typedArray.getDrawable(attr);
            } else if (attr == R.styleable.DrawableTextView_drawableTop) {
                mTopDrawable = typedArray.getDrawable(attr);
            } else if (attr == R.styleable.DrawableTextView_drawableBottom) {
                mBottomDrawable = typedArray.getDrawable(attr);
            } else if (attr == R.styleable.DrawableTextView_drawableWidth) {
                mDrawableWidth = typedArray.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()));
            } else if (attr == R.styleable.DrawableTextView_drawableHeight) {
                mDrawableHeight = typedArray.getDimensionPixelSize(attr, (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()));
            }
        }
        typedArray.recycle();

        setCompoundDrawablesWithIntrinsicBounds(mLeftDrawable, mTopDrawable, mRightDrawable, mBottomDrawable);
    }


    @Override
    public void setCompoundDrawablesWithIntrinsicBounds(Drawable left, Drawable top, Drawable right, Drawable bottom) {

        if (left != null) {
            left.setBounds(0, 0, mDrawableWidth, mDrawableHeight);
        }

        if (top != null) {
            top.setBounds(0, 0, mDrawableWidth, mDrawableHeight);
        }

        if (right != null) {
            right.setBounds(0, 0, mDrawableWidth, mDrawableHeight);
        }

        if (bottom != null) {
            bottom.setBounds(0, 0, mDrawableWidth, mDrawableHeight);
        }

        setCompoundDrawables(left, top, right, bottom);
    }
}
