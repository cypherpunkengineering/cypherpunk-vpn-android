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
import java.util.ArrayList;
import java.util.Random;


public class BinaryTextureView extends TextureView implements TextureView.SurfaceTextureListener {

    private static final float SCROLL_DISTANCE_PER_SEC_IN_DP = 0.2f;

    private volatile RenderingThread renderingThread;
    private float scrollDistancePerSec;
    private ArrayList<String> strings = new ArrayList<>();

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
        private Paint paintForText;

        private int allTileHeight;
        private int tileHeight;

        RenderingThread() {
            paint = new Paint();

            paintForText = new Paint(Paint.ANTI_ALIAS_FLAG);
            paintForText.setTextSize(getResources().getDimension(R.dimen.binary_text));
            paintForText.setTypeface(FontUtil.getDosisRegular(getContext()));
            paintForText.setTextAlign(Paint.Align.CENTER);
        }

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
            paintForText.setColor(color);
            Paint.FontMetrics fontMetrics = paintForText.getFontMetrics();
            int width = (int) getResources().getDimension(R.dimen.binary_text_width); // round
            int height = (int) getResources().getDimension(R.dimen.binary_text_height);
            float baseline = (height / 2) - (fontMetrics.ascent + fontMetrics.descent) / 2;
            Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(image);
            // TODO 背景色をカスタムattributeから取得する
            canvas.drawColor(Color.BLACK);
            canvas.drawText(text, width / 2, baseline, paintForText);
            return image;
        }

        private void setupTile() {
            Random random = new Random();
            tileHeight = getResources().getDimensionPixelOffset(R.dimen.binary_text_height);
            int tileWidth = getResources().getDimensionPixelOffset(R.dimen.binary_text_width);
            int tileRowCount = (int) Math.ceil((double) getHeight() / tileHeight) + 1;
            int tileColumnCount = (int) Math.ceil((double) getWidth() / tileWidth);
            allTileHeight = tileRowCount * tileHeight;
            keyItems = new KeyItem[tileColumnCount * tileRowCount];

            ArrayList<Integer> stringColumnNumbers = new ArrayList<>();
            for (int i = 0; i < strings.size(); i++) {
                stringColumnNumbers.add(random.nextInt(tileColumnCount));
            }
            for (int i = 0; i < tileRowCount; i++) {
                int y = i * tileHeight;
                int x = 0;
                for (int j = 0; j < tileColumnCount; j++) {
                    String character;
                    String text = strings.get(0);
                    int color;
                    if (stringColumnNumbers.contains(j) && i < text.length()) {
                        character = text.substring(i, i + 1);
                        color = ContextCompat.getColor(getContext(), R.color.binary_text_disconnected);
                    } else {
                        character = String.valueOf(random.nextInt(2));
                        color = ContextCompat.getColor(getContext(), R.color.binary_text_color);
                    }
                    String encryptedText = String.valueOf((char) (random.nextInt(63) + 33));
                    KeyItem item = new KeyItem(x, y, textAsBitmap(character, color),
                            textAsBitmap(encryptedText, color), j % 2 == 0);
                    keyItems[i * tileColumnCount + j] = item;
                    x += tileWidth;
                }
            }
        }

        private void drawTiles() {
            Canvas canvas = lockCanvas();
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
        setOpaque(true);
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

    public void setStrings(ArrayList<String> strings) {
        this.strings = strings;
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