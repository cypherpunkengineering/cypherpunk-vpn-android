package com.cypherpunk.privacy.ui.region;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class RotateGradientDrawable extends Drawable {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final double sin;
    private final double cos;
    private final int color;

    public RotateGradientDrawable() {
        final double theta = Math.PI * 7 / 180;
        sin = Math.sin(theta);
        cos = Math.cos(theta);
        color = Color.argb((int) (255 * 0.3), 0, 0, 0);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        final int width = canvas.getWidth();
        final int height = canvas.getHeight();
        final double l = width * sin + height * cos;
        paint.setShader(new LinearGradient(0f, height, (float) (l * sin), (float) (height - l * cos),
                new int[]{color, Color.TRANSPARENT, Color.TRANSPARENT},
                new float[]{0, 0.5f, 1f},
                Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, width, height, paint);
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
