package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

public class LevelManager {
    private Array<Level> levels = new Array<>();
    private int currentLevelIndex = 0;
    private int maxLevels = 2;

    public LevelManager() {
        loadLevels();
    }

    private void loadLevels() {
        Json json = new Json();
        for(int i = 2; i <= maxLevels; i++) {
            Level level = json.fromJson(
                Level.class,
                Gdx.files.internal("levels/level" + i + ".json")
            );
            levels.add(level);
        }
    }

    public Level getCurrentLevel() {
        return levels.get(currentLevelIndex);
    }

    public void nextLevel() {
        currentLevelIndex = Math.min(currentLevelIndex + 1, levels.size - 1);
    }

    public void reset() {
        currentLevelIndex = 0;
    }

    public boolean isFinalLevel() {
        return currentLevelIndex == levels.size - 1;
    }
}
