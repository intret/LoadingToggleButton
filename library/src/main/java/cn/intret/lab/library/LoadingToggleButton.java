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


/**
 * A toggle button.
 */
public class LoadingToggleButton extends View implements Checkable, Animatable {

    private static final String TAG = LoadingToggleButton.class.getSimpleName();


    /*
     * Interaction
     */
    public interface OnCheckedChangeListener {
        void onCheckedChanged(View buttonView, boolean isChecked);
    }

    private OnCheckedChangeListener mOnCheckedChangeListener;


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
    private ToggleSettings mToggleSettings;

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
    boolean mIsIndicatorAnimating;

    private final float SCALE = (float) (Math.sqrt(2) / 2);
    private ValueAnimator mToggleAnimator;
    private boolean mIsAnimating;

    private static final int[] CheckedStateSet = {
            android.R.attr.state_checked,
    };
    private boolean mHasAnimators;
    private ArrayList<ValueAnimator> mAnimators;

    private long flickerDelayMillis = 500;
    private boolean mIndicatorOn = false;
    private float mSpinnerProgress;

    /* ---------------------------------------------------------------------------------------------
     * Getters and setters
     * ---------------------------------------------------------------------------------------------
     */

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
        mIndicatorColor = mToggleSettings.mBackgroundCheckedColor;
    }

    private void readAttrs(AttributeSet attrs) {
        ToggleSettings settings = new ToggleSettings();
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

            settings.toggleIndicatorFlickerWhen = typedArray.getInt(R.styleable.LoadingToggleButton_ltbToggleIndicatorFlickWhen, ToggleSettings.INDICATOR_FLICK_WHEN_TOGGLE_TO_ON);

            settings.loadingAnimationType = typedArray.getInt(R.styleable.LoadingToggleButton_ltbToggleLoadingAnimationType, ToggleSettings.LOADING_ANIMATION_FLICK);

            typedArray.recycle();
        }
        setToggleSettings(settings);
    }

    private void setUp() {

        // toggle size, it may be a long toggle, a circle toggle
        mToggleHeight = mHeight - 2 * mToggleSettings.mPadding;
        mToggleWidth = mToggleHeight;

        final int maxBgRadius = mHeight / 2;
        final int maxToggleRadius = mToggleHeight / 2;

        mBgRadius = (mToggleSettings.mBgRadius > maxBgRadius) ? maxBgRadius
                : (mToggleSettings.mBgRadius < 0 ? maxBgRadius : mToggleSettings.mBgRadius);

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

                // toggle indicator flicker

                if (containsFlag(mToggleSettings.toggleIndicatorVisibility, ToggleSettings.INDICATOR_VISIBLE_FLICKER)) {
                    if (toggleToOn) {

                        if (containsFlag(mToggleSettings.toggleIndicatorFlickerWhen, ToggleSettings.INDICATOR_FLICK_WHEN_TOGGLE_TO_ON)) {
                            Log.d(TAG, "indicator flicking started when 'toggle to on' animation ended");
                            start();
                        }
                    } else {
                        if (containsFlag(mToggleSettings.toggleIndicatorVisibility, ToggleSettings.INDICATOR_VISIBLE_FLICKER)) {
                            if (containsFlag(mToggleSettings.toggleIndicatorFlickerWhen, ToggleSettings.INDICATOR_FLICK_WHEN_TOGGLE_TO_OFF)) {
                                Log.d(TAG, "indicator flicking started when 'toggle to off' animation ended");
                                start();
                            } else {

                            }
                        }
                    }
                }

            }
        });
        mToggleAnimator.setDuration(mToggleSettings.mDuration);
        mToggleAnimator.start();
        mIsAnimating = true;
    }

    public void setOnCheckChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.mOnCheckedChangeListener = onCheckedChangeListener;
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

    /* ---------------------------------------------------------------------------------------------
     * Animatable
     * ---------------------------------------------------------------------------------------------
     */

    Runnable flickerRunnable = new Runnable() {
        @Override
        public void run() {
            if (mIsIndicatorAnimating) {

                toggleIndicatorFlickingState();
                drawFlickingIndicator();

                // schedule next flicking
                mAnimationHandler.postDelayed(this, flickerDelayMillis);
            }
        }
    };

    private void toggleIndicatorFlickingState() {
        mIndicatorOn = !mIndicatorOn;
        mIndicatorColor = mIndicatorOn ? mToggleSettings.mBackgroundCheckedColor : mToggleSettings.mBackgroundUnCheckedColor;
    }

    private void drawFlickingIndicator() {
        postInvalidate();
    }

    @Override
    public void start() {
        switch (mToggleSettings.loadingAnimationType) {
            case ToggleSettings.LOADING_ANIMATION_FLICK:
                startFlickingAnimation();
                break;
            case ToggleSettings.LOADING_ANIMATION_LINE_SPINNER:
                startLineSpinnerAnimation();
                break;
            case ToggleSettings.LOADING_ANIMATION_DOT_SPINNER:
                break;
        }
    }

    private void startLineSpinnerAnimation() {
        if (!mIsIndicatorAnimating) {
            mIsIndicatorAnimating = true;

            onCreateAnimators();
        }
    }

    @Override
    public void stop() {
        stopFlickingAnimation();
    }

    private void startFlickingAnimation() {
        if (!mIsIndicatorAnimating) {
            mIsIndicatorAnimating = true;
            mAnimationHandler.post(flickerRunnable);
        }
    }

    private void stopFlickingAnimation() {
        mIsIndicatorAnimating = false;
    }

    @Override
    public boolean isRunning() {
        return mIsIndicatorAnimating;
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
        drawToggleLoadingIndicator(canvas);

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


    private void drawToggleLoadingIndicator(Canvas canvas) {

        switch (mToggleSettings.loadingAnimationType) {
            case ToggleSettings.LOADING_ANIMATION_LINE_SPINNER:
                drawLineSpinnerLoadingIndicator(canvas, mPaint);
                break;
            case ToggleSettings.LOADING_ANIMATION_DOT_SPINNER:
                drawDotSpinnerLoadingIndicator(canvas, mPaint);
                break;
            case ToggleSettings.LOADING_ANIMATION_FLICK: {
                if (containsFlag(mToggleSettings.toggleIndicatorVisibility, ToggleSettings.INDICATOR_VISIBLE_SHOW)) {
                    drawFlickIndicator(canvas);
                }
                break;
            }
        }
    }

    /**
     * 圆O的圆心为(a,b),半径为R,点A与到X轴的为角α.
     * 则点A的坐标为(a+R*cosα,b+R*sinα)
     *
     * @param width
     * @param height
     * @param radius
     * @param angle
     * @return
     */
    PointF circleAt(float width, float height, float radius, double angle) {
        float x = (float) (width / 2f + radius * (Math.cos(angle)));
        float y = (float) (height / 2f + radius * (Math.sin(angle)));
        return new PointF(x, y);
    }

    void pointOnCircle(PointF outPoint, PointF circleCenter, float radius, @FloatRange(from = 0, to = 360f) double angle) {

        // 角度 angle -> 弧度 radian
        outPoint.set(
                (float) (circleCenter.x + (radius * Math.cos(Math.toRadians(angle)))),
                (float) (circleCenter.y - radius * Math.sin(Math.toRadians(angle))));
    }

    private void drawDotSpinnerLoadingIndicator(Canvas canvas, Paint paint) {

    }

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
        int alphaTo = 100;

        PointF tempDrawingCenterPoint = new PointF();
        for (int i = 0; i < spinnerLeafCount; i++) {
            canvas.save();

            leafAngle = (float) i * (360f / (float) spinnerLeafCount);
            pointOnCircle(tempDrawingCenterPoint, spinnerCenter, spinnerRadius, leafAngle);

            canvas.translate(tempDrawingCenterPoint.x, tempDrawingCenterPoint.y);
            canvas.rotate(-leafAngle);

            paint.setAlpha((int) (alphaFrom - (alphaFrom - alphaTo) * (leafAngle / 360) ));

            canvas.drawRoundRect(leafOutlineRect,
                    leafHeight / 2f, leafHeight / 2f, paint);

            canvas.restore();
        }
//        paint.setAlpha(alphas[i]);
//        RectF rectF = new RectF(-radius, -radius / 1.5f, 1.5f * radius, radius / 1.5f);
//        canvas.drawRoundRect(rectF, 5, 5, paint);
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
                mToggleSettings.toggleIndicatorWidth, mToggleSettings.toggleIndicatorHeight,
                mDrawingToggleIndRect);

        if (!mIsIndicatorAnimating) {
            mIndicatorColor = mBackgroundColor;
        }
        // TODO: add corresponding color attribute
        mPaint.setColor(mIndicatorColor);

        canvas.drawRoundRect(mDrawingToggleIndRect, mToggleSettings.toggleIndicatorRadius, mToggleSettings.toggleIndicatorRadius, mPaint);
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

    public ArrayList<ValueAnimator> onCreateAnimators() {
        ArrayList<ValueAnimator> valueAnimators = new ArrayList<>();

        switch (mToggleSettings.loadingAnimationType) {
            case ToggleSettings.LOADING_ANIMATION_DOT_SPINNER: {
                ValueAnimator progressAnimator = ValueAnimator.ofFloat(0, 1);

                progressAnimator.setInterpolator(new LinearInterpolator());
                progressAnimator.setRepeatCount(ValueAnimator.INFINITE);
                progressAnimator.setRepeatMode(ValueAnimator.RESTART);

                progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mSpinnerProgress = animation.getAnimatedFraction();
                    }
                });

                valueAnimators.add(progressAnimator);
            }
            break;
        }

        return new ArrayList<>();
    }

}
