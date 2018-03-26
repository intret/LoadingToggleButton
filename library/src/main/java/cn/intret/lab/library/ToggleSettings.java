package cn.intret.lab.library;

import android.graphics.Color;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by SilenceDut on 16/6/3.
 */

public class ToggleSettings {

    /*
     * Toggle indicator visibility constants
     */
    public static final int INDICATOR_VISIBLE_HIDE = 0;
    public static final int INDICATOR_VISIBLE_SHOW = 1;
    public static final int INDICATOR_VISIBLE_FLICKER = 2;

    @IntDef({INDICATOR_VISIBLE_HIDE, INDICATOR_VISIBLE_SHOW})
    @Retention(RetentionPolicy.SOURCE)
    public @interface IndicatorVisible {}

    /*
     * Indicator animation time
     */

    public static final int SHOW_INDICATOR_WHEN_TOGGLE_TO_ON = 1;
    public static final int SHOW_INDICATOR_WHEN_TOGGLE_TO_OFF = 2;

    /*
     * Loading animation
     */

    public static final int LOADING_ANIMATION_NONE = 0;
    public static final int LOADING_ANIMATION_FLICK = 1;
    public static final int LOADING_ANIMATION_LINE_SPINNER = 2;
    public static final int LOADING_ANIMATION_DOT_SPINNER = 3;

    @IntDef({
            LOADING_ANIMATION_NONE,
            LOADING_ANIMATION_FLICK,
            LOADING_ANIMATION_DOT_SPINNER,
            LOADING_ANIMATION_LINE_SPINNER
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface AnimationType {}

    /* ---------------------------------------------------------------------------------------------
     * Constants
     * ---------------------------------------------------------------------------------------------
     */


    public static final int BACKGROUND_UNCHECKED_COLOR = Color.parseColor("#7c4dff");
    public static final int BACKGROUND_CHECKED_COLOR = Color.parseColor("#424242");
    public static final int TOGGLE_CHECKED_COLOR = Color.WHITE;
    public static final int TOGGLE_UNCHECKED_COLOR = Color.parseColor("#ff5722");
    public static final int PADDING_DEFAULT = 2;
    public static final int RADIUS_DEFAULT = -1; // half of height
    public static final int DURATION_DEFAULT = 300;
    public static final int DEFAULT_TOGGLE_INDICATOR_WIDTH = 4; // in dp
    public static final int DEFAULT_TOGGLE_INDICATOR_HEIGHT = 4; // in dp
    public static final int DEFAULT_TOGGLE_INDICATOR_RADIUS = 2; // in dp

    public static final int DEFAULT_INDICATOR_VISIBLE = INDICATOR_VISIBLE_HIDE;


    /* ---------------------------------------------------------------------------------------------
     * Setting
     * ---------------------------------------------------------------------------------------------
     */


    public int mBgRadius;
    public int mBackgroundUnCheckedColor;
    public int mBackgroundCheckedColor;
    public int mToggleUnCheckedColor;
    public int mToggleCheckedColor;
    public int mPadding;
    public int mDuration;
    public int mToggleRadius;
    public int toggleIndicatorWidth;
    public int toggleIndicatorHeight;
    public int toggleIndicatorRadius;
    public int toggleIndicatorNormalColor = TOGGLE_UNCHECKED_COLOR;
    public int toggleIndicatorActiveColor = TOGGLE_CHECKED_COLOR;

    @AnimationType
    public int loadingAnimationType;


    @IndicatorVisible
    public int toggleIndicatorVisibility = INDICATOR_VISIBLE_HIDE;
    public int showIndicatorWhen = SHOW_INDICATOR_WHEN_TOGGLE_TO_ON;

    ToggleSettings() {
        this.mBackgroundUnCheckedColor = BACKGROUND_UNCHECKED_COLOR;
        this.mBackgroundCheckedColor = BACKGROUND_CHECKED_COLOR;
        this.mToggleUnCheckedColor = TOGGLE_UNCHECKED_COLOR;
        this.mToggleCheckedColor = TOGGLE_CHECKED_COLOR;
        this.mPadding = PADDING_DEFAULT;
        this.mDuration = DURATION_DEFAULT;
//        this.mToggleRadius = ;
//        this.mBgRadius;
    }
}
