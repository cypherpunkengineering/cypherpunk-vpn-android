package com.cypherpunk.android.vpn.widget;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
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

    private Bitmap mapBitmap;
    private Bitmap originalPositionBitmap;
    private Bitmap currentPositionBitmap;
    private Matrix matrix;
    private Matrix originalPositionMatrix;
    private Matrix currentPositionMatrix;
    private float scale;

    public WorldMapView(Context context) {
        this(context, null);
    }

    public WorldMapView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WorldMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mapBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.map_wh);
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        int viewWidth = dm.widthPixels - (getResources().getDimensionPixelOffset(R.dimen.world_map_margin) * 2);

        scale = (float) viewWidth / mapBitmap.getWidth();
        matrix = new Matrix();
        matrix.postScale(scale, scale);
        matrix.postTranslate(getResources().getDimension(R.dimen.world_map_margin), 0);

        originalPositionBitmap = getBitmap(R.drawable.map_original_position);
        currentPositionBitmap = getBitmap(R.drawable.map_current_potision_big);
        originalPositionMatrix = new Matrix();
        currentPositionMatrix = new Matrix();

        linePaint.setColor(ContextCompat.getColor(context, R.color.colorAccent));

        setOriginalPosition(305, 56);
        setCurrentPosition(20, 59);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        setMeasuredDimension(dm.widthPixels, (int) (mapBitmap.getHeight() * scale));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();

        // map
        canvas.drawBitmap(mapBitmap, matrix, paint);

        // original position
        canvas.drawBitmap(originalPositionBitmap, originalPositionMatrix, paint);

        // current position
        canvas.drawBitmap(currentPositionBitmap, currentPositionMatrix, paint);

        // line
        canvas.drawLine(0, 0, 10, 20, linePaint);

        canvas.restore();
    }

    public void setOriginalPosition(int x, int y) {
        originalPositionMatrix.reset();
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        float dx = (x * dm.density);
        float dy = (y * dm.density);
        originalPositionMatrix.postTranslate(
                dx * scale + getResources().getDimension(R.dimen.world_map_margin) - originalPositionBitmap.getWidth() / 2,
                dy * scale - originalPositionBitmap.getHeight() / 2);
    }

    public void setCurrentPosition(int x, int y) {
        currentPositionMatrix.reset();
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        float dx = (x * dm.density);
        float dy = (y * dm.density);
        currentPositionMatrix.postTranslate(
                dx * scale + getResources().getDimension(R.dimen.world_map_margin) - currentPositionBitmap.getWidth() / 2,
                dy * scale - currentPositionBitmap.getHeight() / 2);
    }

    private Bitmap getBitmap(@DrawableRes int resId) {
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), resId, null);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
