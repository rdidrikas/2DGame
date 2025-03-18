package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

public class MapEditorState extends GameState {
    private OrthographicCamera camera;
    private Texture tileSheet;
    private TextureRegion[][] tiles;

    private int[][] mapData;
    private int selectedTile = 0;
    private float tileSize = 1f; // In meters

    public MapEditorState(GameStateManager gsm) {
        super(gsm);
        setupEditor();
    }

    private void setupEditor() {
        // Initialize camera
        camera = new OrthographicCamera(20, 15);
        camera.update();

        // Load tile textures
        tileSheet = new Texture("Map/temple_bricks.png");
        tiles = TextureRegion.split(tileSheet, 32, 32);

        // Initialize empty map (50x50 grid)
        mapData = new int[50][50];
    }

    @Override
    public void update(float delta) {
        handleInput();
    }

    private void handleInput() {

        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) selectedTile = 0;
        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) selectedTile = 1;

        // Place tiles
        if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(mousePos);

            int gridX = (int)(mousePos.x / tileSize);
            int gridY = (int)(mousePos.y / tileSize);

            if(gridX >= 0 && gridX < mapData.length &&
                gridY >= 0 && gridY < mapData[0].length) {
                mapData[gridX][gridY] = selectedTile;
            }
        }

        // Save/Load
        if(Gdx.input.isKeyJustPressed(Input.Keys.S)) saveMap();
        if(Gdx.input.isKeyJustPressed(Input.Keys.L)) loadMap();
    }

    @Override
    public void render(SpriteBatch batch) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Draw map
        for(int x = 0; x < mapData.length; x++) {
            for(int y = 0; y < mapData[0].length; y++) {
                TextureRegion tile = tiles[mapData[x][y]][0];
                batch.draw(tile, x * tileSize, y * tileSize, tileSize, tileSize);
            }
        }

        batch.end();
    }

    private void saveMap() {
        Level map = new Level();
        map.mapFile = "map";
        map.enemySpawnPoints = new Array<>();

        Json json = new Json();
        FileHandle file = Gdx.files.local("Levels/Custom/" + map.mapFile + ".json");
        file.writeString(json.prettyPrint(map), false);
    }

    private void loadMap() {
        // Load map data
        // IT DOESN'T WORK YET
    }

    @Override
    public void dispose() {
        tileSheet.dispose();
    }
}
