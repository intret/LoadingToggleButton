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
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.support.annotation.FloatRange;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Checkable;

import java.util.ArrayList;
import java.util.List;


/**
 * A loading toggle button with loading animation.
 */
public class LoadingToggleButton extends View implements Checkable, Animatable {

    private static final String TAG = LoadingToggleButton.class.getSimpleName();

    private static final int DEFAULT_WIDTH = 44; // in dp
    private static final int DEFAULT_HEIGHT = 22; // in dp

    private long mStartAnimationTime;
    private boolean mShowLoading = false;


    /*
     * Interaction
     */
    public interface OnCheckedChangeListener {
        void onCheckedChanged(LoadingToggleButton toggleButton, boolean isChecked);
    }

    public interface OnLoadingChangeListener {
        void onLoadingChanged(LoadingToggleButton toggleButton, boolean loading);
    }

    private OnCheckedChangeListener mOnCheckedChangeListener;
    private OnLoadingChangeListener mOnLoadingChangeListener;


    /*
     * Debug
     */

    private Paint mDebugPaint;
    private boolean mShowAssistantLine = false;

    /*
    * View size, basic information
    */
    private int mWidth;
    private int mHeight;
    private boolean mChecked;

    /*
     * Configurations from XML attributes
     */
    private ToggleConfiguration mToggleConfiguration;

    /*
     * Toggle size position
     */
    private float mAnimatedToggleX;
    private float mSunCenterX;

    private int mToggleWidth;
    private int mToggleHeight;
    private int mToggleRadius;

    /*
     * Toggle area
     */
    private RectF mDrawingToggleRect = new RectF();
    private RectF mDrawingToggleIndRect = new RectF();
    private RectF mFixedLeftToggleOutline = new RectF();
    private RectF mFixedRightToggleOutline = new RectF();

    private float mCenterDistance;

    /*
     * Toggle drawing
     */
    private Paint mPaint;
    private Path mTogglePath;
    private int mToggleColor;

    /*
     * Toggle indicator
     */

    private int mIndicatorColor;

    /*
     * Drawing Background
     */
    private RectF mBgRect = new RectF();
    private int mBgRadius;

    private Path mBackgroundPath;
    private int mBackgroundColor;

    private Path pathMoon;

    /*
     * Animation
     */

    // Create the Handler object (on the main thread by default)
    android.os.Handler mAnimationHandler = new android.os.Handler();

    // whether is is showing loading toggle indicator
    boolean mIsIndicatorAnimating;

    private ValueAnimator mToggleAnimator;

    /**
     * whether it is on-off animation
     */
    private boolean mIsToggleAnimating;

    private static final int[] CheckedStateSet = {
            android.R.attr.state_checked,
    };
    private boolean mHasAnimators = false;
    private List<Animator> mAnimators;

    private long loadingIndicatorRedrawIntervalMillis = 500;
    private boolean mIndicatorOn = false;
    private float mSpinnerProgress;

    /* ---------------------------------------------------------------------------------------------
     * Getters and setters
     * ---------------------------------------------------------------------------------------------
     */

    public void setToggleConfiguration(ToggleConfiguration toggleConfiguration) {
        this.mToggleConfiguration = toggleConfiguration;

        if (mChecked) {
            mToggleColor = mToggleConfiguration.mToggleCheckedColor;
            mBackgroundColor = mToggleConfiguration.mBackgroundCheckedColor;
        } else {
            mToggleColor = mToggleConfiguration.mToggleUnCheckedColor;
            mBackgroundColor = mToggleConfiguration.mBackgroundUnCheckedColor;
        }
    }

    public LoadingToggleButton setOnLoadingChangeListener(OnLoadingChangeListener onLoadingChangeListener) {
        mOnLoadingChangeListener = onLoadingChangeListener;
        return this;
    }

    public LoadingToggleButton setOnCheckChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.mOnCheckedChangeListener = onCheckedChangeListener;
        return this;
    }

    public ToggleConfiguration getToggleConfiguration() {
        return mToggleConfiguration;
    }

    /* ---------------------------------------------------------------------------------------------
     * Constructor and initialization
     * ---------------------------------------------------------------------------------------------
     */


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

        mTogglePath = new Path();
        pathMoon = new Path();
        mBackgroundPath = new Path();

        readAttrs(attrs);
        postInit();
    }

    private void postInit() {
        mIndicatorColor = mToggleConfiguration.mBackgroundCheckedColor;
    }

    private void readAttrs(AttributeSet attrs) {
        ToggleConfiguration settings = new ToggleConfiguration();
        if (attrs != null) {

            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LoadingToggleButton);

            mChecked = typedArray.getBoolean(R.styleable.LoadingToggleButton_ltbToggleChecked, false);

            // background
            settings.mBackgroundCheckedColor = typedArray.getColor(R.styleable.LoadingToggleButton_ltbBackgroundCheckedColor, ToggleConfiguration.BACKGROUND_CHECKED_COLOR);
            settings.mBackgroundUnCheckedColor = typedArray.getColor(R.styleable.LoadingToggleButton_ltbBackgroundUncheckedColor, ToggleConfiguration.BACKGROUND_UNCHECKED_COLOR);
            settings.mBgRadius = typedArray.getDimensionPixelSize(R.styleable.LoadingToggleButton_ltbBackgroundRadius, ToggleConfiguration.RADIUS_DEFAULT);

            // toggle color
            settings.mToggleUnCheckedColor = typedArray.getColor(R.styleable.LoadingToggleButton_ltbToggleUncheckedColor, ToggleConfiguration.TOGGLE_UNCHECKED_COLOR);
            settings.mToggleCheckedColor = typedArray.getColor(R.styleable.LoadingToggleButton_ltbToggleCheckedColor, ToggleConfiguration.TOGGLE_CHECKED_COLOR);

            // toggle size
            settings.mToggleRadius = typedArray.getDimensionPixelSize(R.styleable.LoadingToggleButton_ltbToggleRadius, dpToPx(ToggleConfiguration.RADIUS_DEFAULT));
            settings.mPadding = typedArray.getDimensionPixelSize(R.styleable.LoadingToggleButton_ltbTogglePadding, dpToPx(ToggleConfiguration.PADDING_DEFAULT));

            // toggle behaviors
            boolean withAnimator = typedArray.getBoolean(R.styleable.LoadingToggleButton_ltbToggleWithAnimate, true);
            int duration = typedArray.getInt(R.styleable.LoadingToggleButton_ltbToggleDuration, ToggleConfiguration.DURATION_DEFAULT);
            settings.mDuration = !withAnimator ? 1 : duration;

            // toggle indicator
            settings.toggleIndicatorHeight = typedArray.getDimensionPixelSize(R.styleable.LoadingToggleButton_ltbIndicatorHeight, dpToPx(ToggleConfiguration.DEFAULT_TOGGLE_INDICATOR_HEIGHT));
            settings.toggleIndicatorWidth = typedArray.getDimensionPixelSize(R.styleable.LoadingToggleButton_ltbIndicatorWidth, dpToPx(ToggleConfiguration.DEFAULT_TOGGLE_INDICATOR_WIDTH));
            settings.toggleIndicatorRadius = typedArray.getDimensionPixelSize(R.styleable.LoadingToggleButton_ltbIndicatorRadius, dpToPx(ToggleConfiguration.DEFAULT_TOGGLE_INDICATOR_RADIUS));
            settings.indicatorVisibility = typedArray.getInt(R.styleable.LoadingToggleButton_ltbIndicatorVisibility, ToggleConfiguration.DEFAULT_INDICATOR_VISIBLE);

            settings.showIndicatorWhen = typedArray.getInt(R.styleable.LoadingToggleButton_ltbIndicatorAnimatingWhen, ToggleConfiguration.SHOW_INDICATOR_WHEN_TOGGLE_TO_ON);

            settings.loadingAnimationType = typedArray.getInt(R.styleable.LoadingToggleButton_ltbIndicatorAnimationType, ToggleConfiguration.LOADING_ANIMATION_FLICK);

            typedArray.recycle();
        }
        setToggleConfiguration(settings);
    }

    private void setUp() {

        // toggle size, it may be a long toggle, a circle toggle
        mToggleHeight = mHeight - 2 * mToggleConfiguration.mPadding;
        mToggleWidth = mToggleHeight;

        final int maxBgRadius = mHeight / 2;
        final int maxToggleRadius = mToggleHeight / 2;

        mBgRadius = (mToggleConfiguration.mBgRadius > maxBgRadius) ? maxBgRadius
                : (mToggleConfiguration.mBgRadius < 0 ? maxBgRadius : mToggleConfiguration.mBgRadius);

        mToggleRadius = mToggleConfiguration.mToggleRadius > maxToggleRadius ? maxToggleRadius
                : (mToggleConfiguration.mToggleRadius < 0 ? maxToggleRadius : mToggleConfiguration.mToggleRadius);

        // Two fixed position of toggle's outline, the toggle will animated between these two position
        mFixedRightToggleOutline.right = mWidth - mToggleConfiguration.mPadding;
        mFixedRightToggleOutline.top = mToggleConfiguration.mPadding;
        mFixedRightToggleOutline.left = mFixedRightToggleOutline.right - mToggleWidth;
        mFixedRightToggleOutline.bottom = mHeight - mToggleConfiguration.mPadding;

        mFixedLeftToggleOutline.left = mToggleConfiguration.mPadding;
        mFixedLeftToggleOutline.top = mToggleConfiguration.mPadding;
        mFixedLeftToggleOutline.right = mFixedLeftToggleOutline.left + mToggleWidth;
        mFixedLeftToggleOutline.bottom = mHeight - mToggleConfiguration.mPadding;


        mDrawingToggleRect.set(mChecked ? mFixedRightToggleOutline : mFixedLeftToggleOutline);
        mAnimatedToggleX = mDrawingToggleRect.left;

        // TODO deprecated?
        mSunCenterX = mChecked ? getWidth() - mBgRadius : mBgRadius;
        mCenterDistance = 2 * mToggleRadius;

    }

    /* ---------------------------------------------------------------------------------------------
     * Helper
     * ---------------------------------------------------------------------------------------------
     */

    private boolean containsFlag(int flagSet, int flag) {
        return (flagSet | flag) == flagSet;
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    void getCenterRectOfRect(RectF containerRect, float width, float height, RectF outCenterRect) {
        outCenterRect.left = containerRect.left + containerRect.width() / 2f - width / 2f;
        outCenterRect.right = outCenterRect.left + width;

        outCenterRect.top = containerRect.top + containerRect.height() / 2f - height / 2f;
        outCenterRect.bottom = outCenterRect.top + height;
    }


    /* ---------------------------------------------------------------------------------------------
     * Actions
     * ---------------------------------------------------------------------------------------------
     */


    private void animateToggle(final boolean toggleToOn) {
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


            bgStartColor = mToggleConfiguration.mBackgroundUnCheckedColor;
            bgEndColor = mToggleConfiguration.mBackgroundCheckedColor;
            toggleStartColor = mToggleConfiguration.mToggleUnCheckedColor;
            toggleEndColor = mToggleConfiguration.mToggleCheckedColor;
        } else {

            // for DayNightToggle mode
            originX = getWidth() - mBgRadius;
            endX = mBgRadius;
            originY = mToggleRadius;
            endY = 2 * mToggleRadius;


            // for a normal toggle
            toggleStartX = mFixedRightToggleOutline.left;
            toggleEndX = mFixedLeftToggleOutline.left;

            bgStartColor = mToggleConfiguration.mBackgroundCheckedColor;
            bgEndColor = mToggleConfiguration.mBackgroundUnCheckedColor;
            toggleStartColor = mToggleConfiguration.mToggleCheckedColor;
            toggleEndColor = mToggleConfiguration.mToggleUnCheckedColor;
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
                mIsToggleAnimating = false;

                // toggle indicator flicker
                if (mToggleConfiguration.loadingAnimationType != ToggleConfiguration.LOADING_ANIMATION_NONE) {
                    if (toggleToOn) {

                        if (containsFlag(mToggleConfiguration.showIndicatorWhen, ToggleConfiguration.SHOW_INDICATOR_WHEN_TOGGLE_TO_ON)) {
                            Log.d(TAG, "indicator flicking started when 'toggle to on' animation ended");
                            mShowLoading = true;
                            start();
                        }
                    } else {
                        if (containsFlag(mToggleConfiguration.showIndicatorWhen, ToggleConfiguration.SHOW_INDICATOR_WHEN_TOGGLE_TO_OFF)) {
                            Log.d(TAG, "indicator flicking started when 'toggle to off' animation ended");
                            mShowLoading = true;
                            start();
                        }
                    }
                }

                // notify 'checked' changed
                if (mOnCheckedChangeListener != null) {
                    mOnCheckedChangeListener.onCheckedChanged(LoadingToggleButton.this, mChecked);
                }
            }
        });
        mToggleAnimator.setDuration(mToggleConfiguration.mDuration);
        mToggleAnimator.start();
        mIsToggleAnimating = true;
    }


    /* ---------------------------------------------------------------------------------------------
     * Checkable
     * ---------------------------------------------------------------------------------------------
     */

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    @Override
    public void setChecked(final boolean checked) {
        if (mIsToggleAnimating) {
            return;
        }

        if (checked == mChecked) {
            Log.d(TAG, "Duplicated status, LoadingToggleButton is already " + (checked ? "checked" : "unchecked"));
            return;
        }

        animateToggle(checked);
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    /* ---------------------------------------------------------------------------------------------
     * Animatable
     * ---------------------------------------------------------------------------------------------
     */

    /**
     * called every loadingIndicatorRedrawIntervalMillis
     */
    Runnable mRedrawRunnable = new Runnable() {
        @Override
        public void run() {
            if (mIsIndicatorAnimating) {

                switch (mToggleConfiguration.loadingAnimationType) {
                    case ToggleConfiguration.LOADING_ANIMATION_FLICK:
                        toggleIndicatorFlickingState();
                        break;
                    case ToggleConfiguration.LOADING_ANIMATION_LINE_SPINNER:
                        updateSpinnerProgress();
                        break;
                }

                postInvalidate();

                // schedule next drawing
                mAnimationHandler.postDelayed(this, loadingIndicatorRedrawIntervalMillis);
            }
        }

        private void updateSpinnerProgress() {
            long c = System.currentTimeMillis();
            mSpinnerProgress = ((c - mStartAnimationTime) % 1500) / 1500f;
        }

        private void toggleIndicatorFlickingState() {
            mIndicatorOn = !mIndicatorOn;
            mIndicatorColor = mIndicatorOn ? mToggleConfiguration.mBackgroundCheckedColor : mToggleConfiguration.mBackgroundUnCheckedColor;
        }
    };

    @Override
    public void start() {
        if (mIsIndicatorAnimating) {
            return;
        } else {

            switch (mToggleConfiguration.loadingAnimationType) {
                case ToggleConfiguration.LOADING_ANIMATION_FLICK:
                    startFlickingAnimation();
                    break;
                case ToggleConfiguration.LOADING_ANIMATION_LINE_SPINNER:
                    startLineSpinnerAnimation();
                    break;
                case ToggleConfiguration.LOADING_ANIMATION_DOT_SPINNER:
                    break;
            }

            if (mOnLoadingChangeListener != null) {
                mOnLoadingChangeListener.onLoadingChanged(this, mShowLoading);
            }
        }

    }

    private void startLineSpinnerAnimation() {
        mIsIndicatorAnimating = true;
        mShowLoading = true;

        mStartAnimationTime = System.currentTimeMillis();
        loadingIndicatorRedrawIntervalMillis = 20;

        mAnimationHandler.post(mRedrawRunnable);
    }

    private void startFlickingAnimation() {

        mIsIndicatorAnimating = true;
        mShowLoading = true;

        loadingIndicatorRedrawIntervalMillis = 500;
        mAnimationHandler.post(mRedrawRunnable);
    }


    @Override
    public void stop() {
        switch (mToggleConfiguration.loadingAnimationType) {
            case ToggleConfiguration.LOADING_ANIMATION_FLICK:
            case ToggleConfiguration.LOADING_ANIMATION_DOT_SPINNER:
            case ToggleConfiguration.LOADING_ANIMATION_LINE_SPINNER:
                exitAnimationRedrawLoop();
                break;
            case ToggleConfiguration.LOADING_ANIMATION_NONE:
                break;
        }
    }

    private void exitAnimationRedrawLoop() {
        mIsIndicatorAnimating = false;
        mShowLoading = false;

        if (mOnLoadingChangeListener != null) {
            mOnLoadingChangeListener.onLoadingChanged(this, mShowLoading);
        }
        postInvalidate();
    }

    @Override
    public boolean isRunning() {
        return mIsIndicatorAnimating;
    }

    /**
     *
     * @return true, if {@link #getIndicatorVisibility()} = {@link ToggleConfiguration#INDICATOR_VISIBLE_SHOW},
     * and {@link #getIndicatorAnimationType()} is not {@link} {@link ToggleConfiguration#LOADING_ANIMATION_NONE}.
     */
    public boolean isLoading() {
        return mShowLoading;
    }

    @ToggleConfiguration.AnimationType
    public int getIndicatorAnimationType() {
        return mToggleConfiguration.loadingAnimationType;
    }

    @ToggleConfiguration.IndicatorVisible
    public int getIndicatorVisibility() {
        return mToggleConfiguration.indicatorVisibility;
    }

    /* ---------------------------------------------------------------------------------------------
     * View
     * ---------------------------------------------------------------------------------------------
     */

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mToggleAnimator != null) {
            mToggleAnimator.cancel();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(TAG, "onSizeChanged() called with: w = [" + w + "], h = [" + h + "], oldw = [" + oldw + "], oldh = [" + oldh + "]");
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = w;
        mHeight = h;

        setUp();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = dpToPx(DEFAULT_WIDTH);
        int desiredHeight = dpToPx(DEFAULT_HEIGHT);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }

    @Override
    public boolean performClick() {
        toggle();
        return super.performClick();
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
        mBackgroundPath.reset();
        mTogglePath.reset();
        pathMoon.reset();

        drawBackground(canvas);
        drawToggle(canvas);
        drawToggleIndicator(canvas);

        //drawSun(canvas);

        if (mShowAssistantLine) {
            canvas.drawRect(mFixedLeftToggleOutline, mDebugPaint);
            canvas.drawRect(mFixedRightToggleOutline, mDebugPaint);
        }
    }

    /* ---------------------------------------------------------------------------------------------
     * Drawing
     * ---------------------------------------------------------------------------------------------
     */


    private void drawToggleIndicator(Canvas canvas) {

        if (containsFlag(mToggleConfiguration.indicatorVisibility, ToggleConfiguration.INDICATOR_VISIBLE_SHOW)) {


            switch (mToggleConfiguration.loadingAnimationType) {
                case ToggleConfiguration.LOADING_ANIMATION_NONE:
                    drawFlickIndicator(canvas);
                    break;
                case ToggleConfiguration.LOADING_ANIMATION_FLICK:
                    if (mShowLoading) {
                        drawFlickIndicator(canvas);
                    }
                    break;
                case ToggleConfiguration.LOADING_ANIMATION_LINE_SPINNER: {
                    if (mShowLoading) {
                        drawLineSpinnerLoadingIndicator(canvas, mPaint);
                    }
                }

                break;
                case ToggleConfiguration.LOADING_ANIMATION_DOT_SPINNER: {
                    if (mShowLoading) {
                        drawDotSpinnerLoadingIndicator(canvas, mPaint);
                    }
                }
                break;
            }
        }
    }

    void pointOnCircle(PointF outPoint, PointF circleCenter, float radius, @FloatRange(from = 0, to = 360f) double angle) {

        // angle -> radian
        // 角度 -> 弧度
        outPoint.set(
                (float) (circleCenter.x + (radius * Math.cos(Math.toRadians(angle)))),
                (float) (circleCenter.y - radius * Math.sin(Math.toRadians(angle))));
    }

    private void drawDotSpinnerLoadingIndicator(Canvas canvas, Paint paint) {

    }

    // https://math.stackexchange.com/questions/260096/find-the-coordinates-of-a-point-on-a-circle
    public void drawLineSpinnerLoadingIndicator(Canvas canvas, Paint paint) {

        // mDrawingToggleRect should be call first on
        mDrawingToggleRect.set(mFixedLeftToggleOutline);
        mDrawingToggleRect.offset(mAnimatedToggleX - mFixedLeftToggleOutline.left, 0f);


        PointF spinnerCenter = new PointF(mDrawingToggleRect.centerX(), mDrawingToggleRect.centerY());
        int spinnerMaxSize = Math.min(mToggleWidth, mToggleHeight);

        // spinner size is 1/10 smaller than max size
        float spinnerRadius = ((spinnerMaxSize / 2 - spinnerMaxSize / 5.0f));

        // leaf's count
        final int spinnerLeafCount = 8;

        float leafWidth = spinnerRadius - spinnerMaxSize / 10f;
        float leafHeight = leafWidth / 2f;

        RectF leafOutlineRect = new RectF(-leafWidth / 2f, -leafHeight / 2f,
                leafWidth / 2f, leafHeight / 2f);

        paint.setColor(mBackgroundColor);

        float leafAngle;

        int alphaFrom = 255;
        int alphaTo = 50;


        // spinner rotated angle, will use alphaFrom to set the alpha value of leaf ( blade )
        float alphaFromAngle = mSpinnerProgress == 0 ? 0 : (1 - mSpinnerProgress) * 360;

//        Log.d(TAG, "------------------------------ alphaFromAngle = " + alphaFromAngle);

        float relativeAngle;
        float relativePercent;
        float reducedAlpha;
        int leafAlpha;


        PointF tempDrawingCenterPoint = new PointF();
        for (int i = 0; i < spinnerLeafCount; i++) {
            canvas.save();

            leafAngle = (float) i * (360f / (float) spinnerLeafCount);
            pointOnCircle(tempDrawingCenterPoint, spinnerCenter, spinnerRadius, leafAngle);

            canvas.translate(tempDrawingCenterPoint.x, tempDrawingCenterPoint.y);
            canvas.rotate(-leafAngle);


            relativeAngle = leafAngle - (alphaFromAngle - 360);
            relativeAngle = relativeAngle >= 360 ? relativeAngle - 360 : relativeAngle;

            relativePercent = (relativeAngle) / 360f;
            reducedAlpha = (alphaFrom - alphaTo) * relativePercent;

            leafAlpha = (int) (alphaFrom - reducedAlpha);

//            System.out.println("leafAngle = " + leafAngle + " relativeAngle = " + relativeAngle + " reducedAlpha = " + reducedAlpha + " leafAlpha = " + leafAlpha);

            paint.setAlpha(leafAlpha);

            canvas.drawRoundRect(leafOutlineRect,
                    leafHeight / 2f, leafHeight / 2f, paint);

            canvas.restore();
        }
    }

    void drawToggle(Canvas canvas) {
        mDrawingToggleRect.set(mFixedLeftToggleOutline);
        mDrawingToggleRect.offset(mAnimatedToggleX - mFixedLeftToggleOutline.left, 0f);

        mTogglePath.addRoundRect(mDrawingToggleRect, mToggleRadius, mToggleRadius, Path.Direction.CW);

        mPaint.setColor(mToggleColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(mTogglePath, mPaint);

        if (mShowAssistantLine) {
            canvas.drawRect(mDrawingToggleRect, mDebugPaint);
        }
    }

    private void drawFlickIndicator(Canvas canvas) {

        getCenterRectOfRect(mDrawingToggleRect,
                mToggleConfiguration.toggleIndicatorWidth, mToggleConfiguration.toggleIndicatorHeight,
                mDrawingToggleIndRect);

        if (!mIsIndicatorAnimating) {
            mIndicatorColor = mBackgroundColor;
        }
        // TODO: add corresponding color attribute
        mPaint.setColor(mIndicatorColor);

        canvas.drawRoundRect(mDrawingToggleIndRect, mToggleConfiguration.toggleIndicatorRadius, mToggleConfiguration.toggleIndicatorRadius, mPaint);
    }

    void drawSun(Canvas canvas) {
        mTogglePath.addCircle(mSunCenterX, mBgRadius, mToggleRadius, Path.Direction.CW);

        //pathMoon.addCircle(mSunCenterX - mCenterDistance * SCALE, mBgRadius - mCenterDistance * SCALE, mToggleRadius, Path.Direction.CW);

        // a big circle subtracts a small circle, results a moon shape
        //mTogglePath.op(pathMoon, Path.Op.DIFFERENCE);

        mPaint.setColor(mToggleColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(mTogglePath, mPaint);
    }

    void drawBackground(Canvas canvas) {

//        mBackgroundPath.addArc(mFixedLeftToggleOutline, 90, 180);
//        mBackgroundPath.moveTo(mBgRadius, 0);
//        mBackgroundPath.lineTo(mBgRadius * 4, 0);
//        mBackgroundPath.addArc(mFixedRightToggleOutline, 270, 180);
//        mBackgroundPath.moveTo(getWidth() - mBgRadius, mBgRadius * 2);
//        mBackgroundPath.addRect(mBgRadius, 0, getWidth() - mBgRadius, mBgRadius * 2, Path.Direction.CW);

        mBgRect.set(0, 0, mWidth, mHeight);
        mBackgroundPath.addRoundRect(mBgRect, mBgRadius, mBgRadius, Path.Direction.CW);

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mBackgroundColor);
        canvas.drawPath(mBackgroundPath, mPaint);
    }

    /* ---------------------------------------------------------------------------------------------
     * Animation
     * ---------------------------------------------------------------------------------------------
     */

    private void ensureAnimators() {
        if (!mHasAnimators) {
            mAnimators = onCreateAnimators();
            mHasAnimators = true;
        }
    }

    public List<Animator> onCreateAnimators() {
        ArrayList<Animator> valueAnimators = new ArrayList<>();

        switch (mToggleConfiguration.loadingAnimationType) {
            case ToggleConfiguration.LOADING_ANIMATION_LINE_SPINNER: {

                Log.d(TAG, "onCreateAnimators: create line spinner animator");

                ValueAnimator progressAnimator = ValueAnimator.ofFloat(0, 1);

                progressAnimator.setInterpolator(new LinearInterpolator());
                progressAnimator.setRepeatCount(ValueAnimator.INFINITE);
                progressAnimator.setRepeatMode(ValueAnimator.RESTART);
                progressAnimator.setDuration(2000);

                progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mSpinnerProgress = animation.getAnimatedFraction();

                        //postInvalidate();
                    }
                });

                valueAnimators.add(progressAnimator);
            }
            break;
        }

        return valueAnimators;
    }

}
