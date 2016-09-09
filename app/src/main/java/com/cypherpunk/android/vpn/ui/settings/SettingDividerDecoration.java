package com.cypherpunk.android.vpn.ui.settings;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.cypherpunk.android.vpn.R;


public class SettingDividerDecoration extends RecyclerView.ItemDecoration {

    private int dividerHeight;
    private Drawable divider;

    public SettingDividerDecoration(Context context) {
        dividerHeight = (int) (1 * context.getResources().getDisplayMetrics().density);
        divider = new ColorDrawable(ContextCompat.getColor(context, R.color.divider));
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        final int childCount = parent.getChildCount();
        final int width = parent.getWidth();
        for (int childViewIndex = 0; childViewIndex < childCount; childViewIndex++) {
            final View view = parent.getChildAt(childViewIndex);
            if (shouldDrawDividerBelow(view, parent)) {
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view
                        .getLayoutParams();
                final int top = view.getBottom() + params.bottomMargin;
                final int bottom = top + dividerHeight;
                divider.setBounds(0, top, width, bottom);
                divider.draw(c);
            }
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        if (shouldDrawDividerBelow(view, parent)) {
            outRect.bottom = dividerHeight;
        }
    }

    private boolean shouldDrawDividerBelow(View view, RecyclerView parent) {
        final RecyclerView.ViewHolder holder = parent.getChildViewHolder(view);
        final boolean dividerAllowedBelow = holder instanceof PreferenceViewHolder
                && ((PreferenceViewHolder) holder).isDividerAllowedBelow();
        if (!dividerAllowedBelow) {
            return false;
        }
        boolean nextAllowed = true;
        int index = parent.indexOfChild(view);
        if (index < parent.getChildCount() - 1) {
            final View nextView = parent.getChildAt(index + 1);
            final RecyclerView.ViewHolder nextHolder = parent.getChildViewHolder(nextView);
            nextAllowed = nextHolder instanceof PreferenceViewHolder
                    && ((PreferenceViewHolder) nextHolder).isDividerAllowedAbove();
        }
        return nextAllowed;
    }
}
