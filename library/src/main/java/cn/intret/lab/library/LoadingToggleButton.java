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
import android.util.Log;
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

    private float mAnimatedToggleX;
    private RectF mDrawingToggleRect = new RectF();

    private float mCenterDistance;
    private ValueAnimator mToggleAnimator;
    private Paint mPaint;
    private int mBgRadius;
    private int mToggleRadius;

    private RectF mFixedLeftToggleOutline = new RectF();
    private RectF mFixedRightToggleOutline = new RectF();

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
    private int mWidth;
    private int mHeight;



    private int mRightIndicatorX;
    private int mRightIndicatorY;
    private int mToggleWidth;
    private int mToggleHeight;

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

            int bgRadius = typedArray.getDimensionPixelSize(R.styleable.LoadingToggleButton_ltbBackgroundRadius, ToggleSettings.RADIUS_DEFAULT);

            int padding = typedArray.getDimensionPixelSize(R.styleable.LoadingToggleButton_ltbTogglePadding, ToggleSettings.PADDING_DEFAULT);
            int toggleRadius = typedArray.getDimensionPixelSize(R.styleable.LoadingToggleButton_ltbToggleRadius, ToggleSettings.RADIUS_DEFAULT);
            int duration = typedArray.getInt(R.styleable.LoadingToggleButton_ltbDuration, ToggleSettings.DURATION_DEFAULT);
            boolean withAnimator = typedArray.getBoolean(R.styleable.LoadingToggleButton_ltbToggleWithAnimate, true);
            toggleSettings = new ToggleSettings.Builder()
                    .setToggleUnCheckedColor(toggleUnchecked)
                    .setToggleCheckedColor(toggleChecked)
                    .setBackgroundUncheckedColor(backgroundUnCheckColor)
                    .setBackgroundCheckedColor(backgroundCheckColor)
                    .setPadding(padding)
                    .setDuration(duration)
                    .setToggleRadius(toggleRadius)
                    .setBgRadius(bgRadius)
                    .withAnimator(withAnimator)
                    .buildSettings();
            typedArray.recycle();
        }
        setToggleSettings(toggleSettings);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(TAG, "onSizeChanged() called with: w = [" + w + "], h = [" + h + "], oldw = [" + oldw + "], oldh = [" + oldh + "]");
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = w;
        mHeight = h;

        setUp();
    }

    private void setUp() {

        // toggle size, it may be a long toggle, a circle toggle
        mToggleHeight = mHeight - 2 * mToggleSettings.mPadding;
        mToggleWidth = mToggleHeight;

        final int maxBgRadius = mHeight / 2;
        final int maxToggleRadius = mToggleHeight / 2;

        mBgRadius = (mToggleSettings.mBgRadius > maxBgRadius) ? maxBgRadius
                : (mToggleSettings.mBgRadius < 0 ? (mToggleSettings.mBgRadius == -1 ? maxBgRadius : 0 ) : mToggleSettings.mBgRadius);

        // TODO check negative?
        mToggleRadius = mToggleSettings.mToggleRadius > maxToggleRadius ? maxToggleRadius
                : (mToggleSettings.mToggleRadius < 0 ? (mToggleSettings.mToggleRadius == -1 ? maxToggleRadius : 0) : mToggleSettings.mToggleRadius);

        // Two fixed position of toggle's outline, the toggle will animated between these two position
        mFixedRightToggleOutline.right = mWidth - mToggleSettings.mPadding;
        mFixedRightToggleOutline.top = mToggleSettings.mPadding;
        mFixedRightToggleOutline.left = mFixedRightToggleOutline.right - mToggleWidth;
        mFixedRightToggleOutline.bottom = mHeight - mToggleSettings.mPadding;

        mFixedLeftToggleOutline.left = mToggleSettings.mPadding;
        mFixedLeftToggleOutline.top = mToggleSettings.mPadding;
        mFixedLeftToggleOutline.right = mFixedLeftToggleOutline.left + mToggleWidth;
        mFixedLeftToggleOutline.bottom = mHeight - mToggleSettings.mPadding;


        mDrawingToggleRect.set(mChecked ? mFixedRightToggleOutline : mFixedLeftToggleOutline);
        mAnimatedToggleX = mDrawingToggleRect.left;

        // TODO deprecated?
        mSunCenterX = mChecked ? getWidth() - mBgRadius : mBgRadius;
        mCenterDistance = 2 * mToggleRadius;

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
        final int bgStartColor;
        final int bgEndColor;
        final int toggleStartColor;
        final int toggleEndColor;

        final float toggleStartX;
        final float toggleEndX;

        if (toggleToOn) {

            // for DayNightToggle mode, the toggle si animated from sun to moon
            originX = mBgRadius;
            originY = 2 * mToggleRadius;

            endX = getWidth() - mBgRadius;
            endY = mToggleRadius;

            // for a normal toggle
            toggleStartX = mFixedLeftToggleOutline.left;
            toggleEndX = mFixedRightToggleOutline.left;


            bgStartColor = mToggleSettings.mBackgroundUnCheckedColor;
            bgEndColor = mToggleSettings.mBackgroundCheckedColor;
            toggleStartColor = mToggleSettings.mToggleUnCheckedColor;
            toggleEndColor = mToggleSettings.mToggleCheckedColor;
        } else {


            // for DayNightToggle mode
            originX = getWidth() - mBgRadius;
            endX = mBgRadius;
            originY = mToggleRadius;
            endY = 2 * mToggleRadius;


            // for a normal toggle
            toggleStartX = mFixedRightToggleOutline.left;
            toggleEndX = mFixedLeftToggleOutline.left;

            bgStartColor = mToggleSettings.mBackgroundCheckedColor;
            bgEndColor = mToggleSettings.mBackgroundUnCheckedColor;
            toggleStartColor = mToggleSettings.mToggleCheckedColor;
            toggleEndColor = mToggleSettings.mToggleUnCheckedColor;
        }

        mToggleAnimator = ValueAnimator.ofFloat(0, 1.0f);
//        mToggleAnimator.setInterpolator(new FastOutLinearInInterpolator());
        mToggleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            FloatEvaluator floatEvaluator = new FloatEvaluator();
            ArgbEvaluator argbEvaluator = new ArgbEvaluator();

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                mAnimatedToggleX = floatEvaluator.evaluate(fraction, toggleStartX, toggleEndX);

                mSunCenterX = floatEvaluator.evaluate(fraction, originX, endX);
                mCenterDistance = floatEvaluator.evaluate(fraction, originY, endY);

                mBackgroundColor = (int) argbEvaluator.evaluate(fraction, bgStartColor, bgEndColor);
                mToggleColor = (int) argbEvaluator.evaluate(fraction, toggleStartColor, toggleEndColor);
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

    RectF mBgRect = new RectF();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        pathBg.reset();
        pathSun.reset();
        pathMoon.reset();

        drawBackground(canvas);
        drawToggle(canvas);
        //drawSun(canvas);

        if (mShowAssistantLine) {
            canvas.drawRect(mFixedLeftToggleOutline, mDebugPaint);
            canvas.drawRect(mFixedRightToggleOutline, mDebugPaint);
        }
    }


    void drawToggle(Canvas canvas) {
        mDrawingToggleRect.set(mFixedLeftToggleOutline);
        mDrawingToggleRect.offset(mAnimatedToggleX - mFixedLeftToggleOutline.left, 0f);

        pathSun.addRoundRect(mDrawingToggleRect, mToggleRadius, mToggleRadius, Path.Direction.CW);

        mPaint.setColor(mToggleColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(pathSun, mPaint);

        if (mShowAssistantLine) {
            canvas.drawRect(mDrawingToggleRect, mDebugPaint);
        }
    }

    void drawSun(Canvas canvas) {
        pathSun.addCircle(mSunCenterX, mBgRadius, mToggleRadius, Path.Direction.CW);

        //pathMoon.addCircle(mSunCenterX - mCenterDistance * SCALE, mBgRadius - mCenterDistance * SCALE, mToggleRadius, Path.Direction.CW);

        // a big circle subtracts a small circle, results a moon shape
        //pathSun.op(pathMoon, Path.Op.DIFFERENCE);

        mPaint.setColor(mToggleColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(pathSun, mPaint);
    }

    void drawBackground(Canvas canvas) {

//        pathBg.addArc(mFixedLeftToggleOutline, 90, 180);
//        pathBg.moveTo(mBgRadius, 0);
//        pathBg.lineTo(mBgRadius * 4, 0);
//        pathBg.addArc(mFixedRightToggleOutline, 270, 180);
//        pathBg.moveTo(getWidth() - mBgRadius, mBgRadius * 2);
//        pathBg.addRect(mBgRadius, 0, getWidth() - mBgRadius, mBgRadius * 2, Path.Direction.CW);

        mBgRect.set(0, 0, mWidth, mHeight);
        pathBg.addRoundRect(mBgRect, mBgRadius , mBgRadius , Path.Direction.CW);

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mBackgroundColor);
        canvas.drawPath(pathBg, mPaint);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mToggleAnimator != null) {
            mToggleAnimator.cancel();
        }
    }
}
