package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import static java.lang.Math.abs;
import static java.lang.Math.acos;

public class EnemyBullet {

    private Body body;
    private boolean active;
    private float aliveTime = 0;
    private Vector2 direction;


    public EnemyBullet(World world, float x, float y, boolean isFacingLeft, Vector2 direction) {

        this.direction = direction.nor();
        this.active = true;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x + (isFacingLeft ? -Constants.RAMBO_BULLET_XOFFSET : Constants.RAMBO_BULLET_XOFFSET),
            y - Constants.RAMBO_BULLET_YOFFSET);
        body = world.createBody(bodyDef);

        CircleShape shape = new CircleShape();
        shape.setRadius(0.1f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0f;
        fixtureDef.filter.categoryBits = Constants.ENEMY_BULLET_CATEGORY;
        fixtureDef.filter.maskBits = Constants.PLAYER_CATEGORY | Constants.TILE_CATEGORY;

        Fixture enemyBulletFixture = body.createFixture(fixtureDef);
        enemyBulletFixture.setUserData(this);

        body.setGravityScale(0f);
        body.setBullet(true);

        body.setLinearVelocity((isFacingLeft ? -Constants.ENEMY_BULLET_SPEED : Constants.ENEMY_BULLET_SPEED), 0);

        shape.dispose();

    }

    public void update(float delta) {
        if (!active) return;

        aliveTime += delta;

        if (aliveTime > Constants.ENEMY_BULLET_ALIVE_TIME) {
            markForRemoval();
        }
    }

    public void render(SpriteBatch batch, TextureRegion bulletTexture, boolean isFacingLeft) {

        if (active) {
            System.out.println("Bullet is active");

            float newHeight = bulletTexture.getRegionHeight() / Constants.PPM;
            float newWidth = bulletTexture.getRegionWidth() / Constants.PPM;

            float x = body.getPosition().x - newWidth / 2f;
            float y = body.getPosition().y - newHeight / 2f;

            // Draw the bullet at its calculated position
            batch.draw(bulletTexture, x, y, newWidth, newHeight);
        }
    }
    public void markForRemoval() {
        active = false;
        if (body != null) {
            body.getWorld().destroyBody(body);
            body = null;
        }
    }

    public boolean isActive() {
        return active;
    }


}
