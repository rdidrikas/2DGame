package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;
import java.util.Map;


public class AnimationManager {

    private final Map<String, Animation<TextureRegion>> animations;
    private String currentState;
    private String currentGunState;
    private float stateTime;

    public AnimationManager() {
        animations = new HashMap<>();
        stateTime = 0;
        currentState = "idle";
    }

    public void addAnimation(String name, Animation<TextureRegion> animation) {
        animations.put(name, animation);
    }

    public void update(float delta, boolean isGrounded, boolean isMoving, boolean isFiring, int type) {
        stateTime += delta;

        // type == 0 -> player
        // type == 1 -> gun

        // Update animation state based on player conditions

        if(type == 0){
            if (!isGrounded) {
                currentState = "jump";
            } else if (isMoving) {
                currentState = "walk";
            } else {
                currentState = "idle";
            }
        } else if (type == 1){
            if (isFiring){
                currentGunState = "gunFire";
            } else if (isMoving){
                currentGunState = "gunWalk";
            } else {
                currentGunState = "gunIdle";
            }
        }

    }

    public TextureRegion getCurrentPlayerFrame(boolean isFacingLeft) {
        TextureRegion frame = animations.get(currentState).getKeyFrame(stateTime, true);
        TextureRegion flippedFrame = new TextureRegion(frame);
        if (isFacingLeft) {
            flippedFrame.flip(true, false); // Flip horizontally
        }
        return flippedFrame;
    }

    public TextureRegion getCurrentGunFrame(boolean isFacingLeft) {
        TextureRegion frame = animations.get(currentGunState).getKeyFrame(stateTime, true);
        TextureRegion flippedFrame = new TextureRegion(frame);
        if (isFacingLeft) {
            flippedFrame.flip(true, false); // Flip horizontally
        }
        return flippedFrame;
    }

    public TextureRegion getBulletFrame(){
        return animations.get("bullet").getKeyFrame(stateTime, true);
    }

}
