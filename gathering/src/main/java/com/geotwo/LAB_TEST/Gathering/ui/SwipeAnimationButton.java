package com.geotwo.LAB_TEST.Gathering.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.geotwo.LAB_TEST.Gathering.PathDrawingActivity;
import com.geotwo.LAB_TEST.Gathering.util.Constance;
import com.geotwo.LAB_TEST.Gathering.util.WataLog;
import com.wata.LAB_TEST.Gathering.R;

import java.util.Locale;


public class SwipeAnimationButton extends RelativeLayout {

    private static final String TAG = "SwipeButton";
    private static final boolean RIGHT = true;
    private static final boolean LEFT = false;
    SwipeAnimationListener mSwipeAnimationListener;
    RelativeLayout mBackground;
    private ImageView slidingButton;
    private float initialX;
    private boolean active;
    private int initialButtonWidth;

    private Drawable defaultDrawable;
    private Drawable defaultBackground;

//    private Drawable rightSwipeDrawable;
//    private Drawable rightSwipeBackground;
//    private Drawable leftSwipeDrawable;
//    private Drawable leftSwipeBackground;

    private long mDuration;
    long[] mVibratePattern = new long[]{0, 300};
    Vibrator mVibrate;

    public SwipeAnimationButton(Context context) {
        super(context);
        init(context, null, -1, -1);
    }

    public SwipeAnimationButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, -1, -1);
    }

    public SwipeAnimationButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, -1);
    }

    public void setOnSwipeAnimationListener(SwipeAnimationListener swipeAnimationListener) {
        this.mSwipeAnimationListener = swipeAnimationListener;
    }

    private ImageView swipeButton;
    private Context mContext;
    private TypedArray ta;
    private String mLanguage = "ko";
    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mLanguage = Locale.getDefault().getLanguage();
        WataLog.d("mLanguage=" + mLanguage);
        mContext = context;
        ta = getContext().obtainStyledAttributes(attrs, R.styleable.SwipeAnimationButton);

        if(!"ko".equals(mLanguage)) {
            defaultBackground = ContextCompat.getDrawable(context, ta.getInteger(R.styleable.SwipeAnimationButton_defaultBackground, R.drawable.bt_start_en));
        } else {
            defaultBackground = ContextCompat.getDrawable(context, ta.getInteger(R.styleable.SwipeAnimationButton_defaultBackground, R.drawable.bt_start));
        }

//        defaultDrawable = ContextCompat.getDrawable(context, ta.getInteger(R.styleable.SwipeAnimationButton_defaultDrawable, R.mipmap.bt_collect));

        mVibrate = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mBackground = new RelativeLayout(context);

        LayoutParams layoutParamsView = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        layoutParamsView.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        TextView textView = new TextView(context);
        textView.setText("Text View");
        textView.setLayoutParams(layoutParamsView);

        mBackground.setBackground(ContextCompat.getDrawable(context, ta.getInteger(R.styleable.SwipeAnimationButton_swipe_background, R.color.color_f8f8f8)));
//        mBackground.setPadding(40, 40, 40, 40);
        addView(mBackground, layoutParamsView);

        swipeButton = new ImageView(mContext);
        this.slidingButton = swipeButton;

//        rightSwipeDrawable = ContextCompat.getDrawable(getContext(), ta.getInteger(R.styleable.SwipeAnimationButton_rightSwipeDrawable, R.drawable.sentimental_satisfied));
//        rightSwipeBackground = ContextCompat.getDrawable(context, ta.getInteger(R.styleable.SwipeAnimationButton_defaultBackground, R.drawable.shape_button));
//        leftSwipeDrawable = ContextCompat.getDrawable(getContext(), ta.getInteger(R.styleable.SwipeAnimationButton_leftSwipeDrawable, R.drawable.sentimental_dissatisfied));
//        leftSwipeBackground = ContextCompat.getDrawable(getContext(), ta.getInteger(R.styleable.SwipeAnimationButton_leftSwipeBackground, R.drawable.gradient_radius_grey));

        mDuration = ta.getInteger(R.styleable.SwipeAnimationButton_duration, 200);
        slidingButton.setImageDrawable(defaultDrawable);
        slidingButton.setPadding(66, 60, 66, 60);

        LayoutParams layoutParamsButton = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        layoutParamsButton.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        layoutParamsButton.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

        swipeButton.setImageDrawable(defaultDrawable);
        swipeButton.setBackground(defaultBackground);

        addView(swipeButton, layoutParamsButton);
        setOnTouchListener(getButtonTouchListener());
    }

    private void setFinishImg() {
        WataLog.d("mLanguage=" + mLanguage);
        if(!"ko".equals(mLanguage)) {
            defaultBackground = ContextCompat.getDrawable(mContext, ta.getInteger(R.styleable.SwipeAnimationButton_defaultBackground, R.drawable.bt_finish_en));
        } else {
            defaultBackground = ContextCompat.getDrawable(mContext, ta.getInteger(R.styleable.SwipeAnimationButton_defaultBackground, R.drawable.bt_finish));
        }

        setImageChange(defaultBackground);
    }

    private void setPointImg() {
        WataLog.d("mLanguage=" + mLanguage);
        if(!"ko".equals(mLanguage)) {
            defaultBackground = ContextCompat.getDrawable(mContext, ta.getInteger(R.styleable.SwipeAnimationButton_defaultBackground, R.drawable.bt_point_en));
        } else {
            defaultBackground = ContextCompat.getDrawable(mContext, ta.getInteger(R.styleable.SwipeAnimationButton_defaultBackground, R.drawable.bt_point));
        }
        setImageChange(defaultBackground);
    }

    private void setTransMissionImg() {
        if(!"ko".equals(mLanguage)) {
            defaultBackground = ContextCompat.getDrawable(mContext, ta.getInteger(R.styleable.SwipeAnimationButton_defaultBackground, R.drawable.bt_transmission_en));
        } else {
            defaultBackground = ContextCompat.getDrawable(mContext, ta.getInteger(R.styleable.SwipeAnimationButton_defaultBackground, R.drawable.bt_transmission));
        }

        setImageChange(defaultBackground);
    }

    private void setCollectImg() {
        WataLog.d("mLanguage=" + mLanguage);
        if(!"ko".equals(mLanguage)) {
            defaultBackground = ContextCompat.getDrawable(mContext, ta.getInteger(R.styleable.SwipeAnimationButton_defaultBackground, R.drawable.bt_collect_en));
        } else {
            defaultBackground = ContextCompat.getDrawable(mContext, ta.getInteger(R.styleable.SwipeAnimationButton_defaultBackground, R.drawable.bt_collect));
        }
        setImageChange(defaultBackground);
    }

    private void setStartImg() {
        WataLog.d("mLanguage=" + mLanguage);
        if(!"ko".equals(mLanguage)) {
            defaultBackground = ContextCompat.getDrawable(mContext, ta.getInteger(R.styleable.SwipeAnimationButton_defaultBackground, R.drawable.bt_start_en));
        } else {
            defaultBackground = ContextCompat.getDrawable(mContext, ta.getInteger(R.styleable.SwipeAnimationButton_defaultBackground, R.drawable.bt_start));
        }

        setImageChange(defaultBackground);
    }

    private void setImageChange(Drawable drawable){
        LayoutParams layoutParamsButton = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        layoutParamsButton.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        layoutParamsButton.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

//        swipeButton.setImageDrawable(drawable);
        swipeButton.setBackground(drawable);
    }

    public static String MODE_CHECK = Constance.STOP_RECORD;

    private OnTouchListener getButtonTouchListener() {
        return new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        WataLog.i("ACTION_DOWN");
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        if (initialX == 0) {
                            initialX = slidingButton.getX();
                        }
                        if (event.getX() > initialX + slidingButton.getWidth() / 2 && event.getX() + slidingButton.getWidth() / 2 < getWidth()) {
                            slidingButton.setX(event.getX() - slidingButton.getWidth() / 2);
//                            WataLog.d( "sliding Right");
                        }

                        if (event.getX() < initialX + slidingButton.getWidth() / 2 && event.getX() + slidingButton.getWidth() / 2 < getWidth()) {
                            slidingButton.setX(event.getX() - slidingButton.getWidth() / 2);
//                            WataLog.d( "sliding left");
                        }

                        if (event.getX() + slidingButton.getWidth() / 2 > getWidth() && slidingButton.getX() + slidingButton.getWidth() / 2 < getWidth() + 100) {
//                            WataLog.d( "stop at right");

                            if(MODE_CHECK.equals(Constance.STOP_RECORD)) {
                                setTransMissionImg();
                            } else {
                                setFinishImg();
                            }
                            slidingButton.setX(getWidth() - slidingButton.getWidth());
                        }

                        if (event.getX() + slidingButton.getWidth() / 2 < getWidth() && slidingButton.getX() < 4) {
                            slidingButton.setX(0);
                            setPointImg();
                        }

                        if (event.getX() < slidingButton.getWidth() / 2 && slidingButton.getX() > 0) {
                            slidingButton.setX(initialX);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (active) {
//                            WataLog.d("active=" + active);
                            collapseButton();
                        } else {
                            initialButtonWidth = slidingButton.getWidth();
//                            WataLog.d("slidingButton.getX() + slidingButton.getWidth()=" + (slidingButton.getX() + slidingButton.getWidth()));
//                            WataLog.d("getWidth() * 0.75==" + (getWidth() * 0.60));
//                            WataLog.d("getWidth() * 0.75==" + (getWidth() * 0.75));

                            if(slidingButton.getX() + slidingButton.getWidth() > getWidth() * 0.60 && slidingButton.getX() + slidingButton.getWidth() < getWidth() * 0.85) {
                                // 가운데
                                if (PathDrawingActivity.mLastPointX != 0 && PathDrawingActivity.mLastPointY != 0) {
                                    if(MODE_CHECK.equals(Constance.STOP_RECORD)) {
                                        MODE_CHECK = Constance.START_RECORD;
                                    } else {
                                        MODE_CHECK = Constance.STEP_RECORD;
                                    }
                                    WataLog.d("MODE_CHECK=" + MODE_CHECK);
                                    mSwipeAnimationListener.onSwiped(Constance.CENTER_BTN,MODE_CHECK );
                                    moveToCenter();
                                    setCollectImg();
                                } else {
                                    onToastMessage();
                                    moveToCenter();
                                }
//
                            } else if (slidingButton.getX() + slidingButton.getWidth() > getWidth() * 0.90) {
                                // 오른쪽
                                WataLog.d("MODE_CHECK=" + MODE_CHECK);
                                if(MODE_CHECK.equals(Constance.STOP_RECORD)) {
                                    mSwipeAnimationListener.onSwiped(Constance.RIGHT_BTN, MODE_CHECK);
//                                    MODE_CHECK = Constance.SEND_RECORD;
                                } else {
                                    MODE_CHECK = Constance.STOP_RECORD;
                                    mSwipeAnimationListener.onSwiped(Constance.RIGHT_BTN, MODE_CHECK);
                                }
                                moveToCenter();
                            } else if (slidingButton.getX() - slidingButton.getWidth() < (getWidth() * -0.35)) {
                                // 왼쪽
                                mSwipeAnimationListener.onSwiped(Constance.LEFT_BTN, MODE_CHECK);
                                WataLog.d("MODE_CHECK=" + MODE_CHECK);
                                moveToCenter();
                            } else {
                                WataLog.d("MODE_CHECK=" + MODE_CHECK);
                                moveToCenter();
                            }
                        }
                        return true;
                }

                return false;
            }
        };
    }

    private void onToastMessage() {
        Toast mToast = Toast.makeText(mContext, mContext.getString(R.string.start_point_check_message), Toast.LENGTH_SHORT);
        mToast.setText(mContext.getString(R.string.start_point_check_message));
        mToast.setGravity(Gravity.CENTER, 0, 0);
        mToast.show();
        SwipeAnimationButton.MODE_CHECK = Constance.STOP_RECORD;
    }


    private void collapseButton() {
        final ValueAnimator widthAnimator = ValueAnimator.ofInt(
                slidingButton.getWidth(),
                initialButtonWidth);

        widthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ViewGroup.LayoutParams params = slidingButton.getLayoutParams();
                params.width = (Integer) widthAnimator.getAnimatedValue();
                slidingButton.setLayoutParams(params);
            }
        });

        widthAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                active = false;
                slidingButton.setPadding(66, 60, 66, 60);

                slidingButton.setImageDrawable(defaultDrawable);
                slidingButton.setBackground(defaultBackground);
            }
        });

        AnimatorSet animatorSet = new AnimatorSet();

        animatorSet.play(widthAnimator);
        animatorSet.start();
    }

    private void moveToCenter() {
        final ValueAnimator positionAnimator = ValueAnimator.ofFloat(slidingButton.getX(), 0);
        positionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        positionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float x = (Float) positionAnimator.getAnimatedValue();
                slidingButton.setX(initialX);
            }
        });
        positionAnimator.setDuration(mDuration);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(positionAnimator);
        animatorSet.start();

        if( MODE_CHECK.equals(Constance.STOP_RECORD)) {
            setStartImg();
        } else if( MODE_CHECK.equals(Constance.START_RECORD)) {
            setCollectImg();
        } else if( MODE_CHECK.equals(Constance.STEP_RECORD)) {
            setCollectImg();
        }

    }
}