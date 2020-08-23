package com.example.platformer;

import android.content.Context;
import android.graphics.PointF;

import com.example.platformer.GOSpec.GameObjectSpec;

public class GameObjectFactory {
    private Context mContext;
    private GameEngine mGameEngineReference;
    private int mPixelsPerMetre;

    GameObjectFactory(Context context, GameEngine gameEngine, int pixelsPerMetre) {
        mContext = context;
        mGameEngineReference = gameEngine;
        mPixelsPerMetre = pixelsPerMetre;
    }

    GameObject create(GameObjectSpec spec, PointF location) {
        GameObject object = new GameObject();

        int mNumComponents = spec.getComponents().length;
        object.setTag(spec.getTag());

        // First give the game object the right kind of transform

        switch (object.getTag()) {
            case "Background":
                object.setTransform(new BackgroundTransform(spec.getSpeed(), spec.getSize().x, spec.getSize().y, location));
                break;

            case "Player":
                object.setTransform(new PlayerTransform(spec.getSpeed(), spec.getSize().x, spec.getSize().y, location));
                break;

            default:// normal transform
                object.setTransform(new Transform(spec.getSpeed(), spec.getSize().x, spec.getSize().y, location));
                break;
        }

        // Loop through and add/initialize all the components
        for (int i = 0; i < mNumComponents; i++) {
            switch (spec.getComponents()[i]) {
                case "PlayerInputComponent":
                    object.setPlayerInputTransform(new PlayerInputComponent(mGameEngineReference));
                    break;
                case "AnimatedGraphicsComponent":
                    object.setGraphics(new AnimatedGraphicsComponent(), mContext, spec, spec.getSize(), mPixelsPerMetre);
                    break;
                case "PlayerUpdateComponent":
                    object.setMovement(new PlayerUpdateComponent());
                    break;
                case "InanimateBlockGraphicsComponent":
                    object.setGraphics(new InanimateBlockGraphicsComponent(), mContext, spec, spec.getSize(), mPixelsPerMetre);
                    break;
                case "InanimateBlockUpdateComponent":
                    object.setMovement(new InanimateBlockUpdateComponent());
                    break;
                case "MovableBlockUpdateComponent":
                    object.setMovement(new MovableBlockUpdateComponent());
                    break;
                case "DecorativeBlockUpdateComponent":
                    object.setMovement(new DecorativeBlockUpdateComponent());
                    break;
                case "BackgroundGraphicsComponent":
                    object.setGraphics(new BackgroundGraphicsComponent(), mContext, spec, spec.getSize(), mPixelsPerMetre);
                    break;
                case "BackgroundUpdateComponent":
                    object.setMovement(new BackgroundUpdateComponent());
                    break;

                default:
                    // Error unidentified component
                    break;
            }
        }

        // Return the completed GameObject to the LevelManager class
        return object;
    }
}
