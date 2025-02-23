package com.mygdx.game;

import java.io.*;

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

public class PlayingState extends GameState implements CollisionChecker{

    private World world;
    private Box2DDebugRenderer debugRenderer;
    private Player player;
    private TextureRegion[][] dirtTiles;

    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;

    private int tileSize = 16;

    private boolean wasSpacePressed = false;


    public PlayingState(GameStateManager gsm) {
        super(gsm);

        world = new World(new Vector2(0, Constants.GRAVITY), true);
        debugRenderer = new Box2DDebugRenderer();

        // Load map
        map = new TmxMapLoader().load("Map/map.tmx");
        renderer = new OrthogonalTiledMapRenderer(map);

        Texture playerSheet = new Texture("Animations/RAMBO_anim.png");
        TextureRegion[][] tmpFrames = TextureRegion.split(playerSheet, 32, 32);

        TextureRegion[] idleFrames = { tmpFrames[0][0] };
        TextureRegion[] walkFrames = { tmpFrames[0][1], tmpFrames[0][2] };
        TextureRegion[] jumpFrames = new TextureRegion[6];
        for (int i = 6, temp = 0; i < 12; i++, temp++) {
            jumpFrames[temp] = tmpFrames[2][i];
        }

        createCollisionTiles(); // Loads collision tiles from the map



        player = new Player(idleFrames, walkFrames, jumpFrames, this, tileSize, world, 100, 300, 32, 32);

        camera = new OrthographicCamera();
        camera.zoom = 0.3f;
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void update(float delta) {

        world.step(delta, 6, 2);

        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Fixture fixtureA = contact.getFixtureA();
                Fixture fixtureB = contact.getFixtureB();

                Object userDataA = fixtureA.getUserData();
                Object userDataB = fixtureB.getUserData();

                // Check for collision between player and ground
                if (("player".equals(userDataA) && "ground".equals(userDataB))) {
                    System.out.println("Player landed on the ground");

                    // Handle player landing on the ground
                }
                else if (("player".equals(userDataB) && "ground".equals(userDataA))) {
                    System.out.println("Player landed on the ground");

                    // Handle player landing on the ground
                }
            }

            @Override
            public void endContact(Contact contact) {
                Fixture fixtureA = contact.getFixtureA();
                Fixture fixtureB = contact.getFixtureB();

                Object userDataA = fixtureA.getUserData();
                Object userDataB = fixtureB.getUserData();

                // Check for end of collision between player and ground
                if (("player".equals(userDataA) && "ground".equals(userDataB))) {
                    System.out.println("Player left the ground");

                    // Handle player leaving the ground
                } else if (("player".equals(userDataB) && "ground".equals(userDataA))) {
                    System.out.println("Player left the ground");

                    // Handle player leaving the ground
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
        });

        float playerX = player.getBody().getPosition().x;
        float playerY = player.getBody().getPosition().y;
        handleInput();
        player.update(delta);

        // Update camera position
        camera.position.set(
            playerX,
            playerY,
            0
        );
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

        //debugRenderer.render(world, camera.combined);
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
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            player.reset();
        }

    }

    @Override
    public boolean isCollidable(float x, float y) {

        if (map == null || map.getLayers().get("Solid") == null) {
            return false;
        }

        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("Solid");

        // Convert world coordinates to tile coordinates
        int tileX = (int) (x / layer.getTileWidth());
        int tileY = (int) (y / layer.getTileHeight());

        // Check if the tile coordinates are within the layer bounds
        if (tileX < 0 || tileX >= layer.getWidth() || tileY < 0 || tileY >= layer.getHeight()) {
            return false; // Outside the layer bounds
        }

        // Get the cell at the tile coordinates
        TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);

        // Check if the cell and tile are not null, and if the tile has the "collidable" property
        if (cell != null && cell.getTile() != null) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isCollidingBelow(float x, float y, float width, float height) {
        float playerBottom = y - height / 2; // Bottom of the player
        float playerLeft = x - width / 4;
        float playerRight = x + width / 4;

        // Check the tiles below the player
        return isCollidable(playerLeft, playerBottom - 1) || isCollidable(playerRight, playerBottom - 1);
    }

    @Override
    public boolean isCollidingAbove(float x, float y, float width, float height) {
        float playerTop = y + height; // Top edge of the player
        float playerLeft = x;
        float playerRight = x + width;

        // Check the tiles above the player
        return isCollidable(playerLeft, playerTop + 0.5f) || isCollidable(playerRight, playerTop + 0.5f);
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

                    Fixture groundFixture = body.createFixture(fixtureDef);
                    groundFixture.setUserData("ground");

                    shape.dispose();
                }
            }
        }
    }





    @Override
    public void dispose() {
        world.dispose();
        debugRenderer.dispose();
    }
}
