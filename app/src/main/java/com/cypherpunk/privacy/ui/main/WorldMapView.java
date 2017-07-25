package com.cypherpunk.privacy.ui.main;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
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

    private static final int MARKER_COLOR = Color.rgb(136, 255, 255);
    private static final int MARKER_COLOR_DIM = Color.rgb(68, 136, 136);

    private final Drawable mapDrawable;
    private final Drawable markerDrawable;

    private final int MAP_SIZE;
    private final int MARKER_RADIUS;
    private final float POINT_RADIUS;
    private final float MARKER_UP_OFFSET;

    private final Path path = new Path();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RadialGradient gradient;

    private final PointF mapCenter = new PointF();
    private final PointF markerCenter = new PointF();
    @Nullable
    private PointF markerBase;

    private final List<VpnServer> vpnServerList = new ArrayList<>();
    private VpnServer vpnServer;
    private boolean isOffsetMode;

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

        markerDrawable = ContextCompat.getDrawable(context, R.drawable.ic_map_pin_vector);
        markerDrawable.setColorFilter(MARKER_COLOR, PorterDuff.Mode.SRC_ATOP);
        MARKER_RADIUS = (int) (24 * density);
        MARKER_UP_OFFSET = 20 * density;

        POINT_RADIUS = 2 * density;
        paint.setStrokeWidth(1 * density);
        gradient = new RadialGradient(0, 0, POINT_RADIUS * 5,
                Color.argb(255, 0, 255, 255), Color.TRANSPARENT, Shader.TileMode.CLAMP);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        markerBase = new PointF(w * 0.5f, h * 0.65f);
        updatePosition();
    }

    private void updatePosition() {
        if (markerBase == null) {
            return;
        }
        if (vpnServer == null) {
            return;
        }

        final int[] newXY = getXY(vpnServer.lat(), vpnServer.lng());

        if (isOffsetMode) {
            final float scale = vpnServer.scale();
            final float s = 2 * (scale > 0 ? scale : 1f);
            mapCenter.x = newXY[0] - markerBase.x * 0.5f / s;
            mapCenter.y = newXY[1] + markerBase.y * 0.2f / s;
            markerCenter.x = markerBase.x * (1 + 0.5f);
            markerCenter.y = markerBase.y * (1 - 0.2f);
        } else {
            mapCenter.x = newXY[0];
            mapCenter.y = newXY[1];
            markerCenter.x = markerBase.x;
            markerCenter.y = markerBase.y;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (markerBase == null) {
            return;
        }

        if (vpnServer != null) {
            final float scale = vpnServer.scale();
            final float s = 2 * (scale > 0 ? scale : 1f);

            final int count = canvas.save();
            canvas.scale(s, s, markerBase.x, markerBase.y);

            // draw map
            final int left = (int) (markerBase.x - mapCenter.x);
            final int top = (int) (markerBase.y - mapCenter.y);
            mapDrawable.setBounds(left, top,
                    left + mapDrawable.getIntrinsicWidth(),
                    top + mapDrawable.getIntrinsicHeight());
            mapDrawable.draw(canvas);

            // draw vpn server points
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

            // draw current point
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
            canvas.translate(markerCenter.x, markerCenter.y);

            markerDrawable.setBounds(-MARKER_RADIUS, -MARKER_RADIUS * 2, MARKER_RADIUS, 0);
            markerDrawable.draw(canvas);

            canvas.restoreToCount(count4);
        }
    }

    public void setOffsetMode(boolean isEnabled) {
        isOffsetMode = isEnabled;
        markerDrawable.setColorFilter(isEnabled ? MARKER_COLOR_DIM : MARKER_COLOR, PorterDuff.Mode.SRC_ATOP);

        if (markerBase == null) {
            return;
        }

        final float newX;
        final float newY;
        final float newMarkerCenterX;
        final float newMarkerCenterY;

        final int[] newXY = getXY(vpnServer.lat(), vpnServer.lng());

        if (isEnabled) {
            final float scale = vpnServer.scale();
            final float s = 2 * (scale > 0 ? scale : 1f);
            newX = newXY[0] - markerBase.x * 0.5f / s;
            newY = newXY[1] + markerBase.y * 0.2f / s;
            newMarkerCenterX = markerBase.x * (1 + 0.5f);
            newMarkerCenterY = markerBase.y * (1 - 0.2f);
        } else {
            newX = newXY[0];
            newY = newXY[1];
            newMarkerCenterX = markerBase.x;
            newMarkerCenterY = markerBase.y;
        }

        if (mapCenter.x > 0 && mapCenter.y > 0) {
            final float lastX = mapCenter.x;
            final float lastY = mapCenter.y;
            final float lastMarkerCenterX = markerCenter.x;
            final float lastMarkerCenterY = markerCenter.y;

            final ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float fraction = animation.getAnimatedFraction();
                    mapCenter.x = lastX + (newX - lastX) * fraction;
                    mapCenter.y = lastY + (newY - lastY) * fraction;
                    markerCenter.x = lastMarkerCenterX + (newMarkerCenterX - lastMarkerCenterX) * fraction;
                    markerCenter.y = lastMarkerCenterY + (newMarkerCenterY - lastMarkerCenterY) * fraction;
                    invalidate();
                }
            });
            animator.setDuration(500);
            animator.start();
        } else {
            mapCenter.x = newX;
            mapCenter.y = newY;
            markerCenter.x = newMarkerCenterX;
            markerCenter.y = newMarkerCenterY;
            invalidate();
        }
    }

    public void setVpnServer(@NonNull VpnServer newVpnServer) {
        markerDrawable.setColorFilter(MARKER_COLOR, PorterDuff.Mode.SRC_ATOP);
        vpnServer = newVpnServer;

        if (markerBase == null) {
            return;
        }

        final int[] newXY = getXY(vpnServer.lat(), vpnServer.lng());

        if (mapCenter.x > 0 && mapCenter.y > 0) {
            final float lastX = mapCenter.x;
            final float lastY = mapCenter.y;
            final float lastMarkerCenterY = markerCenter.y;
            final float newMarkerCenterY1 = lastMarkerCenterY - MARKER_UP_OFFSET;

            // maker up
            final ValueAnimator animator1 = ValueAnimator.ofFloat(0f, 1f);
            animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float fraction = animation.getAnimatedFraction();
                    markerCenter.y = lastMarkerCenterY + (newMarkerCenterY1 - lastMarkerCenterY) * fraction;
                    invalidate();
                }
            });
            animator1.setDuration(300);

            final float lastMarkerCenterX = markerCenter.x;
            final float newMarkerCenterX = markerBase.x;
            final float newMarkerCenterY2 = markerBase.y - MARKER_UP_OFFSET;

            // translate map
            final ValueAnimator animator2 = ValueAnimator.ofFloat(0f, 1f);
            animator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float fraction = animation.getAnimatedFraction();
                    mapCenter.x = lastX + (newXY[0] - lastX) * fraction;
                    mapCenter.y = lastY + (newXY[1] - lastY) * fraction;
                    markerCenter.x = lastMarkerCenterX + (newMarkerCenterX - lastMarkerCenterX) * fraction;
                    markerCenter.y = newMarkerCenterY1 + (newMarkerCenterY2 - newMarkerCenterY1) * fraction;
                    invalidate();
                }
            });
            animator2.setDuration(1600);

            // marker down
            final float newMarkerCenterY3 = markerBase.y;

            final ValueAnimator animator3 = ValueAnimator.ofFloat(0f, 1f);
            animator3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final float fraction = animation.getAnimatedFraction();
                    markerCenter.y = newMarkerCenterY2 + (newMarkerCenterY3 - newMarkerCenterY2) * fraction;
                    invalidate();
                }
            });
            animator3.setDuration(300);

            final AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playSequentially(animator1, animator2, animator3);
            animatorSet.start();

        } else {
            mapCenter.x = newXY[0];
            mapCenter.y = newXY[1];
            markerCenter.x = markerBase.x;
            markerCenter.y = markerBase.y;
            invalidate();
        }
    }

    public void setVpnServers(List<VpnServer> vpnServerList) {
        this.vpnServerList.clear();
        this.vpnServerList.addAll(vpnServerList);
        invalidate();
    }

    //
    // SavedState
    //

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        final SavedState ss = new SavedState(superState);
        ss.isOffsetMode = isOffsetMode;
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        final SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setOffsetMode(ss.isOffsetMode);
    }

    private static class SavedState extends BaseSavedState {
        boolean isOffsetMode;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            isOffsetMode = (Boolean) in.readValue(null);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(isOffsetMode);
        }

        @Override
        public String toString() {
            return "WorldMapView.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " isOffsetMode=" + isOffsetMode + "}";
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

    //
    // Calculate Map
    //

    private static final int LNG_OFFSET = 11;
    private static final double PI = Math.PI;
    private static final double HALF_PI = PI / 2;
    private static final double EPSILON = Math.ulp(1.0);

    private static double toRad(float degree) {
        return degree * PI / 180;
    }

    private int[] getXY(float lat, float lng) {
        final double[] coordinates = vanDerGrinten3Raw(toRad(lng - LNG_OFFSET), toRad(lat));
        return new int[]{
                (int) ((coordinates[0] * 150.0 + 460.0) * MAP_SIZE / 920.0),
                (int) ((-coordinates[1] * 150.0 + 325.0) * MAP_SIZE / 920.0)
        };
    }

    private static double[] vanDerGrinten3Raw(double lambda, double phi) {
        if (Math.abs(phi) < EPSILON) {
            return new double[]{lambda, 0};
        }

        final double sinTheta = phi / HALF_PI;
        final double theta = Math.asin(sinTheta);
        if (Math.abs(lambda) < EPSILON || Math.abs(Math.abs(phi) - HALF_PI) < EPSILON) {
            return new double[]{0, PI * Math.tan(theta / 2)};
        }

        final double A = (PI / lambda - lambda / PI) / 2;
        final double y1 = sinTheta / (1 + Math.cos(theta));
        return new double[]{
                PI * ((lambda < 0 ? -1 : 1) * Math.sqrt(A * A + 1 - y1 * y1) - A),
                PI * y1};
    }
}
