package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;

public class Bullet {

    private AnimationManager animationManager;

    private Body body; // Box2D body for physics
    private boolean active = true; // Track if the bullet is active
    private float speed = 500f; // Bullet speed

    public Bullet(World world, float x, float y, float angle, Texture bulletSheet) {

        animationManager = new AnimationManager();
        loadBullet(bulletSheet);

        // Create a Box2D body for the bullet
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        body = world.createBody(bodyDef);

        // Load Animation


        // Define the bullet's collision shape
        CircleShape shape = new CircleShape();
        shape.setRadius(0.1f); // Small bullet radius

        // Define the bullet's fixture
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.1f;
        fixtureDef.friction = 0f;
        fixtureDef.restitution = 0f;

        // Attach the fixture to the body
        body.createFixture(fixtureDef);
        shape.dispose();

        // Set the bullet's velocity based on the angle
        body.setLinearVelocity(
            (float) Math.cos(angle) * speed,
            (float) Math.sin(angle) * speed
        );

        // Set user data for collision detection
        body.setUserData(this);
    }

    public void loadBullet(Texture bulletSheet){

        TextureRegion[][] tmpBulletFrames = TextureRegion.split(bulletSheet, 16, 16);

        // Bullet animation
        TextureRegion[] bulletFrames = {tmpBulletFrames[0][0], tmpBulletFrames[0][1], tmpBulletFrames[0][2]};
        animationManager.addAnimation("bullet", new Animation<>(0.5f, bulletFrames));

    }

    public void update(float delta) {
        // Deactivate the bullet if it goes off-screen
        if (body.getPosition().x < 0 || body.getPosition().x > Gdx.graphics.getWidth() ||
            body.getPosition().y < 0 || body.getPosition().y > Gdx.graphics.getHeight()) {
            active = false;
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

    public void setActive(boolean active) {
        this.active = active;
    }

    public Body getBody() {
        return body;
    }
}


