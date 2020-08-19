package com.example.scrollingshooter;

public class BackgroundSpawnComponent implements  SpawnComponent {

    @Override
    public void spawn(Transform playerLTransform, Transform t) {
        // Place the background in the top left corner
        t.setLocation(0f,0);
    }
}
