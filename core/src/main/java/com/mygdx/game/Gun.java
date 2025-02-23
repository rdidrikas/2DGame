package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Gun {
    private AnimationManager animationManager;
    private boolean isFiring;
    private boolean isFacingLeft;

    public Gun() {
        animationManager = new AnimationManager();
        loadGunAnimations();
    }

    private void loadGunAnimations() {

        Texture gunSheet = new Texture("Animations/RAMBRO_gun_anim.png");
        TextureRegion[][] tmpGunFrames = TextureRegion.split(gunSheet, 32, 32);

        // Gun animation idle
        TextureRegion[] gunIdleFrames = {tmpGunFrames[1][2]};
        animationManager.addAnimation("gunIdle", new Animation<>(0.5f, gunIdleFrames));

        // Gun animation walk
        TextureRegion[] gunWalkFrames = {
            tmpGunFrames[1][18], tmpGunFrames[1][20], tmpGunFrames[1][2],
            tmpGunFrames[1][10], tmpGunFrames[1][15], tmpGunFrames[1][1]
        };
        animationManager.addAnimation("gunWalk", new Animation<>(0.5f, gunWalkFrames));

        // Gun animation fire
        TextureRegion[] gunFireFrames = {
            tmpGunFrames[0][1], tmpGunFrames[0][2], tmpGunFrames[0][3],
            tmpGunFrames[0][7], tmpGunFrames[0][8], tmpGunFrames[0][9]};
        animationManager.addAnimation("gunFire", new Animation<>(0.1f, gunFireFrames));

    }

    public void update(float delta, boolean isGrounded, boolean isMoving, boolean isFiring, boolean isFacingLeft) {
        this.isFiring = isFiring;
        this.isFacingLeft = isFacingLeft;
        animationManager.update(delta, isGrounded, isMoving, isFiring, 1);
    }

    public void render(SpriteBatch batch, float playerX, float playerY) {
        TextureRegion currentGunFrame = animationManager.getCurrentGunFrame(isFacingLeft);
        batch.draw(currentGunFrame, playerX, playerY, 32, 32);
    }

    public void fire() {
        isFiring = true;
    }

    public void stopFiring() {
        isFiring = false;
    }
}
