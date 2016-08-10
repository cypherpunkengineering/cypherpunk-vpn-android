package com.cypherpunk.android.vpn.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.TextureView;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.utils.FontUtil;

import java.lang.annotation.Retention;
import java.util.Random;

import static java.lang.annotation.RetentionPolicy.SOURCE;


public class BinaryTextureView extends TextureView implements TextureView.SurfaceTextureListener {

    private static final float SCROLL_DISTANCE_PER_SEC_IN_DP = 0.2f;

    private volatile RenderingThread renderingThread;
    private float scrollDistancePerSec;
    private KeyItem[] keyItems;
    private Paint paint;
    private int allTileHeight;
    private int tileHeight;

    @Retention(SOURCE)
    @IntDef({STATE_RUNNING, STATE_STOPPED})
    private @interface AnimationState {
    }

    private static final int STATE_RUNNING = 0;
    private static final int STATE_STOPPED = 1;

    @AnimationState
    // guarded by `stateMonitor`
    private int animationState = STATE_STOPPED;
    private final Object stateMonitor = new Object();

    private final class RenderingThread extends Thread {

        @Override
        public void run() {
            if (keyItems == null) {
                setupTile();
            }

            while (renderingThread == this) {
                drawTiles();
                sleepIfStopped();
            }
        }

        private Bitmap textAsBitmap(String text, int color) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setTextSize(getResources().getDimension(R.dimen.binary_text));
            paint.setColor(color);
            paint.setTypeface(FontUtil.getDosisRegular(getContext()));
            paint.setTextAlign(Paint.Align.CENTER);
            float baseline = -paint.ascent();
            int width = (int) getResources().getDimension(R.dimen.binary_text_width); // round
            int height = (int) getResources().getDimension(R.dimen.binary_text_height);
            Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(image);
            canvas.drawText(text, width / 2, baseline, paint);
            return image;
        }

        private void setupTile() {
            Random random = new Random();
            paint = new Paint();
            tileHeight = getResources().getDimensionPixelOffset(R.dimen.binary_text_height);
            int tileWidth = getResources().getDimensionPixelOffset(R.dimen.binary_text_width);
            int tileRowCount = (int) Math.ceil((double) getHeight() / tileHeight) + 1;
            int tileColumnCount = (int) Math.ceil((double) getWidth() / tileWidth);
            allTileHeight = tileRowCount * tileHeight;
            keyItems = new KeyItem[tileColumnCount * tileRowCount];

            for (int i = 0; i < tileRowCount; i++) {
                int y = i * tileHeight;
                int x = 0;
                for (int j = 0; j < tileColumnCount; j++) {
                    String text = String.valueOf(random.nextInt(2));
                    int color = ContextCompat.getColor(getContext(), R.color.binary_text_color);
                    KeyItem item = new KeyItem(x, y, textAsBitmap(text, color), j % 2 == 0);
                    keyItems[i * tileColumnCount + j] = item;
                    x += tileWidth;
                }
            }
        }

        private void drawTiles() {
            Canvas canvas = lockCanvas();
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            drawTile(canvas);
            unlockCanvasAndPost(canvas);
        }

        private void drawTile(Canvas canvas) {
            synchronized (stateMonitor) {
                switch (animationState) {
                    case STATE_RUNNING:
                        for (KeyItem item : keyItems) {
                            if (item == null) {
                                continue;
                            }

                            canvas.drawBitmap(item.bitmap, item.x, item.y, paint);

                            if (item.downAnimation) {
                                item.y += scrollDistancePerSec;
                                // タイルが下から画面外にでたら上に移動
                                if (item.y > allTileHeight - tileHeight) {
                                    item.y = -tileHeight;
                                }
                            } else {
                                item.y -= scrollDistancePerSec;
                                // タイルが上から画面外にでたら下に移動
                                if (item.y < -tileHeight) {
                                    item.y += allTileHeight;
                                }
                            }
                        }
                        break;
                    case STATE_STOPPED:
                        for (KeyItem item : keyItems) {
                            if (item == null) {
                                continue;
                            }

                            canvas.drawBitmap(item.bitmap, item.x, item.y, paint);
                        }
                        break;
                }
            }
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

    public BinaryTextureView(Context context) {
        this(context, null);
    }

    public BinaryTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BinaryTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        scrollDistancePerSec = getResources().getDisplayMetrics().density * SCROLL_DISTANCE_PER_SEC_IN_DP;
        setOpaque(false);
        setSurfaceTextureListener(this);
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
            animationState = STATE_RUNNING;
            stateMonitor.notifyAll();
        }
    }

    public void stopAnimation() {
        synchronized (stateMonitor) {
            if (animationState == STATE_RUNNING) {
                animationState = STATE_STOPPED;
            }
        }
    }

    private static class KeyItem {
        public int x;
        public float y;
        public Bitmap bitmap;
        public boolean downAnimation; //  key image transition direction

        public KeyItem(int x, int y, Bitmap bitmap, boolean down) {
            this.x = x;
            this.y = y;
            this.bitmap = bitmap;
            this.downAnimation = down;
        }
    }
}