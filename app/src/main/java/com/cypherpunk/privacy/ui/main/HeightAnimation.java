package com.cypherpunk.privacy.ui.main;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;


public class HeightAnimation extends Animation {

    private final int targetHeight;
    private final int startHeight;

    private final View view;

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


