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

import static java.lang.Math.abs;

public class Enemy {

    private Body body;
    private Player player;

    private int health = 10;
    private boolean isMoving = false;
    private float width, height;
    public String currentState;

    private AnimationManager animationManager;
    private boolean enemyIsFacingLeft;

    public Enemy(World world, float x, float y, Player player) {

        enemyIsFacingLeft = false;
        isMoving = false;
        width = 32 / Constants.PPM;
        height = 32 / Constants.PPM;

        animationManager = new AnimationManager();

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        bodyDef.fixedRotation = true;
        body = world.createBody(bodyDef);

        // Define the enemy collision shape
        float collisionBoxWidth = width / 3.5f;
        float collisionBoxHeight = height / 1.5f;

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(collisionBoxWidth / 2,
            collisionBoxHeight / 2);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 10f;
        fixtureDef.restitution = 0f;

        fixtureDef.filter.categoryBits = Constants.ENEMY_CATEGORY;
        fixtureDef.filter.maskBits = Constants.TILE_CATEGORY;

        Fixture enemyNormal = body.createFixture(fixtureDef);
        enemyNormal.setUserData("enemyNormal");

        body.setGravityScale(1f);
        body.setLinearDamping(0f);

        shape.dispose();

        loadAnimations();
    }

    private void loadAnimations(){

        Texture playerSheet = new Texture("Animations/enemy_normal_anim.png");
        TextureRegion[][] tmpFrames = TextureRegion.split(playerSheet, 32, 32);

        Texture enemyGunSheet = new Texture("Animations/enemy_normal_gun_anim.png");
        TextureRegion[][] tmpGunFrames = TextureRegion.split(enemyGunSheet, 32, 32);

        Texture enemyBulletSheet = new Texture("Animations/enemy_normal_gun_anim.png");
        TextureRegion[][] tmpBulletFrames = TextureRegion.split(enemyBulletSheet, 16, 16);

        // Gun animation idle
        TextureRegion[] gunIdleFrames = {tmpGunFrames[0][0], tmpGunFrames[0][0], tmpGunFrames[0][0], tmpGunFrames[2][0],
            tmpGunFrames[2][1], tmpGunFrames[2][2], tmpGunFrames[2][3], tmpGunFrames[2][4] };
        // tmpGunFrames[2][5], tmpGunFrames[2][6], tmpGunFrames[3][0],
        // tmpGunFrames[3][1], tmpGunFrames[3][2], tmpGunFrames[3][3], tmpGunFrames[3][4], tmpGunFrames[3][5]};

        animationManager.addAnimation("enemyGunIdle", new Animation<>(8f, gunIdleFrames));

        // Gun animation walk
        TextureRegion[] gunWalkFrames = {
                tmpGunFrames[1][0], tmpGunFrames[1][2], tmpGunFrames[1][5],
                tmpGunFrames[1][10], tmpGunFrames[1][15], tmpGunFrames[1][3]
        };
        animationManager.addAnimation("enemyGunWalk", new Animation<>(0.5f, gunWalkFrames));

        // Gun animation fire
        TextureRegion[] gunFireFrames = {
                tmpGunFrames[0][1], tmpGunFrames[0][2], tmpGunFrames[0][3],
                tmpGunFrames[0][7], tmpGunFrames[0][8], tmpGunFrames[0][9]};
        animationManager.addAnimation("enemyGunFire", new Animation<>(0.1f, gunFireFrames));


        // Bullet animation
        TextureRegion[] bulletFrames = {tmpBulletFrames[0][0], tmpBulletFrames[0][1], tmpBulletFrames[0][2]};
        animationManager.addAnimation("enemyBullet", new Animation<>(0.5f, bulletFrames));


        // Idle (single frame)
        TextureRegion[] idleFrames = { tmpFrames[0][0] };
        animationManager.addAnimation("enemyNormalIdle", new Animation<>(0.1f, idleFrames));

        // Walk (loop)
        TextureRegion[] walkFrames = { tmpFrames[0][1], tmpFrames[0][2], tmpFrames[0][3] };
        animationManager.addAnimation("enemyNormalWalk", new Animation<>(0.3f, walkFrames));

    }

    public void update(float delta, Vector2 playerPosition){
        if (seesPlayer(playerPosition)) {
            moveTowardsPlayer(delta, playerPosition);
        } else {
            isMoving = false;
            body.setLinearVelocity(0, 0); // Stop moving
        }
        animationManager.update(delta, isGroundedEnemy(), isMoving, false, 2);
        animationManager.update(delta, isGroundedEnemy(), isMoving, false, 3);


    }

    public void render(SpriteBatch batch) {


        float x = body.getPosition().x - width / 2;
        float y = body.getPosition().y - height / 2;

        TextureRegion currentEnemyFrame = animationManager.getCurrentPlayerFrame(enemyIsFacingLeft);
        TextureRegion currentGunFrame = animationManager.getCurrentGunFrame(enemyIsFacingLeft);
        batch.draw(currentEnemyFrame, x, y, 32 / Constants.PPM, 32 / Constants.PPM);
        batch.draw(currentGunFrame, x, y, 32 / Constants.PPM, 32 / Constants.PPM);
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) destroy();
    }

    private void destroy() {
        // Remove the enemy from the world
    }

    public boolean seesPlayer(Vector2 playerPosition) {
        float detectionRadius = 3f; // Meters
        return body.getPosition().dst(playerPosition) <= detectionRadius;
    }

    public void moveTowardsPlayer(float delta, Vector2 playerPosition) {
        if (playerPosition.x < body.getPosition().x) {
            body.setLinearVelocity((isGroundedEnemy() ? -Constants.ENEMY_SPEED * Constants.PLAYER_SPEED_MID_AIR : -Constants.ENEMY_SPEED), body.getLinearVelocity().y);
            enemyIsFacingLeft = true;
        } else {
            body.setLinearVelocity((isGroundedEnemy() ? Constants.ENEMY_SPEED * Constants.PLAYER_SPEED_MID_AIR : Constants.ENEMY_SPEED), body.getLinearVelocity().y);
            enemyIsFacingLeft = false;
        }
        isMoving = true;
    }

    public boolean isGroundedEnemy() {
        // Use velocity or other checks instead of collision flags
        return (abs(body.getLinearVelocity().y) < 0.01f && abs(body.getLinearVelocity().y) > -0.01f); // Near-zero vertical velocity
    }

    private boolean hasLineOfSight() {
        float xDiff = player.getBody().getPosition().x - body.getPosition().x;
        return abs(xDiff) < 0.2;
    }

    private void detectPlayer() {

        float detectionRadius = 3f; // Meters
        float distance = body.getPosition().dst(player.getBody().getPosition());

        if (distance <= detectionRadius && hasLineOfSight()) {
            //currentState = State.ATTACK;
        } else {
            // currentState = State.PATROL;
        }
    }

}
