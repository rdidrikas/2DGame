package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Game2D extends ApplicationAdapter {
    private GameStateManager gsm;
    private SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch();
        gsm = new GameStateManager(); // No-arg constructor
        gsm.setState(new MenuState(gsm)); // Pass the manager to MenuState
    }

    @Override
    public void render() {
        gsm.update(Gdx.graphics.getDeltaTime());
        gsm.render(batch);
    }

    @Override
    public void dispose() {
        batch.dispose();
        gsm.dispose();
    }
}
