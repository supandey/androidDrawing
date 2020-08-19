package com.example.scrollingshooter;

public interface MovementComponent {
    boolean move(long fps,
                 Transform t,
                 Transform playerTransform);
}
