package com.cypherpunk.privacy.ui.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * custom view for connection button
 */
public class ConnectionView extends View {

    private enum Status {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        DISCONNECTING
    }

    @NonNull
    private Status connectionStatus = Status.DISCONNECTED;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private LinearGradient pipeLeftGradient;

    private final RectF rectF = new RectF();
    private final Path path = new Path();
    private final RectF frameRect = new RectF();

    public ConnectionView(Context context) {
        this(context, null);
    }

    public ConnectionView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConnectionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final float density = context.getResources().getDisplayMetrics().density;

        pipeInfo = new PipeInfo(density);
        frameInfo = new FrameInfo(density);
        sliderInfo = new SliderInfo(density);
        knobInfo = new KnobInfo(density, sliderInfo.w);
    }

    private Animator connectingAnimator;
    private Animator disconnectingAnimator;

    public void setConnecting() {
        connectionStatus = Status.CONNECTING;
        if (connectingAnimator != null) {
            connectingAnimator.cancel();
        }
        if (disconnectingAnimator != null) {
            disconnectingAnimator.cancel();
        }

        setDot(0f);

        final ObjectAnimator animator1 = ObjectAnimator.ofFloat(this, "fraction", 1f);
        animator1.setDuration(1000);

        final ObjectAnimator animator2 = ObjectAnimator.ofFloat(this, "dot", 1f);
        animator2.setDuration(1000);
        animator2.setRepeatCount(ValueAnimator.INFINITE);

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(animator1, animator2);

        connectingAnimator = animatorSet;
        connectingAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                connectingAnimator = null;
            }
        });
        connectingAnimator.start();
    }

    public void setDisconnecting() {
        connectionStatus = Status.DISCONNECTING;
        if (connectingAnimator != null) {
            connectingAnimator.cancel();
        }
        if (disconnectingAnimator != null) {
            disconnectingAnimator.cancel();
        }
        disconnectingAnimator = ObjectAnimator.ofFloat(this, "fraction", 0f);
        disconnectingAnimator.setDuration(1000);
        disconnectingAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                disconnectingAnimator = null;
            }
        });
        disconnectingAnimator.start();
    }

    public void setConnected() {
        if (disconnectingAnimator != null) {
            disconnectingAnimator.cancel();
        }
        if (connectingAnimator != null) {
            connectingAnimator.cancel();
        }
        connectionStatus = Status.CONNECTED;
        setFraction(1);
        invalidate();
    }

    public void setDisconnected() {
        if (connectingAnimator != null) {
            connectingAnimator.cancel();
        }

        connectionStatus = Status.DISCONNECTED;

        if (disconnectingAnimator == null) {
            setFraction(0f);
            invalidate();
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState ss = new SavedState(superState);

        ss.connectionStatus = connectionStatus;
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;

        super.onRestoreInstanceState(state);
        connectionStatus = ss.connectionStatus;
    }

    static class SavedState extends BaseSavedState {
        Status connectionStatus;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            connectionStatus = (Status) in.readSerializable();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeSerializable(connectionStatus);
        }

        @Override
        public String toString() {
            return "ConnectionView.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " connectionStatus=" + connectionStatus + "}";
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        pipeLeftGradient = new LinearGradient(0f, 0f, (w - frameInfo.w) * 0.5f, 0f,
                new int[]{Color.argb(64, 0, 255, 255), Color.CYAN},
                new float[]{0f, 1f}, Shader.TileMode.CLAMP);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final float centerX = getWidth() * 0.5f;
        final float centerY = getHeight() * 0.5f;

        final float halfFrameWidth = (frameInfo.w * (1 - fraction) + frameInfo.cw * fraction) * 0.5f;
        final float halfFrameHeight = frameInfo.h * 0.5f;

        frameRect.set(centerX, centerY, centerX, centerY);
        frameRect.inset(-halfFrameWidth, -halfFrameHeight);

        // 1. pipe
        drawPipe(canvas, centerX, centerY);

        // 2. frame
        drawFrame(canvas, centerX, centerY);

        // 3. slider
        drawSlider(canvas, centerX, centerY);

        // 4. knob
        drawKnob(canvas, centerX, centerY);
    }

    @FloatRange(from = 0, to = 1f)
    private float fraction = 0f;

    public void setFraction(@FloatRange(from = 0, to = 1f) float fraction) {
        this.fraction = fraction;
        invalidate();
    }

    public float getFraction() {
        return fraction;
    }

    private float dot = 0f;

    public void setDot(@FloatRange(from = 0, to = 1f) float dot) {
        this.dot = dot;
        invalidate();
    }

    public float getDot() {
        return dot;
    }

    //
    // pipe
    //

    private static class PipeInfo {
        private final float radius;
        private final LinearGradient dotGradient;
        private final float dotWidth;

        private PipeInfo(float density) {
            radius = 2 * density;
            dotWidth = 40 * density;
            dotGradient = new LinearGradient(0, -radius, dotWidth, radius,
                    new int[]{Color.TRANSPARENT, Color.WHITE, Color.WHITE, Color.TRANSPARENT},
                    new float[]{0f, 0.33f, 0.66f, 1f}, Shader.TileMode.CLAMP);
        }
    }

    private final PipeInfo pipeInfo;

    private void drawPipe(Canvas canvas, float centerX, float centerY) {
        paint.setShader(pipeLeftGradient);
        paint.setAlpha((int) (64 + (255 - 64) * fraction));
        paint.setStyle(Paint.Style.FILL);

        // left pipe
        rectF.set(0f, centerY - pipeInfo.radius, frameRect.left, centerY + pipeInfo.radius);
        canvas.drawRect(rectF, paint);

        // right pipe
        final int count = canvas.save();
        canvas.rotate(180, centerX, centerY);
        canvas.drawRect(rectF, paint);
        canvas.restoreToCount(count);

        if (connectionStatus == Status.CONNECTING) {
            final int count2 = canvas.save();
            canvas.translate(-pipeInfo.dotWidth * 2 + (2 * centerX + pipeInfo.dotWidth * 4) * dot, centerY);
            rectF.set(0f, -pipeInfo.radius, pipeInfo.dotWidth, pipeInfo.radius);
            paint.setShader(pipeInfo.dotGradient);
            paint.setAlpha(255);
            canvas.drawRect(rectF, paint);
            canvas.restoreToCount(count2);
        }
    }

    //
    // frame
    //

    private static class FrameInfo {
        private final float w;
        private final float cw;
        private final float h;
        private final float strokeWidthMin;
        private final float strokeWidthRange;

        private FrameInfo(float density) {
            w = 152 * density;
            cw = 136 * density;
            h = 76 * density;
            strokeWidthMin = 8 * density;
            strokeWidthRange = density;
        }
    }

    private final FrameInfo frameInfo;

    private void drawFrame(Canvas canvas, float centerX, float centerY) {
        // 2. frame
        paint.setShader(null);
        paint.setColor(colorWith(fraction, Color.CYAN, Color.WHITE));
        paint.setAlpha((int) (64 + (255 - 64) * fraction));

        path.reset();

        // 外側
        rectF.set(frameRect.left, frameRect.top, frameRect.left + frameRect.height(), frameRect.bottom);
        path.moveTo(rectF.centerX(), frameRect.bottom);
        path.arcTo(rectF, -270, 180);

        rectF.set(frameRect.right - frameRect.height(), frameRect.top, frameRect.right, frameRect.bottom);
        path.lineTo(rectF.centerX(), frameRect.top);
        path.arcTo(rectF, -90, 180);
        path.close();

        // 内側
        final float frameStrokeWidth = frameInfo.strokeWidthMin + fraction * frameInfo.strokeWidthRange;
        frameRect.inset(frameStrokeWidth, frameStrokeWidth * fraction);

        rectF.set(frameRect.left,
                frameRect.top,
                frameRect.left + frameRect.height() - 2 * frameStrokeWidth * (1 - fraction),
                frameRect.bottom);
        path.moveTo(rectF.centerX(), frameRect.top);
        path.arcTo(rectF, -90, -180);

        rectF.set(frameRect.right - frameRect.height() + 2 * frameStrokeWidth * (1 - fraction),
                frameRect.top,
                frameRect.right,
                frameRect.bottom);
        path.lineTo(rectF.centerX(), frameRect.bottom);
        path.arcTo(rectF, 90, -180);
        path.close();

        canvas.drawPath(path, paint);
    }

    //
    // slider
    //

    private static class SliderInfo {
        private final float w;
        private final float h;
        private final float halfW;
        private final float halfH;
        private final float strokeWidth;
        private final float strokeWidthHalf;
        private final float shadowRadius;
        private final RadialGradient gradient;
        private final LinearGradient gradient2;
        private final int strokeColorMin = Color.argb(51, 0, 255, 255);
        private final int strokeColorMax = Color.argb(128, 128, 255, 255);
        private final int dropShadowColor = Color.argb(64, 0, 255, 255);

        private SliderInfo(float density) {
            w = 120 * density;
            h = 60 * density;
            halfW = w * 0.5f;
            halfH = h * 0.5f;
            strokeWidth = 3 * density;
            strokeWidthHalf = strokeWidth * 0.5f;
            shadowRadius = 40 * density;

            gradient = new RadialGradient(0, 0, halfH + shadowRadius,
                    new int[]{dropShadowColor, Color.TRANSPARENT},
                    new float[]{0f, 1f}, Shader.TileMode.CLAMP);

            gradient2 = new LinearGradient(0, 0, 0, -halfH - shadowRadius,
                    new int[]{dropShadowColor, Color.TRANSPARENT},
                    new float[]{0f, 1f}, Shader.TileMode.CLAMP);
        }
    }

    private final SliderInfo sliderInfo;

    private void drawSlider(Canvas canvas, float centerX, float centerY) {
        final int count = canvas.save();
        canvas.translate(centerX, centerY);

        paint.setColor(colorWith(fraction, Color.BLACK, Color.CYAN));
        paint.setAlpha(128);
        paint.setStyle(Paint.Style.FILL);

        rectF.set(0, 0, 0, 0);
        rectF.inset(-sliderInfo.halfW, -sliderInfo.halfH);
        canvas.drawRoundRect(rectF, sliderInfo.halfH, sliderInfo.halfH, paint);

        paint.setColor(colorWith(fraction, sliderInfo.strokeColorMin, sliderInfo.strokeColorMax));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(sliderInfo.strokeWidth);

        rectF.inset(sliderInfo.strokeWidthHalf, sliderInfo.strokeWidthHalf);
        final float r = rectF.height() * 0.5f;
        canvas.drawRoundRect(rectF, r, r, paint);


        final float x = sliderInfo.halfW - sliderInfo.halfH;

        // drop shadow center
        paint.setShader(sliderInfo.gradient2);
        paint.setAlpha((int) (255 * fraction));
        paint.setStyle(Paint.Style.FILL);

        path.reset();
        rectF.set(-x, -sliderInfo.halfH - sliderInfo.shadowRadius, x, 0);
        path.addRect(rectF, Path.Direction.CW);
        rectF.top = -sliderInfo.halfH;
        path.addRect(rectF, Path.Direction.CCW);
        canvas.drawPath(path, paint);

        canvas.rotate(180, 0, 0);
        canvas.drawPath(path, paint);

        canvas.restoreToCount(count);


        // drop shadow left, right
        paint.setShader(sliderInfo.gradient);
        paint.setAlpha((int) (255 * fraction));
        paint.setStyle(Paint.Style.FILL);

        // drop shadow left
        final int count2 = canvas.save();
        canvas.translate(centerX - x, centerY);

        path.reset();

        rectF.set(0, 0, 0, 0);
        rectF.inset(-sliderInfo.halfH - sliderInfo.shadowRadius,
                -sliderInfo.halfH - sliderInfo.shadowRadius);
        rectF.right = 0;
        path.addRect(rectF, Path.Direction.CW);

        rectF.set(0, 0, 0, 0);
        rectF.inset(-sliderInfo.halfH, -sliderInfo.halfH);
        path.moveTo(0, sliderInfo.halfH);
        path.arcTo(rectF, -90, -180);
        path.close();

        canvas.drawPath(path, paint);

        // drop shadow right
        canvas.rotate(180, x, 0);
        canvas.drawPath(path, paint);

        canvas.restoreToCount(count2);
    }

    //
    // knob
    //

    private static class KnobInfo {
        private final float strokeWidth;
        private final float strokeWidthHalf;
        private final float radius;
        private final float min;
        private final float range;
        private final int solidColor = Color.rgb(0, 136, 136);
        private final int strokeColorMin = Color.rgb(17, 119, 119);
        private final int strokeColorMax = Color.rgb(204, 238, 238);
        private final LinearGradient gradient;

        private KnobInfo(float density, float sliderWidth) {
            strokeWidth = 4 * density;
            strokeWidthHalf = strokeWidth * 0.5f;
            radius = 25 * density;
            range = sliderWidth - 2 * radius - 12 * density;
            min = -range * 0.5f;

            final int color = Color.argb(102, 255, 255, 255);
            gradient = new LinearGradient(-radius, -radius, radius, radius,
                    new int[]{color, color, Color.TRANSPARENT, Color.TRANSPARENT},
                    new float[]{0f, 0.25f, 0.75f, 1f}, Shader.TileMode.CLAMP);
        }
    }

    private final KnobInfo knobInfo;

    private void drawKnob(Canvas canvas, float centerX, float centerY) {
        final int count = canvas.save();
        canvas.translate(centerX + knobInfo.min + knobInfo.range * fraction, centerY);

        paint.setShader(null);
        paint.setColor(colorWith(fraction, knobInfo.solidColor, Color.WHITE));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(0, 0, knobInfo.radius, paint);

        paint.setShader(knobInfo.gradient);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(0, 0, knobInfo.radius, paint);

        paint.setShader(null);
        paint.setColor(colorWith(fraction, knobInfo.strokeColorMin, knobInfo.strokeColorMax));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(knobInfo.strokeWidth);
        canvas.drawCircle(0, 0, knobInfo.radius - knobInfo.strokeWidthHalf, paint);

        canvas.restoreToCount(count);
    }

    private static int colorWith(float fraction, int startColor, int endColor) {
        int startA = (startColor >> 24) & 0xff;
        int startR = (startColor >> 16) & 0xff;
        int startG = (startColor >> 8) & 0xff;
        int startB = startColor & 0xff;

        int endA = (endColor >> 24) & 0xff;
        int endR = (endColor >> 16) & 0xff;
        int endG = (endColor >> 8) & 0xff;
        int endB = endColor & 0xff;

        return (startA + (int) (fraction * (endA - startA))) << 24 |
                (startR + (int) (fraction * (endR - startR))) << 16 |
                (startG + (int) (fraction * (endG - startG))) << 8 |
                (startB + (int) (fraction * (endB - startB)));
    }
}
