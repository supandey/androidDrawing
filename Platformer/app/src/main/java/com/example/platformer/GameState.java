package com.example.platformer;

import android.content.Context;
import android.content.SharedPreferences;

public class GameState {
    private static volatile boolean mThreadRunning = false;
    private static volatile boolean mPaused = true;
    private static volatile boolean mGameOver = true;
    private static volatile boolean mDrawing = false;
    private EngineController mEngineController;

    private int mFastestUnderground;
    private int mFastestMountains;
    private int mFastestCity;
    private long startTimeInMillis;

    private int mCoinsAvailable;
    private int coinsCollected;

    private SharedPreferences.Editor mEditor;

    private String currentLevel;

    GameState(EngineController gs, Context context) {
        mEngineController = gs;
        SharedPreferences prefs = context.getSharedPreferences("HiScore", Context.MODE_PRIVATE);

        mEditor = prefs.edit();
        mFastestUnderground = prefs.getInt("fastest_underground_time", 1000);
        mFastestMountains = prefs.getInt("fastest_mountains_time", 1000);
        mFastestCity = prefs.getInt("fastest_city_time", 1000);
    }

    void coinCollected() {
        coinsCollected++;
    }

    int getCoinsRemaining() {
        return mCoinsAvailable - coinsCollected;
    }

    void coinAddedToLevel() {
        mCoinsAvailable++;
    }

    void resetCoins() {
        mCoinsAvailable = 0;
        coinsCollected = 0;
    }

    void setCurrentLevel(String level) {
        currentLevel = level;
    }

    String getCurrentLevel() {
        return currentLevel;
    }

    void objectiveReached() {
        endGame();
    }

    int getFastestUnderground() {
        return mFastestUnderground;
    }

    int getFastestMountains() {
        return mFastestMountains;
    }

    int getFastestCity() {
        return mFastestCity;
    }

    void startNewGame() {
        // Don't want to be handling objects while clearing ArrayList and filling it up again
        stopEverything();
        mEngineController.startNewLevel();
        startEverything();
        startTimeInMillis = System.currentTimeMillis();
    }

    int getCurrentTime() {
        long MILLIS_IN_SECOND = 1000;
        return (int) ((System.currentTimeMillis() - startTimeInMillis) / MILLIS_IN_SECOND);
    }

    void death() {
        stopEverything();
        SoundEngine.playPlayerBurn();
    }

    private void endGame() {
        stopEverything();
        int totalTime = ((mCoinsAvailable - coinsCollected) * 10) + getCurrentTime();

        switch (currentLevel) {

            case "underground":
                if (totalTime < mFastestUnderground) {
                    mFastestUnderground = totalTime;
                    // Save new time
                    mEditor.putInt("fastest_underground_time", mFastestUnderground);
                    mEditor.commit();
                }
                break;
            case "city":
                if (totalTime < mFastestCity) {
                    mFastestCity = totalTime;
                    // Save new time
                    mEditor.putInt("fastest_city_time", mFastestCity);
                    mEditor.commit();
                }
                break;
            case "mountains":
                if (totalTime < mFastestMountains) {
                    mFastestMountains = totalTime;
                    // Save new time
                    mEditor.putInt("fastest_mountains_time", mFastestMountains);
                    mEditor.commit();
                }
                break;
        }
    }

    void stopEverything() {// Except the thread
        mPaused = true;
        mGameOver = true;
        mDrawing = false;
    }

    private void startEverything() {
        mPaused = false;
        mGameOver = false;
        mDrawing = true;
    }

    void stopThread() {
        mThreadRunning = false;
    }

    boolean getThreadRunning() {
        return mThreadRunning;
    }

    void startThread() {
        mThreadRunning = true;
    }

    boolean getDrawing() {
        return mDrawing;
    }

    boolean getPaused() {
        return mPaused;
    }

    boolean getGameOver() {
        return mGameOver;
    }
}
