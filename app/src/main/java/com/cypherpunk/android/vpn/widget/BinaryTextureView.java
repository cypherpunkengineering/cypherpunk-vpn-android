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
import java.lang.annotation.RetentionPolicy;
import java.util.Random;


public class BinaryTextureView extends TextureView implements TextureView.SurfaceTextureListener {

    private static final float SCROLL_DISTANCE_PER_SEC_IN_DP = 0.2f;

    private volatile RenderingThread renderingThread;
    private float scrollDistancePerSec;

    public static final int DISCONNECTED = 0;
    public static final int CONNECTING = 1;
    public static final int CONNECTED = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DISCONNECTED, CONNECTING, CONNECTED})
    public @interface ConnectionState {
    }

    @ConnectionState
    private int connectionState = DISCONNECTED;
    private final Object stateMonitor = new Object();

    private final class RenderingThread extends Thread {
        private KeyItem[] keyItems;
        private Paint paint;
        private int allTileHeight;
        private int tileHeight;

        @Override
        public void run() {
            if (keyItems == null) {
                setupTile();
            }

            while (renderingThread == this) {
                drawTiles();
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
                    // ASCII code 33-96
                    char encryptedText = (char) (random.nextInt(63) + 33);
                    int color = ContextCompat.getColor(getContext(), R.color.binary_text_color);
                    KeyItem item = new KeyItem(x, y, textAsBitmap(text, color),
                            textAsBitmap(String.valueOf(encryptedText), color), j % 2 == 0);
                    keyItems[i * tileColumnCount + j] = item;
                    x += tileWidth;
                }
            }
        }

        private void drawTiles() {
            Canvas canvas = lockCanvas();
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            drawTilesInner(canvas);
            unlockCanvasAndPost(canvas);
        }

        private void drawTilesInner(Canvas canvas) {
            synchronized (stateMonitor) {
                for (KeyItem item : keyItems) {
                    if (item == null) {
                        continue;
                    }

                    canvas.drawBitmap(connectionState == CONNECTED ?
                            item.encryptedBitmap : item.bitmap, item.x, item.y, paint);

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

    public void setState(@ConnectionState int state) {
        connectionState = state;
    }

    private static class KeyItem {
        public int x;
        public float y;
        public Bitmap bitmap;
        public Bitmap encryptedBitmap;
        public boolean downAnimation; //  key image transition direction

        public KeyItem(int x, int y, Bitmap bitmap, Bitmap encryptedBitmap, boolean down) {
            this.x = x;
            this.y = y;
            this.bitmap = bitmap;
            this.encryptedBitmap = encryptedBitmap;
            this.downAnimation = down;
        }
    }
}