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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PlayingState extends GameState {

    private World world;
    private Box2DDebugRenderer debugRenderer;
    private Player player;
    private EnemySpawner spawner;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;

    private boolean wasSpacePressed = false;

    // Removing bullets inside world.step() causes a crash
    public Array<Body> bulletsToRemove = new Array<>();

    public Array<Body> bodiesToRemove = new Array<>();



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
            world,
            100 / Constants.PPM,  // X position (meters)
            300 / Constants.PPM,  // Y position (meters)
            32 / Constants.PPM,   // Width (meters)
            32 / Constants.PPM,    // Height (meters)
            bulletsToRemove
        );

        spawner = new EnemySpawner(world, player);



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

        for (Body bullet : bulletsToRemove) {
            world.destroyBody(bullet); // Destroy the body (and its fixtures)
        }
        bulletsToRemove.clear(); // Clear the queue

        for(Body body : bodiesToRemove) {
            world.destroyBody(body);
        }
        bodiesToRemove.clear();

        /*
        if(player.isShot) {
            Gdx.app.postRunnable(() -> {

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                // Destroy old world
                world.dispose();

                // Create new world
                world = new World(new Vector2(0, Constants.GRAVITY), true);

                // Reinitialize game state
                player = new Player(world, 100/Constants.PPM, 300/Constants.PPM, 32/Constants.PPM, 32/Constants.PPM);
                spawner = new EnemySpawner(world, player);
                createCollisionTiles();

                // Transition state
                gsm.setState(new GameOverState(gsm, this));
            });
        }
           */
        world.step(delta, 8, 3);

        /*************** Collisions ***************/

        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {

                // Colliding objects:
                Fixture fixtureA = contact.getFixtureA();
                Fixture fixtureB = contact.getFixtureB();

                Object userDataA = fixtureA.getUserData();
                Object userDataB = fixtureB.getUserData();

                if (userDataA instanceof Bullet && userDataB instanceof Enemy) {
                    System.out.println("Bullet hit enemy");
                    ((Enemy) userDataB).dead();
                    removeBodiesQueue(fixtureB);
                } else if (userDataA instanceof Enemy && userDataB instanceof Bullet) {
                    System.out.println("Bullet hit enemy");
                    ((Enemy) userDataA).dead();
                    removeBodiesQueue(fixtureA);
                }

                if (userDataA instanceof EnemyBullet && userDataB instanceof Player) {
                    System.out.println("Bullet hit enemy");
                    ((Player) userDataB).dead();
                } else if (userDataA instanceof Player && userDataB instanceof EnemyBullet) {
                    System.out.println("Bullet hit enemy");
                    ((Player) userDataA).dead();
                }

                if (userDataA instanceof Bullet) {
                    ((Bullet) userDataA).markForRemoval(); // Removes sprite
                    removeBulletsQueue(fixtureA); // Removes body
                    //System.out.println("Bullet A marked for removal");
                }
                if (userDataB instanceof Bullet) {
                    ((Bullet) userDataB).markForRemoval(); // Removes sprite
                    removeBulletsQueue(fixtureB); // Removes body
                    //System.out.println("Bullet B marked for removal");
                }

                if (userDataA instanceof EnemyBullet) {
                    ((EnemyBullet) userDataA).markForRemoval(); // Removes sprite
                    removeBulletsQueue(fixtureA); // Removes body
                    //System.out.println("Bullet A marked for removal");
                }
                if (userDataB instanceof EnemyBullet) {
                    ((EnemyBullet) userDataB).markForRemoval(); // Removes sprite
                    removeBulletsQueue(fixtureB); // Removes body
                    //System.out.println("Bullet B marked for removal");
                }


            }

            @Override
            public void endContact(Contact contact) {
                Fixture fixtureA = contact.getFixtureA();
                Fixture fixtureB = contact.getFixtureB();

                Object userDataA = fixtureA.getUserData();
                Object userDataB = fixtureB.getUserData();



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
        spawner.update(Gdx.graphics.getDeltaTime(), player.getBody().getPosition());


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

        // Render enemies
        spawner.update(Gdx.graphics.getDeltaTime(), player.getBody().getPosition());
        spawner.render(batch);

        batch.end();

        /*********** DEBUGGER **********/

        debugRenderer.render(world, camera.combined);
    }


    private void handleInput() {
        player.isMoving = false;
        boolean isSpacePressed = Gdx.input.isKeyPressed(Input.Keys.SPACE);

        if(!player.isShot){
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
            if (Gdx.input.isKeyJustPressed(Input.Keys.K)){
                spawner.spawnEnemy();
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.L)){
                System.out.println("Player X: " + player.getBody().getPosition().x);
                System.out.println("Player Y: " + player.getBody().getPosition().y);
            }
        }


    }


    private void createCollisionTiles() {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("Solid");
        float tileWidth = layer.getTileWidth() / Constants.PPM;
        float tileHeight = layer.getTileHeight() / Constants.PPM;

        for (int y = 0; y < layer.getHeight(); y++) {
            Integer segmentStart = null;

            // Scan through tiles + 1 extra column to catch final segment
            for (int x = 0; x <= layer.getWidth(); x++) {
                boolean isSolid = x < layer.getWidth() && layer.getCell(x, y) != null;

                if (isSolid && segmentStart == null) {
                    // Start new segment
                    segmentStart = x;
                }
                else if (!isSolid && segmentStart != null) {
                    // End current segment
                    createSegment(y, segmentStart, x-1, tileWidth, tileHeight);
                    segmentStart = null;
                }
            }
        }
    }

    private void createSegment(int y, int startX, int endX, float tileWidth, float tileHeight) {
        List<Vector2> vertices = new ArrayList<>();

        // Add start ghost vertex (left edge of first tile's ghost)
        vertices.add(new Vector2(startX * tileWidth, y * tileHeight));

        // Add main platform vertices
        for (int x = startX; x <= endX + 1; x++) {
            vertices.add(new Vector2(x * tileWidth, y * tileHeight + tileHeight)); // main platform need to be top of the tile
        }

        // Add end ghost vertex (right edge of last tile's ghost)
        vertices.add(new Vector2((endX + 1) * tileWidth, y * tileHeight));

        // Remove consecutive duplicates
        List<Vector2> cleaned = new ArrayList<>();
        Vector2 last = null;
        for (Vector2 v : vertices) {
            if (!v.equals(last)) {
                cleaned.add(v);
                last = v;
            }
        }

        if (cleaned.size() >= 2) {
            createChainBody(cleaned);
        }
    }

    private void createChainBody(List<Vector2> vertices) {
        ChainShape chain = new ChainShape();
        Vector2[] vertArray = vertices.toArray(new Vector2[0]);
        chain.createChain(vertArray);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        Body groundBody = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = chain;
        fixtureDef.friction = 0.4f;

        fixtureDef.filter.categoryBits = Constants.TILE_CATEGORY;
        fixtureDef.filter.maskBits = Constants.PLAYER_CATEGORY | Constants.ENEMY_CATEGORY | Constants.BULLET_CATEGORY | Constants.ENEMY_BULLET_CATEGORY;

        Fixture fixture = groundBody.createFixture(fixtureDef);
        fixture.setUserData("ground");

        chain.dispose();
    }

    public void removeBulletsQueue(Fixture fixture) {
        Body bulletBody = fixture.getBody();
        bulletsToRemove.add(bulletBody);
    }

    public void removeBodiesQueue(Fixture fixture) {
        Body body = fixture.getBody();
        bodiesToRemove.add(body);
    }

    public void reset() {
        player.reset();
        // Reset other game elements as needed
        spawner.reset();
        bulletsToRemove.clear();
    }

    public void resetWorld() {
        // 1. Destroy old world
        if(world != null) {
            world.dispose();
        }

        // 2. Create new world
        world = new World(new Vector2(0, Constants.GRAVITY), true);

        // 3. Reinitialize everything
        player = new Player(world, 100/Constants.PPM, 300/Constants.PPM, 32/Constants.PPM, 32/Constants.PPM, bulletsToRemove);
        spawner = new EnemySpawner(world, player);

        // 4. Recreate collision tiles
        createCollisionTiles();

        // 5. Reset other state
        bulletsToRemove.clear();
    }


    @Override
    public void dispose() {
        world.dispose();
        debugRenderer.dispose();
    }
}
