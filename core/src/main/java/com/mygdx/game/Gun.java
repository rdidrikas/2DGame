package com.mygdx.game;

import java.util.LinkedList;
import java.util.Iterator;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;


public class Gun {

    private LinkedList<Bullet> bullets = new LinkedList<>();
    private Texture bulletSheet = new Texture("Animations/Bullet Friendly.png");

    private AnimationManager animationManager;
    private boolean isFiring;
    private boolean isFacingLeft;

    private float shotTimer = 0f;

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

        TextureRegion[][] tmpBulletFrames = TextureRegion.split(bulletSheet, 16, 16);

        // Bullet animation
        TextureRegion[] bulletFrames = {tmpBulletFrames[0][0], tmpBulletFrames[0][1], tmpBulletFrames[0][2]};
        animationManager.addAnimation("bullet", new Animation<>(0.5f, bulletFrames));

    }

    public void update(float delta, boolean isGrounded, boolean isMoving, boolean isFiring, boolean isFacingLeft) {
        this.isFiring = isFiring;
        this.isFacingLeft = isFacingLeft;

        if (shotTimer > 0) {
            shotTimer -= delta;
        }

        animationManager.update(delta, isGrounded, isMoving, isFiring, 1);

        Iterator<Bullet> iterator = bullets.iterator();
        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            bullet.update(delta, isFacingLeft);
            if (!bullet.isActive()) {
                iterator.remove();
            }
        }

    }

    public void render(SpriteBatch batch, float playerX, float playerY) {
        TextureRegion currentGunFrame = animationManager.getCurrentGunFrame(isFacingLeft);
        batch.draw(currentGunFrame, playerX, playerY, 32 / Constants.PPM, 32 / Constants.PPM);
        for (Bullet bullet : bullets) {
            bullet.render(batch, animationManager.getBulletFrame(), isFacingLeft);
        }
    }

    public void fire(World world, float x, float y, float angle) {
        if(shotTimer <= 0){
            isFiring = true;
            bullets.add(new Bullet(world, x, y, angle, isFacingLeft));
            shotTimer = Constants.RAMBO_SHOT_COOLDOWN;
        }

    }

    public void stopFiring() {
        isFiring = false;
    }
}
