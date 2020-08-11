package com.example.snakeactivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import java.util.Random;

public class Apple {

    // The location of the apple on the grid. Not in pixels
    private Point mLocation = new Point();

    // The range of values we can choose from to spawn an apple
    private Point mSpawnRange;
    private int mSize;

    // An image to represent the apple
    private Bitmap mBitmapApple;

    Apple(Context context, Point sr, int s){


        mSpawnRange = sr;   // Size of spawn range
        mSize = s;          // Size of an apple
        mLocation.x = -10;   // Hide the apple off-screen until the game starts

        // Load the image to the bitmap
        mBitmapApple = BitmapFactory.decodeResource(context.getResources(), R.drawable.apple);

        // Resize the bitmap
        mBitmapApple = Bitmap.createScaledBitmap(mBitmapApple, s, s, false);
    }

    // This is called every time an apple is eaten
    void spawn(){
        // Choose two random values and place the apple
        Random random = new Random();
        mLocation.x = random.nextInt(mSpawnRange.x) + 1;
        mLocation.y = random.nextInt(mSpawnRange.y - 1) + 1;
    }

    // Let SnakeGame know where the apple is. SnakeGame can share this with the snake
    Point getLocation(){
        return mLocation;
    }

    // Draw the apple
    void draw(Canvas canvas, Paint paint){
        canvas.drawBitmap(mBitmapApple,mLocation.x * mSize, mLocation.y * mSize, paint);
    }

}
