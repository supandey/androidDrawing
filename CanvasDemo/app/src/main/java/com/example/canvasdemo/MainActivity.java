package com.example.canvasdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    ImageView mImageView;
    Bitmap mBlankBitmap;
    Canvas mCanvas;
    Paint mPaint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int widthInPixels = 800;
        int heightInPixels = 600;

        mBlankBitmap = Bitmap.createBitmap(widthInPixels, heightInPixels, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBlankBitmap);
        mImageView = new ImageView(this);
        mPaint = new Paint();

        mCanvas.drawColor(Color.argb(255,0,0,255));
        mPaint.setTextSize(100);
        mPaint.setColor((Color.argb(255,255,255,255)));  // white
        mCanvas.drawText("Hello World!",100, 100, mPaint);
        mPaint.setColor((Color.argb(255,212,207,62)));  // yellow
        mCanvas.drawCircle(400,250, 100, mPaint);

        mImageView.setImageBitmap((mBlankBitmap));
        setContentView(mImageView);
    }
}