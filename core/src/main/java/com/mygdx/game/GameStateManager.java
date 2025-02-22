package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class GameStateManager {
    private GameState currentState; // Refers to the ABSTRACT GameState class

    public GameStateManager() {
        // Empty constructor (no args needed)
    }

    public void setState(GameState state) {
        if (currentState != null) {
            currentState.dispose();
        }
        currentState = state;
    }

    public void update(float delta) {
        if (currentState != null) {
            currentState.update(delta);
        }
    }

    public void render(SpriteBatch batch) {
        if (currentState != null) {
            currentState.render(batch);
        }
    }

    public void dispose() {
        if (currentState != null) {
            currentState.dispose();
        }
    }
}
