package com.mygdx.game;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Texture;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

public class PlayingState extends GameState {

    private World world;
    private Box2DDebugRenderer debugRenderer;
    private Player player;
    private TextureRegion[][] dirtTiles;

    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;

    private int tileSize = 16;

    private boolean wasSpacePressed = false;

    // Removing bullets inside world.step() causes a crash
    private Array<Body> bulletsToRemove = new Array<>();


    public PlayingState(GameStateManager gsm) {
        super(gsm);

        world = new World(new Vector2(0, Constants.GRAVITY), true);
        world.setContinuousPhysics(true);
        debugRenderer = new Box2DDebugRenderer();

        // Load map
        map = new TmxMapLoader().load("Map/map.tmx");
        renderer = new OrthogonalTiledMapRenderer(map);


        createCollisionTiles(); // Loads collision tiles from the map



        player = new Player (tileSize, world, 100, 300, 32, 32);

        camera = new OrthographicCamera();
        camera.zoom = 0.3f;
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void update(float delta) {

        world.step(delta, 8, 3);

        // Fixture body remover
        for (Body bullet : bulletsToRemove) {
            world.destroyBody(bullet); // Destroy the body (and its fixtures)
        }
        bulletsToRemove.clear(); // Clear the queue


        /*************** Collisions ***************/

        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {

                // Colliding objects:
                Fixture fixtureA = contact.getFixtureA();
                Fixture fixtureB = contact.getFixtureB();

                Object userDataA = fixtureA.getUserData();
                Object userDataB = fixtureB.getUserData();

                if (userDataA.equals("friendlyBullet") && userDataB.equals("ground")) {
                    handleBulletTileCollision(fixtureA);
                } else if (userDataA.equals("ground") && userDataB.equals("friendlyBullet")) {
                    handleBulletTileCollision(fixtureB);
                }

            }

            @Override
            public void endContact(Contact contact) {

            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
                // Handle pre-solve
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
                // Handle post-solve
            }
        });

        float playerX = player.getBody().getPosition().x;
        float playerY = player.getBody().getPosition().y;
        handleInput();
        player.update(delta);

        // Update camera position

        if (playerX < 200) {
            camera.position.set(
                200,
                playerY,
                0
            );
        } else {
            camera.position.set(
                playerX,
                playerY,
                0
            );
        }

        camera.update();
    }

    @Override
    public void render(SpriteBatch batch) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(camera.combined);
        camera.update();
        batch.begin();

        // Render tile map
        renderer.setView(camera);
        renderer.render();

        // Render player
        player.update(Gdx.graphics.getDeltaTime());
        player.render(batch);
        batch.end();

        debugRenderer.render(world, camera.combined);
    }

    public void handleBulletTileCollision(Fixture bulletFixture) {
        // Remove bullet
        Bullet bullet = (Bullet) bulletFixture.getBody().getUserData();
        if (bullet != null) {
            bullet.markForRemoval();
        }
        removeBulletsQueue(bulletFixture);
    }

    private void handleInput() {
        player.isMoving = false;
        boolean isSpacePressed = Gdx.input.isKeyPressed(Input.Keys.SPACE);

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            player.jump();
        }
        if (wasSpacePressed && !isSpacePressed) {
            // Key was released this frame
            player.handleJumpRelease();
        }

        // Update previous state for next frame
        wasSpacePressed = isSpacePressed;

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            player.moveLeft();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            player.moveRight();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            player.reset();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.J)){
            player.fire();
        }

    }


    private void createCollisionTiles() {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("Solid");
        for (int y = 0; y < layer.getHeight(); y++) {
            for (int x = 0; x < layer.getWidth(); x++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell != null) {
                    // Create a static Box2D body for this tile
                    BodyDef bodyDef = new BodyDef();
                    bodyDef.type = BodyDef.BodyType.StaticBody;
                    bodyDef.position.set(
                        (x + 0.5f) * layer.getTileWidth(), // Center of tile
                        (y + 0.5f) * layer.getTileHeight()
                    );

                    Body body = world.createBody(bodyDef);
                    PolygonShape shape = new PolygonShape();
                    shape.setAsBox(
                        (float) layer.getTileWidth() / 2,
                        (float) layer.getTileHeight() / 2
                    );

                    FixtureDef fixtureDef = new FixtureDef();
                    fixtureDef.shape = shape;
                    fixtureDef.density = 0.0f; // Static body
                    fixtureDef.friction = 0.5f;

                    fixtureDef.filter.categoryBits = Constants.TILE_CATEGORY; // Tile category
                    // Collide with player and bullets
                    fixtureDef.filter.maskBits = Constants.BULLET_CATEGORY | Constants.PLAYER_CATEGORY;


                    Fixture groundFixture = body.createFixture(fixtureDef);
                    groundFixture.setUserData("ground");

                    shape.dispose();
                }
            }
        }
    }

    public void removeBulletsQueue(Fixture fixture) {
        Body bulletBody = fixture.getBody();
        bulletsToRemove.add(bulletBody);
    }



    @Override
    public void dispose() {
        world.dispose();
        debugRenderer.dispose();
    }
}
