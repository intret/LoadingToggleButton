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
import android.graphics.drawable.Animatable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Checkable;

import java.util.ArrayList;


/**
 * Created by intret on 18/3/23.
 */

public class LoadingToggleButton extends View implements Checkable, Animatable {
    private static final String TAG = LoadingToggleButton.class.getSimpleName();

    public interface OnCheckedChangeListener {
        void onCheckedChanged(View buttonView, boolean isChecked);
    }

    private OnCheckedChangeListener mOnCheckedChangeListener;

    private Paint mDebugPaint;
    private boolean mShowAssistantLine = false;

    private final float SCALE = (float) (Math.sqrt(2) / 2);

    private boolean mChecked;
    private float mSunCenterX;

    private float mAnimatedToggleX;
    private RectF mDrawingToggleRect = new RectF();
    private RectF mDrawingToggleIndRect = new RectF();

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
    private boolean mHasAnimators;
    private ArrayList<ValueAnimator> mAnimators;

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

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private void init(AttributeSet attrs) {

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

        readAttrs(attrs);
    }

    private void readAttrs(AttributeSet attrs) {
        ToggleSettings settings = new ToggleSettings.Builder().buildSettings();
        if (attrs != null) {

            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LoadingToggleButton);

            mChecked = typedArray.getBoolean(R.styleable.LoadingToggleButton_ltbChecked, false);

            // background
            settings.mBackgroundCheckedColor = typedArray.getColor(R.styleable.LoadingToggleButton_ltbBackgroundCheckedColor, ToggleSettings.BACKGROUND_CHECKED_COLOR);
            settings.mBackgroundUnCheckedColor = typedArray.getColor(R.styleable.LoadingToggleButton_ltbBackgroundUncheckedColor, ToggleSettings.BACKGROUND_UNCHECKED_COLOR);
            settings.mBgRadius = typedArray.getDimensionPixelSize(R.styleable.LoadingToggleButton_ltbBackgroundRadius, ToggleSettings.RADIUS_DEFAULT);

            // toggle color
            settings.mToggleUnCheckedColor = typedArray.getColor(R.styleable.LoadingToggleButton_ltbToggleUncheckedColor, ToggleSettings.TOGGLE_UNCHECKED_COLOR);
            settings.mToggleCheckedColor = typedArray.getColor(R.styleable.LoadingToggleButton_ltbToggleCheckedColor, ToggleSettings.TOGGLE_CHECKED_COLOR);

            // toggle size
            settings.mToggleRadius = typedArray.getDimensionPixelSize(R.styleable.LoadingToggleButton_ltbToggleRadius, dpToPx(ToggleSettings.RADIUS_DEFAULT));
            settings.mPadding = typedArray.getDimensionPixelSize(R.styleable.LoadingToggleButton_ltbTogglePadding, dpToPx(ToggleSettings.PADDING_DEFAULT));

            // toggle behaviors
            boolean withAnimator = typedArray.getBoolean(R.styleable.LoadingToggleButton_ltbToggleWithAnimate, true);
            int duration = typedArray.getInt(R.styleable.LoadingToggleButton_ltbDuration, ToggleSettings.DURATION_DEFAULT);
            settings.mDuration = !withAnimator ? 1 : duration;

            // toggle indicator
            settings.toggleIndicatorHeight = typedArray.getDimensionPixelSize(R.styleable.LoadingToggleButton_ltbToggleIndicatorHeight, dpToPx(ToggleSettings.DEFAULT_TOGGLE_INDICATOR_HEIGHT));
            settings.toggleIndicatorWidth = typedArray.getDimensionPixelSize(R.styleable.LoadingToggleButton_ltbToggleIndicatorWidth, dpToPx(ToggleSettings.DEFAULT_TOGGLE_INDICATOR_WIDTH));
            settings.toggleIndicatorRadius = typedArray.getDimensionPixelSize(R.styleable.LoadingToggleButton_ltbToggleIndicatorRadius, dpToPx(ToggleSettings.DEFAULT_TOGGLE_INDICATOR_RADIUS));
            settings.toggleIndicatorVisibility = typedArray.getInt(R.styleable.LoadingToggleButton_ltbToggleIndicatorVisibility, ToggleSettings.DEFAULT_INDICATOR_VISIBLE);

            typedArray.recycle();
        }
        setToggleSettings(settings);
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
                : (mToggleSettings.mBgRadius < 0 ? maxBgRadius : mToggleSettings.mBgRadius);

        // TODO check negative?
        mToggleRadius = mToggleSettings.mToggleRadius > maxToggleRadius ? maxToggleRadius
                : (mToggleSettings.mToggleRadius < 0 ? maxToggleRadius : mToggleSettings.mToggleRadius);

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
    public void setChecked(final boolean checked) {
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

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return false;
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

        if (mToggleSettings.toggleIndicatorVisibility == ToggleSettings.INDICATOR_VISIBLE_SHOW) {
            drawToggleIndicator(canvas);
        }

        //drawSun(canvas);

        if (mShowAssistantLine) {
            canvas.drawRect(mFixedLeftToggleOutline, mDebugPaint);
            canvas.drawRect(mFixedRightToggleOutline, mDebugPaint);
        }
    }

    private void ensureAnimators() {
        if (!mHasAnimators) {
            mAnimators = createAnimators();
            mHasAnimators = true;
        }
    }

    public ArrayList<ValueAnimator> createAnimators() {
        return new ArrayList<>();
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

    void getCenterRectOfRect(RectF containerRect, float width, float height, RectF outCenterRect) {
        outCenterRect.left = containerRect.left + containerRect.width() / 2f - width / 2f;
        outCenterRect.right = outCenterRect.left + width;

        outCenterRect.top = containerRect.top + containerRect.height() / 2f - height / 2f;
        outCenterRect.bottom = outCenterRect.top + height;
    }

    private void drawToggleIndicator(Canvas canvas) {

        getCenterRectOfRect(mDrawingToggleRect,
                mToggleSettings.toggleIndicatorWidth, mToggleSettings.toggleIndicatorHeight,
                mDrawingToggleIndRect);

        // TODO: add corresponding color attribute
        mPaint.setColor(mBackgroundColor);

        canvas.drawRoundRect(mDrawingToggleIndRect, mToggleSettings.toggleIndicatorRadius, mToggleSettings.toggleIndicatorRadius, mPaint);
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
        pathBg.addRoundRect(mBgRect, mBgRadius, mBgRadius, Path.Direction.CW);

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
