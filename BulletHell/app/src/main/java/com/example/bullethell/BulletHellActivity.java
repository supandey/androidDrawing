package com.example.bullethell;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

public class BulletHellActivity extends AppCompatActivity {

    private BulletHellGame mBHGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);


        // Get the screen resolution
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        // Call the constructor(initialize) the BulletHellGame instance
        mBHGame = new BulletHellGame(this, size.x, size.y);
        setContentView(mBHGame);
    }

    @Override
    // Start the main game thread when the game is launched
    protected void onResume() {
        super.onResume();

        mBHGame.resume();
    }

    @Override
    // Stop the thread when the player quits
    protected void onPause() {
        super.onPause();

        mBHGame.pause();
    }
}