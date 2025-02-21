package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;


/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Game2D extends ApplicationAdapter {

    private enum GameState { MENU, PLAYING }
    private GameState gameState = GameState.MENU;

    private SpriteBatch batch;

    private Texture playerSheet, forestDirt, menuTexture;

    private Animation<TextureRegion> walkAnimation;
    private Animation<TextureRegion> jumpAnimation;

    public TextureRegion[][] dirtTiles;

    private TextureRegion[] walkFrames;
    private TextureRegion[] jumpFrames;
    private TextureRegion[] idleFrames;
    private TextureRegion currentFrame;
    private float stateTime;

    private Rectangle player, platform;
    public int player_width = 100;
    public int player_height = 100;

    private float gravity = -0.5f;
    private float velocityY = 0;

    private boolean isOnGround = false;
    private boolean isMoving = false;

    public int dirtTileRenderSize = 48;



    public int[][] tileMap = {
        {4, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}

    };

    @Override
    public void create() {
        batch = new SpriteBatch();
        menuTexture = new Texture("menu_screen.png");
        playerSheet = new Texture("RAMBO_anim.png");
        forestDirt = new Texture("Forest_dirt.png");

        TextureRegion[][] tmpFrames = TextureRegion.split(playerSheet, 32, 32);
        dirtTiles = TextureRegion.split(forestDirt, 16, 16);

        idleFrames = new TextureRegion[1];
        idleFrames[0] = tmpFrames[0][0];
        walkFrames = new TextureRegion[2];
        jumpFrames = new TextureRegion[6];

        for (int i = 1, temp = 0; i < 3; i++, temp++) {
            walkFrames[temp] = tmpFrames[0][i];
        }

        for (int i = 6, temp = 0; i < 12; i++, temp++) {
            jumpFrames[temp] = tmpFrames[2][i];
        }

        jumpAnimation = new Animation<>(0.2f, jumpFrames);
        walkAnimation = new Animation<>(0.1f, walkFrames);
        stateTime = 0f;

        player = new Rectangle(100, 150, player_width, player_height);
        platform = new Rectangle(50, 100, 300, 20);
    }

    @Override
    public void render() {
        if (gameState == GameState.MENU) {
            renderMenu();
        } else {
            update();
            renderGame();
        }
    }

    private void renderMenu() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.draw(menuTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            gameState = GameState.PLAYING;
        }
    }

    private void renderGame() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        stateTime += Gdx.graphics.getDeltaTime();

        for (int y = 0; y < tileMap.length; y++) {
            for (int x = 0; x < tileMap[y].length; x++) {
                int tileType = tileMap[y][x];

                if (tileType != 0) { // Skip empty tiles
                    batch.draw(dirtTiles[0][tileType-1], x * dirtTileRenderSize, y * dirtTileRenderSize, dirtTileRenderSize, dirtTileRenderSize);
                }
            }
        }

        if (!isOnGround) {
            currentFrame = jumpAnimation.getKeyFrame(stateTime, true);
        } else if (isMoving) {
            currentFrame = walkAnimation.getKeyFrame(stateTime, true);
        } else {
            currentFrame = idleFrames[0]; // Idle animation (single frame)
        }
        batch.draw(currentFrame, player.x, player.y, player.width, player.height);
        batch.end();
    }

    private void update() {
        isMoving = false;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            player.x -= 5;
            isMoving = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            player.x += 5;
            isMoving = true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && isOnGround) {
            velocityY = 10;
            isOnGround = false;
        }

        velocityY += gravity;
        player.y += velocityY;

        if (player.overlaps(platform)) {
            player.y = platform.y + platform.height;
            velocityY = 0;
            isOnGround = true;
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        menuTexture.dispose();
        playerSheet.dispose();
        forestDirt.dispose();
    }
}
