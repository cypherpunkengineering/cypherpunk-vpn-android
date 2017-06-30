package com.cypherpunk.privacy.ui.main;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.cypherpunk.privacy.R;
import com.cypherpunk.privacy.datasource.vpn.VpnServer;

import java.util.ArrayList;
import java.util.List;

public class WorldMapView extends View {

    private static final int LNG_OFFSET = 11;
    private static final double pi = Math.PI;
    private static final double halfPi = pi / 2;
    private static final double epsilon = Math.ulp(1.0);

    private final Path path = new Path();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RadialGradient gradient;
    private final int MAP_SIZE;
    private final float POINT_RADIUS;
    private final float MARKER_SIZE;
    private final float markerUpOffset;

    private float MARKER_BASE_POSITION_X;
    private float MARKER_BASE_POSITION_Y;

    private final Drawable mapDrawable;
    private final Drawable markerDrawable;

    // map center
    private float centerX;
    private float centerY;

    // marker position
    private float markerCenterX;
    private float markerCenterY;

    private final List<VpnServer> vpnServerList = new ArrayList<>();
    private VpnServer vpnServer;

    public WorldMapView(Context context) {
        this(context, null);
    }

    public WorldMapView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WorldMapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final float density = context.getResources().getDisplayMetrics().density;

        mapDrawable = ContextCompat.getDrawable(context, R.drawable.world_map);
        mapDrawable.setAlpha(77);
        MAP_SIZE = mapDrawable.getIntrinsicWidth();

        markerDrawable = ContextCompat.getDrawable(context, R.drawable.ic_map_pin);
        markerDrawable.setColorFilter(Color.rgb(136, 255, 255), PorterDuff.Mode.SRC_ATOP);
        MARKER_SIZE = 48 * density;

        POINT_RADIUS = 2 * density;
        paint.setStrokeWidth(1 * density);
        gradient = new RadialGradient(0, 0, POINT_RADIUS * 5,
                Color.argb(255, 0, 255, 255), Color.TRANSPARENT, Shader.TileMode.CLAMP);

        markerUpOffset = 20 * density;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        MARKER_BASE_POSITION_X = w * 0.5f;
        MARKER_BASE_POSITION_Y = h * 0.65f;

        markerCenterX = MARKER_BASE_POSITION_X;
        markerCenterY = MARKER_BASE_POSITION_Y;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (vpnServer != null) {
            final float scale = vpnServer.scale();
            final float s = scale > 0 ? 2 * scale : 2f;

            final int count = canvas.save();
            canvas.scale(s, s, MARKER_BASE_POSITION_X, MARKER_BASE_POSITION_Y);

            // draw map
            final int left = (int) (MARKER_BASE_POSITION_X - centerX);
            final int top = (int) (MARKER_BASE_POSITION_Y - centerY);
            mapDrawable.setBounds(left, top,
                    left + mapDrawable.getIntrinsicWidth(),
                    top + mapDrawable.getIntrinsicHeight());
            mapDrawable.draw(canvas);

            // draw vpn server markers
            for (VpnServer vpnServer : vpnServerList) {
                final int[] xy = getXY(vpnServer.lat(), vpnServer.lng());

                final int count2 = canvas.save();
                canvas.translate(left + xy[0], top + xy[1]);

                if (!TextUtils.equals(vpnServer.id(), this.vpnServer.id())) {
                    paint.setColor(Color.argb(128, 0, 136, 136));
                    paint.setStyle(Paint.Style.FILL_AND_STROKE);
                    canvas.drawCircle(0, 0, POINT_RADIUS, paint);

                    paint.setColor(Color.argb(128, 0, 68, 68));
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawCircle(0, 0, POINT_RADIUS, paint);
                }

                canvas.restoreToCount(count2);
            }

            // draw current marker
            final int[] xy = getXY(vpnServer.lat(), vpnServer.lng());
            {
                final int count3 = canvas.save();
                canvas.translate(left + xy[0], top + xy[1]);

                paint.setColor(Color.argb((int) (192 * 0.3f), 0, 255, 255));
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawCircle(0, 0, POINT_RADIUS, paint);

                paint.setColor(Color.argb((int) (255 * 0.1f), 0, 255, 255));
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle(0, 0, POINT_RADIUS, paint);

                paint.setShader(gradient);
                paint.setAlpha(48);
                paint.setStyle(Paint.Style.FILL);
                path.reset();
                path.addCircle(0, 0, POINT_RADIUS * 6, Path.Direction.CW);
                path.addCircle(0, 0, POINT_RADIUS + paint.getStrokeWidth() * 0.5f, Path.Direction.CCW);
                canvas.drawPath(path, paint);
                paint.setShader(null);

                canvas.restoreToCount(count3);
            }

            canvas.restoreToCount(count);

            // draw marker
            final int count4 = canvas.save();
            canvas.translate(markerCenterX, markerCenterY);

            markerDrawable.setBounds((int) (-MARKER_SIZE * 0.5f), -(int) MARKER_SIZE,
                    (int) (MARKER_SIZE * 0.5f), 0);
            markerDrawable.draw(canvas);

            canvas.restoreToCount(count4);
        }
    }

    private int[] getXY(float lat, float lng) {
        final double[] coordinates = vanDerGrinten3Raw(toRad(lng - LNG_OFFSET), toRad(lat));
        return new int[]{
                (int) ((coordinates[0] * 150.0 + 460.0) * MAP_SIZE / 920.0),
                (int) ((-coordinates[1] * 150.0 + 325.0) * MAP_SIZE / 920.0)
        };
    }

    public void setOffsetMode(boolean isEnabled) {
        markerDrawable.setColorFilter(isEnabled ? Color.rgb(68, 136, 136) : Color.rgb(136, 255, 255),
                PorterDuff.Mode.SRC_ATOP);

        final float scale = 2 * vpnServer.scale();

        final float newX;
        final float newY;
        final float newMarkerCenterX;
        final float newMarkerCenterY;
        if (isEnabled) {
            newX = centerX - getWidth() * 0.25f / scale;
            newY = centerY + getHeight() * 0.1f / scale;
            newMarkerCenterX = getWidth() * 0.75f;
            newMarkerCenterY = getHeight() * 0.54f;
        } else {
            final int[] newXY = getXY(vpnServer.lat(), vpnServer.lng());
            newX = newXY[0];
            newY = newXY[1];
            newMarkerCenterX = MARKER_BASE_POSITION_X;
            newMarkerCenterY = MARKER_BASE_POSITION_Y;
        }
        if (centerX > 0 && centerY > 0) {
            final float lastX = centerX;
            final float lastY = centerY;
            final float lastMarkerCenterX = markerCenterX;
            final float lastMarkerCenterY = markerCenterY;
            final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float fraction = animation.getAnimatedFraction();
                    centerX = lastX + (newX - lastX) * fraction;
                    centerY = lastY + (newY - lastY) * fraction;
                    markerCenterX = lastMarkerCenterX + (newMarkerCenterX - lastMarkerCenterX) * fraction;
                    markerCenterY = lastMarkerCenterY + (newMarkerCenterY - lastMarkerCenterY) * fraction;
                    invalidate();
                }
            });
            animator.setDuration(500);
            animator.start();

        } else {
            centerX = newX;
            centerY = newY;
            invalidate();
        }
    }

    public void setVpnServer(@NonNull VpnServer newVpnServer) {
        markerDrawable.setColorFilter(Color.rgb(136, 255, 255), PorterDuff.Mode.SRC_ATOP);
        vpnServer = newVpnServer;

        final int[] newXY = getXY(vpnServer.lat(), vpnServer.lng());

        if (centerX > 0 && centerY > 0) {
            final float lastX = centerX;
            final float lastY = centerY;
            final float lastMarkerCenterY1 = markerCenterY;
            final float newMarkerCenterY1 = lastMarkerCenterY1 - markerUpOffset;

            // maker up
            final ValueAnimator animator1 = ValueAnimator.ofFloat(0f, 1f);
            animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float fraction = animation.getAnimatedFraction();
                    markerCenterY = lastMarkerCenterY1 + (newMarkerCenterY1 - lastMarkerCenterY1) * fraction;
                    invalidate();
                }
            });
            animator1.setDuration(300);

            final float lastMarkerCenterX = markerCenterX;
            final float newMarkerCenterX = MARKER_BASE_POSITION_X;
            final float newMarkerCenterY2 = MARKER_BASE_POSITION_Y - markerUpOffset;

            // translate map
            final ValueAnimator animator2 = ValueAnimator.ofFloat(0f, 1f);
            animator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float fraction = animation.getAnimatedFraction();
                    centerX = lastX + (newXY[0] - lastX) * fraction;
                    centerY = lastY + (newXY[1] - lastY) * fraction;
                    markerCenterX = lastMarkerCenterX + (newMarkerCenterX - lastMarkerCenterX) * fraction;
                    markerCenterY = newMarkerCenterY1 + (newMarkerCenterY2 - newMarkerCenterY1) * fraction;
                    invalidate();
                }
            });
            animator2.setDuration(1600);

            // marker down
            final float newMarkerCenterY3 = MARKER_BASE_POSITION_Y;

            final ValueAnimator animator3 = ValueAnimator.ofFloat(0f, 1f);
            animator3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float fraction = animation.getAnimatedFraction();
                    markerCenterY = newMarkerCenterY2 + (newMarkerCenterY3 - newMarkerCenterY2) * fraction;
                    invalidate();
                }
            });
            animator3.setDuration(300);

            final AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playSequentially(animator1, animator2, animator3);
            animatorSet.start();

        } else {
            centerX = newXY[0];
            centerY = newXY[1];
            markerCenterX = MARKER_BASE_POSITION_X;
            markerCenterY = MARKER_BASE_POSITION_Y;
            invalidate();
        }
    }

    private static double toRad(float degree) {
        return degree * Math.PI / 180;
    }

    private static double[] vanDerGrinten3Raw(double lambda, double phi) {
        if (Math.abs(phi) < epsilon) {
            return new double[]{lambda, 0};
        }

        double sinTheta = phi / halfPi;
        double theta = Math.asin(sinTheta);
        if (Math.abs(lambda) < epsilon || Math.abs(Math.abs(phi) - halfPi) < epsilon) {
            return new double[]{0, pi * Math.tan(theta / 2)};
        }

        double A = (pi / lambda - lambda / pi) / 2;
        double y1 = sinTheta / (1 + Math.cos(theta));
        return new double[]{
                pi * ((lambda < 0 ? -1 : 1) * Math.sqrt(A * A + 1 - y1 * y1) - A),
                pi * y1};
    }

    public void setVpnServers(List<VpnServer> vpnServerList) {
        this.vpnServerList.clear();
        this.vpnServerList.addAll(vpnServerList);
        invalidate();
    }
}
