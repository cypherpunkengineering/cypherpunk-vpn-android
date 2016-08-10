package com.cypherpunk.android.vpn.widget;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cypherpunk.android.vpn.R;

public class WorldMapView extends FrameLayout {

    public WorldMapView(Context context) {
        this(context, null);
    }

    public WorldMapView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WorldMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        ImageView mapImageView = new ImageView(context);
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.map_wh, null);
        mapImageView.setImageDrawable(drawable);

        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();

        assert drawable != null;
        int imageWidth = drawable.getIntrinsicWidth();
        int imageHeight = drawable.getIntrinsicHeight();

        int viewWidth = dm.widthPixels - (getResources().getDimensionPixelOffset(R.dimen.world_map_margin) * 2);

        float a = (float) viewWidth / imageWidth;
        Matrix matrix = new Matrix();
        matrix.postScale(a, a);

        mapImageView.setLayoutParams(new ViewGroup.LayoutParams(dm.widthPixels, (int) (imageHeight * a)));

        addView(mapImageView);
    }
}
