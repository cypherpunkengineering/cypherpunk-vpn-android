package com.cypherpunk.privacy.ui.startup;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.cypherpunk.privacy.ui.common.FontCache;

/**
 * Created by yanzm on 2017/07/20.
 */

public class PipeView extends View {

    private static final String CIPHER_TEXT = "x`8 0 # = v 7 mb\" | y 9 # 8 M } _ + kl $ #mn x -( }e f l]> ! 03 @jno x~`.xl ty }[sx k j ";
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public PipeView(Context context) {
        this(context, null);
    }

    public PipeView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PipeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        density = context.getResources().getDisplayMetrics().density;

        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(10 * density);
        paint.setTypeface(FontCache.getDosisSemiBold(getContext()));
    }

    final float density;
    private float textPosition;
    private ValueAnimator animator;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (animator != null) {
            animator.cancel();
        }

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                textPosition = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(20000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) {
            animator.cancel();
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final float cipherTextLength = paint.measureText(CIPHER_TEXT);
        final float width = cipherTextLength + getWidth();

        float textLength = cipherTextLength;
        String text = CIPHER_TEXT;
        while (textLength < width) {
            textLength += cipherTextLength;
            text += CIPHER_TEXT;
        }

        final float centerY = getHeight() * 0.5f;

        final int count2 = canvas.save();
        canvas.translate(-cipherTextLength * textPosition, centerY - 2 * density);
        paint.setColor(Color.rgb(127, 255, 251));
        canvas.drawText(text, 0f, 0f, paint);
        canvas.restoreToCount(count2);

        final int count3 = canvas.save();
        canvas.translate(-cipherTextLength + cipherTextLength * textPosition, centerY + 10 * density);
        paint.setColor(Color.rgb(95, 191, 187));
        canvas.drawText(text, 0f, 0f, paint);
        canvas.restoreToCount(count3);
    }
}
