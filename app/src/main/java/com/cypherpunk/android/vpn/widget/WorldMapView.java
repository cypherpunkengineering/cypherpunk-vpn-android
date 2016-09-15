package com.cypherpunk.android.vpn.widget;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.cypherpunk.android.vpn.R;

public class WorldMapView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Path linePath = new Path();
    private float[] originalPositionMatrix = new float[2]; // x, y
    private float[] newPositionMatrix = new float[2]; // x, y
    private Bitmap mapBitmap;
    private Bitmap originalPositionBitmap;
    private Bitmap newPositionBitmap;
    private Matrix mapMatrix;
    private DisplayMetrics dm;
    private float scale;
    private boolean isNewPositionVisible;

    public WorldMapView(Context context) {
        this(context, null);
    }

    public WorldMapView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WorldMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mapBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.map_bk);
        dm = Resources.getSystem().getDisplayMetrics();
        int viewWidth = dm.widthPixels - (getResources().getDimensionPixelOffset(R.dimen.world_map_margin) * 2);

        scale = (float) viewWidth / mapBitmap.getWidth();
        mapMatrix = new Matrix();
        mapMatrix.postScale(scale, scale);
        mapMatrix.postTranslate(getResources().getDimension(R.dimen.world_map_margin), 0);

        originalPositionBitmap = getBitmap(R.drawable.map_original_position);
        newPositionBitmap = getBitmap(R.drawable.map_current_potision_big);

        linePaint.setColor(ContextCompat.getColor(context, R.color.world_map_line));
        linePaint.setStrokeWidth(5);
        linePaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();

        // map
        canvas.drawBitmap(mapBitmap, mapMatrix, paint);

        if (isNewPositionVisible) {
            // line
            linePath.reset();
            linePath.moveTo(originalPositionMatrix[0], originalPositionMatrix[1]);
            linePath.quadTo(
                    Math.abs(newPositionMatrix[0] + originalPositionMatrix[0]) / 2,
                    Math.abs(-newPositionMatrix[1] + originalPositionMatrix[1] / 2),
                    newPositionMatrix[0], newPositionMatrix[1]);
            canvas.drawPath(linePath, linePaint);

            // new position
            canvas.drawBitmap(newPositionBitmap,
                    newPositionMatrix[0] - newPositionBitmap.getWidth() / 2,
                    newPositionMatrix[1] - newPositionBitmap.getHeight() / 2, paint);
        }

        // original position
        canvas.drawBitmap(originalPositionBitmap,
                originalPositionMatrix[0] - originalPositionBitmap.getWidth() / 2,
                originalPositionMatrix[1] - originalPositionBitmap.getHeight() / 2, paint);

        canvas.restore();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        setMeasuredDimension(dm.widthPixels, (int) (mapBitmap.getHeight() * scale));
    }

    /**
     * original position
     *
     * @param x pixel x
     * @param y pixel y
     */
    public void setOriginalPosition(int x, int y) {
        float dx = (x * dm.density);
        float dy = (y * dm.density);
        originalPositionMatrix[0] = dx * scale + getResources().getDimension(R.dimen.world_map_margin);
        originalPositionMatrix[1] = dy * scale;
        invalidate();
    }

    /**
     * new position
     *
     * @param x pixel x
     * @param y pixel y
     */
    public void setNewPosition(int x, int y) {
        float dx = (x * dm.density);
        float dy = (y * dm.density);
        newPositionMatrix[0] = dx * scale + getResources().getDimension(R.dimen.world_map_margin);
        newPositionMatrix[1] = dy * scale;
        setNewPositionVisibility(x == 0 && y == 0);
    }

    /**
     * new position and line visibility
     *
     * @param visible true visible
     */
    public void setNewPositionVisibility(boolean visible) {
        isNewPositionVisible = visible;
        invalidate();
    }

    private Bitmap getBitmap(@DrawableRes int resId) {
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), resId, null);
        assert drawable != null;
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
