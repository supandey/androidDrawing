package com.example.scrollingshooter;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.logging.Level;

public class GameEngine extends SurfaceView implements Runnable, GameStarter, GameEngineBroadcaster, PlayerLaserSpawner {

    private Thread mThread = null;
    private long mFPS;

    private ArrayList<InputObserver> inputObservers = new ArrayList();
    UIController mUIController;

    private GameState mGameState;
    private SoundEngine mSoundEngine;
    HUD mHUD;
    Renderer mRenderer;
    ParticleSystem mParticleSystem;
    PhysicsEngine mPhysicsEngine;

    public GameEngine(Context context, Point size) {
        super(context);

        mUIController = new UIController(this);
        mGameState = new GameState(this, context);
        mSoundEngine = new SoundEngine(context);
        mHUD = new HUD(size);
        mRenderer = new Renderer(this);
        mPhysicsEngine = new PhysicsEngine();

        mParticleSystem = new ParticleSystem();
        mParticleSystem.init(1000);
    }

    // For the game engine broadcaster interface
    public void addObserver(InputObserver o) {
        inputObservers.add(o);
    }

    @Override
    public void run() {
        while (mGameState.getThreadRunning()) {
            long frameStartTime = System.currentTimeMillis();

            if (!mGameState.getPaused()) {
                // Update all the game objects here in a new way

                // This call to update will eveolve with the project
                if (mPhysicsEngine.update(mFPS, mParticleSystem)) {
                    // Player hit
                    deSpawnReSpawn();
                }
            }

            // Draw all the game objects here in a new way
            mRenderer.draw(mGameState, mHUD, mParticleSystem);

            // Measure the frames per second in the usual way
            long timeThisFrame = System.currentTimeMillis() - frameStartTime;
            if (timeThisFrame >= 1) {
                final int MILLIS_IN_SECOND = 1000;
                mFPS = MILLIS_IN_SECOND / timeThisFrame;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        for (InputObserver o : inputObservers) {
            o.handleInput(motionEvent, mGameState, mHUD.getControls());
        }

        // This is temporary code to emit a particle system
        mParticleSystem.emitParticles(new PointF(500,500));

        return true;
    }

    public void deSpawnReSpawn() {
        // Eventually this will despawn and then respawn all the game objects
    }

    public void stopThread() {
        mGameState.stopEverything();

        try {
            mThread.join();
        } catch (InterruptedException e) {
            Log.e("Exception","stopThread()" + e.getMessage());
        }
    }

    public void startThread() {
        mGameState.startThread();

        mThread = new Thread(this);
        mThread.start();
    }

    @Override
    public boolean spawnPlayerLaser(Transform transform) {

        return false;
    }


}
