package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class GameState {
    protected GameStateManager gsm; // Reference to the manager

    public GameState(GameStateManager gsm) {
        this.gsm = gsm;
    }

    public abstract void update(float delta);
    public abstract void render(SpriteBatch batch);
    public abstract void dispose();
}
