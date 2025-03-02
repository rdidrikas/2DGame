package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import java.util.Iterator;
import java.util.LinkedList;

import static java.lang.Math.abs;

public class Enemy {

    private Body body;
    private Player player;
    private World world;

    private Vector2 initialPosition;

    private int health = 10;
    public boolean isMoving;
    private float width, height;

    private enum State { PATROL, ATTACK, DEAD }
    private State currentState = State.PATROL;
    private Vector2 patrolTarget;
    private float shootTimer = 0;
    public boolean isFiring;
    public boolean isShot;
    public boolean alreadyRendered;
    private float patrolCooldown = 0;
    public float deathTimer;
    private float reactionTime = Constants.ENEMY_DETECTION_REACTION;

    private AnimationManager animationManager;
    private boolean enemyIsFacingLeft;

    public LinkedList<EnemyBullet> bullets = new LinkedList<EnemyBullet>();

    public Enemy(World world, float x, float y, Player player) {

        this.initialPosition = new Vector2(x, y);

        this.player = player;
        this.world = world;
        this.isMoving = false;
        this.isShot = false;
        this.isFiring = false;
        this.alreadyRendered = false;
        this.deathTimer = Constants.ENEMY_DEATH_TIMER;

        enemyIsFacingLeft = false;
        width = 32 / Constants.PPM;
        height = 32 / Constants.PPM;

        animationManager = new AnimationManager();

        // Define the enemy collision shape
        float collisionBoxWidth = width / 3.5f;
        float collisionBoxHeight = height / 1.8f;

        // Calculate offset to align collision box with sprite's feet
        float yOffset = (height - collisionBoxHeight) / 2;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y - yOffset);
        bodyDef.fixedRotation = true;
        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(collisionBoxWidth / 2,
            collisionBoxHeight / 2 );

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 10f;
        fixtureDef.restitution = 0f;

        fixtureDef.filter.categoryBits = Constants.ENEMY_CATEGORY;
        fixtureDef.filter.maskBits = Constants.TILE_CATEGORY | Constants.BULLET_CATEGORY;

        Fixture enemyNormal = body.createFixture(fixtureDef);
        // enemyNormal.setUserData("enemyNormal");
        enemyNormal.setUserData(this);

        body.setGravityScale(1f);
        body.setLinearDamping(0f);

        shape.dispose();

        loadAnimations();
        setRandomPatrolTarget();

    }

    private void loadAnimations(){

        Texture playerSheet = new Texture("Animations/enemy_normal_anim.png");
        TextureRegion[][] tmpFrames = TextureRegion.split(playerSheet, 32, 32);

        Texture enemyGunSheet = new Texture("Animations/enemy_normal_gun_anim.png");
        TextureRegion[][] tmpGunFrames = TextureRegion.split(enemyGunSheet, 32, 32);

        Texture enemyBulletSheet = new Texture("Animations/Bullet Friendly.png");
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

        // Enemy shot animation
        TextureRegion[] enemyShotFrames = new TextureRegion[15];
        for (int i = 16, temp = 0; i < 31; i++, temp++) {
            enemyShotFrames[temp] = tmpFrames[11][i];
        }

        Animation<TextureRegion> shotAnimation = new Animation<>(0.3f, enemyShotFrames);
        shotAnimation.setPlayMode(Animation.PlayMode.NORMAL); // play once
        animationManager.addAnimation("enemyNormalShot", shotAnimation);


        // Enemy dead
        TextureRegion[] enemyDeadFrames = {tmpFrames[11][30]};
        animationManager.addAnimation("enemyNormalDead", new Animation<>(0.1f, enemyDeadFrames));

    }

    public void update(float delta, Vector2 playerPosition) {

        for (EnemyBullet bullet : bullets) {
            bullet.update(delta);
        }

        if(currentState != State.DEAD) {
            if (isMoving && currentState == State.PATROL && body.getLinearVelocity().x == 0) {
                setRandomPatrolTarget();
            }

            detectPlayer(delta);
            handleState(delta);
            animationManager.update(delta, isGroundedEnemy(), isMoving, isFiring, isShot, 3);
        }

        animationManager.update(delta, isGroundedEnemy(), isMoving, isFiring, isShot,2);


        Iterator<EnemyBullet> iterator = bullets.iterator();
        while (iterator.hasNext()) {
            EnemyBullet bullet = iterator.next();
            if (bullet.isMarkedForRemoval()) {
                iterator.remove();
                // System.out.println("Bullet removed here");
            }
        }

        if(isShot){
            deathTimer -= delta;
            if(deathTimer <= 0){
                alreadyRendered = true;
            }
        }

    }

    public void render(SpriteBatch batch) {

        if (currentState == State.DEAD && alreadyRendered) {
            return;
        }

        float x = body.getPosition().x - width / 2;
        float y = body.getPosition().y - height / 2 + Constants.SPRITE_YOFFSET;

        TextureRegion currentEnemyFrame = animationManager.getCurrentPlayerFrame(enemyIsFacingLeft);

        if (currentState == State.DEAD) batch.draw(currentEnemyFrame, x, y - Constants.SPRITE_YOFFSET, 32 / Constants.PPM, 32 / Constants.PPM); // Need to offset the sprite
        else batch.draw(currentEnemyFrame, x, y, 32 / Constants.PPM, 32 / Constants.PPM);

        if (currentState != State.DEAD) {
            TextureRegion currentGunFrame = animationManager.getCurrentGunFrame(enemyIsFacingLeft);
            batch.draw(currentGunFrame, x, y, 32 / Constants.PPM, 32 / Constants.PPM);
        }

        for (EnemyBullet bullet : bullets) {
            bullet.render(batch, animationManager.getBulletFrame("enemyBullet"), enemyIsFacingLeft);
        }
    }

    private void destroy() {
        // Remove the enemy from the world
    }

    public boolean isGroundedEnemy() {
        // Use velocity or other checks instead of collision flags
        return (abs(body.getLinearVelocity().y) < 0.01f && abs(body.getLinearVelocity().y) > -0.01f); // Near-zero vertical velocity
    }

    private boolean hasLineOfSight() {
        float xDiff = player.getBody().getPosition().y - body.getPosition().y;
        return abs(xDiff) < 0.2;
    }

    private void detectPlayer(float delta) {

        float detectionRadius = 3f; // Meters
        float distance = body.getPosition().x - player.getBody().getPosition().x;

        if (abs(distance) <= detectionRadius && hasLineOfSight()) {
            if((distance > 0 && enemyIsFacingLeft) || (distance < 0 && !enemyIsFacingLeft) || distance == 0) {
                reactionTime -= delta;
                if (reactionTime <= 0) {
                    currentState = State.ATTACK;
                }
            }
        } else {
            if(currentState != State.ATTACK){
                currentState = State.PATROL;
            } else {
                reactionTime = Constants.ENEMY_DETECTION_REACTION;
            }
        }
    }

    private void handleState(float delta) {
        switch (currentState) {
            case PATROL:
                patrol(delta);
                break;
            case ATTACK:
                attack(delta);
                break;
        }
    }

    private void setRandomPatrolTarget() {
        patrolCooldown = 10f;
        patrolTarget = new Vector2(
            body.getPosition().x + MathUtils.random(-3, 3),
            body.getPosition().y
        );
    }

    private void patrol(float delta) {
        // Basic patrol logic

        patrolCooldown -= delta;
        isFiring = false;

        if (body.getPosition().dst(patrolTarget) < 0.2f) {
            isMoving = false;
            if(patrolCooldown <= 0){
                setRandomPatrolTarget();
            }
        } else {
            Vector2 direction = new Vector2(patrolTarget).sub(body.getPosition()).nor();
            body.setLinearVelocity(Constants.ENEMY_SPEED * direction.x, body.getLinearVelocity().y);
            enemyIsFacingLeft = direction.x < 0;
            isMoving = true;
        }

    }

    private void attack(float delta) {
        // Stop moving when attacking
        body.setLinearVelocity(0, 0);
        isMoving = false;
        enemyIsFacingLeft = player.getBody().getPosition().x <= body.getPosition().x; // Face Player

        shootTimer -= delta;
        if (shootTimer <= 0) {
            shoot();
            shootTimer = Constants.ENEMY_SHOT_COOLDOWN;
            // System.out.println("Enemy shot");
        }
    }

    public void shoot(){

        isFiring = true;

        Vector2 playerPos = player.getBody().getPosition();
        boolean playerIsLeft = playerPos.x < body.getPosition().x;

        // Create projectile (similar to player bullets)
        float offsetX = enemyIsFacingLeft ? -0.05f : 0.05f;
        Vector2 spawnPos = body.getPosition().cpy().add(offsetX, 0);

        bullets.add(new EnemyBullet(world, spawnPos.x, spawnPos.y, enemyIsFacingLeft, playerIsLeft));
    }

    public void dead(){
        if(!isShot) {
            currentState = State.DEAD;
            isShot = true;
            animationManager.stateTime = 0;
        }
    }

    public void cleanup() {
        // Only need to destroy body, no state reset needed
        if(body != null) {
            world.destroyBody(body);
            body = null;
        }

        // Cleanup bullets
        for(EnemyBullet bullet : bullets) {
            bullet.markForRemoval();
        }
        bullets.clear();
    }

}
