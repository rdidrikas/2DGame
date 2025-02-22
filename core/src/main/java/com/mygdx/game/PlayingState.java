package com.mygdx.game;

import java.io.*;

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
import org.lwjgl.Sys;

public class PlayingState extends GameState implements CollisionChecker{

    private Player player;
    private TextureRegion[][] dirtTiles;

    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;

    private int tileSize = 16;

    public PlayingState(GameStateManager gsm) {
        super(gsm);

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

        player = new Player(idleFrames, walkFrames, jumpFrames, this, tileSize);

        camera = new OrthographicCamera();
        camera.zoom = 0.4f;
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void update(float delta) {
        handleInput();
        player.update(delta);

        // Update camera position
        camera.position.set(player.getBounds().x + player.getBounds().width / 2, player.getBounds().y + player.getBounds().height / 2, 0);
        camera.update();
    }

    @Override
    public void render(SpriteBatch batch) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Render tile map
        renderer.setView(camera);
        renderer.render();
        // Render player
        player.render(batch);
        batch.end();
    }

    private void handleInput() {
        player.isMoving = false;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            player.moveLeft();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            player.moveRight();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            player.jump();
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
    public boolean isCollidingBelow(float x, float y, float width) {
        float playerBottom = y; // Bottom edge of the player
        float playerLeft = x;
        float playerRight = x + width;

        // Check the tiles below the player
        return isCollidable(playerLeft, playerBottom - 3) || isCollidable(playerRight, playerBottom - 3);
    }

    @Override
    public boolean isCollidingAbove(float x, float y, float width, float height) {
        float playerTop = y + height; // Top edge of the player
        float playerLeft = x;
        float playerRight = x + width;

        // Check the tiles above the player
        return isCollidable(playerLeft, playerTop + 1) || isCollidable(playerRight, playerTop + 1);
    }

    @Override
    public void dispose() {
        // Dispose of resources
    }
}
