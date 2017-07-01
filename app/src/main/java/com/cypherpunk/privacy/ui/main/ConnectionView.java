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
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.cypherpunk.privacy.ui.common.FontCache;

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
    private Animator connectedAnimator;

    public void setConnecting() {
        connectionStatus = Status.CONNECTING;
        if (disconnectingAnimator != null) {
            disconnectingAnimator.cancel();
            disconnectingAnimator = null;
        }
        if (connectedAnimator != null) {
            connectedAnimator.cancel();
            connectedAnimator = null;
        }
        if (connectingAnimator != null) {
            connectingAnimator.cancel();
            connectingAnimator = null;
        }

        dot = 0f;
        pipeScale = 1f;

        final ObjectAnimator animator1 = ObjectAnimator.ofFloat(this, "fraction", 1f);
        animator1.setDuration(500);

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
            connectingAnimator = null;
        }
        if (disconnectingAnimator != null) {
            disconnectingAnimator.cancel();
            disconnectingAnimator = null;
        }
        if (connectedAnimator != null) {
            connectedAnimator.cancel();
            connectedAnimator = null;
        }

        final ValueAnimator animator1 = ObjectAnimator.ofFloat(this, "pipeScale", 10f, 1f);
        animator1.setInterpolator(new LinearInterpolator());
        animator1.setDuration(200);

        final ObjectAnimator animator2 = ObjectAnimator.ofFloat(this, "fraction", 0f);
        animator2.setDuration(500);
        animator2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                disconnectingAnimator = null;
            }
        });

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(animator1, animator2);

        disconnectingAnimator = animatorSet;
        disconnectingAnimator.start();
    }

    public void setConnected() {
        if (disconnectingAnimator != null) {
            disconnectingAnimator.cancel();
            disconnectingAnimator = null;
        }
        if (connectingAnimator != null) {
            connectingAnimator.cancel();
            connectingAnimator = null;
        }
        if (connectedAnimator == null) {
            pipeScale = 1f;
            textPosition = 0f;

            final ValueAnimator animator1 = ObjectAnimator.ofFloat(this, "pipeScale", 1f, 10f);
            animator1.setInterpolator(new LinearInterpolator());
            animator1.setDuration(800);

            final ObjectAnimator animator2 = ObjectAnimator.ofFloat(this, "textPosition", 0f, 1f);
            animator2.setInterpolator(new LinearInterpolator());
            animator2.setDuration(20000);
            animator2.setRepeatCount(ValueAnimator.INFINITE);

            final AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playSequentially(animator1, animator2);

            connectedAnimator = animatorSet;
            connectedAnimator.start();
        }
        connectionStatus = Status.CONNECTED;
        setFraction(1);
        invalidate();
    }

    public void setDisconnected() {
        if (connectingAnimator != null) {
            connectingAnimator.cancel();
            connectingAnimator = null;
        }
        if (connectedAnimator != null) {
            connectedAnimator.cancel();
            connectedAnimator = null;
        }
        connectionStatus = Status.DISCONNECTED;

        if (disconnectingAnimator == null) {
            setFraction(0f);
            invalidate();
        }
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
    // pipeScale
    //

    private float pipeScale = 1f;

    public float getPipeScale() {
        return pipeScale;
    }

    public void setPipeScale(float pipeScale) {
        this.pipeScale = pipeScale;
        invalidate();
    }

    //
    // textPosition
    //

    private float textPosition;

    public float getTextPosition() {
        return textPosition;
    }

    public void setTextPosition(float textPosition) {
        this.textPosition = textPosition;
        invalidate();
    }

    //
    // pipe
    //

    private static class PipeInfo {
        private final float radius;
        private final LinearGradient dotGradient;
        private final float dotHalfWidth;

        private PipeInfo(float density) {
            radius = 2 * density;
            dotHalfWidth = 20 * density;
            dotGradient = new LinearGradient(-dotHalfWidth, -radius, dotHalfWidth, radius,
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
        final float pipeRadius = pipeInfo.radius * pipeScale;
        final float pipeRight = pipeScale > 1
                ? frameRect.left + frameInfo.strokeWidthMin
                : frameRect.left;
        rectF.set(0f, centerY - pipeRadius, pipeRight, centerY + pipeRadius);
        canvas.drawRect(rectF, paint);

        // right pipe
        final int count = canvas.save();
        canvas.rotate(180, centerX, centerY);
        canvas.drawRect(rectF, paint);
        canvas.restoreToCount(count);

        final float density = getContext().getResources().getDisplayMetrics().density;

        if (connectionStatus == Status.CONNECTED && textPosition > 0) {
            final String text = "x`8 0 # = v 7 mb\" | y 9 # 8 M } _ + kl $ #mn x -( }e f l]> ! 03 @jno x~`.xl ty }[sx k j";
            final float textLength = paint.measureText(text);

            paint.setShader(null);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(11 * density);
            paint.setTypeface(FontCache.getDosisSemiBold(getContext()));

            final int count2 = canvas.save();
            canvas.translate(-textLength * textPosition, centerY - 2 * density);
            paint.setColor(Color.rgb(127, 255, 251));
            canvas.drawText(text + text, 0f, 0f, paint);
            canvas.restoreToCount(count2);

            final int count3 = canvas.save();
            canvas.translate(-textLength + textLength * textPosition, centerY + 13 * density);
            paint.setColor(Color.rgb(95, 191, 187));
            canvas.drawText(text + text, 0f, 0f, paint);
            canvas.restoreToCount(count3);
        }

        // dot
        if (connectionStatus == Status.CONNECTING) {
            final float x = -pipeInfo.dotHalfWidth + 2 * (centerX + pipeInfo.dotHalfWidth) * dot;
            final float x2 = x - centerX;
            final float h;
            final float w;

            if (x2 - pipeInfo.dotHalfWidth <= -frameInfo.cw * 0.5f) {
                w = pipeInfo.dotHalfWidth;
                h = pipeInfo.radius;

            } else if (x2 - pipeInfo.dotHalfWidth < frameInfo.cFrameRect.left) {
                w = pipeInfo.dotHalfWidth;

                final float dis = frameInfo.cFrameRect.left - (x2 - pipeInfo.dotHalfWidth);
                h = (float) Math.sqrt(Math.pow(frameInfo.cFrameRect.bottom, 2) - Math.pow(dis, 2));

            } else if (x2 + pipeInfo.dotHalfWidth <= frameInfo.cFrameRect.right) {
                w = pipeInfo.dotHalfWidth;
                h = frameInfo.cFrameRect.bottom;

            } else if (x2 + pipeInfo.dotHalfWidth < frameInfo.cw * 0.5f) {
                w = pipeInfo.dotHalfWidth;

                final float dis = (x2 + pipeInfo.dotHalfWidth) - frameInfo.cFrameRect.right;
                h = (float) Math.sqrt(Math.pow(frameInfo.cFrameRect.bottom, 2) - Math.pow(dis, 2));

            } else {
                w = pipeInfo.dotHalfWidth;
                h = pipeInfo.radius;
            }
            paint.setShader(pipeInfo.dotGradient);
            paint.setAlpha(255);

            final int count2 = canvas.save();
            canvas.translate(x, centerY);
            canvas.drawRect(-w, -h, w, h, paint);
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
        private final RectF cFrameRect;

        private FrameInfo(float density) {
            w = 152 * density;
            cw = 136 * density;
            h = 76 * density;
            strokeWidthMin = 8 * density;
            strokeWidthRange = density;
            cFrameRect = new RectF(-cw * 0.5f + h * 0.5f, -h * 0.5f, cw * 0.5f - h * 0.5f, h * 0.5f);
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

        paint.setColor(colorWith(fraction, Color.BLACK, Color.rgb(0, 128, 128)));
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
