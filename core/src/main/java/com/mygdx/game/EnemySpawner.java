package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

public class EnemySpawner {
    private Array<Enemy> enemies = new Array<>();
    private World world;

    public EnemySpawner(World world) {
        this.world = world;
    }

    public void spawnEnemy(float x, float y) {
        enemies.add(new Enemy(world, x, y));
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
