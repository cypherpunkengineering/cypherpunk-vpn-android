package com.cypherpunk.privacy.ui.settings;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.cypherpunk.privacy.R;


public class DividerDecoration extends RecyclerView.ItemDecoration {

    private final Paint paint;
    private final int dividerHeight;
    private final int startPosition;

    public DividerDecoration(Context context) {
        this(context, 1);
    }

    public DividerDecoration(@NonNull Context context, @IntRange(from = 1) int startPosition) {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(ContextCompat.getColor(context, R.color.divider));
        dividerHeight = (int) (1 * context.getResources().getDisplayMetrics().density);
        this.startPosition = startPosition;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        final RecyclerView.LayoutManager manager = parent.getLayoutManager();

        final int childCount = parent.getChildCount();
        for (int i = 1; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params =
                    (RecyclerView.LayoutParams) child.getLayoutParams();
            if (params.getViewLayoutPosition() < startPosition) {
                continue;
            }
            // ViewCompat.getTranslationY()を入れないと追加・削除のアニメーション時の位置が変になる
            final int top = manager.getDecoratedTop(child)
                    - params.topMargin
                    + Math.round(ViewCompat.getTranslationY(child));
            final int bottom = top + dividerHeight;
            c.drawRect(0, top, parent.getWidth(), bottom, paint);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        final int position = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();
        final int top = position < startPosition ? 0 : dividerHeight;
        outRect.set(0, top, 0, 0);
    }
}
