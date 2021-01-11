package com.geotwo.LAB_TEST.Gathering;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.RotateAnimation;

import com.geotwo.LAB_TEST.Gathering.util.WataLog;

public class SpinnableImageView extends android.support.v7.widget.AppCompatImageView {
    private double mCurrAngle = 0;
    private double mPrevAngle = 0;
    private double mAddAngle = 0;

    public SpinnableImageView(Context context) {
        super(context);
    }

    public SpinnableImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        final float centerOfWidth = getWidth() / 2;
        final float centerOfHeight = getHeight() / 2;
        final float x = motionEvent.getX();
        final float y = motionEvent.getY();

        WataLog.d("motionEvent.getAction()=" + motionEvent.getAction());
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mCurrAngle = Math.toDegrees(Math.atan2(x - centerOfWidth, centerOfHeight - y));
                break;

            case MotionEvent.ACTION_MOVE:
                mPrevAngle = mCurrAngle;
                mCurrAngle = Math.toDegrees(Math.atan2(x - centerOfWidth, centerOfHeight - y));
                animate(this, mAddAngle, mAddAngle + mCurrAngle - mPrevAngle);
                mAddAngle += mCurrAngle - mPrevAngle;
                break;

            case MotionEvent.ACTION_UP:
                performClick();
                break;

        }
        return true;
    }

    private void animate(View view, double fromDegrees, double toDegrees) {
        WataLog.d("fromDegrees=" + fromDegrees);
        WataLog.d("toDegrees=" + toDegrees);
//        WataLog.d("mDegressListener=" + mDegressListener);
        try {
            mDegressListener.getDegress(toDegrees);
        } catch (Exception e) {
            WataLog.e("excetpion=" + e.toString());
        }

        final RotateAnimation rotate = new RotateAnimation((float) fromDegrees, (float) toDegrees,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(0);
        rotate.setFillAfter(true);
        view.startAnimation(rotate);
    }

//    public static double mDegrees = 0.0;
    public interface onDegress {
        void getDegress(double degrees);
    }

    private onDegress mDegressListener = null;

    public void setItemListener(onDegress listener) {
        this.mDegressListener = listener;
    }


}