package com.cypherpunk.android.vpn.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Size;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.cypherpunk.android.vpn.R;
import com.cypherpunk.android.vpn.utils.FontUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Random;


public class BinarySurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "BinarySurfaceView";
    private static final int FPS = 60;
    private static final float SCROLL_DISTANCE_PER_SEC_IN_DP = 6f;

    private static final boolean DUMP_FPS = false;

    private volatile RenderingThread renderingThread;

    public static final int DISCONNECTED = 0;
    public static final int CONNECTING = 1;
    public static final int CONNECTED = 2;
    private String[] strings;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DISCONNECTED, CONNECTING, CONNECTED})
    public @interface ConnectionState {
    }

    @ConnectionState
    private volatile int connectionState = DISCONNECTED;

    private final class RenderingThread extends Thread {
        private final float scrollDistancePerMilliSec;
        private final TileDrawable tileDrawable;
        private final Drawable background;

        private long baseTime;

        private RenderingThread(@NonNull Context context, int width, int height, @NonNull String[] strings) {
            final Resources res = context.getResources();
            scrollDistancePerMilliSec = res.getDisplayMetrics().density * SCROLL_DISTANCE_PER_SEC_IN_DP / 1000;

            final int tileWidth = res.getDimensionPixelOffset(R.dimen.binary_text_width);
            final int tileHeight = res.getDimensionPixelOffset(R.dimen.binary_text_height);

            final int rowCount = (int) Math.ceil((double) height / tileHeight) + 2;
            final int columnCount = (int) Math.ceil((double) width / tileWidth);

            final KeyItemGenerator keyItemGenerator = new KeyItemGenerator(
                    ContextCompat.getColor(context, R.color.binary_text_color),
                    ContextCompat.getColor(context, R.color.binary_text_disconnected),
                    ContextCompat.getColor(context, R.color.binary_text_connecting),
                    ContextCompat.getColor(context, R.color.binary_text_connected),
                    strings, rowCount, columnCount);

            final KeyItemDrawer keyItemDrawer = new KeyItemDrawer(
                    res.getDimension(R.dimen.binary_text),
                    FontUtil.getInconsolataRegular(context));

            tileDrawable = new TileDrawable(tileWidth, tileHeight, rowCount, columnCount,
                    keyItemGenerator, keyItemDrawer);
            background = ContextCompat.getDrawable(context, R.drawable.window_background_indigo);

            final Point displaySize = new Point();
            getDisplay().getSize(displaySize);
            background.setBounds(0, 0, displaySize.x, displaySize.y);
        }

        @Override
        public void run() {
            /**
             * In order to work reliable on Nexus 7, we place ~500ms delay at the start of drawing thread
             * (AOSP - Issue 58385)
             */
            if (android.os.Build.BRAND.equalsIgnoreCase("google") &&
                    android.os.Build.MANUFACTURER.equalsIgnoreCase("asus") &&
                    android.os.Build.MODEL.equalsIgnoreCase("Nexus 7"))
            {
                SystemClock.sleep(500);
            }

            final long minDrawingTime = 1000L / FPS;
            long lastDrawTime = SystemClock.uptimeMillis();
            baseTime = lastDrawTime;
            int actualFps = 0;
            while (renderingThread == this) {
                drawTiles();
                final long now = SystemClock.uptimeMillis();
                final long elapsed = now - lastDrawTime;
                if (elapsed < minDrawingTime) {
                    SystemClock.sleep(minDrawingTime - elapsed);
                }
                if (DUMP_FPS) {
                    actualFps++;
                    if (now % 1000 < lastDrawTime % 1000) {
                        Log.d(TAG, "fps: " + actualFps);
                        actualFps = 0;
                    }
                }
                lastDrawTime = SystemClock.uptimeMillis();
                yield(); // this solved the hangup issue.
            }
        }

        private void drawTiles()
        {
            // see http://stackoverflow.com/questions/15770467/drawing-surface-unlockcanvasandpost-and-illegalargumentexception

            final SurfaceHolder holder = getHolder();
            final Surface surface = holder.getSurface();

            if (surface == null || !surface.isValid())
                return;

            final Canvas canvas = holder.lockCanvas();

            try
            {
                //noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (holder)
                {
                    final int state = BinarySurfaceView.this.connectionState;
                    final long elapsedTime = SystemClock.uptimeMillis() - baseTime;
                    final float distance = elapsedTime * scrollDistancePerMilliSec;
                    if (canvas != null)
                    {
                        background.draw(canvas);
                        tileDrawable.draw(canvas, distance, state);
                    }
                }
            }
            catch (Exception e)
            {
                Log.e(TAG, "exception thrown in drawTiles()", e);
                return;
            }
            finally
            {
                if (canvas != null)
                    holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    public BinarySurfaceView(Context context) {
        this(context, null);
    }

    public BinarySurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BinarySurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setZOrderOnTop(false);

        final SurfaceHolder holder = getHolder();
        holder.setFormat(PixelFormat.RGBA_8888);
        holder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (renderingThread == null) {
            renderingThread = new RenderingThread(getContext(), getWidth(), getHeight(), strings);
            renderingThread.start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
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
    }

    public void setState(@ConnectionState int state) {
        connectionState = state;
    }

    public void setText(String[] text) {
        this.strings = text;
    }

    /**
     * 位置計算のみ
     * <p>
     * 描画関係は {@link KeyItemDrawer}
     */
    private static class TileDrawable {
        private final int tileWidth;
        private final int tileHeight;
        private final int allTileHeight;

        private final int rowCount;
        private final int columnCount;

        private final KeyItemDrawer keyItemDrawer;
        private final KeyItem[][] keyItems;

        private TileDrawable(int tileWidth, int tileHeight, int rowCount, int columnCount,
                             @NonNull KeyItemGenerator keyItemGenerator,
                             @NonNull KeyItemDrawer keyItemDrawer) {

            this.tileWidth = tileWidth;
            this.tileHeight = tileHeight;
            this.rowCount = rowCount;
            this.columnCount = columnCount;
            this.keyItemDrawer = keyItemDrawer;

            allTileHeight = rowCount * tileHeight;

            keyItems = new KeyItem[rowCount][columnCount];
            for (int row = 0; row < rowCount; row++) {
                for (int column = 0; column < columnCount; column++) {
                    keyItems[row][column] = keyItemGenerator.generate(row, column);
                }
            }
        }

        private void draw(Canvas canvas, float distance, @ConnectionState int connectedState) {
            float y = 0;
            for (int row = 0; row < rowCount; row++) {
                float x = tileWidth * 0.5f;
                boolean downAnimation = true;
                for (int column = 0; column < columnCount; column++) {
                    final float animatedY;
                    if (downAnimation) {
                        // tileHeightひとつ分上が基準位置
                        animatedY = ((y + distance + tileHeight) % allTileHeight) - tileHeight;
                    } else {
                        // tileHeightひとつ分上が基準位置なのは同じだが、全体をallTileHeight分ずらして常に負数になるように調整してから計算する
                        animatedY = ((y - distance + tileHeight - allTileHeight) % allTileHeight) - tileHeight + allTileHeight;
                    }

                    keyItemDrawer.draw(canvas, x, animatedY, keyItems[row][column], connectedState);

                    x += tileWidth;
                    downAnimation = !downAnimation;
                }
                y += tileHeight;
            }
        }
    }

    private static class KeyItemGenerator {
        private static final char randomCharFrom = '!'; // 0x21
        private static final char randomCharTo = '`'; // 0x60
        private static final int randomCharLength = randomCharTo - randomCharFrom + 1;
        private static final char[] binaryChar = {'0', '1'};

        private final SparseArray<Pair<Integer, String>> stringInfo = new SparseArray<>();

        private final Random random = new Random();

        @ColorInt
        private final int normalTextColor;
        @ColorInt
        private final int disconnectTextColor;
        @ColorInt
        private final int connectingTextColor;
        @ColorInt
        private final int connectedTextColor;

        private KeyItemGenerator(@ColorInt int normalTextColor, @ColorInt int disconnectTextColor,
                                 @ColorInt int connectingTextColor, @ColorInt int connectedTextColor,
                                 String[] strings, int rowCount, int columnCount) {

            this.normalTextColor = normalTextColor;
            this.disconnectTextColor = disconnectTextColor;
            this.connectingTextColor = connectingTextColor;
            this.connectedTextColor = connectedTextColor;

            final ArrayList<Integer> positions = new ArrayList<>();
            for (int i = 0; i < columnCount; i++) {
                positions.add(i);
            }
            for (String s : strings) {
                if (positions.isEmpty()) {
                    break;
                }
                int position = random.nextInt(positions.size());
                int column = positions.remove(position);
                stringInfo.put(column, new Pair<>(random.nextInt(rowCount), s));
            }
        }

        private KeyItem generate(int row, int column) {
            final Pair<Integer, String> pair = stringInfo.get(column);
            if (pair != null) {
                final int offset = pair.first;
                final int index = row - offset;
                final String text = pair.second;
                if (text != null && index >= 0 && index < text.length()) {
                    return new TextItem(text.charAt(index), newRandomChar(),
                            disconnectTextColor, connectingTextColor, connectedTextColor);
                } else {
                    return new BinaryItem(newBinaryChar(), newRandomChar(), normalTextColor);
                }
            } else {
                return new BinaryItem(newBinaryChar(), newRandomChar(), normalTextColor);
            }
        }

        private char newRandomChar() {
            return (char) (randomCharFrom + random.nextInt(randomCharLength));
        }

        private char newBinaryChar() {
            return binaryChar[random.nextInt(binaryChar.length)];
        }
    }

    private static class KeyItemDrawer {

        private final Paint paint;

        private KeyItemDrawer(float textSize, Typeface typeface) {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setTextSize(textSize);
            paint.setTypeface(typeface);
            paint.setTextAlign(Paint.Align.CENTER);
        }

        private void draw(@NonNull Canvas canvas, float x, float y,
                          @NonNull KeyItem keyItem, @ConnectionState int connectedState) {
            paint.setColor(keyItem.getTextColor(connectedState));
            canvas.drawText(keyItem.c, keyItem.getIndex(connectedState), 1, x, y, paint);
        }
    }

    private abstract static class KeyItem {
        @Size(2)
        private final char[] c;

        private KeyItem(char normalChar, char connectedChar) {
            this.c = new char[]{normalChar, connectedChar};
        }

        @ColorInt
        protected abstract int getTextColor(@ConnectionState int connectedState);

        private int getIndex(@ConnectionState int connectedState) {
            return connectedState == CONNECTED ? 1 : 0;
        }
    }

    private static class BinaryItem extends KeyItem {
        @ColorInt
        public final int textColor;

        private BinaryItem(char normalChar, char connectedChar, @ColorInt int textColor) {
            super(normalChar, connectedChar);
            this.textColor = textColor;
        }

        @ColorInt
        @Override
        protected int getTextColor(@ConnectionState int connectedState) {
            return textColor;
        }
    }

    private static class TextItem extends KeyItem {
        @ColorInt
        private final int normalTextColor;
        @ColorInt
        private final int connectingTextColor;
        @ColorInt
        private final int connectedTextColor;

        private TextItem(char normalChar, char connectedChar,
                         @ColorInt int normalTextColor,
                         @ColorInt int connectingTextColor,
                         @ColorInt int connectedTextColor) {
            super(normalChar, connectedChar);
            this.normalTextColor = normalTextColor;
            this.connectingTextColor = connectingTextColor;
            this.connectedTextColor = connectedTextColor;
        }

        @ColorInt
        @Override
        protected int getTextColor(@ConnectionState int connectedState) {
            switch (connectedState) {
                case DISCONNECTED:
                    return normalTextColor;
                case CONNECTING:
                    return connectingTextColor;
                case CONNECTED:
                    return connectedTextColor;
                default:
                    return normalTextColor;
            }
        }
    }
}
