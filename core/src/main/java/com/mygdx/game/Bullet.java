package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import static java.lang.Math.abs;

public class Bullet {

    private AnimationManager animationManager;

    private Body body; // Box2D body for physics
    private boolean active = true; // Track if the bullet is active
    private Vector2 startPosition;

    private boolean applyOffset;

    public Bullet(World world, float x, float y, float angle, boolean isFacingLeft) {

        applyOffset = true;

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
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0f;

        // Set the bullet's collision category and with what it can collide
        fixtureDef.filter.categoryBits = Constants.BULLET_CATEGORY; // Bullet
        fixtureDef.filter.maskBits = Constants.TILE_CATEGORY; // Tile collision

        // Attach the fixture to the body
        Fixture bulletFixture = body.createFixture(fixtureDef);
        bulletFixture.setUserData("friendlyBullet");

        body.setGravityScale(0f);
        // body.applyLinearImpulse(new Vector2(Constants.RAMBO_BULLET_SPEED, 0), body.getWorldCenter(), true);

        if(isFacingLeft) body.setLinearVelocity(-Constants.RAMBO_BULLET_SPEED, 0);
        else body.setLinearVelocity(Constants.RAMBO_BULLET_SPEED, 0);

        body.setLinearDamping(0f);
        body.setAngularDamping(0f);
        body.setBullet(true);

        shape.dispose();

        // Set the bullet's velocity based on the angle

        System.out.println("Bullet Velocity: " + body.getLinearVelocity());

        // Store the starting position
        startPosition = new Vector2(x, y);

    }

    public void update(float delta, boolean isFacingLeft) {

        // Manual bullet speed
        //float bulletX = isFacingLeft ? body.getPosition().x - Constants.RAMBO_BULLET_SPEED * delta : Constants.RAMBO_BULLET_SPEED * delta + body.getPosition().x;
        //body.setTransform(bulletX, body.getPosition().y, 0);

        // Deactivate the bullet if it goes too far
        if (active) {
            Vector2 currentPosition = body.getPosition();
            float distanceTraveled = startPosition.dst(currentPosition);
            if (abs(distanceTraveled) > Constants.RAMBO_BULLET_DISTANCE) {
                this.active = false;
            }
            System.out.println("Bullet Velocity: " + body.getLinearVelocity());

        }

    }

    public void render(SpriteBatch batch, TextureRegion bulletTexture, boolean isFacingLeft) {
        if (active) {
            float x = body.getPosition().x - bulletTexture.getRegionWidth() / 2f;
            float y = body.getPosition().y - bulletTexture.getRegionHeight() / 2f - Constants.RAMBO_BULLET_YOFFSET;

            // Apply the offset only if the flag is true
            if (isFacingLeft) {
                if (applyOffset) {
                    x -= Constants.RAMBO_BULLET_XOFFSET; // Apply the offset to the left
                }
            } else {
                if (applyOffset) {
                    x += Constants.RAMBO_BULLET_XOFFSET; // Apply the offset to the right
                }
            }

            applyOffset = false;

            // Draw the bullet at its calculated position
            batch.draw(bulletTexture, x, y);
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


