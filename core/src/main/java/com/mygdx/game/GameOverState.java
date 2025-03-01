package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class GameOverState extends GameState {
    private BitmapFont font;
    private PlayingState playingState;
    private SpriteBatch batch;

    public GameOverState(GameStateManager gsm, PlayingState playingState) {
        super(gsm);
        this.playingState = playingState;
        font = new BitmapFont();
        font.getData().setScale(3);
        System.out.println("Game Over");
        batch = new SpriteBatch();
    }

    @Override
    public void update(float delta) {
        System.out.println("Game Over State Active");
        if(Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            // Reset the game
            playingState.resetWorld();
            gsm.setState(playingState);
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        float gameOverWidth = font.getRegion().getTexture().getWidth();
        float restartWidth = font.getRegion().getTexture().getWidth();

        font.setColor(1, 1, 1, 1);

        // Center text by shifting it left by half of its width
        if (font != null) {

            System.out.println("Font is not NULL!");

            font.draw(batch, "GAME OVER",
                Gdx.graphics.getWidth() / 2f ,
                Gdx.graphics.getHeight() / 2f );

            font.draw(batch, "Press R to restart",
                Gdx.graphics.getWidth() / 2f ,
                Gdx.graphics.getHeight() / 2f );
        } else {
            System.out.println("Font is NULL!");
        }
        batch.end();
    }

    @Override
    public void dispose() {
        font.dispose();
    }
}
