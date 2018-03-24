package cn.intret.lab.library;

import android.graphics.Color;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by SilenceDut on 16/6/3.
 */

public class ToggleSettings {

    public static final int INDICATOR_VISIBLE_HIDE = 0;
    public static final int INDICATOR_VISIBLE_SHOW = 1;


    @IntDef({INDICATOR_VISIBLE_HIDE, INDICATOR_VISIBLE_SHOW})
    @Retention(RetentionPolicy.SOURCE)
    public @interface IndicatorVisible {
    }

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
    public boolean showIndicator = true;

    @IndicatorVisible
    public int toggleIndicatorVisibility = INDICATOR_VISIBLE_HIDE;

    private ToggleSettings(Builder builder) {
        this.mBackgroundUnCheckedColor = builder.backgroundUncheckedColor;
        this.mBackgroundCheckedColor = builder.backgroundCheckedColor;
        this.mToggleUnCheckedColor = builder.toggleUnCheckedColor;
        this.mToggleCheckedColor = builder.toggleCheckedColor;
        this.mPadding = builder.padding;
        this.mDuration = builder.duration;
        this.mDuration = !builder.withAnimator ? 1 : mDuration;
        this.mToggleRadius = builder.mToggleRadius;
        this.mBgRadius = builder.mBgRadius;
    }

    public static class Builder {
        int backgroundUncheckedColor = BACKGROUND_UNCHECKED_COLOR;
        int backgroundCheckedColor = BACKGROUND_CHECKED_COLOR;
        int toggleUnCheckedColor = TOGGLE_UNCHECKED_COLOR;
        int toggleCheckedColor = TOGGLE_CHECKED_COLOR;
        int padding = PADDING_DEFAULT;
        int duration = DURATION_DEFAULT;
        boolean withAnimator = true;
        int mToggleRadius;
        int mBgRadius;

        public Builder setBackgroundUncheckedColor(int backgroundUncheckedColor) {
            this.backgroundUncheckedColor = backgroundUncheckedColor;
            return this;
        }

        public Builder setBackgroundCheckedColor(int backgroundCheckedColor) {
            this.backgroundCheckedColor = backgroundCheckedColor;
            return this;
        }

        public Builder setToggleUnCheckedColor(int toggleUnCheckedColor) {
            this.toggleUnCheckedColor = toggleUnCheckedColor;
            return this;
        }

        public Builder setToggleCheckedColor(int toggleCheckedColor) {
            this.toggleCheckedColor = toggleCheckedColor;
            return this;
        }

        public Builder setDuration(int duration) {
            this.duration = duration;
            return this;
        }

        public Builder withAnimator(boolean withAnimator) {
            this.withAnimator = withAnimator;
            return this;
        }

        public Builder setPadding(int padding) {
            this.padding = padding;
            return this;
        }

        public ToggleSettings buildSettings() {
            return new ToggleSettings(this);
        }

        public Builder setToggleRadius(int radius) {
            mToggleRadius = radius;
            return this;
        }

        public int getToggleRadius() {
            return mToggleRadius;
        }

        public Builder setBgRadius(int bgRadius) {
            mBgRadius = bgRadius;
            return this;
        }

        public int getBgRadius() {
            return mBgRadius;
        }
    }
}
