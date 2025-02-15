package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Game2D extends ApplicationAdapter {

    public static final float SPEED = 150;

    private SpriteBatch batch;
    private Texture image;
    float y=0, x=0;

    @Override
    public void create() {
        this.create();
        batch = new SpriteBatch();
        image = new Texture("libgdx.png");

    }

    @Override
    public void render() {
        this.render();
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        if(Gdx.input.isKeyPressed(Input.Keys.UP)) {
            y += SPEED * Gdx.graphics.getDeltaTime();
        }
        if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            y -= SPEED * Gdx.graphics.getDeltaTime();
        }
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            x += SPEED * Gdx.graphics.getDeltaTime();
        }
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            x -= SPEED * Gdx.graphics.getDeltaTime();
        }

        batch.begin();
        batch.draw(image, x, y);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
    }
}
