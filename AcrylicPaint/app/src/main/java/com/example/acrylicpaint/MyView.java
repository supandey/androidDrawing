package com.example.acrylicpaint;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class MyView extends View {

    private static final float TOUCH_TOLERANCE = 4;
    public static int DEFAULT_BRUSH_SIZE = 10;
    private static int MAX_POINTERS = 10;

    private Paint mPaint;

    public Bitmap mBitmap;
    private Bitmap mBitmapBackground;
    private Canvas mCanvas;
    private Paint mBitmapPaint;
    private MultiLinePathManager multiLinePathManager;

    private boolean waitingForBackgroundColor = false; //If true and colorChanged() is called, fill the background, else mPaint.setColor()
    private boolean extractingColor = false; //If this is true, the next touch event should extract a color rather than drawing a line.

    Paint getmPaint() { return mPaint;}
    Bitmap getmBitmap() { return mBitmap; }
    Bitmap getmBitmapBackground() { return mBitmapBackground; }
    boolean getWaitingForBackgroundColor() {
        return waitingForBackgroundColor;
    }
    int getStrokeSize() {
        return (int) mPaint.getStrokeWidth();
    }
    void setExtractingColor(boolean val) { extractingColor = val; }
    void setWaitingForBackgroundColor(boolean val) { waitingForBackgroundColor = val; }

    private class LinePath extends Path {
        private Integer idPointer;
        private float lastX;
        private float lastY;

        LinePath() {
            this.idPointer = null;
        }

        public float getLastX() {
            return lastX;
        }

        public float getLastY() {
            return lastY;
        }

        public void touchStart(float x, float y) {
            this.reset();
            this.moveTo(x, y);
            this.lastX = x;
            this.lastY = y;
        }

        public void touchMove(float x, float y) {
            float dx = Math.abs(x - lastX);
            float dy = Math.abs(y - lastY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                this.quadTo(lastX, lastY, (x + lastX) / 2, (y + lastY) / 2);
                lastX = x;
                lastY = y;
            }
        }

        public boolean isDisassociatedFromPointer() {
            return idPointer == null;
        }

        public boolean isAssociatedToPointer(int idPointer) {
            return this.idPointer != null
                    && (int) this.idPointer == idPointer;
        }

        public void disassociateFromPointer() {
            idPointer = null;
        }

        public void associateToPointer(int idPointer) {
            this.idPointer = idPointer;
        }
    }

    private class MultiLinePathManager {
        public LinePath[] superMultiPaths;

        MultiLinePathManager(int maxPointers) {
            superMultiPaths = new LinePath[maxPointers];
            for (int i = 0; i < maxPointers; i++) {
                superMultiPaths[i] = new LinePath();
            }
        }

        public LinePath findLinePathFromPointer(int idPointer) {
            for (LinePath superMultiPath : superMultiPaths) {
                if (superMultiPath.isAssociatedToPointer(idPointer)) {
                    return superMultiPath;
                }
            }
            return null;
        }

        public LinePath addLinePathWithPointer(int idPointer) {
            for (LinePath superMultiPath : superMultiPaths) {
                if (superMultiPath.isDisassociatedFromPointer()) {
                    superMultiPath.associateToPointer(idPointer);
                    return superMultiPath;
                }
            }
            return null;
        }
    }

    public MyView(Context c) {
        super(c);

        setId(R.id.CanvasId);

        int width = getContext().getResources().getDisplayMetrics().widthPixels;
        int height = getContext().getResources().getDisplayMetrics().heightPixels;
        Point size = new Point(width, height);

        //Display display = getWindowManager().getDefaultDisplay();
        //Point size = new Point(display.getWidth(), display.getHeight());

        mBitmapBackground = Bitmap.createBitmap(size.x, size.y, Bitmap.Config.ARGB_8888);
        mBitmap = Bitmap.createBitmap(size.x, size.y,
                Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        multiLinePathManager = new MultiLinePathManager(MAX_POINTERS);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(DEFAULT_BRUSH_SIZE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(0xFFFFFFFF);
        canvas.drawBitmap(mBitmapBackground, 0, 0, new Paint());
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        for (int i = 0; i < multiLinePathManager.superMultiPaths.length; i++) {
            canvas.drawPath(multiLinePathManager.superMultiPaths[i], mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        LinePath linePath;
        int index;
        int id;
        int eventMasked = event.getActionMasked();
        switch (eventMasked) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                index = event.getActionIndex();
                id = event.getPointerId(index);

                if (extractingColor) { //If the user chose the 'extract color' menu option, the touch event indicates where they want to extract the color from.
                    extractingColor = false;

                    View v = findViewById(R.id.CanvasId);
                    v.setDrawingCacheEnabled(true);
                    Bitmap cachedBitmap = v.getDrawingCache();

                    int newColor = cachedBitmap.getPixel(Math.round(event.getX(index)), Math.round(event.getY(index)));

                    v.destroyDrawingCache();
                    colorChanged(newColor);

                    Toast.makeText(getContext(),
                            R.string.color_extracted,
                            Toast.LENGTH_SHORT).show();
                } else {

                    linePath = multiLinePathManager.addLinePathWithPointer(id);
                    if (linePath != null) {
                        linePath.touchStart(event.getX(index), event.getY(index));
                    } else {
                        Log.e("anupam", "Too many fingers!");
                    }
                }

                break;
            }
            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < event.getPointerCount(); i++) {
                    id = event.getPointerId(i);
                    index = event.findPointerIndex(id);
                    linePath = multiLinePathManager.findLinePathFromPointer(id);
                    if (linePath != null) {
                        linePath.touchMove(event.getX(index), event.getY(index));
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                index = event.getActionIndex();
                id = event.getPointerId(index);
                linePath = multiLinePathManager.findLinePathFromPointer(id);
                if (linePath != null) {
                    linePath.lineTo(linePath.getLastX(), linePath.getLastY());

                    // Commit the path to our offscreen
                    mCanvas.drawPath(linePath, mPaint);

                    // Kill this so we don't double draw
                    linePath.reset();

                    // Allow this LinePath to be associated to another idPointer
                    linePath.disassociateFromPointer();
                }
                break;
        }
        invalidate();
        return true;
    }

    public void colorChanged(int color) {
        if( waitingForBackgroundColor ) {
            waitingForBackgroundColor = false;
            mBitmapBackground.eraseColor( color );
            //int[] colors = new int[ 1 ];
            //colors[ 0 ] = color;
            //contentView.mBitmapBackground = Bitmap.createBitmap( colors, contentView.mBitmapBackground.getWidth(), contentView.mBitmapBackground.getHeight(), contentView.mBitmapBackground.getConfig() );
        } else {
            mPaint.setColor( color );
        }
    }
}

