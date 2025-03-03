package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class Level {
    public String mapFile;
    public Vector2 playerStartPosition;
    public Vector2 levelCompletionPosition;
    public Array<Vector2> enemySpawnPoints;
    public String backgroundMusic;

    public Level() {
        // Initialize default values
        mapFile = "";
        playerStartPosition = new Vector2();
        enemySpawnPoints = new Array<>();
        levelCompletionPosition = new Vector2();
    }

    public Level(String mapFile, Vector2 playerStart, Array<Vector2> enemies, Vector2 levelCompletion) {
        this.mapFile = mapFile;
        this.playerStartPosition = playerStart;
        this.enemySpawnPoints = enemies;
        this.levelCompletionPosition = levelCompletion;
    }

}
