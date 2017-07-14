/*
Name of the module : CameraActivity.java

Date on which the module was created: 03/4/2017

Author’s name: By Shubham Singhal 03/04/2017

Modification history :  By  Roopansh Bansal 05/04/2017

Synopsis of the module : executed when user clicks on camera button in home page of app.

Different functions supported, along with their input/output parameters.

Global variables accessed/modified by the module.
*/

package com.tzutalin.dlibtest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class TrasparentTitleView extends View {
    private static final float TEXT_SIZE_DIP = 24;
    private String mShowText;
    private final float mTextSizePx;
    private final Paint mFgPaint;
    private final Paint mBgPaint;

    public TrasparentTitleView(final Context context, final AttributeSet set) {
        super(context, set);

        mTextSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        mFgPaint = new Paint();
        mFgPaint.setTextSize(mTextSizePx);
        mFgPaint.setColor(0xccFFFFFF);
        mBgPaint = new Paint();
        mBgPaint.setColor(0xcc4d004d);
    }

    @NonNull
    public void setText(@NonNull String text) {
        this.mShowText = text;
        postInvalidate();
    }

    @Override
    public void onDraw(final Canvas canvas) {
        final int x = 10;
        int y = (int) (mFgPaint.getTextSize() * 1.5f);

        canvas.drawPaint(mBgPaint);

        if (mShowText != null) {
            canvas.drawText(mShowText, x, y, mFgPaint);
        }
    }
}
