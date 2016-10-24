package com.cypherpunk.android.vpn.ui.main;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;


public class HeightAnimation extends Animation {

    private int targetHeight;
    private int startHeight;

    private View view;

    public HeightAnimation(int targetHeight, View view) {
        this.targetHeight = targetHeight;
        this.startHeight = view.getHeight();
        this.view = view;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        view.getLayoutParams().height = (int) (startHeight + (targetHeight - startHeight) * interpolatedTime);
        view.requestLayout();
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}


