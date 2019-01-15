package com.weisc.alarm.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;

public class DrawableCenterTextView extends DrawableTextView {
    private static final String TAG = "DrawableCenterTextView";

    private Rect mRect = new Rect();

    public DrawableCenterTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 获取TextView的Drawable对象，返回的数组长度应该是4，对应左上右下
        Drawable[] drawables = getCompoundDrawables();
        Drawable drawable = drawables[0];
        if (drawable != null) {
            Log.i(TAG, "onDraw: ");
            // 当左边Drawable的不为空时，测量要绘制文本的宽度
            float textWidth = getPaint().measureText(getText().toString());
            Log.d(TAG, "onDraw: textWidth = " + textWidth);
            int drawablePadding = getCompoundDrawablePadding();
            Log.d(TAG, "onDraw: drawablePadding " + drawablePadding);
            int drawableWidth = drawable.getIntrinsicWidth();
            Log.d(TAG, "onDraw: drawableWidth " + drawableWidth);
            // 计算总宽度（文本宽度 + drawablePadding + drawableWidth）
            float bodyWidth = textWidth + drawablePadding + drawableWidth;
            Log.d(TAG, "onDraw: width = " + getWidth() + " bodyWidth = " + bodyWidth);
            // 移动画布开始绘制的X轴
            canvas.translate(-(getWidth() - bodyWidth) / 2, 0);
        } else if ((drawable = drawables[1]) != null) {
            // 否则如果上边的Drawable不为空时，获取文本的高度
            getPaint().getTextBounds(getText().toString(), 0, getText().toString().length(), mRect);
            float textHeight = mRect.height();
            int drawablePadding = getCompoundDrawablePadding();
            int drawableHeight = drawable.getIntrinsicHeight();
            // 计算总高度（文本高度 + drawablePadding + drawableHeight）
            float bodyHeight = textHeight + drawablePadding + drawableHeight;
            // 移动画布开始绘制的Y轴
            canvas.translate(0, (getHeight() - bodyHeight) / 2);
        }
        super.onDraw(canvas);
    }
}
