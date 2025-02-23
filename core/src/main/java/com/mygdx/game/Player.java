package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;

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

    private AnimationManager animationManager;

    private Animation<TextureRegion> walkAnimation;
    private Animation<TextureRegion> jumpAnimation;

    private TextureRegion[] idleFrames;
    private TextureRegion currentFrame;
    private TextureRegion[] walkFrames;
    private TextureRegion[] jumpFrames;

    private float playerX, playerY;
    private float width, height;
    private float stateTime;
    public boolean isOnGround;
    public boolean isMoving;
    public boolean isFiring;

    public boolean isFacingLeft = false;


    public Player(TextureRegion[] idleFrames, TextureRegion[] walkFrames, TextureRegion[] jumpFrames, CollisionChecker collisionChecker, int tileSize,
                  World world, float x, float y, float width, float height) {

        this.width = width;
        this.height = height;
        this.playerX = x;
        this.playerY = y;

        animationManager = new AnimationManager();
        loadAnimations();

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
        this.walkFrames = walkFrames;
        this.jumpFrames = jumpFrames;
        this.stateTime = 0;
        this.isOnGround = true;
        this.isMoving = false;
        this.isFiring = false;
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

    private void loadAnimations() {
        Texture playerSheet = new Texture("Animations/RAMBO_anim.png");
        TextureRegion[][] tmpFrames = TextureRegion.split(playerSheet, 32, 32);

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

        // Idle (single frame)
        TextureRegion[] idleFrames = { tmpFrames[0][0] };
        animationManager.addAnimation("idle", new Animation<>(0.1f, idleFrames));

        // Walk (loop)
        TextureRegion[] walkFrames = { tmpFrames[0][1], tmpFrames[0][2], tmpFrames[0][3] };
        animationManager.addAnimation("walk", new Animation<>(0.3f, walkFrames));

        // Jump (6 frames, no loop)
        TextureRegion[] jumpFrames = new TextureRegion[6];
        for (int i = 6, temp = 0; i < 12; i++, temp++) {
            jumpFrames[temp] = tmpFrames[2][i];
        }
        animationManager.addAnimation("jump", new Animation<>(0.2f, jumpFrames));
    }


    public void update(float delta) {

        stateTime += delta;

        animationManager.update(delta, isGrounded(), isMoving, isFiring, 0); // player render animation
        animationManager.update(delta, isGrounded(), isMoving, isFiring, 1); // gun render animation

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

        TextureRegion currentPlayerFrame = animationManager.getCurrentPlayerFrame(isFacingLeft);
        batch.draw(currentPlayerFrame, x, y, width, height);

        TextureRegion currentGunFrame = animationManager.getCurrentGunFrame(isFacingLeft);
        batch.draw(currentGunFrame, x, y, width, height);
    }


    public void setMoving(boolean moving) {
        isMoving = moving;
    }


    public void fire(){
        isFiring = true;
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
