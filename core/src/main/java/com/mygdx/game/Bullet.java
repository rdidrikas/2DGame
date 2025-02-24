package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Bullet {

    private AnimationManager animationManager;

    private Body body; // Box2D body for physics
    private boolean active = true; // Track if the bullet is active
    private Vector2 startPosition;

    public Bullet(World world, float x, float y, float angle, Texture bulletSheet) {

        // Create a Box2D body for the bullet
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        body = world.createBody(bodyDef);

        // Define the bullet's collision shape
        CircleShape shape = new CircleShape();
        shape.setRadius(0.1f); // Small bullet radius

        // Define the bullet's fixture
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0f;
        fixtureDef.friction = 0f;
        fixtureDef.restitution = 0f;

        // Set the bullet's collision category and with what it can collide
        fixtureDef.filter.categoryBits = Constants.BULLET_CATEGORY; // Bullet
        fixtureDef.filter.maskBits = Constants.TILE_CATEGORY; // Tile collision

        // Attach the fixture to the body
        Fixture bulletFixture = body.createFixture(fixtureDef);
        bulletFixture.setUserData("friendlyBullet");

        body.setGravityScale(0);

        shape.dispose();

        // Set the bullet's velocity based on the angle
        body.setLinearVelocity(
            Constants.RAMBO_BULLET_SPEED,
            0
        );
        // Store the starting position
        startPosition = new Vector2(x, y);

    }

    public void update(float delta) {
        // Deactivate the bullet if it goes too far
        if (active) {
            Vector2 currentPosition = body.getPosition();
            float distanceTraveled = startPosition.dst(currentPosition);
            if (distanceTraveled > Constants.RAMBO_BULLET_DISTANCE) {
                this.active = false;
            }
        }

    }

    public void render(SpriteBatch batch, TextureRegion bulletTexture) {
        if (active) {
            // Draw the bullet at its current position
            batch.draw(
                bulletTexture,
                body.getPosition().x - bulletTexture.getRegionWidth() / 2f,
                body.getPosition().y - bulletTexture.getRegionHeight() / 2f
            );
        }
    }

    public boolean isActive() {
        return active;
    }

    public void markForRemoval(){
        active = false;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Body getBody() {
        return body;
    }
}


