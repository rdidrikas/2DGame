package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MenuState extends GameState {
    private Stage stage;
    private Skin skin;
    private BitmapFont customFont;

    public MenuState(GameStateManager gsm) {
        super(gsm);
        createFont();
        createUI();
    }

    private void createFont() {
        // Load and generate custom font
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
            Gdx.files.internal("Fonts/Font/MenuFont.ttf")
        );
        FreeTypeFontGenerator.FreeTypeFontParameter params = new FreeTypeFontGenerator.FreeTypeFontParameter();
        params.size = 80; // Font size in pixels
        customFont = generator.generateFont(params);
        generator.dispose();
    }

    private void createUI() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Create skin and configure styles
        skin = new Skin();
        skin.add("default-font", customFont);

        // Configure button style
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = skin.getFont("default-font");

        skin.add("default", buttonStyle);

        // Create table layout
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Create UI elements
        TextButton playButton = new TextButton("Play", skin);
        TextButton exitButton = new TextButton("Exit", skin);

        // Add button listeners
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gsm.setState(new PlayingState(gsm));
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        // Arrange UI elements
        table.add(playButton).padBottom(20).row();
        table.add(exitButton).padBottom(20).row();
    }

    @Override
    public void update(float delta) {
        stage.act(delta);
    }

    @Override
    public void render(SpriteBatch batch) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        customFont.dispose();
    }
}
