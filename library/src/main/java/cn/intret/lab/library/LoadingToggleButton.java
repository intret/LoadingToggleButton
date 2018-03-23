package cn.intret.lab.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;


/**
 * Created by intret on 18/3/23.
 */

public class LoadingToggleButton extends View implements Checkable {
    private static final String TAG = LoadingToggleButton.class.getSimpleName();

    private Paint mDebugPaint;
    private boolean mShowAssistantLine = false;

    private final float SCALE = (float) (Math.sqrt(2) / 2);
    private OnCheckedChangeListener mOnCheckedChangeListener;
    private boolean mChecked;
    private float mSunCenterX;
    private float mCenterDistance;
    private ValueAnimator mToggleAnimator;
    private Paint mPaint;
    private int mBgRadius;
    private int mToggleRadius;

    private RectF mLeftToggleOutline;
    private RectF mRightToggleOutline;

    private Path pathBg;
    private Path pathSun;
    private Path pathMoon;
    private int mBackgroundColor;
    private int mToggleColor;
    private ToggleSettings mToggleSettings;
    private boolean mIsAnimating;

    private static final int[] CheckedStateSet = {
            android.R.attr.state_checked,
    };

    public LoadingToggleButton(Context context) {
        super(context);
        init(null);
    }

    public LoadingToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public LoadingToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        ToggleSettings toggleSettings = new ToggleSettings.Builder().buildSettings();
        setClickable(true);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.FILL);

        if (mShowAssistantLine) {
            mDebugPaint = new Paint();
            mDebugPaint.setAntiAlias(true);
            mDebugPaint.setColor(Color.RED);
            mDebugPaint.setStyle(Paint.Style.STROKE);
            mDebugPaint.setStrokeWidth(1);
        }

        pathSun = new Path();
        pathMoon = new Path();
        pathBg = new Path();

        if (attrs != null) {

            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LoadingToggleButton);

            mChecked = typedArray.getBoolean(R.styleable.LoadingToggleButton_ltbChecked, false);

            int toggleUnchecked = typedArray.getColor(R.styleable.LoadingToggleButton_ltbToggleUncheckedColor, ToggleSettings.TOGGLE_UNCHECKED_COLOR);
            int toggleChecked = typedArray.getColor(R.styleable.LoadingToggleButton_ltbToggleCheckedColor, ToggleSettings.TOGGLE_CHECKED_COLOR);
            int backgroundUnCheckColor = typedArray.getColor(R.styleable.LoadingToggleButton_ltbBackgroundUncheckedColor, ToggleSettings.BACKGROUND_UNCHECKED_COLOR);
            int backgroundCheckColor = typedArray.getColor(R.styleable.LoadingToggleButton_ltbBackgroundCheckedColor, ToggleSettings.BACKGROUND_CHECKED_COLOR);
            int padding = typedArray.getDimensionPixelSize(R.styleable.LoadingToggleButton_ltbTogglePadding, ToggleSettings.PADDING_DEFAULT);
            int duration = typedArray.getInt(R.styleable.LoadingToggleButton_ltbDuration, ToggleSettings.DURATION_DEFAULT);
            boolean withAnimator = typedArray.getBoolean(R.styleable.LoadingToggleButton_ltbToggleWithAnimate, true);
            toggleSettings = new ToggleSettings.Builder()
                    .setToggleUnCheckedColor(toggleUnchecked)
                    .setToggleCheckedColor(toggleChecked)
                    .setBackgroundUncheckedColor(backgroundUnCheckColor)
                    .setBackgroundCheckedColor(backgroundCheckColor)
                    .setPadding(padding)
                    .setDuration(duration)
                    .withAnimator(withAnimator)
                    .buildSettings();
            typedArray.recycle();
        }
        setToggleSettings(toggleSettings);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setUp();
    }

    private void setUp() {
        mBgRadius = getHeight() / 2;
        mToggleRadius = mBgRadius - mToggleSettings.mPadding;

        mSunCenterX = mChecked ? getWidth() - mBgRadius: mBgRadius;
        mCenterDistance = 2 * mToggleRadius;

        mLeftToggleOutline = new RectF(0, 0, mBgRadius * 2, mBgRadius * 2);
        mRightToggleOutline = new RectF(getWidth() - mBgRadius * 2, 0, getWidth(), mBgRadius * 2);
    }

    public void setToggleSettings(ToggleSettings toggleSettings) {
        this.mToggleSettings = toggleSettings;

        if (mChecked) {
            mToggleColor = mToggleSettings.mToggleCheckedColor;
            mBackgroundColor = mToggleSettings.mBackgroundCheckedColor;
        } else {
            mToggleColor = mToggleSettings.mToggleUnCheckedColor;
            mBackgroundColor = mToggleSettings.mBackgroundUnCheckedColor;
        }
    }

    public ToggleSettings getToggleSettings() {
        return mToggleSettings;
    }

    private void animateToggle(boolean toggleToOn) {
        final float originX;
        final float endX;
        final float originY;
        final float endY;
        final int startBackgroundColor;
        final int endBackgroundColor;
        final int startToggleColor;
        final int endToggleColor;
        if (toggleToOn) {
            originX = mBgRadius;
            endX = getWidth() - mBgRadius;
            originY = 2 * mToggleRadius;
            endY = mToggleRadius;
            startBackgroundColor = mToggleSettings.mBackgroundUnCheckedColor;
            endBackgroundColor = mToggleSettings.mBackgroundCheckedColor;
            startToggleColor = mToggleSettings.mToggleUnCheckedColor;
            endToggleColor = mToggleSettings.mToggleCheckedColor;
        } else {
            originX = getWidth() - mBgRadius;
            endX = mBgRadius;
            originY = mToggleRadius;
            endY = 2 * mToggleRadius;
            startBackgroundColor = mToggleSettings.mBackgroundCheckedColor;
            endBackgroundColor = mToggleSettings.mBackgroundUnCheckedColor;
            startToggleColor = mToggleSettings.mToggleCheckedColor;
            endToggleColor = mToggleSettings.mToggleUnCheckedColor;
        }

        mToggleAnimator = ValueAnimator.ofFloat(0, 1.0f);
//        mToggleAnimator.setInterpolator(new FastOutLinearInInterpolator());
        mToggleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            FloatEvaluator floatEvaluator = new FloatEvaluator();
            ArgbEvaluator argbEvaluator = new ArgbEvaluator();

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                mSunCenterX = floatEvaluator.evaluate(fraction, originX, endX);
                mCenterDistance = floatEvaluator.evaluate(fraction, originY, endY);
                mBackgroundColor = (int) argbEvaluator.evaluate(fraction, startBackgroundColor, endBackgroundColor);
                mToggleColor = (int) argbEvaluator.evaluate(fraction, startToggleColor, endToggleColor);
                invalidate();
            }
        });
        mToggleAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mChecked = !mChecked;
                mIsAnimating = false;
            }
        });
        mToggleAnimator.setDuration(mToggleSettings.mDuration);
        mToggleAnimator.start();
        mIsAnimating = true;
    }

    public void setOnCheckChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.mOnCheckedChangeListener = onCheckedChangeListener;
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    @Override
    public void setChecked(boolean checked) {
        if (mIsAnimating) {
            return;
        }
        if (mOnCheckedChangeListener != null) {
            mOnCheckedChangeListener.onCheckedChanged(this, checked);
        }
        animateToggle(checked);
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public boolean performClick() {
        toggle();
        return super.performClick();
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(View buttonView, boolean isChecked);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CheckedStateSet);
        }
        return drawableState;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        pathBg.reset();
        pathSun.reset();
        pathMoon.reset();



        pathBg.addArc(mLeftToggleOutline, 90, 180);
        pathBg.moveTo(mBgRadius, 0);
        pathBg.lineTo(mBgRadius * 4, 0);
        pathBg.addArc(mRightToggleOutline, 270, 180);
        pathBg.moveTo(getWidth() - mBgRadius, mBgRadius * 2);
        pathBg.addRect(mBgRadius, 0, getWidth() - mBgRadius, mBgRadius * 2, Path.Direction.CW);

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mBackgroundColor);
        canvas.drawPath(pathBg, mPaint);

        pathSun.addCircle(mSunCenterX, mBgRadius, mToggleRadius, Path.Direction.CW);
        pathMoon.addCircle(mSunCenterX - mCenterDistance * SCALE, mBgRadius - mCenterDistance * SCALE, mToggleRadius, Path.Direction.CW);

        // a big circle subtracts a small circle, results a moon shape
        //pathSun.op(pathMoon, Path.Op.DIFFERENCE);

        mPaint.setColor(mToggleColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(pathSun, mPaint);

        if (mShowAssistantLine) {
            canvas.drawRect(mLeftToggleOutline, mDebugPaint);
            canvas.drawRect(mRightToggleOutline, mDebugPaint);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mToggleAnimator != null) {
            mToggleAnimator.cancel();
        }
    }
}
