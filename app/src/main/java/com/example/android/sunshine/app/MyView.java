package com.example.android.sunshine.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by chrissebesta on 3/17/16.
 */
public class MyView extends View {

    private static final float STROKE_WIDTH = 3;

    private Paint mPaint;
    private Path mPath;
    private RectF mLargeCircle;
    private RectF mSmallCircle;

    private float mDegrees;

    public MyView(Context context) {
        this(context, null, 0);
    }

    public MyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDegrees = 0;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(STROKE_WIDTH * getResources().getDisplayMetrics().density);
        mPath = new Path();
        mLargeCircle = new RectF();
        mSmallCircle = new RectF();
    }

    public void setDegrees(float degrees) {
        mDegrees = degrees;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        final float halfWidth = w * .5f;
        final float halfHeight = w * .5f;
        mLargeCircle.set(w * .30f, h * .30f, w * .70f, h * .70f);
        mSmallCircle.set(w * .47f, h * .47f, w * .53f, h * .53f);
        mPath.moveTo(halfWidth, 0);
        mPath.lineTo(w * .7f, halfHeight);
        mPath.lineTo(w * .3f, halfHeight);
        mPath.close();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Rotate to degrees entered.
        canvas.rotate(mDegrees, getWidth() * .5f, getHeight() * .5f);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.RED);
        // Draw the red triangle.
        canvas.drawPath(mPath, mPaint);
        canvas.save();
        // Rotate 180 degrees to draw the blue triangle.
        canvas.rotate(180, getWidth() * .5f, getHeight() * .5f);
        mPaint.setColor(Color.BLUE);
        canvas.drawPath(mPath, mPaint);
        // Restore canvas transformation before save() method.
        canvas.restore();
        mPaint.setColor(Color.WHITE);
        // Draw the background of white large circle.
        canvas.drawOval(mLargeCircle, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.BLACK);
        // Draw the contour of white large circle.
        canvas.drawOval(mLargeCircle, mPaint);
        // Draw the small circle.
        canvas.drawOval(mSmallCircle, mPaint);
    }
}