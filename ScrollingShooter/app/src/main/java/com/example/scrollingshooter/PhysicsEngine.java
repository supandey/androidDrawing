package com.example.scrollingshooter;

public class PhysicsEngine {

    // This signature and much more will change later in the project
    boolean update(long fps, ParticleSystem ps) {

        if (ps.mIsRunning) {
            ps.update(fps);
        }

        return false;
    }

    // Collision detection will go here
}
