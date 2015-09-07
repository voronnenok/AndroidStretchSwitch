package com.stretchswitch;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Property;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by voronnenok on 26.07.15.
 */
public class StretchSwitch extends RelativeLayout {
    private static final String TAG = StretchSwitch.class.getSimpleName();
    private static final int SLIDER_ANIMATE_DURATION = 250;

    private Drawable mSlider;
    private Drawable mTrack;
    private TextView mSliderView;
    private boolean isChecked;
    private float touchX;
    private final float sliderBeginPos = 0;
    private int minFlingVelocity;
    private String mTrackTextOff;
    private String mTrackTextOn;
    private TextView tvTxtTrackOff;
    private TextView tvTextTrackOn;
    private String mSliderTextOff;
    private String mSliderTextOn;
    private ColorStateList mTextColors;
    private ViewGroup trackLayout;
    private View mTrackView;
    private OnCheckedChangeListener checkedChangeListener;

    public StretchSwitch(Context context) {
        this(context, null);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public StretchSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.StretchSwitch, 0, 0);
        mSlider = attributes.getDrawable(R.styleable.StretchSwitch_slider);
        mTrack = attributes.getDrawable(R.styleable.StretchSwitch_sliderTrack);
        mTrackTextOff = attributes.getString(R.styleable.StretchSwitch_trackTextOff);
        mTrackTextOn = attributes.getString(R.styleable.StretchSwitch_trackTextOn);
        mSliderTextOff = attributes.getString(R.styleable.StretchSwitch_sliderTextOff);
        mSliderTextOn = attributes.getString(R.styleable.StretchSwitch_sliderTextOn);

        int padding,
            leftPadding,
            topPadding,
            rightPadding,
            bottomPadding;

        final int DEFAULT_PADDING_VALUE = 0;

        padding = attributes.getDimensionPixelSize(R.styleable.StretchSwitch_trackPadding, DEFAULT_PADDING_VALUE);
        leftPadding = attributes.getDimensionPixelSize(R.styleable.StretchSwitch_trackPaddingLeft, DEFAULT_PADDING_VALUE);
        rightPadding = attributes.getDimensionPixelSize(R.styleable.StretchSwitch_trackPaddingRight, DEFAULT_PADDING_VALUE);
        topPadding = attributes.getDimensionPixelSize(R.styleable.StretchSwitch_trackPaddingTop, DEFAULT_PADDING_VALUE);
        bottomPadding = attributes.getDimensionPixelSize(R.styleable.StretchSwitch_trackPaddingBottom, DEFAULT_PADDING_VALUE);;

        if(padding > 0) {
            leftPadding = padding;
            rightPadding = padding;
            topPadding = padding;
            bottomPadding = padding;
        }

        trackLayout = new LinearLayout(context, null);
        addView(trackLayout);
        trackLayout.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        trackLayout.setPadding(
                leftPadding,
                topPadding,
                rightPadding,
                bottomPadding);

        mTrackView = new View(context, null);
        trackLayout.addView(mTrackView);
        setTrackDrawable(mTrack);

        LinearLayout textLayout = new LinearLayout(context, null);
        addView(textLayout);
        textLayout.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        tvTxtTrackOff = new TextView(context, null);
        tvTextTrackOn = new TextView(context, null);
        tvTxtTrackOff.setText(mTrackTextOff);
        tvTextTrackOn.setText(mTrackTextOn);

        textLayout.addView(tvTxtTrackOff);
        textLayout.addView(tvTextTrackOn);

        LinearLayout.LayoutParams trackTextLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        tvTextTrackOn.setLayoutParams(trackTextLayoutParams);
        tvTextTrackOn.setGravity(Gravity.CENTER);
        tvTxtTrackOff.setLayoutParams(trackTextLayoutParams);
        tvTxtTrackOff.setGravity(Gravity.CENTER);

        LinearLayout sliderLayout = new LinearLayout(context, null);
        addView(sliderLayout);
        sliderLayout.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        sliderLayout.setWeightSum(2);

        mSliderView = new Button(context, null);
        mSliderView.setBackgroundDrawable(mSlider);
        sliderLayout.addView(mSliderView);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        mSliderView.setLayoutParams(layoutParams);
        mSliderView.setGravity(Gravity.CENTER);

        isChecked = attributes.getBoolean(R.styleable.StretchSwitch_switchChecked, false);

        final int trackTextAppearanceId = attributes.getResourceId(R.styleable.StretchSwitch_trackTextAppearance, 0);

        if(trackTextAppearanceId > 0) {
            setTrackTextAppearance(context, trackTextAppearanceId);
        }

        final int sliderTextAppearanceId = attributes.getResourceId(R.styleable.StretchSwitch_sliderTextAppearance, 0);

        if(sliderTextAppearanceId > 0) {
            setSliderTextAppearance(context, sliderTextAppearanceId);
        }

        attributes.recycle();

        final ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        minFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
    }

    /**
     * Sets the switch text color, size, style, hint color, and highlight color
     * from the specified TextAppearance resource.
     *
     * @attr ref android.R.styleable#Switch_switchTextAppearance
     */
    public void setTrackTextAppearance(Context context, int resId) {
        tvTextTrackOn.setTextAppearance(context, resId);
        tvTxtTrackOff.setTextAppearance(context, resId);
    }


    /**
     * Sets the switch text color, size, style, hint color, and highlight color
     * from the specified TextAppearance resource.
     *
     * @attr ref android.R.styleable#Switch_switchTextAppearance
     */
    public void setSliderTextAppearance(Context context, int resId) {
        mSliderView.setTextAppearance(context, resId);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        logEvent("onSizeChanged " + w + " " + h);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        invalidateState();
    }

    void invalidateState() {
        invalidateSliderText();
        animateSliderCheckState(isChecked);
    }

    void logEvent(String message) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, message);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        velocityTracker.addMovement(event);

        int action = MotionEventCompat.getActionMasked(event);

        final int pointerIndex = MotionEventCompat.getActionIndex(event);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                touchX = MotionEventCompat.getX(event, pointerIndex);
                break;
            case MotionEvent.ACTION_MOVE:
                final float x = MotionEventCompat.getX(event, pointerIndex);
                float dx = x - touchX;
                float sliderDragX = mSliderView.getX() + dx;
                float newPosX = constrain(sliderDragX, 0, getWidth() - mSliderView.getWidth());
                mSliderView.setX(newPosX);
                touchX = x;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                velocityTracker.computeCurrentVelocity(1000);
                float xVelocity = velocityTracker.getXVelocity();
                boolean newState;
                if(Math.abs(xVelocity) > minFlingVelocity) {
                    newState = xVelocity > 0;
                } else {
                    newState = !isChecked();
                }
                setChecked(newState);
                velocityTracker.clear();
        }

        return true;
    }

    private float getSliderPosition() {
        return mSliderView.getX();
    }

    private float getSliderBeginPoint() {
        return sliderBeginPos;
    }

    private float getSliderScrollRange(){
        return getWidth() - mSliderView.getWidth();
    }

    public static float constrain(float amount, float low, float high) {
        return amount < low ? low : (amount > high ? high : amount);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void animateSliderCheckState(boolean checkState) {
        final float targetPosition = checkState ? getSliderScrollRange() : getSliderBeginPoint();
        objectAnimator = ObjectAnimator.ofFloat(mSliderView, floatProperty, targetPosition);
        objectAnimator.setDuration(SLIDER_ANIMATE_DURATION);
        objectAnimator.start();
    }

    private ObjectAnimator objectAnimator;
    private final VelocityTracker velocityTracker = VelocityTracker.obtain();
    private FloatProperty<View> floatProperty = new FloatProperty<View>("") {
        @Override
        public void setValue(View object, float value) {
            object.setX(value);
        }

        @Override
        public Float get(View view) {
            return view.getX();
        }
    };

    @Override
    protected Parcelable onSaveInstanceState() {
        logEvent("OnSavedInstanceState " + isChecked);
        Parcelable state = super.onSaveInstanceState();
        return new SavedState(state, isChecked);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState)state;
        logEvent("onRestoreInstanceState " + savedState.checked);
        setChecked(savedState.checked);
        super.onRestoreInstanceState(savedState.getSuperState());
    }

    static class SavedState extends BaseSavedState {
        final static byte STATE_CHECKED = 1;
        final static byte STATE_UNCHECKED = 0;
        boolean checked;

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>(){

            @Override
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            @Override
            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }
        };

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeByte(checked ? STATE_CHECKED : STATE_UNCHECKED);
        }

        public SavedState(Parcel source) {
            super(source);
            checked = source.readByte() == STATE_CHECKED;
        }

        public SavedState(Parcelable superState, boolean checked) {
            super(superState);
            this.checked = checked;
        }
    }

    public void invalidateSliderText() {
        mSliderView.setText(isChecked ? mSliderTextOn : mSliderTextOff);
    }


    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
        if(checkedChangeListener != null) {
            checkedChangeListener.onCheckedChange(isChecked);
        }
        invalidateState();
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setSliderDrawable(Drawable sliderDrawable) {
        mSliderView.setBackgroundDrawable(sliderDrawable);
    }

    public void setTrackDrawable(Drawable trackDrawable) {
        mTrackView.setBackgroundDrawable(trackDrawable);
    }

    public void setTrackPadding(int padding) {
        trackLayout.setPadding(
                padding,
                padding,
                padding,
                padding
        );
    }

    public void setTrackTextOff(String textOff) {
        tvTxtTrackOff.setText(textOff);
    }

    public void setTrackTextOn(String textOn) {
        tvTextTrackOn.setText(textOn);
    }

    public void setSliderTextOff(String textOff) {
        mSliderTextOff = textOff;
        invalidateSliderText();
    }

    public void setSliderTextOn(String textOn) {
        mSliderTextOn = textOn;
        invalidateSliderText();
    }

    public interface OnCheckedChangeListener {
        public void onCheckedChange(boolean checked);
    }

    public void setCheckedChangeListener(OnCheckedChangeListener checkedChangeListener) {
        this.checkedChangeListener = checkedChangeListener;
    }

    /**
     * An implementation of {@link Property} to be used specifically with fields of type
     * <code>float</code>. This type-specific subclass enables performance benefit by allowing
     * calls to a {@link #set(Object, Float) set()} function that takes the primitive
     * <code>float</code> type and avoids autoboxing and other overhead associated with the
     * <code>Float</code> class.
     *
     * @param <T> The class on which the Property is declared.
     *
     * @hide
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    abstract class FloatProperty<T> extends Property<T, Float> {

        public FloatProperty(String name) {
            super(Float.class, name);
        }

        /**
         * A type-specific override of the {@link #set(Object, Float)} that is faster when dealing
         * with fields of type <code>float</code>.
         */
        public abstract void setValue(T object, float value);

        @Override
        final public void set(T object, Float value) {
            setValue(object, value);
        }

    }
}


