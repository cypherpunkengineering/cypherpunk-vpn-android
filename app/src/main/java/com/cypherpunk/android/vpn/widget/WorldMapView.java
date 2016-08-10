package com.cypherpunk.android.vpn.widget;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.cypherpunk.android.vpn.R;

public class WorldMapView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Bitmap bitmap;
    private Matrix matrix;
    private float scale;

    public WorldMapView(Context context) {
        this(context, null);
    }

    public WorldMapView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WorldMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.map_wh);

        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        int imageWidth = bitmap.getWidth();
        int viewWidth = dm.widthPixels - (getResources().getDimensionPixelOffset(R.dimen.world_map_margin) * 2);

        scale = (float) viewWidth / imageWidth;
        matrix = new Matrix();
        matrix.postScale(scale, scale);
        matrix.postTranslate(getResources().getDimension(R.dimen.world_map_margin), 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        setMeasuredDimension(dm.widthPixels, (int) (bitmap.getHeight() * scale));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.drawBitmap(bitmap, matrix, paint);
        canvas.restore();
    }
}
