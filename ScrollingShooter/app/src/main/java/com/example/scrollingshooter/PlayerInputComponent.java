package com.example.scrollingshooter;

import android.graphics.Rect;
import android.view.MotionEvent;

import java.util.ArrayList;

public class PlayerInputComponent implements InputComponent, InputObserver {
    private Transform mTransform;
    private PlayerLaserSpawner mPLS;

    PlayerInputComponent(GameEngine ger) {
        ger.addObserver(this);
        mPLS = ger;
    }

    @Override
    public void setTransform(Transform transform) {
        mTransform = transform;
    }

    // Required method of InputObserver interface called from the onTouchEvent method
    @Override
    public void handleInput(MotionEvent event, GameState gameState, ArrayList<Rect> buttons) {
        
    }
}
