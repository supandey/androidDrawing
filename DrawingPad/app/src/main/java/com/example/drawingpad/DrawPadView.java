package com.example.drawingpad;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class DrawPadView extends View {

    private Bitmap cacheBitmap;
    private Canvas cacheCanvas;
    private Paint paint;
    private Paint BitmapPaint;
    private Path path;
    private int height;
    private int width;

    private float pX;   // Last saved X-coordinate
    private float pY;   // Last saved Y-coordinate
    private int paintColor = Color.RED;  //Initial color
    private static Paint.Style paintStyle = Paint.Style.STROKE;
    private static int paintWidth = 3;    // Paint Point size

    private Canvas canvas;

    public static final int PEN = 1;
    public static final int PAIL = 2;

    public DrawPadView(Context context) {
        super(context);
    }

    public DrawPadView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /** get the height and width */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        height = h;
        width = w;
        init();
    }

    private void init(){
        cacheBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        cacheCanvas = new Canvas(cacheBitmap);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        path = new Path();
        BitmapPaint = new Paint();
        updatePaint();
    }

    private void updatePaint(){
        paint.setColor(paintColor);
        paint.setStyle(paintStyle);
        paint.setStrokeWidth(paintWidth);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(event.getX(), event.getY());
                pX = event.getX();
                pY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                path.quadTo(pX, pY, event.getX(), event.getY());
                pX = event.getX();
                pY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                cacheCanvas.drawPath(path, paint);
                path.reset();
                break;
        }
        invalidate();

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        this.canvas = canvas;
        BitmapPaint = new Paint();
        canvas.drawBitmap(cacheBitmap, 0,0, BitmapPaint);
        canvas.drawPath(path, paint);

    }

    public void setColor(int color){
        paintColor = color;
        updatePaint();
    }

    public void setPaintWidth(int width){
        paintWidth = width;
        updatePaint();
    }

    public void setStyle(int style){
        switch(style){
            case PEN:
                paintStyle = Paint.Style.STROKE;
                break;
            case PAIL:
                paintStyle = Paint.Style.FILL;
                break;
        }
        updatePaint();
    }

    /** clear your drawing*/
    public void clearScreen(){
        if(canvas != null){
            Paint backPaint = new Paint();
            backPaint.setColor(Color.WHITE);
            canvas.drawRect(new Rect(0, 0, width, height), backPaint);
            cacheCanvas.drawRect(new Rect(0, 0, width, height), backPaint);
        }
        invalidate();
    }
}
