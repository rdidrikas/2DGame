package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import org.lwjgl.Sys;

import java.util.Set;

public class Player {

    private Body body;
    private Gun gun;

    private float coyoteTime = 0.15f; // 150ms
    private float coyoteTimer = 0f;
    private boolean canJump = false;
    private boolean isJumping = false;

    private AnimationManager animationManager;
    public PlayingState playingState;

    private float playerX, playerY;
    private float width, height;
    private float stateTime;
    public boolean isOnGround;
    public boolean isMoving;
    public boolean isFiring;
    public boolean isShot;
    public boolean isFacingLeft = false;
    public boolean levelCompleted;

    public float deathTimer = Constants.PLAYER_DEATH_DURATION;
    public Set<Body> bulletsToRemove;



    public Player(World world, float x, float y, float width, float height, Set<Body> bulletsToRemove, PlayingState playingState) {

        this.width = width;
        this.height = height;
        this.playerX = x;
        this.playerY = y;

        this.stateTime = 0;
        this.isOnGround = true;
        this.isMoving = false;
        this.isFiring = false;
        this.isShot = false;
        this.levelCompleted = false;

        this.playingState = playingState;
        this.bulletsToRemove = bulletsToRemove;

        animationManager = new AnimationManager();
        loadAnimations();

        gun = new Gun(bulletsToRemove);

        float collisionBoxWidth = width / 3.5f;
        float collisionBoxHeight = height / 1.8f;

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
        fixtureDef.friction = 0.000001f;
        fixtureDef.restitution = 0f;

        //body.setLinearDamping(0f);
        //body.setAngularDamping(0f);

        fixtureDef.filter.categoryBits = Constants.PLAYER_CATEGORY;
        fixtureDef.filter.maskBits = Constants.TILE_CATEGORY | Constants.ENEMY_BULLET_CATEGORY;

        Fixture playerFixture = body.createFixture(fixtureDef);
        playerFixture.setUserData(this);

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

        // Wall slide (single frame)
        TextureRegion[] wallSlideFrames1 = { tmpFrames[0][11], tmpFrames[0][12], tmpFrames[0][13] };
        animationManager.addAnimation("wallSlide1", new Animation<>(0.1f, wallSlideFrames1));
        TextureRegion[] wallSlideFrames2 = { tmpFrames[0][14], tmpFrames[0][15], tmpFrames[0][16] };
        animationManager.addAnimation("wallSlide2", new Animation<>(0.1f, wallSlideFrames2));

        // Player dead
        TextureRegion[] deadFrames = { tmpFrames[7][11]};
        deadFrames[0].flip(true, false); // Flip the frame
        animationManager.addAnimation("dead", new Animation<>(0.1f, deadFrames));

    }


    public void update(float delta) {

        if (body.getPosition().y < 0.1) {
            isShot = true;
            playingState.resetPosition();
            reset();
        }

        if (isShot) {
            deathTimer -= delta;
            if (deathTimer <= 0) {
                isShot = false; // Unlock player
                playingState.resetPosition();
                reset(); // Reset player position
            }
        }

        stateTime += delta;

        if(!isMoving){
            body.setLinearVelocity(0, body.getLinearVelocity().y); // Prevents sliding
        }

        animationManager.update(delta, isGrounded(), isMoving, isFiring, isShot,false, 0); // player render animation
        gun.update(delta, isGrounded(), isMoving, isFiring, isFacingLeft); // render gun animation


        if (isGrounded()) {
            coyoteTimer = coyoteTime;
            canJump = true;
        } else {
            // System.out.println(body.getLinearVelocity().y);
            coyoteTimer -= delta;
            if (coyoteTimer <= 0) {
                canJump = false;
            }
        }

        isFiring = false;

    }

    public void render(SpriteBatch batch) {

        float x = body.getPosition().x - width / 2;
        float y = body.getPosition().y + Constants.SPRITE_YOFFSET - height / 2;

        TextureRegion currentPlayerFrame = animationManager.getCurrentPlayerFrame(isFacingLeft);
        batch.draw(currentPlayerFrame, x, y, width, height);

        if (!isShot) gun.render(batch, x, y); // draw gun

    }

    public void fire() {
        isFiring = true;
        gun.fire(body.getWorld(), body.getPosition().x, body.getPosition().y, 0);
    }

    public void jump() {
        if (canJump) {

            body.setLinearVelocity(body.getLinearVelocity().x, Constants.PLAYER_JUMP);
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

        // body.setLinearVelocity(0, 0);
        isShot = false;
        isOnGround = isGrounded();
        canJump = false;
        coyoteTimer = 0;
    }

    public Body getBody() {
        return body;
    }

    public boolean isGrounded() {
        // Use velocity or other checks instead of collision flags
        return (Math.abs(body.getLinearVelocity().y) < 0.01f && Math.abs(body.getLinearVelocity().y) > -0.01f); // Near-zero vertical velocity
    }

    public void dead (){
        this.isShot = true;
    }


    public void dispose() {
        gun.dispose();
    }
}
