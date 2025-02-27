package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

public class EnemySpawner {
    private Array<Enemy> enemies = new Array<>();
    private Array<Vector2> spawnPoints = new Array<>();
    private World world;
    private Player player;
    private Array<Vector2> enemiesLevel1 = new Array<>();

    public EnemySpawner(World world, Player player) {
        this.world = world;

        enemiesLevel1.add(new Vector2(9, 6.85f));
        enemiesLevel1.add(new Vector2(10, 6.85f));
        enemiesLevel1.add(new Vector2(15.7f, 6.4f));
        enemiesLevel1.add(new Vector2(21.8f, 5.85f));
        enemiesLevel1.add(new Vector2(33.5f, 8.85f));
        enemiesLevel1.add(new Vector2(37.25f, 9.85f));

        spawnPoints.addAll(enemiesLevel1);
        spawnEnemy();
    }

    public void spawnEnemy() {
        for (Vector2 pos : spawnPoints) {
            enemies.add(new Enemy(world, pos.x, pos.y, player));
        }
    }

    public void update(float delta, Vector2 playerPosition) {
        for (Enemy enemy : enemies) {
            enemy.update(delta, playerPosition);
        }
    }

    public void render(SpriteBatch batch) {
        for (Enemy enemy : enemies) {
            enemy.render(batch);
        }
    }
}
