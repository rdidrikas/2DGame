package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Player {

    private Rectangle bounds;

    private CollisionChecker collisionChecker;
    private int tileSize;

    private int playerWidth = 32;
    private int playerHeight = 32;
    private int playerSpeed = 4;
    private int playerBoundBias = 10;

    private Animation<TextureRegion> walkAnimation;
    private Animation<TextureRegion> jumpAnimation;

    private TextureRegion[] idleFrames;
    private TextureRegion currentFrame;

    private float stateTime;
    public boolean isOnGround;
    private float velocityY;
    public boolean isMoving;


    public Player(TextureRegion[] idleFrames, TextureRegion[] walkFrames, TextureRegion[] jumpFrames, CollisionChecker collisionChecker, int tileSize) {
        this.bounds = new Rectangle(100, 300, playerWidth, playerHeight);
        this.walkAnimation = new Animation<>(0.1f, walkFrames);
        this.jumpAnimation = new Animation<>(0.2f, jumpFrames);
        this.idleFrames = idleFrames;
        this.stateTime = 0;
        this.isOnGround = false;
        this.velocityY = 0;
        this.isMoving = false;
        this.collisionChecker = collisionChecker;
        this.tileSize = tileSize;

    }

    public void update(float delta) {
        stateTime += delta;

        // Update player position based on velocity

        bounds.y += velocityY;

        // Apply gravity
        if(!isOnGround){
            velocityY -= 0.5f; // Gravity
        }

        // Check if player is colliding with the ground

        if (collisionChecker.isCollidingBelow(bounds.x, bounds.y, bounds.width)) {
            bounds.y = (int) (bounds.y / tileSize) * tileSize; // Snap to the top of the tile
            velocityY = 0; // Stop falling
            isOnGround = true;
        } else {
            isOnGround = false;
        }

        // Check for collisions above
        if (collisionChecker.isCollidingAbove(bounds.x, bounds.y, bounds.width, bounds.height)) {
            bounds.y = (int) ((bounds.y + bounds.height) / tileSize) * tileSize - bounds.height; // Snap to the bottom of the tile
            velocityY = 0; // Stop jumping
            System.out.println("ouch");
            isOnGround = true;
        }




    }

    public void render(SpriteBatch batch) {

        if (!isOnGround) {
            currentFrame = jumpAnimation.getKeyFrame(stateTime, true);
        } else if (isMoving) {
            currentFrame = walkAnimation.getKeyFrame(stateTime, true);
        } else {
            currentFrame = idleFrames[0]; // Idle animation
        }
        batch.draw(currentFrame, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void setMoving(boolean moving) {
        isMoving = moving;
    }


    public void jump() {
        if (isOnGround) {
            velocityY = 10;
            isOnGround = false;
        }
    }

    public void moveLeft() {
        float newX = bounds.x - playerSpeed;
        if (!collisionChecker.isCollidable(newX + playerBoundBias, bounds.y)) {
            bounds.x = newX;
        }
        isMoving = true;
    }

    public void moveRight() {
        float newX = bounds.x + playerSpeed;
        if (!collisionChecker.isCollidable(newX + bounds.width - playerBoundBias, bounds.y)) {
            bounds.x = newX;
        }
        isMoving = true;
    }

    public void reset() {
        bounds.x = 100;
        bounds.y = 300;
    }


}
