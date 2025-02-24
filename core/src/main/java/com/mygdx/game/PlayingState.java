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
        debugRenderer = new Box2DDebugRenderer();

        map = new TmxMapLoader().load("Map/map.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1 / Constants.PPM);
        // *** PPM CHANGE: Scale map renderer to meters

        createCollisionTiles();

        // Initialize player with METER-based position/size
        player = new Player(
            tileSize,
            world,
            100 / Constants.PPM,  // X position (meters)
            300 / Constants.PPM,  // Y position (meters)
            32 / Constants.PPM,   // Width (meters)
            32 / Constants.PPM    // Height (meters)
        );

        // Set camera viewport to METERS
        camera = new OrthographicCamera();
        camera.setToOrtho(
            false,
            Gdx.graphics.getWidth() / Constants.PPM,  // Viewport width (meters)
            Gdx.graphics.getHeight() / Constants.PPM  // Viewport height (meters)
        );
        camera.zoom = 0.3f; // *** PPM CHANGE: Zoom out camera
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

                    System.out.println("Bullet hit ground");
                } else if (userDataA.equals("ground") && userDataB.equals("friendlyBullet")) {
                    handleBulletTileCollision(fixtureB);
                    System.out.println("Bullet hit ground");
                }
                else if(userDataA.equals("player") && userDataB.equals("ground")){
                    //System.out.println("Player hit ground");
                } else if (userDataA.equals("ground") && userDataB.equals("player")) {
                    //System.out.println("Player hit ground");
                }

            }

            @Override
            public void endContact(Contact contact) {
                Fixture fixtureA = contact.getFixtureA();
                Fixture fixtureB = contact.getFixtureB();

                Object userDataA = fixtureA.getUserData();
                Object userDataB = fixtureB.getUserData();



                if(userDataA.equals("player") && userDataB.equals("ground")){
                    if(!player.isGrounded()){
                        System.out.println("Player left ground");
                    }
                } else if (userDataA.equals("ground") && userDataB.equals("player")) {
                    if(!player.isGrounded()){
                        System.out.println("Player left ground");
                    }
                }


            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
                // Handle pre-solve
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
                // Handle post-solve
            }
            private boolean isFootSensorContact(Fixture a, Fixture b) {
                return (a.getUserData() == "foot" && b.getUserData() == "ground") ||
                    (b.getUserData() == "foot" && a.getUserData() == "ground");
            }
        });

        float playerX = player.getBody().getPosition().x;
        float playerY = player.getBody().getPosition().y;
        handleInput();
        player.update(delta);

        // Update camera position

        if (playerX < 200 / Constants.PPM) {
            camera.position.set(
                200 / Constants.PPM,
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
        float tileWidthMeters = layer.getTileWidth() / Constants.PPM;  // *** PPM CHANGE
        float tileHeightMeters = layer.getTileHeight() / Constants.PPM; // ***

        for (int y = 0; y < layer.getHeight(); y++) {
            for (int x = 0; x < layer.getWidth(); x++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell != null) {
                    BodyDef bodyDef = new BodyDef();
                    bodyDef.type = BodyDef.BodyType.StaticBody;
                    bodyDef.position.set(
                        (x + 0.5f) * tileWidthMeters, // Center X (meters)
                        (y + 0.5f) * tileHeightMeters  // Center Y (meters)
                    );

                    Body body = world.createBody(bodyDef);
                    PolygonShape shape = new PolygonShape();
                    shape.setAsBox(
                        tileWidthMeters / 2 - 0.01f,  // Half-width (meters)
                        tileHeightMeters / 2 - 0.01f // Half-height (meters)
                    );

                    FixtureDef fixtureDef = new FixtureDef();
                    fixtureDef.shape = shape;
                    fixtureDef.density = 0.0f;
                    fixtureDef.filter.categoryBits = Constants.TILE_CATEGORY;
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
