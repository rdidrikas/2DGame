package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import org.lwjgl.Sys;

import java.util.HashMap;
import java.util.Map;


public class AnimationManager {

    private final Map<String, Animation<TextureRegion>> animations;
    private String currentState;
    private String currentGunState;

    private float stateTime;
    private float gunStateTime;
    private float enemyStateTime;
    private float enemyGunStateTime;
    private float bulletStateTime;
    private float someStateTime;
    private float playerStateTime;

    public boolean animIsShot;

    public AnimationManager() {
        animations = new HashMap<>();
        this.stateTime = 0;
        this.gunStateTime = 0;
        this.enemyStateTime = 0;
        this.enemyGunStateTime = 0;
        this.bulletStateTime = 0;
        this.someStateTime = 0;
        this.playerStateTime = 0;
        currentState = "idle";
    }

    public void addAnimation(String name, Animation<TextureRegion> animation) {
        animations.put(name, animation);
    }

    public void update(float delta, boolean isGrounded, boolean isMoving, boolean isFiring, boolean isShot, boolean playerDetected, int type ) {
        stateTime += delta;
        playerStateTime += delta;
        gunStateTime += delta;
        enemyStateTime += delta;
        enemyGunStateTime += delta;
        bulletStateTime += delta;
        someStateTime += delta;

        animIsShot = isShot; // Used to play animation only one time
        // type == 0 -> player
        // type == 1 -> gun
        // type == 2 -> enemy
        // type == 3 -> enemy gun

        // Update animation state based on player conditions

        if(type == 0){

            if(isShot){
                currentState = "dead";
            } else if (!isGrounded) {
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
        } else if(type == 2){
            if (isShot) {
                if (animations.get("enemyNormalShot").isAnimationFinished(enemyStateTime)) { // Dont know why it doesnt work with enemyStateTime
                    currentState = "enemyNormalDead";
                }
                else currentState = "enemyNormalShot";
            } else if(isMoving) {
                currentState = "enemyNormalWalk";
            } else {
                currentState = "enemyNormalIdle";
            }
        } else if (type == 3) {
            if (isFiring) {
                currentGunState = "enemyGunFire";
            } else if(playerDetected) {
                currentGunState = "enemyGunDetect";
            } else if (isMoving) {
                currentGunState = "enemyGunWalk";
            } else {
                currentGunState = "enemyGunIdle";
            }
        }

    }

    public TextureRegion getCurrentPlayerFrame(boolean isFacingLeft) {

        TextureRegion frame = animations.get(currentState).getKeyFrame(playerStateTime, true);
        TextureRegion flippedFrame = new TextureRegion(frame);
        if (isFacingLeft) {
            flippedFrame.flip(true, false); // Flip horizontally
        }
        return flippedFrame;
    }

    public TextureRegion getCurrentEnemyFrame(boolean isFacingLeft) {

        TextureRegion frame = animations.get(currentState).getKeyFrame(enemyStateTime, true);
        TextureRegion flippedFrame = new TextureRegion(frame);
        if (isFacingLeft) {
            flippedFrame.flip(true, false); // Flip horizontally
        }
        return flippedFrame;
    }

    public TextureRegion getCurrentGunFrame(boolean isFacingLeft) {
        TextureRegion frame = animations.get(currentGunState).getKeyFrame(gunStateTime, true);
        TextureRegion flippedFrame = new TextureRegion(frame);
        if (isFacingLeft) {
            flippedFrame.flip(true, false); // Flip horizontally
        }
        return flippedFrame;
    }


    public TextureRegion getCurrentEnemyGunFrame(boolean isFacingLeft) {
        TextureRegion frame = animations.get(currentGunState).getKeyFrame(enemyGunStateTime, true);
        TextureRegion flippedFrame = new TextureRegion(frame);
        if (isFacingLeft) {
            flippedFrame.flip(true, false); // Flip horizontally
        }
        return flippedFrame;
    }

    public TextureRegion getBulletFrame(String name) {
        return animations.get(name).getKeyFrame(bulletStateTime, true);
    }


    public TextureRegion getSomeFrame(String name) {
        return animations.get(name).getKeyFrame(someStateTime, true);
    }

    public boolean getSomeAnimationFinish(String name){
        return animations.get(name).isAnimationFinished(someStateTime);
    }

    public TextureRegion getLastFrame(String name) {
        return animations.get(name).getKeyFrames()[animations.get(name).getKeyFrames().length - 1];
    }

    public void resetSomeStateTime() {
        this.someStateTime = 0;
    }

    public void resetEnemyStateTime() {
        this.stateTime = 0;
    }

}
