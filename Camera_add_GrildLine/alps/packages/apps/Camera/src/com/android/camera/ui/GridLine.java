package com.android.camera.ui;

import android.util.AttributeSet;
import android.view.View;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color; 
import android.graphics.Paint;

public class GridLine extends View {
    private Paint mPaint;
    
    public GridLine(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(2);
    }
    
    protected void onDraw(Canvas canvas) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        float vertz = (float)(height / 3);
        float hortz = (float)(width / 3);
        for (int i = 1; i < 3; i++) {
            canvas.drawLine(0, vertz*i, width, vertz*i, mPaint);
            canvas.drawLine(hortz*i, 0, hortz*i, height, mPaint);
        }
    }
}
