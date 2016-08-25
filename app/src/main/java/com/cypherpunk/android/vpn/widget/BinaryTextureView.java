package com.cypherpunk.android.vpn.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.os.SystemClock;
import android.support.annotation.ColorInt;
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

    private static final float SCROLL_DISTANCE_PER_SEC_IN_DP = 6f;

    private volatile RenderingThread renderingThread;
    private ArrayList<String> strings = new ArrayList<>();

    @ColorInt
    private int disconnectColor;

    @ColorInt
    private int connectingColor;

    @ColorInt
    private int connectedColor;

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

        private float scrollDistancePerMilliSec;
        private long baseTime;

        RenderingThread() {
            paint = new Paint();

            paintForText = new Paint(Paint.ANTI_ALIAS_FLAG);
            paintForText.setTextSize(getResources().getDimension(R.dimen.binary_text));
            paintForText.setTypeface(FontUtil.getInconsolataRegular(getContext()));
            paintForText.setTextAlign(Paint.Align.CENTER);

            scrollDistancePerMilliSec = getResources().getDisplayMetrics().density * SCROLL_DISTANCE_PER_SEC_IN_DP / 1000;
        }

        @Override
        public void run() {
            if (keyItems == null) {
                setupTile();
                baseTime = SystemClock.uptimeMillis();
            }

            while (renderingThread == this) {
                drawTiles();
            }
        }

        private Bitmap charAsBitmap(char ch, int color) {
            paintForText.setColor(color);
            Paint.FontMetrics fontMetrics = paintForText.getFontMetrics();
            int width = (int) getResources().getDimension(R.dimen.binary_text_width); // round
            int height = (int) getResources().getDimension(R.dimen.binary_text_height);
            float baseline = (height / 2) - (fontMetrics.ascent + fontMetrics.descent) / 2;
            Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(image);
            canvas.drawText(new char[]{ch}, 0, 1, width / 2, baseline, paintForText);
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

            // prepare Bitmaps for normal color
            final int normalTextColor = ContextCompat.getColor(getContext(), R.color.binary_text_color);
            final Bitmap[] binaryBitmaps = {charAsBitmap('0', normalTextColor), charAsBitmap('1', normalTextColor)};
            final char randomCharFrom = '!'; // 0x21
            final char randomCharTo = '`'; // 0x60
            final Bitmap[] randomBitmaps = new Bitmap[randomCharTo - randomCharFrom + 1];
            for (int i = 0; i < randomBitmaps.length; i++) {
                randomBitmaps[i] = charAsBitmap((char) (randomCharFrom + i), normalTextColor);
            }

            // generate initial tile states
            for (int i = 0; i < tileRowCount; i++) {
                int y = i * tileHeight;
                int x = 0;
                for (int j = 0; j < tileColumnCount; j++) {
                    final char character;
                    final Bitmap plainTextBitmap, randomBitmap;
                    final int randomCharIndex = random.nextInt(randomBitmaps.length);
                    KeyItem item;
                    if (stringColumnNumbers.contains(j)) {
                        String text = strings.get(stringColumnNumbers.indexOf(j));
                        if (i < text.length()) {
                            character = text.charAt(i);
                            char ch = (char) (randomCharFrom + randomCharIndex);
                            item = new KeyItem(x, y, j % 2 == 0, charAsBitmap(character, disconnectColor),
                                    charAsBitmap(character, connectingColor), charAsBitmap(ch, connectedColor));
                        } else {
                            // use prepared Bitmaps to avoid to generate same Bitmaps many times
                            final int binaryCharIndex = random.nextInt(binaryBitmaps.length);
                            plainTextBitmap = binaryBitmaps[binaryCharIndex];
                            randomBitmap = randomBitmaps[randomCharIndex];
                            item = new KeyItem(x, y, j % 2 == 0, plainTextBitmap, randomBitmap);
                        }
                    } else {
                        // use prepared Bitmaps to avoid to generate same Bitmaps many times
                        final int binaryCharIndex = random.nextInt(binaryBitmaps.length);
                        plainTextBitmap = binaryBitmaps[binaryCharIndex];
                        randomBitmap = randomBitmaps[randomCharIndex];
                        item = new KeyItem(x, y, j % 2 == 0, plainTextBitmap, randomBitmap);
                    }
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

                    final long elapsedTime = SystemClock.uptimeMillis() - baseTime;
                    final float distance = elapsedTime * scrollDistancePerMilliSec;

                    final float y;

                    if (item.downAnimation) {
                        // tileHeightひとつ分上が基準位置
                        y = ((item.y + distance + tileHeight) % allTileHeight) - tileHeight;
                    } else {
                        // tileHeightひとつ分上が基準位置なのは同じだが、全体をallTileHeight分ずらして常に負数になるように調整してから計算する
                        y = ((item.y - distance + tileHeight - allTileHeight) % allTileHeight) - tileHeight + allTileHeight;
                    }
                    if (item.bitmap != null) {
                        canvas.drawBitmap(connectionState == CONNECTED ?
                                item.encryptedBitmap : item.bitmap, item.x, y, paint);
                    } else {
                        Bitmap bitmap = null;
                        switch (connectionState) {
                            case DISCONNECTED:
                                bitmap = item.disconnectedBitmap;
                                break;
                            case CONNECTING:
                                bitmap = item.connectingBitmap;
                                break;
                            case CONNECTED:
                                bitmap = item.connectedEncryptedBitmap;
                                break;
                        }
                        canvas.drawBitmap(bitmap, item.x, y, paint);
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
        setOpaque(false);
        setSurfaceTextureListener(this);

        disconnectColor = ContextCompat.getColor(getContext(), R.color.binary_text_disconnected);
        connectingColor = ContextCompat.getColor(getContext(), R.color.binary_text_connecting);
        connectedColor = ContextCompat.getColor(getContext(), R.color.binary_text_connected);
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
        for (String string : strings) {
            this.strings.add(string.toUpperCase());
        }
    }

    @SuppressWarnings("WeakerAccess")
    private static class KeyItem {
        public final int x;
        public final float y;
        public final boolean downAnimation; //  key image transition direction
        public final Bitmap bitmap;
        public final Bitmap encryptedBitmap;
        public final Bitmap disconnectedBitmap;
        public final Bitmap connectingBitmap;
        public final Bitmap connectedEncryptedBitmap;

        public KeyItem(int x, int y, boolean down, Bitmap bitmap, Bitmap encryptedBitmap) {
            this.x = x;
            this.y = y;
            this.downAnimation = down;
            this.bitmap = bitmap;
            this.encryptedBitmap = encryptedBitmap;
            this.disconnectedBitmap = null;
            this.connectingBitmap = null;
            this.connectedEncryptedBitmap = null;
        }

        public KeyItem(int x, int y, boolean down, Bitmap disconnectedBitmap, Bitmap connectingBitmap, Bitmap connectedEncryptedBitmap) {
            this.x = x;
            this.y = y;
            this.downAnimation = down;
            this.bitmap = null;
            this.encryptedBitmap = null;
            this.disconnectedBitmap = disconnectedBitmap;
            this.connectingBitmap = connectingBitmap;
            this.connectedEncryptedBitmap = connectedEncryptedBitmap;
        }
    }
}
