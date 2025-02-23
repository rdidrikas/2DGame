package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;
import java.util.Map;


public class AnimationManager {

    private final Map<String, Animation<TextureRegion>> animations;
    private String currentState;
    private float stateTime;

    public AnimationManager() {
        animations = new HashMap<>();
        stateTime = 0;
        currentState = "idle";
    }

    public void addAnimation(String name, Animation<TextureRegion> animation) {
        animations.put(name, animation);
    }

    public void update(float delta, boolean isGrounded, boolean isMoving) {
        stateTime += delta;

        // Update animation state based on player conditions
        if (!isGrounded) {
            currentState = "jump";
        } else if (isMoving) {
            currentState = "walk";
        } else {
            currentState = "idle";
        }
    }

    public TextureRegion getCurrentFrame(boolean isFacingLeft) {
        TextureRegion frame = animations.get(currentState).getKeyFrame(stateTime, true);
        TextureRegion flippedFrame = new TextureRegion(frame);
        if (isFacingLeft) {
            flippedFrame.flip(true, false); // Flip horizontally
        }
        return flippedFrame;
    }
}
