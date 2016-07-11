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
    private volatile RenderingThread renderingThread;
    private KeyItem[] keyItems;
    private Paint paint;
    private Bitmap bitmap;
    private float distanceY;
    private int allTileHeight;
    private int keyHeight;

    private final class RenderingThread extends Thread {

        private static final int EXPECTING_RENDERING_FPS = 30;

        public RenderingThread() {
            super();
        }

        @Override
        public void run() {
            final int timeAlignment = 1000 / EXPECTING_RENDERING_FPS;
            while (renderingThread != null) {

                calculateAndDrawTile();

                /*
                 * EXPECTING_RENDERING_FPS で指定した fps で描画が行われるように、１回の loop の時間を調整する。
                 * 処理の遅い端末だと描画処理が timeAlignment 以内に収まらず、fps が 半分になってしまう可能性がある。
                 * EXPECTING_RENDERING_FPS が 60 の場合、timeAlignment は 16 なので、DEBUG_RENDERING_LOOP
                 * を true にした時に表示される値が 16 前後になっていれば 60fps出ていることになる。
                 *
                 * L Preview on Nexus 5 や 4.4.4 on Xperia Z2 の実測では、calculateAndDrawTile() の実行に
                 * 5-9msec くらいな感じで、 loop １回はほぼ 16msec。
                 */
                SystemClock.sleep(timeAlignment - SystemClock.uptimeMillis() % timeAlignment);
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
        renderingThread = new RenderingThread();
        renderingThread.start();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        final Thread t = renderingThread;
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
        if (keyItems == null) {
            setupTile(getWidth(), getHeight());
        }

        Canvas canvas = lockCanvas();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        drawTile(canvas);
        unlockCanvasAndPost(canvas);
    }

    private void setupTile(int width, int height) {
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.key_bg); //TODO: svg image...
        paint = new Paint();

        keyHeight = bitmap.getHeight();
        int tileWidth = bitmap.getWidth();
        int tileRowCount = (int) Math.ceil((double) height / keyHeight) + 1;
        int tileColumnCount = (int) Math.ceil((double) width / tileWidth);
        allTileHeight = tileRowCount * keyHeight;
        keyItems = new KeyItem[tileColumnCount * tileRowCount];

        for (int i = 0; i < tileRowCount; i++) {
            int y = i * keyHeight;
            int x = 0;
            for (int j = 0; j < tileColumnCount; j++) {
                KeyItem item = new KeyItem(x, y, j % 2 == 0);
                keyItems[i * tileColumnCount + j] = item;
                x += tileWidth;
            }
        }
    }

    private void drawTile(Canvas canvas) {
        if (keyItems == null) {
            return;
        }

        for (KeyItem item : keyItems) {
            if (item == null) {
                continue;
            }

            canvas.drawBitmap(bitmap, item.x, item.y, paint);

            if (item.downAnimation) {
                item.y += distanceY;
                // タイルが下から画面外にでたら上に移動
                if (item.y > allTileHeight - keyHeight) {
                    item.y = -keyHeight;
                }
            } else {
                item.y -= distanceY;
                // タイルが上から画面外にでたら下に移動
                if (item.y < -keyHeight) {
                    item.y += allTileHeight;
                }
            }
        }
    }

    private static class KeyItem {
        public int x;
        public float y;
        public boolean downAnimation; //  key image transition direction

        public KeyItem(int x, int y, boolean down) {
            this.x = x;
            this.y = y;
            this.downAnimation = down;
        }
    }
}
