package com.cypherpunk.privacy.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.View;

import com.cypherpunk.privacy.R;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;


public class KeyTextureView extends TextureView implements TextureView.SurfaceTextureListener {

    private static final float SCROLL_DISTANCE_PER_SEC_IN_DP = 6f;

    private volatile RenderingThread renderingThread;
    private final float scrollDistancePerSec; // in px
    private volatile long startTime;
    private float lastOffset = 0f;

    @Retention(SOURCE)
    @IntDef({STATE_RUNNING, STATE_STOPPING, STATE_STOPPED})
    private @interface AnimationState {
    }

    private static final int STATE_RUNNING = 0;
    private static final int STATE_STOPPING = 1;
    private static final int STATE_STOPPED = 2;

    @AnimationState
    // guarded by `stateMonitor`
    private int animationState = STATE_STOPPED;
    private final Object stateMonitor = new Object();

    private final class RenderingThread extends Thread {
        private Bitmap bitmap;
        private Paint paint;
        private int tileHeight;
        private int tileColumnCount;
        private int tileWidth;
        private int tileRowCount;

        @Override
        public void run() {
            if (bitmap == null) {
                setupTile();
            }

            while (renderingThread == this) {
                drawTiles();
                sleepIfStopped();
            }
        }

        private void setupTile() {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.key_bg); //TODO: svg image...
            paint = new Paint();
            tileHeight = bitmap.getHeight();
            tileWidth = bitmap.getWidth();
            tileRowCount = (int) Math.ceil((double) getHeight() / tileHeight) + 1;
            tileColumnCount = (int) Math.ceil((double) getWidth() / tileWidth);
        }

        private void drawTiles() {
            Canvas canvas = lockCanvas();
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            drawTilesInner(canvas);
            unlockCanvasAndPost(canvas);
        }

        private void drawTilesInner(Canvas canvas) {
            float offset;
            synchronized (stateMonitor) {
                switch (animationState) {
                    case KeyTextureView.STATE_RUNNING:
                        offset = calculateOffsetFromTime();
                        break;
                    case KeyTextureView.STATE_STOPPING:
                        offset = calculateOffsetFromTime();
                        final float halfTileHeight = tileHeight * 0.5f;
                        if (halfTileHeight <= lastOffset && offset < lastOffset) {
                            // キリの良い所までアニメーションしたので止める
                            offset = 0f;
                            animationState = STATE_STOPPED;
                        }
                        if (lastOffset < halfTileHeight && (offset < lastOffset || halfTileHeight <= offset)) {
                            // キリの良い所までアニメーションしたので止める
                            offset = halfTileHeight;
                            animationState = STATE_STOPPED;
                        }
                        break;
                    case KeyTextureView.STATE_STOPPED:
                        offset = lastOffset;
                        break;
                    default:
                        // unknown state
                        return;
                }
            }

            float x = 0;
            for (int j = 0; j < tileColumnCount; j++) {
                boolean down = j % 2 == 0;
                float y = down ? (offset - tileHeight) : -offset;

                for (int i = 0; i < tileRowCount; i++) {
                    canvas.drawBitmap(bitmap, x, y, paint);
                    y += tileHeight;
                }
                x += tileWidth;
            }
            lastOffset = offset;
        }

        private float calculateOffsetFromTime() {
            final long elapsedTime = SystemClock.uptimeMillis() - startTime;
            float offset = elapsedTime * scrollDistancePerSec / 1000;
            if (offset > tileHeight) {
                offset = (offset % tileHeight);
            }
            return offset;
        }

        private void sleepIfStopped() {
            synchronized (stateMonitor) {
                if (animationState == STATE_STOPPED) {
                    try {
                        stateMonitor.wait();
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
    }

    public KeyTextureView(Context context) {
        this(context, null);
    }

    public KeyTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        scrollDistancePerSec = getResources().getDisplayMetrics().density * SCROLL_DISTANCE_PER_SEC_IN_DP;
        setOpaque(false);
        setSurfaceTextureListener(this);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, startTime, lastOffset);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        final SavedState myState = (SavedState) state;
        startTime = myState.getStartTime();
        lastOffset = myState.getLastOffset();

        super.onRestoreInstanceState(myState.getSuperState());
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        if (renderingThread == null) {
            renderingThread = new RenderingThread();
            renderingThread.start();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        Thread t = renderingThread;
        if (t != null) {
            renderingThread = null;

            synchronized (stateMonitor) {
                stateMonitor.notifyAll();
            }
            try {
                /*
                 * onSurfaceTextureDestroyed が true を返したあとで Texture に操作を行わないようにするため
                 * 描画スレッドの終了を待つ。
                 */
                t.join();
            } catch (InterruptedException e) {
                // ignore
            }
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    public void startAnimation() {
        synchronized (stateMonitor) {
            if (animationState == STATE_STOPPED) {
                startTime = SystemClock.uptimeMillis() - (long) (lastOffset / scrollDistancePerSec * 1000);
            }
            animationState = STATE_RUNNING;

            stateMonitor.notifyAll();
        }
    }

    public void stopAnimation() {
        synchronized (stateMonitor) {
            if (animationState == STATE_RUNNING) {
                animationState = STATE_STOPPING;
            }
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class SavedState extends View.BaseSavedState {
        private long startTime;
        private float lastOffset;

        public SavedState(Parcel source) {
            super(source);
            startTime = source.readLong();
            lastOffset = source.readFloat();
        }

        public SavedState(Parcelable superState, long startTime, float lastOffset) {
            super(superState);
            this.startTime = startTime;
            this.lastOffset = lastOffset;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeLong(startTime);
            out.writeFloat(lastOffset);
        }

        public long getStartTime() {
            return startTime;
        }

        public float getLastOffset() {
            return lastOffset;
        }

        public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}