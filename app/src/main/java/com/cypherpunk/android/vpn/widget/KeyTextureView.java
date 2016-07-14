package com.cypherpunk.android.vpn.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.TextureView;

import com.cypherpunk.android.vpn.R;


public class KeyTextureView extends TextureView implements TextureView.SurfaceTextureListener {

    private static final float SCROLL_DISTANCE_PER_MOVE = 0.2f; //0.2dp
    private static final int EXPECTING_RENDERING_FPS = 30;

    private volatile RenderingThread renderingThread;
    private final float distanceY;
    private Paint paint;
    private Bitmap bitmap;
    private int tileHeight;
    private int tileColumnCount;
    private int tileWidth;
    private int tileRowCount;
    private long startTime;
    private long stopTime;
    private boolean stop = true;

    private final class RenderingThread extends Thread {
        @Override
        public void run() {
            while (renderingThread != null) {
                if (!stop) {
                    calculateAndDrawTile();
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
        distanceY = getResources().getDisplayMetrics().density * SCROLL_DISTANCE_PER_MOVE;
        setOpaque(false);
        setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        startTime = SystemClock.uptimeMillis();
        stopTime = SystemClock.uptimeMillis();
        calculateAndDrawTile();
        if (!stop) {
            startAnimation();
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

    private void calculateAndDrawTile() {
        if (bitmap == null) {
            setupTile();
        }

        Canvas canvas = lockCanvas();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        drawTile(canvas);
        unlockCanvasAndPost(canvas);
    }

    public void startAnimation() {
        if (renderingThread == null) {
            renderingThread = new RenderingThread();
            renderingThread.start();
        }
        startTime += SystemClock.uptimeMillis() - stopTime;
        stop = false;
    }

    public void stopAnimation() {
        stop = true;
        stopTime = SystemClock.uptimeMillis();
    }

    private void setupTile() {
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.key_bg); //TODO: svg image...
        paint = new Paint();
        tileHeight = bitmap.getHeight();
        tileWidth = bitmap.getWidth();
        tileRowCount = (int) Math.ceil((double) getHeight() / tileHeight) + 1;
        tileColumnCount = (int) Math.ceil((double) getWidth() / tileWidth);
    }

    private void drawTile(Canvas canvas) {
        float time = (SystemClock.uptimeMillis() - startTime) / (1000 / EXPECTING_RENDERING_FPS);
        float translateY = time * distanceY;
        if (translateY > tileHeight) {
            translateY = (translateY % tileHeight);
        }

        int x = 0;
        for (int j = 0; j < tileColumnCount; j++) {
            boolean down = j % 2 == 0;
            for (int i = 0; i < tileRowCount; i++) {
                int position = i * tileHeight;
                if (down) {
                    canvas.drawBitmap(bitmap, x, position + translateY, paint);
                } else {
                    canvas.drawBitmap(bitmap, x, position - translateY, paint);
                }
            }
            if (down) {
                canvas.drawBitmap(bitmap, x, -(tileHeight - translateY), paint);
            }
            x += tileWidth;
        }
    }
}
