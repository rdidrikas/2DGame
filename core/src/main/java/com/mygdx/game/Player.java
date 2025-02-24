package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import org.lwjgl.Sys;

public class Player {

    private Body body;
    private Gun gun;

    private int tileSize;


    private float coyoteTime = 0.15f; // 150ms
    private float coyoteTimer = 0f;
    private boolean canJump = false;
    private boolean isJumping = false;

    private AnimationManager animationManager;

    private float playerX, playerY;
    private float width, height;
    private float stateTime;
    public boolean isOnGround;
    public boolean isMoving;
    public boolean isFiring;

    public boolean isFacingLeft = false;


    public Player(int tileSize, World world, float x, float y, float width, float height) {

        this.width = width;
        this.height = height;
        this.playerX = x;
        this.playerY = y;

        this.stateTime = 0;
        this.isOnGround = true;
        this.isMoving = false;
        this.isFiring = false;
        this.tileSize = tileSize;

        animationManager = new AnimationManager();
        loadAnimations();

        gun = new Gun();



        float collisionBoxWidth = width / 3.5f;
        float collisionBoxHeight = height / 1.5f;

        // Calculate offset to align collision box with sprite's feet
        float yOffset = (height - collisionBoxHeight) / 2;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y - yOffset);
        bodyDef.fixedRotation = true;

        body = world.createBody(bodyDef);
        body.setBullet(true);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(collisionBoxWidth / 2,
            collisionBoxHeight / 2);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 20f;
        fixtureDef.restitution = 0f;

        body.setLinearDamping(0.1f);

        fixtureDef.filter.categoryBits = Constants.PLAYER_CATEGORY;
        fixtureDef.filter.maskBits = Constants.TILE_CATEGORY;

        Fixture playerFixture = body.createFixture(fixtureDef);
        playerFixture.setUserData("player");

        shape.dispose();

    }

    private void loadAnimations() {
        Texture playerSheet = new Texture("Animations/RAMBO_anim.png");
        TextureRegion[][] tmpFrames = TextureRegion.split(playerSheet, 32, 32);

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
        gun.update(delta, isGrounded(), isMoving, isFiring, isFacingLeft); // render gun animation


        if (isGrounded()) {
            coyoteTimer = coyoteTime;
            canJump = true;
        } else {
            System.out.println(body.getLinearVelocity().y);
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
        gun.render(batch, x, y); // draw gun
    }


    public void setMoving(boolean moving) {
        isMoving = moving;
    }

    public void fire() {
        gun.fire(body.getWorld(), body.getPosition().x, body.getPosition().y, 0);
    }

    public void jump() {
        if (canJump) {
            // body.setGravityScale(0.15f);
            body.applyLinearImpulse(new Vector2(0, Constants.PLAYER_JUMP), body.getWorldCenter(), true);
            canJump = false;
            isJumping = true;
            coyoteTimer = 0;
        }

    }
    public void handleJumpRelease() {
        if (isJumping) {
            // body.setGravityScale(1.5f); // Example: faster fall
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



    public boolean isGrounded() {
        // Use velocity or other checks instead of collision flags
        return (Math.abs(body.getLinearVelocity().y) < 0.01f && Math.abs(body.getLinearVelocity().y) > -0.01f); // Near-zero vertical velocity
    }



}
