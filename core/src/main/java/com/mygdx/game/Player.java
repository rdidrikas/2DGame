package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Player {

    // private Rectangle bounds;

    private Body body;

    private CollisionChecker collisionChecker;
    private int tileSize;



    private float coyoteTime = 0.15f; // 150ms
    private float coyoteTimer = 0f;
    private boolean canJump = false;
    private boolean isJumping = false;
    private float previousY = -1;

    private Animation<TextureRegion> walkAnimation;
    private Animation<TextureRegion> jumpAnimation;

    private TextureRegion[] idleFrames;
    private TextureRegion currentFrame;

    private float playerX, playerY;
    private float width, height;
    private float stateTime;
    public boolean isOnGround;
    public boolean isMoving;

    public boolean isFacingLeft = false;


    public Player(TextureRegion[] idleFrames, TextureRegion[] walkFrames, TextureRegion[] jumpFrames, CollisionChecker collisionChecker, int tileSize,
                  World world, float x, float y, float width, float height) {

        this.width = width;
        this.height = height;
        this.playerX = x;
        this.playerY = y;

        float collisionBoxWidth = width / 3.5f; // Example: 8 pixels
        float collisionBoxHeight = height / 1.5f; // Example: 16 pixels

        // Calculate offset to align collision box with sprite's feet
        float yOffset = (height - collisionBoxHeight) / 2;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y - yOffset);
        bodyDef.fixedRotation = true;

        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(collisionBoxWidth / 2,
            collisionBoxHeight / 2);

        this.walkAnimation = new Animation<>(0.1f, walkFrames);
        this.jumpAnimation = new Animation<>(0.3f, jumpFrames);
        this.idleFrames = idleFrames;
        this.stateTime = 0;
        this.isOnGround = true;
        this.isMoving = false;
        this.collisionChecker = collisionChecker;
        this.tileSize = tileSize;

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 20f;
        fixtureDef.restitution = 0f;

        Fixture playerFixture = body.createFixture(fixtureDef);
        playerFixture.setUserData("player");

        shape.dispose();

    }

    public void update(float delta) {

        stateTime += delta;
        if(body.getLinearVelocity().y < 10){
            if(isJumping){
                handleJumpRelease();
            }
            else {
                body.setGravityScale(1f);
            }
        }
        if (isGrounded()) {
            coyoteTimer = coyoteTime;
            canJump = true;
        } else {
            coyoteTimer -= delta;
            if (coyoteTimer <= 0) {
                canJump = false;
            }
        }


    }

    public void render(SpriteBatch batch) {

        float x = body.getPosition().x - width / 2;
        float y = body.getPosition().y + Constants.SPRITE_YOFFSET - height / 2;

        if (!isGrounded()) {
            currentFrame = jumpAnimation.getKeyFrame(stateTime, true);
        } else if (isMoving) {
            currentFrame = walkAnimation.getKeyFrame(stateTime, true);
        } else {
            currentFrame = idleFrames[0]; // Idle animation
        }
        if (isFacingLeft) {
            batch.draw(
                currentFrame,
                x + width,
                y,
                -width,
                height
            );
        } else {
            batch.draw(currentFrame, x, y, width, height);
        }
    }


    public void setMoving(boolean moving) {
        isMoving = moving;
    }


    public void jump() {
        if (canJump) {
            body.setGravityScale(0.15f);
            body.applyLinearImpulse(new Vector2(0, Constants.PLAYER_JUMP), body.getWorldCenter(), true);
            canJump = false;
            isJumping = true;
            coyoteTimer = 0;
        }

    }
    public void handleJumpRelease() {
        if (isJumping) {
            body.setGravityScale(1.5f); // Example: faster fall
            isJumping = false;
        }
    }
    public void moveLeft() {
        if(isGrounded()){
            body.setLinearVelocity(-Constants.PLAYER_SPEED, body.getLinearVelocity().y);
        }
        else {
            body.setLinearVelocity(-Constants.PLAYER_SPEED * Constants.PLAYER_SPEED_MID_AIR, body.getLinearVelocity().y);
        }
        isMoving = true;
        isFacingLeft = true;
    }

    public void moveRight() {
        if(isGrounded()){
            body.setLinearVelocity(Constants.PLAYER_SPEED, body.getLinearVelocity().y);
        }
        else {
            body.setLinearVelocity(Constants.PLAYER_SPEED * Constants.PLAYER_SPEED_MID_AIR, body.getLinearVelocity().y);
        }

        isMoving = true;
        isFacingLeft = false;
    }

    public void reset() {
        body.setTransform(100, 300, 0); // Reset position
        body.setLinearVelocity(0, 0); // Reset velocity
        isOnGround = false;
    }

    public Body getBody() {
        return body;
    }

    private boolean isGrounded() {
        // Use velocity or other checks instead of collision flags
        return Math.abs(body.getLinearVelocity().y) < 1f && Math.abs(body.getLinearVelocity().y) > -1f; // Near-zero vertical velocity
    }


}
